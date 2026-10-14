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

package com.andrei1058.bedwars.configuration;


import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.NextEvent;
import com.andrei1058.bedwars.api.configuration.ConfigManager;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

import static com.andrei1058.bedwars.BedWars.plugin;
import static com.andrei1058.bedwars.api.configuration.ConfigPath.*;

public class Sounds {

    private static final ConfigManager sounds = new ConfigManager(plugin, "sounds", plugin.getDataFolder().getPath());

    /**
     * Load sounds configuration
     */
    private Sounds() {
    }

    public static void init() {
        YamlConfiguration yml = sounds.getYml();

        addDefSound("game-end", "AMBIENCE_THUNDER");
        addDefSound("rejoin-denied", "VILLAGER_NO");
        addDefSound("rejoin-allowed", "SLIME_WALK");
        addDefSound("spectate-denied", "VILLAGER_NO");
        addDefSound("spectate-allowed", "SLIME_WALK");
        addDefSound("join-denied", "VILLAGER_NO");
        addDefSound("join-allowed", "SLIME_WALK");
        addDefSound("spectator-gui-click", "SLIME_WALK");
        addDefSound(SOUNDS_COUNTDOWN_TICK, "CHICKEN_EGG_POP");
        addDefSound(SOUNDS_COUNTDOWN_TICK_X + "5", "CHICKEN_EGG_POP");
        addDefSound(SOUNDS_COUNTDOWN_TICK_X + "4", "CHICKEN_EGG_POP");
        addDefSound(SOUNDS_COUNTDOWN_TICK_X + "3", "CHICKEN_EGG_POP");
        addDefSound(SOUNDS_COUNTDOWN_TICK_X + "2", "CHICKEN_EGG_POP");
        addDefSound(SOUNDS_COUNTDOWN_TICK_X + "1", "CHICKEN_EGG_POP");
        addDefSound(SOUND_GAME_START, "SLIME_ATTACK");

        addDefSound(SOUNDS_KILL, "ORB_PICKUP");

        addDefSound(SOUNDS_BED_DESTROY, "ENDERDRAGON_GROWL");
        addDefSound(SOUNDS_BED_DESTROY_OWN, "WITHER_DEATH");
        addDefSound(SOUNDS_INSUFF_MONEY, "VILLAGER_NO");
        addDefSound(SOUNDS_INSUFF_INVENTORY, "VILLAGER_NO");
        addDefSound(SOUNDS_BOUGHT, "VILLAGER_YES");

        addDefSound(NextEvent.BEDS_DESTROY.getSoundPath(), "ENDERDRAGON_GROWL");
        addDefSound(NextEvent.DIAMOND_GENERATOR_TIER_II.getSoundPath(), "LEVEL_UP");
        addDefSound(NextEvent.DIAMOND_GENERATOR_TIER_III.getSoundPath(), "LEVEL_UP");
        addDefSound(NextEvent.EMERALD_GENERATOR_TIER_II.getSoundPath(), "GHAST_MOAN");
        addDefSound(NextEvent.EMERALD_GENERATOR_TIER_III.getSoundPath(), "GHAST_MOAN");
        addDefSound(NextEvent.ENDER_DRAGON.getSoundPath(), "ENDERDRAGON_WINGS");

        addDefSound("player-re-spawn", "SLIME_ATTACK");
        addDefSound("arena-selector-open", "CHICKEN_EGG_POP");
        addDefSound("stats-gui-open", "CHICKEN_EGG_POP");
        addDefSound(SOUNDS_TEAM_SELECTOR_OPEN, "CHEST_OPEN");
        addDefSound(SOUNDS_TEAM_SELECTOR_CLOSE, "CHEST_CLOSE");
        addDefSound("trap-sound", "ENDERMAN_TELEPORT");
        addDefSound("shop-auto-equip", "HORSE_ARMOR");
        addDefSound("egg-bridge-block", "CHICKEN_EGG_POP");
        addDefSound("ender-pearl-landed", "ENDERMAN_TELEPORT");
        addDefSound("pop-up-tower-build", "CHICKEN_EGG_POP");
        yml.options().copyDefaults(true);

        // remove old paths
        yml.set("bought", null);
        yml.set("insufficient-money", null);
        yml.set("player-kill", null);
        yml.set("countdown", null);
        sounds.save();
    }

    private static Sound getSound(String path) {
        try {
            return Sound.valueOf(sounds.getString(path + ".sound"));
        } catch (Exception ex) {
            return Sound.valueOf("AMBIENCE_THUNDER");
        }
    }

    public static void playSound(String path, List<Player> players) {
        if(path.equalsIgnoreCase("none")) return;
        final Sound sound = getSound(path);
        int volume = getSounds().getInt(path + ".volume");
        int pitch = getSounds().getInt(path + ".pitch");
        if (sound != null) {
            players.forEach(p -> p.playSound(p.getLocation(), sound, volume, pitch));
        }
    }

    /**
     * @return true if sound is valid and it was played.
     */
    public static boolean playSound(Sound sound, List<Player> players) {
        if (sound == null) return false;
        players.forEach(p -> p.playSound(p.getLocation(), sound, 1f, 1f));
        return true;
    }

    public static void playSound(String path, Player player) {
        final Sound sound = getSound(path);
        float volume = (float) getSounds().getYml().getDouble(path + ".volume");
        float pitch = (float) getSounds().getYml().getDouble(path + ".pitch");
        if (sound != null) player.playSound(player.getLocation(), sound, volume, pitch);
    }

    public static ConfigManager getSounds() {
        return sounds;
    }

    private static void addDefSound(String path, String value) {
        // convert old paths
        if (getSounds().getYml().get(path) != null && getSounds().getYml().get(path + ".volume") == null) {
            String temp = getSounds().getYml().getString(path);
            getSounds().getYml().set(path, null);
            getSounds().getYml().set(path + ".sound", temp);
        }
        getSounds().getYml().addDefault(path + ".sound", value);
        getSounds().getYml().addDefault(path + ".volume", 1);
        getSounds().getYml().addDefault(path + ".pitch", 1);
    }

    public static  void playsoundArea(String path, Location location, float x, float y){
        final Sound sound = getSound(path);
        if (sound != null) location.getWorld().playSound(location, sound, x, y);
    }
}
