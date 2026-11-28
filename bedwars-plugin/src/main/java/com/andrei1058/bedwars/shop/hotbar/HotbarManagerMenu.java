package com.andrei1058.bedwars.shop.hotbar;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent;
import com.andrei1058.bedwars.api.events.player.PlayerJoinArenaEvent;
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent;
import com.andrei1058.bedwars.api.events.player.PlayerLeaveArenaEvent;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.arena.Arena;
import com.andrei1058.bedwars.database.PlayerPreferenceService;
import com.andrei1058.bedwars.shop.ShopManager;
import com.andrei1058.bedwars.shop.quickbuy.LobbyQuickBuyMenu;
import com.andrei1058.bedwars.shop.quickbuy.PlayerQuickBuyCache;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

import static com.andrei1058.bedwars.BedWars.nms;

/**
 * 快捷栏管理 GUI：支持拖动放置、点选放置、数字键快绑与点击取消配置。
 */
public final class HotbarManagerMenu implements Listener {
    public static final int SHOP_BUTTON_SLOT = 53;

    private static final int INVENTORY_SIZE = 54;
    private static final int SEPARATOR_START = 18;
    private static final int SEPARATOR_END = 26;
    private static final int HOTBAR_ROW_START = 27;
    private static final int HOTBAR_ROW_END = 35;
    private static final int BACK_SLOT = 48;
    private static final int RESET_SLOT = 50;
    private static final String CURSOR_PREFIX = "HOTBAR_CURSOR_";

    public enum Source {
        LOBBY,
        SHOP
    }

    public static void open(Player player, Source source) {
        if (player == null || source == null || !isContextValid(player, source)) return;
        Holder holder = new Holder(source);
        Inventory inv = Bukkit.createInventory(holder, INVENTORY_SIZE,
                Language.getMsg(player, Messages.HOTBAR_MANAGER_TITLE));
        holder.inventory = inv;
        render(player, holder);
        player.openInventory(inv);
    }

