package com.andrei1058.bedwars.practice;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.*;

public final class PracticeMenu implements Listener {
    private final PracticeManager manager;
    public PracticeMenu(PracticeManager manager) { this.manager = manager; }
    public void open(Player player) { open(player, null, 0); }
    private void open(Player player, PracticeMode mode, int page) {
        PracticeMenuHolder holder = new PracticeMenuHolder(mode, page);
        holder.inventory = Bukkit.createInventory(holder, mode == null ? 27 : 54,
                PracticeManager.text(player, mode == null ? Messages.PRACTICE_MENU_TITLE : Messages.PRACTICE_TARGET_MENU));
        render(player, holder); player.openInventory(holder.inventory);
    }
    private ItemStack icon(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta(); meta.setDisplayName(name); meta.setLore(lore); item.setItemMeta(meta);
        return item;
    }
    private void render(Player player, PracticeMenuHolder holder) {
        holder.inventory.clear(); holder.targets.clear();
        if (holder.mode == null) {
            int slot = 11;
            for (PracticeMode mode : PracticeMode.values()) {
                List<String> lore = new ArrayList<>();
                String free = String.valueOf(manager.getFreeCount(mode));
                String total = String.valueOf(manager.getTotalCount(mode));
                String players = String.valueOf(manager.getPlayerCount(mode));
                for (String line : Language.getList(player, mode.isShared() ? Messages.PRACTICE_SHARED_MENU_LORE : Messages.PRACTICE_MENU_LORE))
                    lore.add(line.replace("{free}", free).replace("{total}", total).replace("{players}", players));
                holder.inventory.setItem(slot, icon(mode.getMaterial(), PracticeManager.text(player, mode.getMessagePath()), lore)); slot += 2;
            }
        } else {
            holder.inventory.setItem(0, icon(Material.FEATHER, PracticeManager.text(player, Messages.PRACTICE_FREE), Collections.emptyList()));
            Map<String, PracticeTarget> targets = new LinkedHashMap<>();
            PracticeSession session = manager.getSession(player);
            if (session != null && session.getPoint().getMode() == holder.mode)
                for (PracticeTarget target : session.getPoint().getTargets()) targets.put(target.getId(), target);
            for (PracticePoint point : manager.getPoints()) if (point.getMode() == holder.mode && point.isEnabled())
                for (PracticeTarget target : point.getTargets()) targets.putIfAbsent(target.getId(), target);
            int index = 0, first = holder.page * 44;
            for (PracticeTarget target : targets.values()) {
                if (index >= first && index < first + 44) {
                    int slot = index - first + 1;
                    holder.targets.put(slot, target.getId());
                    holder.inventory.setItem(slot, icon(holder.mode.getMaterial(), "§e" + target.getName(), Collections.emptyList()));
                }
                index++;
            }
            holder.inventory.setItem(45, icon(Material.COMPASS, PracticeManager.text(player, Messages.PRACTICE_SETTINGS_ITEM), Collections.emptyList()));
            if (holder.page > 0) holder.inventory.setItem(48, icon(Material.ARROW, "←", Collections.emptyList()));
            if (index > first + 44) holder.inventory.setItem(50, icon(Material.ARROW, "→", Collections.emptyList()));
        }
        PracticeSession session = manager.getSession(player);
        if (session != null) {
            holder.inventory.setItem(holder.mode == null ? 24 : 52, icon(Material.IRON_CHESTPLATE,
                    PracticeManager.text(player, session.isDamageEnabled() ? Messages.PRACTICE_DAMAGE_ON : Messages.PRACTICE_DAMAGE_OFF), Collections.emptyList()));
            holder.inventory.setItem(holder.mode == null ? 26 : 53, icon(Material.BED,
                    PracticeManager.text(player, Messages.PRACTICE_LEAVE_ITEM), Collections.emptyList()));
        }
    }
    public void refresh() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Inventory inventory = player.getOpenInventory().getTopInventory();
            if (inventory.getHolder() instanceof PracticeMenuHolder) render(player, (PracticeMenuHolder) inventory.getHolder());
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void click(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof PracticeMenuHolder)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player) || (event.getClick() != ClickType.LEFT && event.getClick() != ClickType.RIGHT)) return;
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= top.getSize()) return;
        Player player = (Player) event.getWhoClicked();
        PracticeMenuHolder holder = (PracticeMenuHolder) top.getHolder();
        boolean right = event.getClick() == ClickType.RIGHT;
        String target = holder.targets.get(slot);
        Bukkit.getScheduler().runTask(BedWars.plugin, () -> {
            if (!player.isOnline() || player.getOpenInventory().getTopInventory().getHolder() != holder) return;
            PracticeSession session = manager.getSession(player);
            if (slot == (holder.mode == null ? 24 : 52) && session != null) {
                session.toggleDamage();
                if (!session.isDamageEnabled()) { player.setHealth(player.getMaxHealth()); player.setFireTicks(0); }
                render(player, holder);
            } else if (slot == (holder.mode == null ? 26 : 53) && session != null) manager.leave(player);
            else if (holder.mode == null && (slot == 11 || slot == 13 || slot == 15)) {
                PracticeMode mode = PracticeMode.values()[(slot - 11) / 2];
                if (right || mode.isShared()) manager.join(player, mode); else open(player, mode, 0);
            } else if (holder.mode != null) {
                if (slot == 45) open(player);
                else if (slot == 48 && holder.page > 0) open(player, holder.mode, holder.page - 1);
                else if (slot == 50 && top.getItem(50) != null) open(player, holder.mode, holder.page + 1);
                else if (slot == 0 || target != null) manager.join(player, holder.mode, target);
            }
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
        private final PracticeMode mode;
        private final int page;
        private final Map<Integer, String> targets = new HashMap<>();
        private PracticeMenuHolder(PracticeMode mode, int page) { this.mode = mode; this.page = page; }
        @Override public Inventory getInventory() { return inventory; }
    }
}
