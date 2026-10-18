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

package com.andrei1058.bedwars.arena;

import com.andrei1058.bedwars.BedWars;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import java.util.*;

import static com.andrei1058.bedwars.BedWars.plugin;

/**
 * This is where player stuff are stored so he can have them back after a game
 */
public class PlayerGoods {

    private UUID uuid;
    private int level, foodLevel, heldSlot, fireTicks, totalExperience;
    private double health, healthscale;
    private float exp, saturation, exhaustion, fallDistance;
    private List<PotionEffect> potions = new ArrayList<>();
    private ItemStack[] items, armor, enderchest;
    private ItemStack cursor;
    private Vector velocity;
    private GameMode gamemode;
    private boolean allowFlight, flying, healthScaled;
    private String displayName, tabName;

    public PlayerGoods(Player p, boolean prepare){
        this(p, prepare, false);
    }

    PlayerGoods(Player p, boolean prepare, boolean rejoin) {
        if (hasGoods(p)) {
            plugin.getLogger().severe(p.getName() + " is already having a PlayerGoods vault :|");
            return;
        }
        this.uuid = p.getUniqueId();
        this.level = p.getLevel();
        this.exp = p.getExp();
        this.health = p.getHealth();
        this.healthscale = p.getHealthScale();
        this.foodLevel = p.getFoodLevel();
        playerGoods.put(p.getUniqueId(), this);
        // 独立复制各槽位，避免相同物品互相覆盖或训练物品修改原背包。
        items = copy(p.getInventory().getContents());
        armor = copy(p.getInventory().getArmorContents());
        enderchest = rejoin ? new ItemStack[27] : copy(p.getEnderChest().getContents());
        ItemStack heldCursor = p.getItemOnCursor();
        cursor = heldCursor == null ? null : heldCursor.clone();
        heldSlot = p.getInventory().getHeldItemSlot();
        healthScaled = p.isHealthScaled();
        saturation = p.getSaturation();
        exhaustion = p.getExhaustion();
        fallDistance = p.getFallDistance();
        fireTicks = p.getFireTicks();
        totalExperience = p.getTotalExperience();
        velocity = p.getVelocity();
        for (PotionEffect ef : p.getActivePotionEffects()) {
            potions.add(ef);
            if (prepare) p.removePotionEffect(ef.getType());
        }

        this.gamemode = p.getGameMode();
        this.allowFlight = p.getAllowFlight();
        this.flying = p.isFlying();
        this.tabName = p.getPlayerListName();
        this.displayName = p.getDisplayName();

        /* prepare for arena */
        if (prepare) {
            p.setItemOnCursor(null);
            p.closeInventory();
            p.setExp(0);
            p.setLevel(0);
            p.setHealthScale(20);
            p.setHealth(20);
            p.setFoodLevel(20);
            p.getInventory().clear();
            p.getInventory().setArmorContents(null);
            if (!rejoin) {
                p.getEnderChest().clear();
            }
            p.setGameMode(GameMode.SURVIVAL);
            p.setAllowFlight(false);
            p.setFlying(false);
        }
    }

    /**
     * a list where you can get PlayerGoods by player
     */
    private static HashMap<UUID, PlayerGoods> playerGoods = new HashMap<>();

    /**
     * check if a player has a vault
     */
    static boolean hasGoods(Player p) {
        return playerGoods.containsKey(p.getUniqueId());
    }

    /**
     * get a player vault
     */
    static PlayerGoods getPlayerGoods(Player p) {
        return playerGoods.get(p.getUniqueId());
    }

    /**
     * restore player
     */
    public void restore() {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;
        playerGoods.remove(player.getUniqueId());
        player.setItemOnCursor(null);
        player.closeInventory();


        for (PotionEffect pf : player.getActivePotionEffects()) {
            player.removePotionEffect(pf.getType());
        }
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setTotalExperience(totalExperience);
        player.setLevel(level);
        player.setExp(exp);
        player.setHealthScale(healthscale);
        player.setHealthScaled(healthScaled);
        try {
            player.setHealth(health);
        } catch (Exception e){
            BedWars.plugin.getLogger().severe("Something went wrong when restoring player health: "+health+". Giving default of: 20");
            player.setHealth(20);
        }
        player.setFoodLevel(foodLevel);

        player.getInventory().setContents(items);
        player.getInventory().setArmorContents(armor);
        player.getInventory().setHeldItemSlot(heldSlot);
        for (PotionEffect pe : potions) player.addPotionEffect(pe, true);
        player.getEnderChest().setContents(enderchest);
        player.setSaturation(saturation);
        player.setExhaustion(exhaustion);
        player.setFireTicks(fireTicks);
        player.setFallDistance(fallDistance);
        player.setVelocity(velocity);
        player.setItemOnCursor(cursor);
        player.updateInventory();
        player.setGameMode(gamemode);
        player.setAllowFlight(allowFlight);
        player.setFlying(flying);

        if (!displayName.equals(player.getDisplayName())) {
            player.setDisplayName(displayName);
        }
        if (!tabName.equals(player.getPlayerListName())) {
            player.setPlayerListName(tabName);
        }

        uuid = null;
        items = null;
        potions = null;
        armor = null;
        enderchest = null;
        cursor = null;
        velocity = null;
    }

    private static ItemStack[] copy(ItemStack[] contents) {
        ItemStack[] result = new ItemStack[contents.length];
        for (int slot = 0; slot < contents.length; slot++) {
            ItemStack item = contents[slot];
            result[slot] = item == null ? null : item.clone();
        }
        return result;
    }

}
