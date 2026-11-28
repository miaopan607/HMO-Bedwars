package com.andrei1058.bedwars.shop.hotbar;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.arena.Arena;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

/**
 * 监听对局中拾取更高级剑或弓的事件，自动替换当前较低级武器到配置的快捷栏位置。
 */
public final class HotbarListener implements Listener {

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onWeaponPickup(PlayerPickupItemEvent event) {
        if (event.getItem() == null || event.getItem().isDead()) return;
        Player player = event.getPlayer();
        IArena arena = Arena.getArenaByPlayer(player);
        if (arena == null || arena.getStatus() != GameState.playing
                || !arena.isPlayer(player) || arena.isSpectator(player) || arena.isReSpawning(player)) {
            return;
        }

        ItemStack picked = event.getItem().getItemStack();
        if (picked == null || picked.getType() == Material.AIR) return;

        boolean handled = false;
        if (BedWars.nms.isSword(picked)) {
            handled = HotbarService.handleSwordPickup(player, picked, arena.getTeam(player));
        } else if (BedWars.nms.isBow(picked)) {
            handled = HotbarService.handleBowPickup(player, picked, arena.getTeam(player));
        }

        if (handled) {
            event.setCancelled(true);
            if (picked.getAmount() > 1) {
                ItemStack leftover = picked.clone();
                leftover.setAmount(picked.getAmount() - 1);
                event.getItem().setItemStack(leftover);
            } else {
                event.getItem().remove();
            }
            player.playSound(player.getLocation(), Sound.ITEM_PICKUP, 0.2f, 1.8f);
            player.updateInventory();
        }
    }
}
