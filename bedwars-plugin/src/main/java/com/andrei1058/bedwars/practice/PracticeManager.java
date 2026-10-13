package com.andrei1058.bedwars.practice;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.arena.Arena;
import com.andrei1058.bedwars.arena.SetupSession;
import com.andrei1058.bedwars.configuration.Permissions;
import com.andrei1058.bedwars.listeners.BreakPlace;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.*;
import java.util.function.Consumer;

public final class PracticeManager {
    private final PracticeConfig config = new PracticeConfig();
    private final Map<UUID, PracticeSession> sessions = new LinkedHashMap<>();
    private final Map<String, UUID> occupied = new HashMap<>();
    private final Set<UUID> internalTeleports = new HashSet<>();
    private final PracticeListener listener;
    private final PracticeMenu menu;
    private final PracticeNpc npc;

    public PracticeManager() {
        menu = new PracticeMenu(this);
        npc = new PracticeNpc(this);
        listener = new PracticeListener(this);
        BedWars.registerEvents(listener, menu, npc);
        Bukkit.getScheduler().runTask(BedWars.plugin, npc::start);
    }
    public PracticeConfig getConfig() { return config; }
    public PracticeNpc getNpc() { return npc; }
    public Collection<PracticePoint> getPoints() { return config.getPoints(); }
    public Collection<PracticeSession> getSessions() { return sessions.isEmpty() ? Collections.emptyList() : new ArrayList<>(sessions.values()); }
    public PracticeSession getSession(Player player) { return sessions.get(player.getUniqueId()); }
    public boolean isPracticing(Player player) { return getSession(player) != null; }
    public boolean isPointOccupied(String id) { return occupied.containsKey(id); }
    public boolean isPracticeWorld(World world) { return world != null && isPracticeWorld(world.getName()); }
    public boolean isPracticeWorld(String name) { return config.usesWorld(name); }
    public boolean isInternalTeleport(Player player) { return internalTeleports.contains(player.getUniqueId()); }
    public PracticeSession getSession(Projectile projectile) {
        if (!(projectile.getShooter() instanceof Player)) return null;
        PracticeSession session = getSession((Player) projectile.getShooter());
        return session != null && session.ownsProjectile(projectile) ? session : null;
    }
    public PracticePoint getOccupiedPoint(Location location) {
        for (PracticeSession session : sessions.values()) if (session.getPoint().contains(location)) return session.getPoint();
        return null;
    }
    public int getTotalCount(PracticeMode mode) {
        int count = 0;
        for (PracticePoint point : getPoints()) if (point.getMode() == mode && point.isAvailable()) count++;
        return count;
    }
    public int getFreeCount(PracticeMode mode) {
        int count = 0;
        for (PracticePoint point : getPoints()) if (point.getMode() == mode && point.isAvailable() && !isPointOccupied(point.getId())) count++;
        return count;
    }
    public boolean hasPracticeAdmin(CommandSender sender) {
        return sender.isOp() || sender.hasPermission("bw.*") || sender.hasPermission(Permissions.PERMISSION_PRACTICE_ADMIN);
    }
    public boolean canBuild(Player player, Location location) {
        return !isPracticing(player) && hasPracticeAdmin(player) && BreakPlace.isBuildSession(player) && getOccupiedPoint(location) == null;
    }
    public void openMenu(Player player) { menu.open(player); }
    public static void message(CommandSender sender, String key) { message(sender, key, null, null); }
    public static void message(CommandSender sender, String key, String id, String reason) {
        sender.sendMessage(Language.getMsg(sender instanceof Player ? (Player) sender : null, key)
                .replace("{command}", BedWars.mainCmd).replace("{id}", id == null ? "" : id).replace("{reason}", reason == null ? "" : reason));
    }
    public boolean join(Player player, PracticeMode mode) {
        if (isPracticing(player)) { message(player, Messages.PRACTICE_ALREADY_IN_SESSION); return false; }
        if (Arena.isInArena(player) || SetupSession.isInSetupSession(player.getUniqueId()) || BreakPlace.isBuildSession(player)) {
            message(player, Messages.PRACTICE_LEAVE_FIRST); return false;
        }
        PracticePoint selected = null;
        for (PracticePoint point : getPoints()) {
            if (point.getMode() == mode && point.isAvailable() && !isPointOccupied(point.getId()) && BedWars.nms.isPracticeSpawnSafe(point.getSpawn())) {
                selected = point; break;
            }
        }
        if (selected == null) { message(player, Messages.PRACTICE_NO_FREE_POINT); return false; }
        PracticeSession session = new PracticeSession(player, selected);
        sessions.put(player.getUniqueId(), session); occupied.put(selected.getId(), player.getUniqueId());
        try {
            session.prepare();
            if (!teleport(player, selected.getSpawn())) throw new IllegalStateException("Teleport cancelled");
            replenish(player); giveControls(player);
            message(player, Messages.PRACTICE_JOINED, selected.getId(), null);
            return true;
        } catch (RuntimeException ex) {
            finish(session);
            message(player, Messages.PRACTICE_TELEPORT_FAILED, selected.getId(), ex.getMessage());
            return false;
        }
    }
    public boolean reset(Player player) {
        PracticeSession session = getSession(player);
        if (session == null) { message(player, Messages.PRACTICE_NOT_IN_SESSION); return false; }
        if (session.isResetting()) return false;
        if (!BedWars.nms.isPracticeSpawnSafe(session.getPoint().getSpawn())) return leave(player);
        session.setResetting(true);
        try {
            session.nextRound();
            if (!teleport(player, session.getPoint().getSpawn())) {
                message(player, Messages.PRACTICE_TELEPORT_FAILED); return false;
            }
            session.clearBlocks(); session.prepare(); replenish(player); giveControls(player);
            message(player, Messages.PRACTICE_RESET); return true;
        } finally { session.setResetting(false); }
    }
    public boolean leave(Player player) {
        PracticeSession session = getSession(player);
        if (session == null) { message(player, Messages.PRACTICE_NOT_IN_SESSION); return false; }
        if (!teleport(player, returnLocation(session))) { message(player, Messages.PRACTICE_TELEPORT_FAILED); return false; }
        finish(session); message(player, Messages.PRACTICE_LEFT); return true;
    }
    public void disconnect(Player player) {
        PracticeSession session = getSession(player);
        if (session == null) return;
        try {
            if (!teleport(player, returnLocation(session))) BedWars.plugin.getLogger().warning("Practice return teleport cancelled for " + player.getName());
        } finally {
            finish(session);
            // Carried items are not serialized in 1.8 player data after quit.
            ItemStack cursor = player.getItemOnCursor();
            if (cursor != null && cursor.getType() != Material.AIR) {
                player.setItemOnCursor(null);
                Map<Integer, ItemStack> remaining = player.getInventory().addItem(cursor);
                if (!remaining.isEmpty()) {
                    Location destination = returnLocation(session);
                    for (ItemStack item : remaining.values()) destination.getWorld().dropItem(destination, item);
                }
            }
        }
    }
    public void terminateAfterWorldChange(Player player) {
        PracticeSession session = getSession(player);
        if (session != null) finish(session);
    }
    private Location returnLocation(PracticeSession session) {
        Location result = session.getReturnLocation();
        if (result.getWorld() != null && Bukkit.getWorld(result.getWorld().getUID()) != null && BedWars.nms.isPracticeSpawnSafe(result)) return result;
        result = BedWars.config.getConfigLoc("lobbyLoc");
        if (BedWars.nms.isPracticeSpawnSafe(result)) return result;
        return Bukkit.getWorlds().get(0).getSpawnLocation();
    }
    private boolean teleport(Player player, Location location) {
        internalTeleports.add(player.getUniqueId());
        try { return player.teleport(location, TeleportCause.PLUGIN); }
        finally { internalTeleports.remove(player.getUniqueId()); }
    }
    private void release(PracticeSession session) {
        sessions.remove(session.getPlayer().getUniqueId()); occupied.remove(session.getPoint().getId());
    }
    private void finish(PracticeSession session) {
        try { session.nextRound(); session.clearBlocks(); session.restore(); }
        finally { release(session); }
    }
    public void replenish(Player player) {
        PracticeSession session = getSession(player);
        if (session == null) return;
        PracticeMode mode = session.getPoint().getMode();
        fillSlot(player, 0, mode.getMaterial(), mode.getAmount());
        if (mode == PracticeMode.FIREBALL) fillSlot(player, 1, Material.WOOL, 64);
    }
    private void fillSlot(Player player, int slot, Material material, int amount) {
        ItemStack item = player.getInventory().getItem(slot);
        if (item == null || item.getType() != material || item.getAmount() != amount
                || item.getDurability() != 0 || item.hasItemMeta()) {
            player.getInventory().setItem(slot, new ItemStack(material, amount));
        }
    }
    private void giveControls(Player player) {
        player.getInventory().setItem(7, control(player, Material.ARROW, Messages.PRACTICE_RETRY_ITEM, "PRACTICE_RETRY"));
        player.getInventory().setItem(8, control(player, Material.BED, Messages.PRACTICE_LEAVE_ITEM, "PRACTICE_LEAVE"));
        player.getInventory().setHeldItemSlot(0);
    }
    private ItemStack control(Player player, Material material, String path, String marker) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta(); meta.setDisplayName(Language.getMsg(player, path)); item.setItemMeta(meta);
        return BedWars.nms.addCustomData(item, marker);
    }
    public void shutdown() {
        listener.shutdown(); menu.shutdown(); npc.shutdown();
        for (PracticeSession session : getSessions()) disconnect(session.getPlayer());
    }
    public boolean reload(CommandSender sender) {
        if (!sessions.isEmpty()) { message(sender, Messages.PRACTICE_POINT_BUSY); return false; }
        try { config.install(config.readConfiguration()); npc.start(); message(sender, Messages.PRACTICE_RELOAD_SUCCESS); return true; }
        catch (java.io.IOException | InvalidConfigurationException | IllegalArgumentException ex) {
            message(sender, Messages.PRACTICE_INVALID_POINT, null, ex.getMessage());
            return false;
        }
    }
    private boolean change(CommandSender sender, String id, boolean requireExisting, Consumer<YamlConfiguration> operation) {
        if (id == null || !id.matches("[a-z0-9_-]+")) {
            message(sender, Messages.PRACTICE_INVALID_ARGUMENTS, id, "Invalid point ID");
            return false;
        }
        if (isPointOccupied(id)) { message(sender, Messages.PRACTICE_POINT_BUSY, id, null); return false; }
        if (requireExisting && config.getPoint(id) == null) { message(sender, Messages.PRACTICE_POINT_NOT_FOUND, id, null); return false; }
        try {
            YamlConfiguration candidate = config.copyConfiguration(); operation.accept(candidate); config.commit(candidate);
            message(sender, Messages.PRACTICE_ADMIN_SUCCESS, id, null); return true;
        } catch (java.io.IOException ex) {
            message(sender, Messages.PRACTICE_SAVE_FAILED, id, ex.getMessage());
            return false;
        } catch (InvalidConfigurationException | IllegalArgumentException ex) {
            message(sender, Messages.PRACTICE_INVALID_POINT, id, ex.getMessage());
            return false;
        }
    }
    public boolean create(CommandSender sender, String id, PracticeMode mode, Location location) {
        if (config.getPoint(id) != null) { message(sender, Messages.PRACTICE_POINT_EXISTS, id, null); return false; }
        return change(sender, id, false, yaml -> {
            String root = "points." + id + ".";
            yaml.set(root + "mode", mode.getId()); yaml.set(root + "world", location.getWorld().getName());
            yaml.set(root + "spawn", config.stringLocationArenaFormat(location)); yaml.set(root + "fail-y", 0); yaml.set(root + "enabled", false);
        });
    }
    private boolean setLocation(CommandSender sender, String id, String key, Location location) {
        return change(sender, id, true, yaml -> {
            if (!location.getWorld().getName().equals(config.getPoint(id).getWorldName())) throw new IllegalArgumentException("Wrong world");
            yaml.set("points." + id + "." + key, config.stringLocationArenaFormat(location));
        });
    }
    public boolean setSpawn(CommandSender sender, String id, Location location) { return setLocation(sender, id, "spawn", location); }
    public boolean setCorner(CommandSender sender, String id, int corner, Location location) { return setLocation(sender, id, "pos" + corner, location); }
    public boolean setFailY(CommandSender sender, String id, int value) { return change(sender, id, true, yaml -> yaml.set("points." + id + ".fail-y", value)); }
    public boolean setEnabled(CommandSender sender, String id, boolean value) { return change(sender, id, true, yaml -> yaml.set("points." + id + ".enabled", value)); }
    public boolean delete(CommandSender sender, String id) { return change(sender, id, true, yaml -> yaml.set("points." + id, null)); }
}
