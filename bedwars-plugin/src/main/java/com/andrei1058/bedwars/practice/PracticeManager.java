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
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public final class PracticeManager {
    private final PracticeConfig config = new PracticeConfig();
    private final PracticeRecovery recovery = new PracticeRecovery();
    private final Map<UUID, PracticeSession> sessions = new LinkedHashMap<>();
    private final Collection<PracticeSession> sessionView = Collections.unmodifiableCollection(sessions.values());
    private final Map<String, Set<UUID>> occupied = new HashMap<>();
    private final Map<UUID, PracticeMode> waiting = new LinkedHashMap<>();
    private final Set<UUID> internalTeleports = new HashSet<>();
    private final PracticeListener listener;
    private final PracticeMenu menu;
    private final PracticeNpc npc;

    public PracticeManager() {
        menu = new PracticeMenu(this); npc = new PracticeNpc(this); listener = new PracticeListener(this);
        BedWars.registerEvents(listener, menu, npc);
        Bukkit.getScheduler().runTask(BedWars.plugin, npc::start);
    }
    public PracticeConfig getConfig() { return config; }
    public PracticeNpc getNpc() { return npc; }
    public Collection<PracticePoint> getPoints() { return config.getPoints(); }
    public Collection<PracticeSession> getSessions() { return sessionView; }
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
        for (String id : occupied.keySet()) {
            PracticePoint point = config.getPoint(id);
            if (point.containsPlayer(location)) return point;
        }
        return null;
    }
    private int occupancy(PracticePoint point) {
        Set<UUID> members = occupied.get(point.getId());
        return members == null ? 0 : members.size();
    }
    private void occupy(PracticePoint point, UUID player) {
        occupied.computeIfAbsent(point.getId(), id -> new LinkedHashSet<>()).add(player);
    }
    private void release(PracticePoint point, UUID player) {
        Set<UUID> members = occupied.get(point.getId());
        members.remove(player);
        if (members.isEmpty()) occupied.remove(point.getId());
    }
    private boolean isJoinable(PracticePoint point) {
        String state = pointStatus(point);
        return state.equals(Messages.PRACTICE_STATUS_FREE)
                || point.getMode().isShared() && state.equals(Messages.PRACTICE_STATUS_BUSY)
                && BedWars.nms.isPracticeSpawnSafe(point.getSpawn());
    }
    public int getPlayerCount(PracticeMode mode) {
        int count = 0;
        for (PracticePoint point : getPoints()) if (point.getMode() == mode) count += occupancy(point);
        return count;
    }
    public int getTotalCount(PracticeMode mode) {
        int count = 0;
        for (PracticePoint point : getPoints()) if (point.getMode() == mode && point.isAvailable()) count++;
        return count;
    }
    public String pointStatus(PracticePoint point) {
        if (!point.isEnabled()) return Messages.PRACTICE_STATUS_DISABLED;
        if (!point.isAvailable()) return Messages.PRACTICE_STATUS_UNLOADED;
        if (recovery.isBlocked(point.getWorldName())) return Messages.PRACTICE_STATUS_RECOVERY;
        if (isPointOccupied(point.getId())) return Messages.PRACTICE_STATUS_BUSY;
        if (!BedWars.nms.isPracticeSpawnSafe(point.getSpawn())) return Messages.PRACTICE_STATUS_UNSAFE;
        return Messages.PRACTICE_STATUS_FREE;
    }
    public int getFreeCount(PracticeMode mode) {
        int count = 0;
        for (PracticePoint point : getPoints()) if (point.getMode() == mode && isJoinable(point)) count++;
        return count;
    }
    public boolean hasPracticeAdmin(CommandSender sender) {
        return sender.isOp() || sender.hasPermission("bw.*") || sender.hasPermission(Permissions.PERMISSION_PRACTICE_ADMIN);
    }
    public boolean canBuild(Player player, Location location) {
        return !isPracticing(player) && hasPracticeAdmin(player) && BreakPlace.isBuildSession(player) && getOccupiedPoint(location) == null;
    }
    public void openMenu(Player player) { menu.open(player); }
    public static String text(CommandSender sender, String key, String... replacements) {
        String result = Language.getMsg(sender instanceof Player ? (Player) sender : null, key).replace("{command}", BedWars.mainCmd);
        for (int index = 0; index < replacements.length; index += 2) result = result.replace("{" + replacements[index] + "}", replacements[index + 1]);
        return result;
    }
    public static void message(CommandSender sender, String key) { message(sender, key, null, null); }
    public static void message(CommandSender sender, String key, String id, String reason) {
        sender.sendMessage(text(sender, key, "id", id == null ? "" : id, "reason", reason == null ? "" : reason));
    }
    public void list(CommandSender sender) {
        for (PracticeMode mode : PracticeMode.values()) sender.sendMessage(text(sender, Messages.PRACTICE_LIST_LINE,
                "mode", text(sender, mode.getMessagePath()), "free", String.valueOf(getFreeCount(mode)),
                "total", String.valueOf(getTotalCount(mode)), "players", String.valueOf(getPlayerCount(mode))));
        if (!hasPracticeAdmin(sender)) return;
        for (PracticePoint point : getPoints()) {
            StringJoiner names = new StringJoiner(", ");
            Set<UUID> members = occupied.get(point.getId());
            if (members != null) for (UUID member : members) names.add(sessions.get(member).getPlayer().getName());
            sender.sendMessage(text(sender, Messages.PRACTICE_POINT_STATUS, "id", point.getId(), "mode", text(sender, point.getMode().getMessagePath()),
                    "state", text(sender, pointStatus(point)), "players", String.valueOf(occupancy(point)),
                    "player", members == null ? "-" : names.toString()));
        }
        config.getErrors().forEach((id, error) -> message(sender, Messages.PRACTICE_INVALID_POINT, id, error));
    }
    public boolean join(Player player, PracticeMode mode) { return join(player, mode, null); }
    public boolean join(Player player, PracticeMode mode, String targetId) {
        if (mode.isShared() && targetId != null) {
            message(player, Messages.PRACTICE_INVALID_ARGUMENTS, targetId, "Only bridge accepts a target ID");
            return false;
        }
        PracticeSession current = getSession(player);
        if (Arena.isInArena(player) || SetupSession.isInSetupSession(player.getUniqueId()) || BreakPlace.isBuildSession(player)) {
            message(player, Messages.PRACTICE_LEAVE_FIRST); return false;
        }
        if (targetId != null) {
            boolean known = false;
            for (PracticePoint point : getPoints())
                if (point.getMode() == mode && point.getTarget(targetId) != null) { known = true; break; }
            if (!known) { message(player, Messages.PRACTICE_INVALID_ARGUMENTS, targetId, "Unknown target: " + targetId); return false; }
        }
        PracticePoint selected = null;
        if (current != null && current.getPoint().getMode() == mode
                && (targetId == null || current.getPoint().getTarget(targetId) != null)) selected = current.getPoint();
        if (selected == null) for (PracticePoint point : getPoints()) {
            PracticeTarget target = point.getTarget(targetId);
            if (point.getMode() == mode && isJoinable(point)
                    && (targetId == null || target != null && BedWars.nms.isPracticeSpawnSafe(target.getSpawn()))) {
                if (selected == null || occupancy(point) < occupancy(selected)) selected = point;
                if (!mode.isShared()) break;
            }
        }
        if (selected == null) {
            if (mode.isShared()) {
                player.sendMessage(text(player, Messages.PRACTICE_NO_AVAILABLE_AREA, "mode", text(player, mode.getMessagePath())));
            } else {
                waiting.put(player.getUniqueId(), mode);
                player.sendMessage(text(player, Messages.PRACTICE_WAITING, "mode", text(player, mode.getMessagePath())));
            }
            return false;
        }
        PracticeTarget target = selected.getTarget(targetId);
        if (current != null) return switchSelection(current, selected, target);
        PracticeSession session = new PracticeSession(player, selected, target, recovery);
        sessions.put(player.getUniqueId(), session); occupy(selected, player.getUniqueId());
        try {
            session.prepare();
            if (!teleport(player, session.getSpawn())) throw new IllegalStateException("Teleport cancelled");
            waiting.remove(player.getUniqueId()); replenish(player); giveControls(session);
            message(player, Messages.PRACTICE_JOINED, selected.getId(), null);
            return true;
        } catch (RuntimeException ex) {
            finish(session, false); message(player, Messages.PRACTICE_TELEPORT_FAILED, selected.getId(), ex.getMessage()); return false;
        }
    }
    private boolean switchSelection(PracticeSession session, PracticePoint point, PracticeTarget target) {
        Player player = session.getPlayer();
        PracticePoint previous = session.getPoint();
        session.rememberLayout();
        try {
            report(session, Messages.PRACTICE_INCOMPLETE);
            session.clearBlocks();
            Location destination = target == null ? point.getSpawn() : target.getSpawn();
            if (!BedWars.nms.isPracticeSpawnSafe(destination) || !teleport(player, destination)) {
                message(player, Messages.PRACTICE_TELEPORT_FAILED); return false;
            }
            session.nextRound(); session.select(point, target);
            if (previous != point) {
                release(previous, player.getUniqueId());
                occupy(point, player.getUniqueId());
            }
            session.prepare(); replenish(player); giveControls(session); waiting.remove(player.getUniqueId());
            BedWars.nms.playAction(player, "");
            return true;
        } catch (IOException ex) {
            message(player, Messages.PRACTICE_SAVE_FAILED, null, ex.getMessage()); return false;
        }
    }
    public boolean reset(Player player) { return reset(player, Messages.PRACTICE_RESET); }
    public boolean reset(Player player, String reason) {
        PracticeSession session = getSession(player);
        if (session == null) { message(player, Messages.PRACTICE_NOT_IN_SESSION); return false; }
        if (session.isResetting()) return false;
        session.setResetting(true);
        try {
            session.rememberLayout(); report(session, Messages.PRACTICE_INCOMPLETE);
            // 临时羊毛必须先恢复，再判断出生点是否安全。
            session.clearBlocks();
            if (!BedWars.nms.isPracticeSpawnSafe(session.getSpawn())) return leave(player);
            if (!teleport(player, session.getSpawn())) { message(player, Messages.PRACTICE_TELEPORT_FAILED); return false; }
            session.nextRound(); session.prepare(); replenish(player); giveControls(session);
            session.status(text(player, reason)); return true;
        } catch (IOException ex) { message(player, Messages.PRACTICE_SAVE_FAILED, null, ex.getMessage()); return false; }
        finally { session.setResetting(false); }
    }
    public boolean leave(Player player) {
        boolean cancelledWaiting = waiting.remove(player.getUniqueId()) != null;
        PracticeSession session = getSession(player);
        if (session == null) { message(player, cancelledWaiting ? Messages.PRACTICE_LEFT : Messages.PRACTICE_NOT_IN_SESSION); return false; }
        if (!teleport(player, returnLocation(session.getReturnLocation()))) { message(player, Messages.PRACTICE_TELEPORT_FAILED); return false; }
        report(session, Messages.PRACTICE_INCOMPLETE);
        finish(session, false); message(player, Messages.PRACTICE_LEFT); return true;
    }
    public void disconnect(Player player) {
        waiting.remove(player.getUniqueId());
        PracticeSession session = getSession(player);
        if (session == null) return;
        teleport(player, returnLocation(session.getReturnLocation()));
        finish(session, true);
    }
    public void terminateAfterWorldChange(Player player) {
        PracticeSession session = getSession(player);
        if (session != null) finish(session, false);
    }
    private Location returnLocation(Location original) {
        if (original.getWorld() != null && Bukkit.getWorld(original.getWorld().getUID()) != null && BedWars.nms.isPracticeSpawnSafe(original)) return original;
        Location lobby = BedWars.config.getConfigLoc("lobbyLoc");
        return BedWars.nms.isPracticeSpawnSafe(lobby) ? lobby : Bukkit.getWorlds().get(0).getSpawnLocation();
    }
    private boolean teleport(Player player, Location location) {
        internalTeleports.add(player.getUniqueId());
        try { return player.teleport(location, TeleportCause.PLUGIN); }
        finally { internalTeleports.remove(player.getUniqueId()); }
    }
    private void finish(PracticeSession session, boolean quitting) {
        Player player = session.getPlayer();
        session.nextRound();
        try { session.clearBlocks(); }
        catch (IOException | RuntimeException ex) {
            recovery.block(session.getPoint().getWorldName());
            BedWars.plugin.getLogger().severe("Practice terrain recovery retained: " + ex.getMessage());
        }
        try {
            session.restore();
            if (quitting) settleCursor(player);
        } catch (RuntimeException ex) {
            BedWars.plugin.getLogger().severe("Practice player state restoration failed for " + player.getName() + ": " + ex.getMessage());
            message(player, Messages.PRACTICE_SAVE_FAILED, null, ex.getMessage());
        } finally {
            sessions.remove(player.getUniqueId()); release(session.getPoint(), player.getUniqueId());
            BedWars.nms.playAction(player, "");
        }
    }
    private void settleCursor(Player player) {
        ItemStack cursor = player.getItemOnCursor();
        if (cursor == null || cursor.getType() == Material.AIR) return;
        player.setItemOnCursor(null);
        for (ItemStack remaining : player.getInventory().addItem(cursor).values())
            player.getWorld().dropItem(player.getLocation(), remaining);
    }
    public void replenish(Player player) {
        PracticeSession session = getSession(player);
        if (session == null) return;
        session.rememberLayout();
        fillSlot(player, session.itemSlot(false), session.getPoint().getMode().getMaterial(), session.getPoint().getMode().getAmount());
        if (session.getPoint().getMode() == PracticeMode.FIREBALL) fillSlot(player, session.itemSlot(true), Material.WOOL, 64);
    }
    private void fillSlot(Player player, int slot, Material material, int amount) {
        ItemStack item = player.getInventory().getItem(slot);
        if (item == null || item.getType() != material || item.getAmount() != amount)
            player.getInventory().setItem(slot, new ItemStack(material, amount));
    }
    private void giveControls(PracticeSession session) {
        Player player = session.getPlayer();
        player.getInventory().setItem(6, control(player, Material.COMPASS, Messages.PRACTICE_SETTINGS_ITEM, "PRACTICE_MENU"));
        player.getInventory().setItem(7, control(player, Material.ARROW, Messages.PRACTICE_RETRY_ITEM, "PRACTICE_RETRY"));
        player.getInventory().setItem(8, control(player, Material.BED, Messages.PRACTICE_LEAVE_ITEM, "PRACTICE_LEAVE"));
        player.getInventory().setHeldItemSlot(session.heldSlot());
    }
    private ItemStack control(Player player, Material material, String path, String marker) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta(); meta.setDisplayName(text(player, path)); item.setItemMeta(meta);
        return BedWars.nms.addCustomData(item, marker);
    }
    public void beginFireball(PracticeSession session, Location location) {
        if (!session.hasActiveAttempt())
            session.getAttempt().begin(location.getX(), location.getY(), location.getZ(), System.nanoTime());
    }
    public void placed(PracticeSession session) {
        if (session.getPoint().getMode() != PracticeMode.BRIDGE) return;
        PracticeAttempt attempt = session.getAttempt();
        if (!attempt.isActive()) {
            Location location = session.sampleLocation();
            attempt.begin(location.getX(), location.getY(), location.getZ(), System.nanoTime());
        }
        attempt.placed();
    }
    private boolean reached(PracticeSession session, Location location) {
        if (session.getTarget() != null) return session.getTarget().contains(location);
        for (PracticeTarget target : session.getPoint().getTargets()) if (target.contains(location)) return true;
        return false;
    }
    public void sample(PracticeSession session) {
        if (!session.hasActiveAttempt()) return;
        Location location = session.sampleLocation();
        PracticeAttempt attempt = session.getAttempt();
        boolean ground = BedWars.nms.isPracticeGrounded(location);
        attempt.sample(location.getX(), location.getY(), location.getZ(), ground);
        if (attempt.isActive() && session.getPoint().getMode() == PracticeMode.BRIDGE && ground && reached(session, location))
            report(session, Messages.PRACTICE_SUCCESS);
        else if (attempt.isActive() && session.getPoint().getMode() == PracticeMode.FIREBALL && attempt.wasAirborne() && ground)
            report(session, Messages.PRACTICE_SUCCESS);
    }
    private static String number(double value) { return String.format(Locale.ROOT, "%.2f", value); }
    private String targetName(PracticeSession session) {
        return session.getTarget() == null ? text(session.getPlayer(), Messages.PRACTICE_FREE) : session.getTarget().getName();
    }
    private void report(PracticeSession session, String result) {
        if (!session.hasActiveAttempt()) return;
        PracticeAttempt attempt = session.getAttempt();
        long now = System.nanoTime();
        attempt.finish(now);
        PracticeMode mode = session.getPoint().getMode();
        Player player = session.getPlayer();
        String verdict = text(player, result), time = number(attempt.seconds(now)), distance = number(attempt.getDistance());
        String score;
        if (mode == PracticeMode.BRIDGE) score = text(player, Messages.PRACTICE_SCORE_BRIDGE, "target", targetName(session),
                "result", verdict, "time", time, "speed", number(attempt.speed(now)), "distance", distance, "blocks", String.valueOf(attempt.getBlocks()));
        else score = text(player, Messages.PRACTICE_SCORE_FIREBALL,
                "result", verdict, "time", time, "height", number(attempt.getHeight()), "distance", distance);
        session.getPlayer().sendMessage(score); session.status(score);
        BedWars.nms.playAction(player, score);
    }
    public void refreshUi() {
        menu.refresh();
        long now = System.nanoTime();
        for (PracticeSession session : sessions.values()) {
            String state = session.getStatus(now);
            if (state.isEmpty()) {
                PracticeMode mode = session.getPoint().getMode();
                state = mode != PracticeMode.PEARL && session.hasActiveAttempt() ? number(session.getAttempt().seconds(now)) + "s"
                        : text(session.getPlayer(), Messages.PRACTICE_READY);
                state = text(session.getPlayer(), mode.isShared() ? Messages.PRACTICE_SHARED_STATUS : Messages.PRACTICE_STATUS,
                        "mode", text(session.getPlayer(), mode.getMessagePath()), "target", mode.isShared() ? "" : targetName(session), "state", state);
            }
            BedWars.nms.playAction(session.getPlayer(), state);
        }
        Iterator<Map.Entry<UUID, PracticeMode>> iterator = waiting.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, PracticeMode> entry = iterator.next();
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null || !player.isOnline()) iterator.remove();
            else if (getFreeCount(entry.getValue()) > 0) {
                player.sendMessage(text(player, Messages.PRACTICE_AVAILABLE, "mode", text(player, entry.getValue().getMessagePath())));
                iterator.remove();
            }
        }
    }
    public void shutdown() {
        listener.shutdown(); menu.shutdown(); npc.shutdown(); waiting.clear();
        for (PracticeSession session : new ArrayList<>(sessions.values())) disconnect(session.getPlayer());
    }
    public boolean reload(CommandSender sender) { return reload(sender, null); }
    public boolean reload(CommandSender sender, String id) {
        if (id != null && isPointOccupied(id)) { message(sender, Messages.PRACTICE_POINT_BUSY, id, null); return false; }
        try {
            YamlConfiguration candidate = config.readConfiguration();
            if (id == null) {
                config.install(candidate, occupied.keySet()); npc.start();
                if (!occupied.isEmpty()) message(sender, Messages.PRACTICE_RELOAD_DEFERRED, String.join(", ", occupied.keySet()), null);
            } else config.installPoint(candidate, id);
            message(sender, Messages.PRACTICE_RELOAD_SUCCESS);
            config.getErrors().forEach((key, value) -> message(sender, Messages.PRACTICE_INVALID_POINT, key, value));
            return true;
        } catch (IOException | InvalidConfigurationException | IllegalArgumentException ex) {
            message(sender, Messages.PRACTICE_INVALID_POINT, id, ex.getMessage()); return false;
        }
    }
    private boolean change(CommandSender sender, String id, boolean requireExisting, Consumer<YamlConfiguration> operation) {
        if (id == null || !id.matches("[a-z0-9_-]+")) { message(sender, Messages.PRACTICE_INVALID_ARGUMENTS, id, "Invalid point ID"); return false; }
        if (isPointOccupied(id)) { message(sender, Messages.PRACTICE_POINT_BUSY, id, null); return false; }
        if (requireExisting && config.getPoint(id) == null && !config.getErrors().containsKey(id)) {
            message(sender, Messages.PRACTICE_POINT_NOT_FOUND, id, null); return false;
        }
        try {
            YamlConfiguration candidate = config.copyConfiguration(); operation.accept(candidate); config.commit(candidate, id);
            message(sender, Messages.PRACTICE_ADMIN_SUCCESS, id, null); return true;
        } catch (IOException ex) { message(sender, Messages.PRACTICE_SAVE_FAILED, id, ex.getMessage()); return false; }
        catch (InvalidConfigurationException | IllegalArgumentException ex) { message(sender, Messages.PRACTICE_INVALID_POINT, id, ex.getMessage()); return false; }
    }
    public boolean create(CommandSender sender, String id, PracticeMode mode, Location location) {
        if (config.getPoint(id) != null) { message(sender, Messages.PRACTICE_POINT_EXISTS, id, null); return false; }
        return change(sender, id, false, yaml -> {
            String root = "points." + id + ".";
            yaml.set(root + "mode", mode.getId()); yaml.set(root + "world", location.getWorld().getName());
            yaml.set(root + "spawn", config.stringLocationArenaFormat(location)); yaml.set(root + "fail-y", -1); yaml.set(root + "enabled", false);
        });
    }
    private boolean setLocation(CommandSender sender, String id, String key, Location location) {
        return change(sender, id, true, yaml -> {
            if (!location.getWorld().getName().equals(yaml.getString("points." + id + ".world"))) throw new IllegalArgumentException("Wrong world");
            yaml.set("points." + id + "." + key, config.stringLocationArenaFormat(location));
        });
    }
    public boolean setSpawn(CommandSender sender, String id, Location location) { return setLocation(sender, id, "spawn", location); }
    public boolean setCorner(CommandSender sender, String id, int corner, Location location) { return setLocation(sender, id, "pos" + corner, location); }
    public boolean setFailY(CommandSender sender, String id, int value) { return change(sender, id, true, yaml -> yaml.set("points." + id + ".fail-y", value)); }
    public boolean setEnabled(CommandSender sender, String id, boolean value) { return change(sender, id, true, yaml -> yaml.set("points." + id + ".enabled", value)); }
    public boolean delete(CommandSender sender, String id) { return change(sender, id, true, yaml -> yaml.set("points." + id, null)); }
}