    /** 构造商店右下角与个人设置菜单共用的烈焰粉入口图标。 */
    public static ItemStack createEntryButton(Player player) {
        ItemStack icon = new ItemStack(Material.BLAZE_POWDER);
        ItemMeta meta = icon.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Language.getMsg(player, Messages.HOTBAR_MANAGER_BUTTON_NAME));
            meta.setLore(Language.getList(player, Messages.HOTBAR_MANAGER_BUTTON_LORE));
            icon.setItemMeta(meta);
        }
        return icon;
    }

    private static void render(Player player, Holder holder) {
        Inventory inv = holder.inventory;
        inv.clear();

        // 第 2 行 (10-16)：7 个独立物品类别
        List<String> categoryLore = Language.getList(player, Messages.HOTBAR_MANAGER_CATEGORY_LORE);
        for (HotbarCategory category : HotbarCategory.values()) {
            ItemStack icon = category.createIcon();
            ItemMeta meta = icon.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(category.getDisplayName(player));
                meta.setLore(categoryLore);
                icon.setItemMeta(meta);
            }
            inv.setItem(category.getMenuSlot(), icon);
        }

        // 第 3 行 (18-26)：分隔条
        ItemStack separator = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
        ItemMeta sepMeta = separator.getItemMeta();
        if (sepMeta != null) {
            sepMeta.setDisplayName(Language.getMsg(player, Messages.HOTBAR_MANAGER_SEPARATOR_NAME));
            sepMeta.setLore(Language.getList(player, Messages.HOTBAR_MANAGER_SEPARATOR_LORE));
            separator.setItemMeta(sepMeta);
        }
        for (int slot = SEPARATOR_START; slot <= SEPARATOR_END; slot++) {
            inv.setItem(slot, separator);
        }

        // 第 4 行 (27-35)：快捷栏 Slot 1-9
        HotbarLayout layout = PlayerPreferenceService.getInstance().getHotbarLayout(player.getUniqueId());
        for (int i = 0; i < HotbarLayout.HOTBAR_SIZE; i++) {
            int displaySlot = i + 1;
            HotbarCategory assigned = layout.get(i);
            inv.setItem(HOTBAR_ROW_START + i, buildHotbarSlotItem(player, displaySlot, assigned));
        }

        // 第 6 行：返回与重置按钮
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(Language.getMsg(player, Messages.HOTBAR_MANAGER_BACK));
            back.setItemMeta(backMeta);
        }
        inv.setItem(BACK_SLOT, back);

        ItemStack reset = new ItemStack(Material.BARRIER);
        ItemMeta resetMeta = reset.getItemMeta();
        if (resetMeta != null) {
            resetMeta.setDisplayName(Language.getMsg(player, Messages.HOTBAR_MANAGER_RESET_NAME));
            resetMeta.setLore(Language.getList(player, Messages.HOTBAR_MANAGER_RESET_LORE));
            reset.setItemMeta(resetMeta);
        }
        inv.setItem(RESET_SLOT, reset);
    }

    private static ItemStack buildHotbarSlotItem(Player player, int displaySlot, HotbarCategory category) {
        String slotText = String.valueOf(displaySlot);
        if (category == null) {
            ItemStack empty = new ItemStack(Material.STAINED_GLASS_PANE, displaySlot, (short) 8);
            ItemMeta meta = empty.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(Language.getMsg(player, Messages.HOTBAR_MANAGER_EMPTY_SLOT_NAME)
                        .replace("{slot}", slotText));
                meta.setLore(Language.getList(player, Messages.HOTBAR_MANAGER_EMPTY_SLOT_LORE));
                empty.setItemMeta(meta);
            }
            return empty;
        }
        ItemStack icon = category.createIcon();
        ItemMeta meta = icon.getItemMeta();
        if (meta != null) {
            String categoryName = ChatColor.stripColor(category.getDisplayName(player));
            meta.setDisplayName(Language.getMsg(player, Messages.HOTBAR_MANAGER_FILLED_SLOT_NAME)
                    .replace("{slot}", slotText)
                    .replace("{category}", categoryName));
            List<String> lore = new ArrayList<>();
            for (String line : Language.getList(player, Messages.HOTBAR_MANAGER_FILLED_SLOT_LORE)) {
                lore.add(line.replace("{slot}", slotText).replace("{category}", categoryName));
            }
            meta.setLore(lore);
            icon.setItemMeta(meta);
        }
        return icon;
    }

    private static ItemStack createCursorIcon(Player player, HotbarCategory category) {
        ItemStack icon = category.createIcon();
        ItemMeta meta = icon.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(category.getDisplayName(player));
            icon.setItemMeta(meta);
        }
        return nms.addCustomData(icon, CURSOR_PREFIX + category.getKey());
    }

    private static HotbarCategory readCursorCategory(ItemStack cursor) {
        if (cursor == null || cursor.getType() == Material.AIR || !nms.isCustomBedWarsItem(cursor)) return null;
        String data = nms.getCustomData(cursor);
        if (data == null || !data.startsWith(CURSOR_PREFIX)) return null;
        return HotbarCategory.fromKey(data.substring(CURSOR_PREFIX.length()));
    }

    private static void clearVirtualCursor(Player player, Holder holder) {
        if (holder != null) holder.selected = null;
        if (readCursorCategory(player.getItemOnCursor()) != null) {
            player.setItemOnCursor(null);
        }
    }

    private static void syncInventoryLater(Player player, Holder holder) {
        Bukkit.getScheduler().runTask(BedWars.plugin, () -> {
            if (!player.isOnline()) return;
            if (player.getOpenInventory() != null
                    && player.getOpenInventory().getTopInventory() != null
                    && player.getOpenInventory().getTopInventory().getHolder() == holder) {
                player.updateInventory();
            }
        });
    }

    private static boolean isContextValid(Player player, Source source) {
        if (player == null || !player.isOnline()) return false;
        if (source == Source.LOBBY) {
            return Arena.getArenaByPlayer(player) == null
                    && player.getWorld().getName().equalsIgnoreCase(BedWars.config.getLobbyWorldName());
        }
        IArena arena = Arena.getArenaByPlayer(player);
        return arena != null && arena.getStatus() == GameState.playing
                && arena.isPlayer(player) && !arena.isSpectator(player) && !arena.isReSpawning(player);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof Holder)) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        Holder holder = (Holder) top.getHolder();
        if (!isContextValid(player, holder.source)) {
            clearVirtualCursor(player, holder);
            player.closeInventory();
            return;
        }

        int rawSlot = event.getRawSlot();
        HotbarCategory carrying = holder.selected != null ? holder.selected : readCursorCategory(player.getItemOnCursor());

        // 点击背包区域或窗口外时取消拿起状态，防止虚拟图标进入真实背包。
        if (rawSlot < 0 || rawSlot >= top.getSize()) {
            if (carrying != null) {
                clearVirtualCursor(player, holder);
                syncInventoryLater(player, holder);
            }
            return;
        }

        if (rawSlot == BACK_SLOT) {
            clearVirtualCursor(player, holder);
            Bukkit.getScheduler().runTask(BedWars.plugin, () -> {
                if (player.isOnline()
                        && player.getOpenInventory() != null
                        && player.getOpenInventory().getTopInventory() != null
                        && player.getOpenInventory().getTopInventory().getHolder() == holder) {
                    navigateBack(player, holder.source);
                }
            });
            return;
        }

        if (rawSlot == RESET_SLOT) {
            clearVirtualCursor(player, holder);
            PlayerPreferenceService.getInstance().resetHotbarLayout(player);
            render(player, holder);
            syncInventoryLater(player, holder);
            return;
        }

        // 点击类别区域 (10-16)
        HotbarCategory clickedCategory = HotbarCategory.fromMenuSlot(rawSlot);
        if (clickedCategory != null) {
            if (event.getClick() == ClickType.NUMBER_KEY) {
                int hotbarSlot = event.getHotbarButton();
                if (hotbarSlot >= 0 && hotbarSlot < HotbarLayout.HOTBAR_SIZE) {
                    clearVirtualCursor(player, holder);
                    PlayerPreferenceService.getInstance().setHotbarSlot(player, hotbarSlot, clickedCategory);
                    render(player, holder);
                    syncInventoryLater(player, holder);
                }
                return;
            }
            if (event.getClick() == ClickType.RIGHT || event.getClick() == ClickType.SHIFT_RIGHT) {
                clearVirtualCursor(player, holder);
                PlayerPreferenceService.getInstance().clearHotbarCategory(player, clickedCategory);
                render(player, holder);
                syncInventoryLater(player, holder);
                return;
            }
            if (carrying == clickedCategory) {
                clearVirtualCursor(player, holder);
            } else {
                holder.selected = clickedCategory;
                player.setItemOnCursor(createCursorIcon(player, clickedCategory));
            }
            syncInventoryLater(player, holder);
            return;
        }

        // 点击快捷栏槽位区域 (27-35)
        if (rawSlot >= HOTBAR_ROW_START && rawSlot <= HOTBAR_ROW_END) {
            int hotbarSlot = rawSlot - HOTBAR_ROW_START;
            if (carrying != null) {
                PlayerPreferenceService.getInstance().setHotbarSlot(player, hotbarSlot, carrying);
                clearVirtualCursor(player, holder);
                render(player, holder);
                syncInventoryLater(player, holder);
            } else {
                HotbarLayout layout = PlayerPreferenceService.getInstance().getHotbarLayout(player.getUniqueId());
                if (layout.get(hotbarSlot) != null) {
                    PlayerPreferenceService.getInstance().setHotbarSlot(player, hotbarSlot, null);
                    render(player, holder);
                    syncInventoryLater(player, holder);
                }
            }
            return;
        }

        // 点击分隔条或其他空白格：取消当前拿起状态
        if (carrying != null) {
            clearVirtualCursor(player, holder);
            syncInventoryLater(player, holder);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof Holder)) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        Holder holder = (Holder) top.getHolder();
        if (!isContextValid(player, holder.source)) {
            clearVirtualCursor(player, holder);
            player.closeInventory();
            return;
        }

        HotbarCategory carrying = holder.selected != null ? holder.selected : readCursorCategory(event.getOldCursor());
        if (carrying == null) return;

        HotbarLayout layout = PlayerPreferenceService.getInstance().getHotbarLayout(player.getUniqueId());
        HotbarLayout updated = layout;
        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot >= HOTBAR_ROW_START && rawSlot <= HOTBAR_ROW_END) {
                updated = updated.withSlot(rawSlot - HOTBAR_ROW_START, carrying);
            }
        }
        if (updated != layout) {
            PlayerPreferenceService.getInstance().saveHotbarLayout(player, updated);
            render(player, holder);
        }
        clearVirtualCursor(player, holder);
        syncInventoryLater(player, holder);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        if (!(event.getInventory().getHolder() instanceof Holder)) return;
        Player player = (Player) event.getPlayer();
        Holder holder = (Holder) event.getInventory().getHolder();
        clearVirtualCursor(player, holder);
        Bukkit.getScheduler().runTask(BedWars.plugin, () -> {
            if (player.isOnline()) player.updateInventory();
        });
    }

    private static void navigateBack(Player player, Source source) {
        if (!isContextValid(player, source)) {
            player.closeInventory();
            return;
        }
        if (source == Source.LOBBY) {
            LobbyQuickBuyMenu.open(player);
        } else {
            PlayerQuickBuyCache cache = PlayerQuickBuyCache.getQuickBuyCache(player.getUniqueId());
            if (cache != null && cache.isLoaded()) {
                ShopManager.getShop().open(player, cache, false);
            } else {
                player.closeInventory();
            }
        }
    }

    /** 插件停用时关闭全部快捷栏配置界面并清理虚拟光标，避免物品残留。 */
    public static void closeAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            closeIfViewing(player);
            clearVirtualCursor(player, null);
        }
    }

    private static void closeIfViewing(Player player) {
        if (player == null || !player.isOnline()) return;
        if (player.getOpenInventory() == null || player.getOpenInventory().getTopInventory() == null) return;
        InventoryHolder holder = player.getOpenInventory().getTopInventory().getHolder();
        if (holder instanceof Holder) {
            clearVirtualCursor(player, (Holder) holder);
            player.closeInventory();
        }
    }

    @EventHandler
    public void onJoinArena(PlayerJoinArenaEvent event) {
        closeIfViewing(event.getPlayer());
    }

    @EventHandler
    public void onLeaveArena(PlayerLeaveArenaEvent event) {
        closeIfViewing(event.getPlayer());
    }

    @EventHandler
    public void onKill(PlayerKillEvent event) {
        closeIfViewing(event.getVictim());
    }

    @EventHandler
    public void onGameStateChange(GameStateChangeEvent event) {
        if (event.getNewState() != GameState.playing && event.getArena() != null) {
            for (Player player : event.getArena().getPlayers()) {
                closeIfViewing(player);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        closeIfViewing(event.getPlayer());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        if (player.getOpenInventory() != null && player.getOpenInventory().getTopInventory() != null) {
            InventoryHolder holder = player.getOpenInventory().getTopInventory().getHolder();
            if (holder instanceof Holder && !isContextValid(player, ((Holder) holder).source)) {
                clearVirtualCursor(player, (Holder) holder);
                player.closeInventory();
            }
        }
    }

    private static final class Holder implements InventoryHolder {
        final Source source;
        Inventory inventory;
        HotbarCategory selected;

        Holder(Source source) {
            this.source = source;
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }
}
