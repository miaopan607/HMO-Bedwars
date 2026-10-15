package com.andrei1058.bedwars.shop.quickbuy;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.events.player.PlayerJoinArenaEvent;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.arena.Arena;
import com.andrei1058.bedwars.shop.ShopManager;
import com.andrei1058.bedwars.shop.main.CategoryContent;
import com.andrei1058.bedwars.shop.main.ShopCategory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

/** 使用独立库存持有者隔离主城编辑与对局购买。 */
public class LobbyQuickBuyMenu implements Listener {
    private enum Page { MENU, QUICK, CATEGORY, SLOT }

    private static boolean inLobby(Player player) {
        return player.isOnline() && Arena.getArenaByPlayer(player) == null
                && player.getWorld().getName().equalsIgnoreCase(BedWars.config.getLobbyWorldName());
    }

    public static void open(Player player) {
        if (inLobby(player)) show(player, Page.MENU, null, null);
    }

    private static void show(Player player, Page page, ShopCategory category, CategoryContent pending) {
        Menu menu = new Menu(page, category, pending);
        String title = page == Page.MENU ? Messages.LOBBY_MENU_TITLE
                : page == Page.SLOT ? Messages.QUICK_BUY_EDITOR_SELECT_SLOT_TITLE : Messages.QUICK_BUY_EDITOR_TITLE;
        menu.inventory = Bukkit.createInventory(menu, page == Page.MENU ? 27 : 54, Language.getMsg(player, title));
        if (page == Page.MENU) {
            menu.inventory.setItem(11, item(Material.PAPER, Language.getMsg(player, Messages.LOBBY_MENU_STATS)));
            menu.inventory.setItem(15, item(Material.CHEST, Language.getMsg(player, Messages.LOBBY_MENU_QUICK_BUY)));
        } else {
            PlayerQuickBuyCache cache = PlayerQuickBuyCache.getQuickBuyCache(player.getUniqueId());
            if (cache == null || cache.isLoadFailed()) {
                if (cache != null) cache.destroy();
                cache = new PlayerQuickBuyCache(player);
            }
            render(player, menu, cache);
        }
        player.openInventory(menu.inventory);
        if (page != Page.MENU) {
            PlayerQuickBuyCache cache = PlayerQuickBuyCache.getQuickBuyCache(player.getUniqueId());
            if (!cache.isLoaded() && !cache.isLoadFailed()) {
                menu.loading = new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!inLobby(player) || player.getOpenInventory().getTopInventory().getHolder() != menu) {
                            cancel();
                            return;
                        }
                        if (cache.isLoaded() || cache.isLoadFailed()) {
                            render(player, menu, cache);
                            cancel();
                        }
                    }
                }.runTaskTimer(BedWars.plugin, 1L, 2L);
            }
        }
    }

    private static void render(Player player, Menu menu, PlayerQuickBuyCache cache) {
        Inventory inv = menu.inventory;
        inv.clear();
        if (!cache.isLoaded()) {
            inv.setItem(49, item(Material.ARROW, Language.getMsg(player, Messages.QUICK_BUY_EDITOR_BACK)));
            inv.setItem(22, item(Material.WATCH, Language.getMsg(player, cache.isLoadFailed()
                    ? Messages.QUICK_BUY_EDITOR_LOAD_FAILED : Messages.QUICK_BUY_EDITOR_LOADING)));
            return;
        }
        ShopManager.getShop().addSeparator(player, inv);
        inv.setItem(ShopManager.getShop().getQuickBuyButton().getSlot(), ShopManager.getShop().getQuickBuyButton().getItemStack(player));
        for (ShopCategory category : ShopManager.getShop().getCategoryList()) {
            inv.setItem(category.getSlot(), category.getItemStack(player));
        }
        if (menu.page == Page.CATEGORY) {
            for (CategoryContent content : menu.category.getCategoryContentList()) {
                inv.setItem(content.getSlot(), content.getEditorItemStack(player, cache.hasCategoryContent(content)));
            }
        } else {
            ItemStack empty = item(Material.STAINED_GLASS_PANE, Language.getMsg(player,
                    menu.page == Page.SLOT ? Messages.QUICK_BUY_EDITOR_SELECT_SLOT : Messages.SHOP_QUICK_EMPTY_NAME));
            for (int slot : PlayerQuickBuyCache.quickSlots) inv.setItem(slot, empty);
            for (QuickBuyElement element : cache.getElements()) {
                ItemStack preview = element.getCategoryContent().getEditorItemStack(player, true);
                ItemMeta meta = preview.getItemMeta();
                List<String> lore = new ArrayList<>(meta.getLore());
                lore.add(Language.getMsg(player, menu.page == Page.SLOT
                        ? Messages.QUICK_BUY_EDITOR_SELECT_SLOT : Messages.QUICK_BUY_EDITOR_REMOVE));
                meta.setLore(lore);
                preview.setItemMeta(meta);
                inv.setItem(element.getSlot(), preview);
            }
        }
        inv.setItem(49, item(Material.ARROW, Language.getMsg(player, Messages.QUICK_BUY_EDITOR_BACK)));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof Menu)) return;
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        Menu menu = (Menu) top.getHolder();
        if (!inLobby(player)) {
            player.closeInventory();
            return;
        }
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= top.getSize()) return;
        boolean normal = event.getClick() == ClickType.LEFT || event.getClick() == ClickType.RIGHT;
        boolean shift = event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT;
        if (!normal && !shift) return;
        if (menu.page == Page.MENU) {
            if (!normal) return;
            if (slot == 11) later(player, menu, () -> {
                player.closeInventory();
                Bukkit.dispatchCommand(player, "bw stats");
            });
            if (slot == 15) navigate(player, menu, Page.QUICK, null, null);
            return;
        }
        if (slot == 49 && normal) {
            navigate(player, menu, menu.page == Page.QUICK ? Page.MENU : Page.QUICK, null, null);
            return;
        }
        PlayerQuickBuyCache cache = PlayerQuickBuyCache.getQuickBuyCache(player.getUniqueId());
        if (cache == null || !cache.isLoaded()) return;
        if (menu.page == Page.SLOT) {
            if (normal && isQuickSlot(slot) && !cache.hasCategoryContent(menu.pending)) {
                cache.setElement(slot, menu.pending);
                navigate(player, menu, Page.QUICK, null, null);
            }
            return;
        }
        if (normal) {
            if (slot == ShopManager.getShop().getQuickBuyButton().getSlot()) {
                navigate(player, menu, Page.QUICK, null, null);
                return;
            }
            for (ShopCategory category : ShopManager.getShop().getCategoryList()) {
                if (slot == category.getSlot()) {
                    navigate(player, menu, Page.CATEGORY, category, null);
                    return;
                }
            }
        }
        if (menu.page == Page.QUICK && shift && isQuickSlot(slot)) {
            for (QuickBuyElement element : cache.getElements()) {
                if (element.getSlot() != slot) continue;
                cache.setElement(slot, null);
                render(player, menu, cache);
                return;
            }
        } else if (menu.page == Page.CATEGORY && normal) {
            for (CategoryContent content : menu.category.getCategoryContentList()) {
                if (slot != content.getSlot()) continue;
                if (cache.hasCategoryContent(content)) {
                    player.sendMessage(Language.getMsg(player, Messages.QUICK_BUY_EDITOR_ADDED));
                } else navigate(player, menu, Page.SLOT, null, content);
                return;
            }
        }
    }

    private static boolean isQuickSlot(int slot) {
        for (int quick : PlayerQuickBuyCache.quickSlots) if (quick == slot) return true;
        return false;
    }

    private static void navigate(Player player, Menu menu, Page page, ShopCategory category, CategoryContent pending) {
        later(player, menu, () -> show(player, page, category, pending));
    }

    private static void later(Player player, Menu menu, Runnable action) {
        Bukkit.getScheduler().runTask(BedWars.plugin, () -> {
            if (inLobby(player) && player.getOpenInventory().getTopInventory().getHolder() == menu) action.run();
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof Menu) event.setCancelled(true);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof Menu)) return;
        Menu menu = (Menu) event.getInventory().getHolder();
        if (menu.loading != null) menu.loading.cancel();
        if (menu.page != Page.MENU) {
            PlayerQuickBuyCache cache = PlayerQuickBuyCache.getQuickBuyCache(event.getPlayer().getUniqueId());
            if (cache != null) cache.pushChangesToDB();
        }
    }

    private static void close(Player player) {
        if (player.getOpenInventory().getTopInventory().getHolder() instanceof Menu) player.closeInventory();
    }

    @EventHandler
    public void onJoinArena(PlayerJoinArenaEvent event) { close(event.getPlayer()); }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) { close(event.getPlayer()); }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        if (!inLobby(event.getPlayer())) close(event.getPlayer());
    }

    private static ItemStack item(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    private static class Menu implements InventoryHolder {
        final Page page;
        final ShopCategory category;
        final CategoryContent pending;
        Inventory inventory;
        BukkitTask loading;

        Menu(Page page, ShopCategory category, CategoryContent pending) {
            this.page = page;
            this.category = category;
            this.pending = pending;
        }

        @Override
        public Inventory getInventory() { return inventory; }
    }
}
