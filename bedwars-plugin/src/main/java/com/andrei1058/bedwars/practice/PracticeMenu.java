package com.andrei1058.bedwars.practice;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;

public final class PracticeMenu implements Listener {
    private final PracticeManager manager;
    public PracticeMenu(PracticeManager manager) { this.manager = manager; }
    public void open(Player player) {
        PracticeMenuHolder holder = new PracticeMenuHolder();
        holder.inventory = Bukkit.createInventory(holder, 27, Language.getMsg(player, Messages.PRACTICE_MENU_TITLE));
        int slot = 11;
        for (PracticeMode mode : PracticeMode.values()) {
            ItemStack item = new ItemStack(mode.getMaterial());
            ItemMeta meta = item.getItemMeta(); meta.setDisplayName(Language.getMsg(player, mode.getMessagePath()));
            List<String> lore = new ArrayList<>();
            String free = String.valueOf(manager.getFreeCount(mode));
            String total = String.valueOf(manager.getTotalCount(mode));
            for (String line : Language.getList(player, Messages.PRACTICE_MENU_LORE)) {
                lore.add(line.replace("{free}", free).replace("{total}", total));
            }
            meta.setLore(lore); item.setItemMeta(meta); holder.inventory.setItem(slot, item); slot += 2;
        }
        player.openInventory(holder.inventory);
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void click(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof PracticeMenuHolder)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player) || (event.getClick() != ClickType.LEFT && event.getClick() != ClickType.RIGHT)) return;
        int slot = event.getRawSlot();
        if (slot != 11 && slot != 13 && slot != 15) return;
        Player player = (Player) event.getWhoClicked();
        PracticeMode mode = PracticeMode.values()[(slot - 11) / 2];
        if (manager.isPracticing(player)) { PracticeManager.message(player, Messages.PRACTICE_LEAVE_FIRST); return; }
        Bukkit.getScheduler().runTask(BedWars.plugin, () -> {
            if (!player.isOnline() || player.getOpenInventory().getTopInventory().getHolder() != top.getHolder()) return;
            player.closeInventory(); manager.join(player, mode);
        });
    }
    @EventHandler(priority = EventPriority.HIGHEST) public void drag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof PracticeMenuHolder) event.setCancelled(true);
    }
    public void shutdown() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof PracticeMenuHolder) player.closeInventory();
        }
    }
    public static final class PracticeMenuHolder implements InventoryHolder {
        private Inventory inventory;
        @Override public Inventory getInventory() { return inventory; }
    }
}
