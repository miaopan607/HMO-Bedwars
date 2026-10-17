package com.andrei1058.bedwars.arena.compass;

import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static com.andrei1058.bedwars.BedWars.nms;
import static com.andrei1058.bedwars.api.language.Language.getMsg;

/** 集中管理指南针导航、菜单和个人追踪购买状态。 */
public final class CompassManager {

    public static final int DEFAULT_SLOT = 8;
    public static final int TRACKER_COST = 2;
    public static final String COMPASS_MARKER = "COMPASS";

    private static final Set<String> SUPPORTED_GROUPS = new HashSet<>(Arrays.asList(
            "default", "solo", "doubles", "3v3v3v3", "4v4v4v4", "4v4"
    ));
    private static final Map<UUID, CompassState> STATES = new HashMap<>();

    private CompassManager() {
    }

    private static final class CompassState {
        private ITeam teamTarget;
        private ITeam enemyTrackerTarget;
        private boolean enemyTrackerPurchased;
    }

    private static final QuickMessage[] QUICK_MESSAGES = QuickMessage.values();

    private enum QuickMessage {
        DEFENDING(10, Messages.COMPASS_QUICK_DEFENDING),
        ATTACKING(11, Messages.COMPASS_QUICK_ATTACKING),
        RESOURCES(12, Messages.COMPASS_QUICK_RESOURCES),
        INCOMING(13, Messages.COMPASS_QUICK_INCOMING),
        MID(14, Messages.COMPASS_QUICK_MID),
        ATTACK_TEAM(15, Messages.COMPASS_QUICK_ATTACK_TEAM, Messages.COMPASS_QUICK_ATTACK_TEAM_MESSAGE);

        private final int slot;
        private final String menuKey;
        private final String messageKey;

        QuickMessage(int slot, String menuKey) {
            this(slot, menuKey, menuKey);
        }

        QuickMessage(int slot, String menuKey, String messageKey) {
            this.slot = slot;
            this.menuKey = menuKey;
            this.messageKey = messageKey;
        }

        private static QuickMessage forSlot(int slot) {
            for (QuickMessage message : QUICK_MESSAGES) {
                if (message.slot == slot) return message;
            }
            return null;
        }
    }

    public static boolean isSupportedArena(IArena arena) {
        return arena != null && arena.getGroup() != null
                && SUPPORTED_GROUPS.contains(arena.getGroup().toLowerCase());
    }

    public static void giveCompass(Player player) {
        if (player == null) return;
        removeCompass(player);
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        meta.setDisplayName(getMsg(player, Messages.COMPASS_ITEM_NAME));
        compass.setItemMeta(meta);
        player.getInventory().setItem(DEFAULT_SLOT, nms.addCustomData(compass, COMPASS_MARKER));
        player.updateInventory();
    }

    static void restoreCompass(IArena arena, Player player) {
        if (!isActiveSupportedPlayer(arena, player)) return;
        resetState(player);
        giveCompass(player);
    }

