/*
 * BedWars1058 - A bed wars mini-game.
 * Copyright (C) 2021 Andrei Dascălu
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Contact e-mail: andrew.dascalu@gmail.com
 */

package com.andrei1058.bedwars.upgrades.listeners;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.arena.team.TeamEnchant;
import com.andrei1058.bedwars.api.upgrades.MenuContent;
import com.andrei1058.bedwars.arena.Arena;
import com.andrei1058.bedwars.upgrades.UpgradesManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class InventoryListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getWhoClicked() instanceof Player)) return;
        IArena a = Arena.getArenaByPlayer((Player) e.getWhoClicked());
        if (a == null) return;
        if (a.isSpectator((Player) e.getWhoClicked())) return;
        if (!UpgradesManager.isWatchingUpgrades(e.getWhoClicked().getUniqueId())) return;
        e.setCancelled(true);
        if (e.getCurrentItem() == null) return;
        if (e.getCurrentItem().getType() == Material.AIR) return;

        MenuContent mc = UpgradesManager.getMenuContent(e.getCurrentItem());
        if (mc == null) return;
        mc.onClick((Player) e.getWhoClicked(), e.getClick(), a.getTeam((Player) e.getWhoClicked()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryTransfer(InventoryClickEvent event) {
        if (event.getAction() == InventoryAction.NOTHING) return;
        if (event.getWhoClicked() instanceof Player) {
            scheduleSwordEnchantments((Player) event.getWhoClicked());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            scheduleSwordEnchantments((Player) event.getWhoClicked());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPickup(PlayerPickupItemEvent event) {
        ItemStack item = event.getItem().getItemStack();
        if (BedWars.nms.isSword(item) || BedWars.nms.isAxe(item)) {
            scheduleSwordEnchantments(event.getPlayer());
        }
    }

    @EventHandler
    public void onUpgradesClose(InventoryCloseEvent e) {
        UpgradesManager.removeWatchingUpgrades(e.getPlayer().getUniqueId());
        if (e.getPlayer() instanceof Player) {
            scheduleSwordEnchantments((Player) e.getPlayer());
        }
    }

    private void scheduleSwordEnchantments(Player player) {
        IArena arena = Arena.getArenaByPlayer(player);
        if (arena == null || arena.getStatus() != GameState.playing || !arena.isPlayer(player)
                || arena.isSpectator(player) || arena.getRespawnSessions().containsKey(player)) return;
        ITeam team = arena.getTeam(player);
        if (team == null || team.getSwordsEnchantments().isEmpty()) return;

        // 等待物品转移完成，再同步背包中的队伍武器附魔。
        Bukkit.getScheduler().runTask(BedWars.plugin, () -> {
            if (!player.isOnline() || Arena.getArenaByPlayer(player) != arena
                    || arena.getStatus() != GameState.playing || !arena.isPlayer(player)
                    || arena.isSpectator(player) || arena.getRespawnSessions().containsKey(player)
                    || arena.getTeam(player) != team) return;

            boolean changed = false;
            for (ItemStack item : player.getInventory().getContents()) {
                if (item == null || item.getType() == Material.AIR) continue;
                if (!BedWars.nms.isSword(item) && !BedWars.nms.isAxe(item)) continue;
                ItemMeta meta = null;
                for (TeamEnchant enchant : team.getSwordsEnchantments()) {
                    int level = meta == null ? item.getEnchantmentLevel(enchant.getEnchantment())
                            : meta.getEnchantLevel(enchant.getEnchantment());
                    if (level >= enchant.getAmplifier()) continue;
                    if (meta == null) meta = item.getItemMeta();
                    meta.addEnchant(enchant.getEnchantment(), enchant.getAmplifier(), true);
                }
                if (meta != null) {
                    item.setItemMeta(meta);
                    changed = true;
                }
            }
            if (changed) player.updateInventory();
        });
    }
}
