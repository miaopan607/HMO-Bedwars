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

package com.andrei1058.bedwars.listeners;

import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.server.ServerType;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import static com.andrei1058.bedwars.support.version.common.VersionCommon.api;

public class ItemDropPickListener {

    // 1.8 玩家丢弃事件。
    public static class PlayerDrop implements Listener {
        @EventHandler
        public void onDrop(PlayerDropItemEvent e){
            if (manageDrop(e.getPlayer(), e.getItemDrop())) e.setCancelled(true);
        }
    }

    /**
     * @return true to cancel the event.
     */
    private static boolean manageDrop(Entity player, Item item) {
        if (!(player instanceof Player)) return false;
        if (api.getServerType() == ServerType.MULTIARENA) {
            //noinspection ConstantConditions
            if (player.getLocation().getWorld().getName().equalsIgnoreCase(api.getLobbyWorld())) {
                return true;
            }
        }
        IArena a = api.getArenaUtil().getArenaByPlayer((Player) player);
        if (a == null) return false;

        if (!a.isPlayer((Player) player)) {
            return true;
        }

        if (a.getStatus() != GameState.playing) {
            return true;
        } else {
            ItemStack i = item.getItemStack();
            if (i.getType() == Material.COMPASS) {
                return true;
            }
        }

        return a.getRespawnSessions().containsKey(player);
    }
}