    public static void removeCompass(Player player) {
        if (player == null) return;
        for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
            if (isCompass(player.getInventory().getItem(slot))) {
                player.getInventory().setItem(slot, null);
            }
        }
        if (isCompass(player.getItemOnCursor())) {
            player.setItemOnCursor(null);
        }
        player.updateInventory();
    }

    public static void resetState(Player player) {
        if (player == null) return;
        CompassState state = state(player);
        state.teamTarget = null;
        state.enemyTrackerTarget = null;
        state.enemyTrackerPurchased = false;
        clearActionBar(player);
    }

    public static void clearState(Player player) {
        if (player == null) return;
        STATES.remove(player.getUniqueId());
        removeCompass(player);
        clearActionBar(player);
    }

    public static void updateArena(IArena arena) {
        if (!isSupportedArena(arena) || arena.getStatus() != GameState.playing) return;
        for (Player player : arena.getPlayers()) {
            CompassState state = STATES.get(player.getUniqueId());
            if (state == null || !isActivePlayer(arena, player)) continue;
            ITeam targetTeam = resolveTargetTeam(arena, player, state);
            Player target = findNearestTarget(arena, player, targetTeam);
            if (target == null) {
                player.setCompassTarget(player.getLocation());
                clearActionBar(player);
                continue;
            }
            player.setCompassTarget(target.getLocation());
            int distance = (int) Math.sqrt(player.getLocation().distanceSquared(target.getLocation()));
            String message = getMsg(player, Messages.FORMATTING_ACTION_BAR_TRACKING)
                    .replace("{team}", teamDisplay(targetTeam, player) + " " + target.getDisplayName())
                    .replace("{distance}", String.valueOf(distance));
            nms.playAction(player, message.replace("&", "§"));
        }
    }

    public static void openMainMenu(Player player) {
        IArena arena = Arena.getArenaByPlayer(player);
        if (!isActiveSupportedPlayer(arena, player)) return;
        CompassMenuHolder holder = new CompassMenuHolder(arena, MenuType.MAIN, Collections.emptyList());
        Inventory inventory = Bukkit.createInventory(holder, 27, getMsg(player, Messages.COMPASS_MENU_MAIN_TITLE));
        holder.inventory = inventory;
        inventory.setItem(11, button(player, Material.PAPER, Messages.COMPASS_MAIN_QUICK_NAME,
                arena.getGroup().equalsIgnoreCase("Solo") ? Messages.COMPASS_SOLO_DISABLED_LORE : Messages.COMPASS_MAIN_QUICK_LORE));
        String trackerLore = isTrackerUnlocked(arena, player)
                ? Messages.COMPASS_MAIN_TRACKER_LORE : Messages.COMPASS_TRACKER_LOCKED_LORE;
        inventory.setItem(15, button(player, Material.COMPASS, Messages.COMPASS_MAIN_TRACKER_NAME, trackerLore));
        inventory.setItem(22, button(player, Material.BARRIER, Messages.COMPASS_CLOSE, null));
        player.openInventory(inventory);
    }

    public static void handleInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof CompassMenuHolder)) return;
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        CompassMenuHolder holder = (CompassMenuHolder) top.getHolder();
        if (!isValidMenuPlayer(holder, player)) {
            player.closeInventory();
            return;
        }
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= top.getSize()) return;
        switch (holder.type) {
            case MAIN:
                handleMainClick(player, holder.arena, slot);
                break;
            case QUICK:
                handleQuickClick(player, holder.arena, slot);
                break;
            case TEAM_TARGET:
                if (slot == 22) {
                    openQuickMenu(player, holder.arena);
                } else if (slot >= 10 && slot <= 17) {
                    selectTeamTarget(player, holder.arena, holder.teams, slot - 10);
                }
                break;
            case TRACKER:
                if (slot == 22) {
                    openMainMenu(player);
                } else if (slot >= 10 && slot <= 17) {
                    purchaseTracker(player, holder.arena, holder.teams, slot - 10);
                }
                break;
            default:
                break;
        }
    }

    public static void handleInventoryDrag(InventoryDragEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof CompassMenuHolder)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        CompassMenuHolder holder = (CompassMenuHolder) event.getView().getTopInventory().getHolder();
        if (!isValidMenuPlayer(holder, player)) {
            player.closeInventory();
        }
    }

    public static void closeArenaMenus(IArena arena) {
        if (arena == null) return;
        for (Player player : Bukkit.getOnlinePlayers()) {
            Inventory top = player.getOpenInventory().getTopInventory();
            if (top.getHolder() instanceof CompassMenuHolder
                    && ((CompassMenuHolder) top.getHolder()).arena == arena) {
                player.closeInventory();
            }
        }
    }

    public static void clearArena(IArena arena) {
        if (arena == null) return;
        closeArenaMenus(arena);
        for (Player player : arena.getPlayers()) clearState(player);
        for (Player player : arena.getSpectators()) clearState(player);
    }

    public static void clearAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            clearState(player);
            Inventory top = player.getOpenInventory().getTopInventory();
            if (top.getHolder() instanceof CompassMenuHolder) player.closeInventory();
        }
        STATES.clear();
    }

    public static boolean isTrackerUnlocked(IArena arena, Player player) {
        if (!isActiveSupportedPlayer(arena, player)) return false;
        ITeam ownTeam = arena.getTeam(player);
        if (ownTeam == null) return false;
        for (ITeam team : arena.getTeams()) {
            if (sameTeam(team, ownTeam) || team.getMembersCache().isEmpty()) continue;
            if (!team.isBedDestroyed()) return false;
        }
        return true;
    }

    private static void handleMainClick(Player player, IArena arena, int slot) {
        if (slot == 22) {
            player.closeInventory();
        } else if (slot == 11) {
            if (arena.getGroup().equalsIgnoreCase("Solo")) {
                player.sendMessage(getMsg(player, Messages.COMPASS_SOLO_DISABLED));
            } else {
                openQuickMenu(player, arena);
            }
        } else if (slot == 15) {
            if (!isTrackerUnlocked(arena, player)) {
                List<String> lockedLore = Language.getList(player, Messages.COMPASS_TRACKER_LOCKED_LORE);
                if (!lockedLore.isEmpty()) player.sendMessage(lockedLore.get(0));
            } else {
                openTeamSelectionMenu(player, arena, MenuType.TRACKER,
                        Messages.COMPASS_MENU_TRACKER_TITLE, Messages.COMPASS_TRACKER_TEAM_LORE);
            }
        }
    }

    private static void handleQuickClick(Player player, IArena arena, int slot) {
        if (slot == 22) {
            openMainMenu(player);
            return;
        }
        QuickMessage message = QuickMessage.forSlot(slot);
        if (message == null) return;
        if (message == QuickMessage.ATTACK_TEAM) {
            openTeamSelectionMenu(player, arena, MenuType.TEAM_TARGET,
                    Messages.COMPASS_MENU_TEAM_TARGET_TITLE, Messages.COMPASS_TEAM_TARGET_LORE);
        } else {
            sendTeamChat(player, arena, getMsg(player, message.messageKey));
            player.closeInventory();
        }
    }

    private static void selectTeamTarget(Player player, IArena arena, List<ITeam> teams, int index) {
        ITeam selected = index >= 0 && index < teams.size() ? teams.get(index) : null;
        if (!isValidEnemyTeam(arena, player, selected)) {
            player.closeInventory();
            return;
        }
        CompassState state = state(player);
        state.teamTarget = selected;
        sendTeamTargetMessage(player, arena, selected);
        player.closeInventory();
    }

    private static void purchaseTracker(Player player, IArena arena, List<ITeam> teams, int index) {
        ITeam selected = index >= 0 && index < teams.size() ? teams.get(index) : null;
        if (!isTrackerUnlocked(arena, player) || !isValidEnemyTeam(arena, player, selected)) {
            player.closeInventory();
            return;
        }
        if (countEmeralds(player) < TRACKER_COST) {
            player.sendMessage(getMsg(player, Messages.COMPASS_TRACKER_INSUFFICIENT));
            player.closeInventory();
            return;
        }
        removeEmeralds(player, TRACKER_COST);
        CompassState state = state(player);
        state.enemyTrackerPurchased = true;
        state.enemyTrackerTarget = selected;
        player.sendMessage(getMsg(player, Messages.COMPASS_TRACKER_PURCHASED)
                .replace("{team}", teamDisplay(selected, player)));
        player.closeInventory();
    }

    private static void sendTeamTargetMessage(Player sender, IArena arena, ITeam target) {
        sendTeamChat(sender, arena, getMsg(sender, QuickMessage.ATTACK_TEAM.messageKey)
                .replace("{team}", teamDisplay(target, sender)));
    }

    private static void sendTeamChat(Player sender, IArena arena, String message) {
        if (arena.getTeam(sender) != null) sender.chat(message);
    }

    private static void openQuickMenu(Player player, IArena arena) {
        CompassMenuHolder holder = new CompassMenuHolder(arena, MenuType.QUICK, Collections.emptyList());
        Inventory inventory = Bukkit.createInventory(holder, 27, getMsg(player, Messages.COMPASS_MENU_QUICK_TITLE));
        holder.inventory = inventory;
        for (QuickMessage message : QUICK_MESSAGES) {
            inventory.setItem(message.slot, button(player, Material.PAPER, message.menuKey, null));
        }
        inventory.setItem(22, button(player, Material.BARRIER, Messages.COMPASS_BACK, null));
        player.openInventory(inventory);
    }

    private static void openTeamSelectionMenu(Player player, IArena arena, MenuType type,
                                              String titleKey, String loreKey) {
        List<ITeam> teams = enemyTeams(arena, player);
        CompassMenuHolder holder = new CompassMenuHolder(arena, type, teams);
        Inventory inventory = Bukkit.createInventory(holder, 27, getMsg(player, titleKey));
        holder.inventory = inventory;
        for (int i = 0; i < Math.min(teams.size(), 8); i++) {
            ITeam team = teams.get(i);
            ItemStack item = teamItem(team);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(team.getDisplayName(Language.getPlayerLanguage(player)));
            meta.setLore(replaceLore(player, loreKey, team));
            item.setItemMeta(meta);
            inventory.setItem(10 + i, item);
        }
        inventory.setItem(22, button(player, Material.BARRIER, Messages.COMPASS_BACK, null));
        player.openInventory(inventory);
    }

    private static List<ITeam> enemyTeams(IArena arena, Player player) {
        List<ITeam> result = new ArrayList<>();
        ITeam ownTeam = arena.getTeam(player);
        for (ITeam team : arena.getTeams()) {
            if (!sameTeam(team, ownTeam) && !team.getMembers().isEmpty()) result.add(team);
        }
        return result;
    }

    private static List<String> replaceLore(Player player, String path, ITeam team) {
        List<String> result = new ArrayList<>();
        for (String line : Language.getList(player, path)) {
            result.add(line.replace("{members}", String.valueOf(team.getMembers().size()))
                    .replace("{cost}", String.valueOf(TRACKER_COST)));
        }
        return result;
    }

    private static ItemStack teamItem(ITeam team) {
        return nms.colourItem(new ItemStack(nms.woolMaterial()), team);
    }

    private static ItemStack button(Player player, Material material, String name, String lorePath) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(getMsg(player, name));
        if (lorePath != null) {
            List<String> lore = new ArrayList<>();
            for (String line : Language.getList(player, lorePath)) {
                lore.add(line.replace("{cost}", String.valueOf(TRACKER_COST)));
            }
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }

    private static CompassState state(Player player) {
        CompassState state = STATES.get(player.getUniqueId());
        if (state == null) {
            state = new CompassState();
            STATES.put(player.getUniqueId(), state);
        }
        return state;
    }

    private static ITeam resolveTargetTeam(IArena arena, Player player, CompassState state) {
        if (state.enemyTrackerPurchased) {
            if (isArenaTeam(arena, state.enemyTrackerTarget) && !state.enemyTrackerTarget.getMembers().isEmpty()) {
                return state.enemyTrackerTarget;
            }
            state.enemyTrackerPurchased = false;
            state.enemyTrackerTarget = null;
        }
        if (isArenaTeam(arena, state.teamTarget) && !state.teamTarget.getMembers().isEmpty()) {
            return state.teamTarget;
        }
        state.teamTarget = null;
        ITeam ownTeam = arena.getTeam(player);
        if (!arena.getGroup().equalsIgnoreCase("Solo") && ownTeam != null) {
            return ownTeam;
        }
        return null;
    }

    private static Player findNearestTarget(IArena arena, Player player, ITeam team) {
        if (team == null) return null;
        Player nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Player candidate : team.getMembers()) {
            if (candidate == player || !isActivePlayer(arena, candidate)
                    || arena.getRespawnSessions().containsKey(candidate) || candidate.isDead()) continue;
            double distance = player.getLocation().distanceSquared(candidate.getLocation());
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = candidate;
            }
        }
        return nearest;
    }

    static boolean isActiveSupportedPlayer(IArena arena, Player player) {
        return isSupportedArena(arena) && arena.getStatus() == GameState.playing
                && isActivePlayer(arena, player);
    }

    private static boolean isActivePlayer(IArena arena, Player player) {
        return player != null && player.isOnline() && arena.isPlayer(player) && !arena.isSpectator(player);
    }

    private static boolean isValidMenuPlayer(CompassMenuHolder holder, Player player) {
        return isActiveSupportedPlayer(holder.arena, player)
                && Arena.getArenaByPlayer(player) == holder.arena;
    }

    private static boolean isValidEnemyTeam(IArena arena, Player player, ITeam team) {
        ITeam ownTeam = arena.getTeam(player);
        return team != null && !sameTeam(team, ownTeam) && isArenaTeam(arena, team) && !team.getMembers().isEmpty();
    }

    private static boolean isArenaTeam(IArena arena, ITeam target) {
        if (target == null) return false;
        for (ITeam team : arena.getTeams()) if (sameTeam(team, target)) return true;
        return false;
    }

    private static boolean sameTeam(ITeam first, ITeam second) {
        return first == second || first != null && second != null
                && Objects.equals(first.getIdentity(), second.getIdentity());
    }

    private static String teamDisplay(ITeam team, Player viewer) {
        return team.getColor().chat() + team.getDisplayName(Language.getPlayerLanguage(viewer));
    }

    private static int countEmeralds(Player player) {
        int amount = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.EMERALD) amount += item.getAmount();
        }
        return amount;
    }

    private static void removeEmeralds(Player player, int amount) {
        int remaining = amount;
        for (int slot = 0; slot < player.getInventory().getSize() && remaining > 0; slot++) {
            ItemStack item = player.getInventory().getItem(slot);
            if (item == null || item.getType() != Material.EMERALD) continue;
            int removed = Math.min(remaining, item.getAmount());
            if (removed == item.getAmount()) player.getInventory().setItem(slot, null);
            else item.setAmount(item.getAmount() - removed);
            remaining -= removed;
        }
        player.updateInventory();
    }

    static boolean isCompass(ItemStack item) {
        return item != null && item.getType() == Material.COMPASS
                && nms.isCustomBedWarsItem(item) && COMPASS_MARKER.equals(nms.getCustomData(item));
    }

    private static void clearActionBar(Player player) {
        nms.playAction(player, "");
    }

    private enum MenuType {
        MAIN,
        QUICK,
        TEAM_TARGET,
        TRACKER
    }

    private static final class CompassMenuHolder implements InventoryHolder {
        private final IArena arena;
        private final MenuType type;
        private final List<ITeam> teams;
        private Inventory inventory;

        private CompassMenuHolder(IArena arena, MenuType type, List<ITeam> teams) {
            this.arena = arena;
            this.type = type;
            this.teams = teams;
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }
}
