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

import com.andrei1058.bedwars.api.configuration.ConfigManager;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.andrei1058.bedwars.BedWars.plugin;

public class UpgradesConfig extends ConfigManager {

    private static final String INTERVAL_VERSION = "generator-edit-interval-version";
    private static final Pattern GENERATOR_EDIT = Pattern.compile("^(\\s*generator-edit\\s*:\\s*[^,]+,)([+-]?\\d+)(,[+-]?\\d+,[+-]?\\d+(?:,.*)?\\s*)$", Pattern.CASE_INSENSITIVE);

    public UpgradesConfig(String name, String dir) {
        super(plugin, name, dir);
        YamlConfiguration yml = this.getYml();
        List<String> elements = Arrays.asList("upgrade-swords,10", "upgrade-armor,11", "upgrade-miner,12", "upgrade-forge,13",
                "upgrade-heal-pool,14", "upgrade-dragon,15", "category-traps,16", "separator-glass,18,19,20,21,22,23,24,25,26",
                "trap-slot-first,30", "trap-slot-second,31", "trap-slot-third,32");
        yml.addDefault("default-upgrades-settings.menu-content", elements);
        yml.addDefault("default-upgrades-settings.trap-start-price", 1);
        yml.addDefault("default-upgrades-settings.trap-increment-price", 1);
        yml.addDefault("default-upgrades-settings.trap-currency", "diamond");
        yml.addDefault("default-upgrades-settings.trap-queue-limit", 3);

        if (isFirstTime()) {
            yml.addDefault("upgrade-swords.tier-1.cost", 4);
            yml.addDefault("upgrade-swords.tier-1.currency", "diamond");
            addDefaultDisplayItem("upgrade-swords.tier-1", "IRON_SWORD", 0, 1, false);
            //noinspection ArraysAsListWithZeroOrOneArgument
            yml.addDefault("upgrade-swords.tier-1.receive", Arrays.asList("enchant-item: DAMAGE_ALL,1,sword"));

            yml.addDefault("upgrade-armor.tier-1.currency", "diamond");
            yml.addDefault("upgrade-armor.tier-1.cost", 2);
            addDefaultDisplayItem("upgrade-armor.tier-1", "IRON_CHESTPLATE", 0, 1, false);
            yml.addDefault("upgrade-armor.tier-1.receive", Collections.singletonList("enchant-item: PROTECTION_ENVIRONMENTAL,1,armor"));

            yml.addDefault("upgrade-armor.tier-2.currency", "diamond");
            yml.addDefault("upgrade-armor.tier-2.cost", 4);
            addDefaultDisplayItem("upgrade-armor.tier-2", "IRON_CHESTPLATE", 0, 2, false);
            yml.addDefault("upgrade-armor.tier-2.receive", Collections.singletonList("enchant-item: PROTECTION_ENVIRONMENTAL,2,armor"));

            yml.addDefault("upgrade-armor.tier-3.currency", "diamond");
            yml.addDefault("upgrade-armor.tier-3.cost", 8);
            addDefaultDisplayItem("upgrade-armor.tier-3", "IRON_CHESTPLATE", 0, 3, false);
            yml.addDefault("upgrade-armor.tier-3.receive", Collections.singletonList("enchant-item: PROTECTION_ENVIRONMENTAL,3,armor"));

            yml.addDefault("upgrade-armor.tier-4.currency", "diamond");
            yml.addDefault("upgrade-armor.tier-4.cost", 16);
            addDefaultDisplayItem("upgrade-armor.tier-4", "IRON_CHESTPLATE", 0, 4, false);
            yml.addDefault("upgrade-armor.tier-4.receive", Collections.singletonList("enchant-item: PROTECTION_ENVIRONMENTAL,4,armor"));

            yml.addDefault("upgrade-miner.tier-1.currency", "diamond");
            yml.addDefault("upgrade-miner.tier-1.cost", 2);
            addDefaultDisplayItem("upgrade-miner.tier-1", "GOLD_PICKAXE", 0, 1, false);
            yml.addDefault("upgrade-miner.tier-1.receive", Collections.singletonList("player-effect: FAST_DIGGING,0,0,team"));

            yml.addDefault("upgrade-miner.tier-2.currency", "diamond");
            yml.addDefault("upgrade-miner.tier-2.cost", 4);
            addDefaultDisplayItem("upgrade-miner.tier-2", "GOLD_PICKAXE", 0, 2, false);            yml.addDefault("upgrade-miner.tier-2.receive", Collections.singletonList("player-effect: FAST_DIGGING,1,0,team"));

            yml.addDefault("upgrade-forge.tier-1.currency", "diamond");
            yml.addDefault("upgrade-forge.tier-1.cost", 4);
            addDefaultDisplayItem("upgrade-forge.tier-1", "FURNACE", 0, 1, false);
            yml.addDefault("upgrade-forge.tier-1.receive", Arrays.asList("generator-edit: iron,2,8,192", "generator-edit: gold,4,1,24"));

            yml.addDefault("upgrade-forge.tier-2.currency", "diamond");
            yml.addDefault("upgrade-forge.tier-2.cost", 8);
            addDefaultDisplayItem("upgrade-forge.tier-2", "FURNACE", 0, 2, false);
            yml.addDefault("upgrade-forge.tier-2.receive", Arrays.asList("generator-edit: iron,1,5,256", "generator-edit: gold,3,1,32"));

            yml.addDefault("upgrade-forge.tier-3.currency", "diamond");
            yml.addDefault("upgrade-forge.tier-3.cost", 12);
            addDefaultDisplayItem("upgrade-forge.tier-3", "FURNACE", 0, 3, false);
            yml.addDefault("upgrade-forge.tier-3.receive", Arrays.asList("generator-edit: iron,1,5,320", "generator-edit: gold,3,1,48",
                    "generator-edit: emerald,30,1,4"));

            yml.addDefault("upgrade-forge.tier-4.currency", "diamond");
            yml.addDefault("upgrade-forge.tier-4.cost", 16);
            addDefaultDisplayItem("upgrade-forge.tier-4", "FURNACE", 0, 4, false);
            yml.addDefault("upgrade-forge.tier-4.receive", Arrays.asList("generator-edit: iron,1,8,384", "generator-edit: gold,2,1,64",
                    "generator-edit: emerald,20,1,8"));

            yml.addDefault("upgrade-heal-pool.tier-1.currency", "diamond");
            yml.addDefault("upgrade-heal-pool.tier-1.cost", 1);
            addDefaultDisplayItem("upgrade-heal-pool.tier-1", "BEACON", 0, 1, false);
            yml.addDefault("upgrade-heal-pool.tier-1.receive", Collections.singletonList("player-effect: REGENERATION,1,0,base"));

            yml.addDefault("upgrade-dragon.tier-1.currency", "diamond");
            yml.addDefault("upgrade-dragon.tier-1.cost", 5);
            addDefaultDisplayItem("upgrade-dragon.tier-1", "DRAGON_EGG", 0, 1, false);
            yml.addDefault("upgrade-dragon.tier-1.receive", Collections.singletonList("dragon: 2"));

            addDefaultDisplayItem("category-traps", "LEATHER", 0, 1, false);
            yml.addDefault("category-traps.category-content", Arrays.asList("base-trap-1,10", "base-trap-2,11",
                    "base-trap-3,12", "base-trap-4,13", "separator-back,31"));

            yml.addDefault("separator-glass.on-click", "");
            addDefaultDisplayItem("separator-glass", "STAINED_GLASS_PANE", 7, 1, false);

            yml.addDefault("trap-slot-first.trap", 1);
            addDefaultDisplayItem("trap-slot-first", "STAINED_GLASS", 8, 1, false);
            yml.addDefault("trap-slot-second.trap", 2);
            addDefaultDisplayItem("trap-slot-second", "STAINED_GLASS", 8, 2, false);
            yml.addDefault("trap-slot-third.trap", 3);
            addDefaultDisplayItem("trap-slot-third", "STAINED_GLASS", 8, 3, false);

            addDefaultDisplayItem("base-trap-1", "TRIPWIRE_HOOK", 0, 1, false);
            yml.addDefault("base-trap-1.receive", Arrays.asList("player-effect: BLINDNESS,1,5,enemy", "player-effect: SLOW,1,5,enemy"));

            addDefaultDisplayItem("base-trap-2", "FEATHER", 0, 1, false);
            yml.addDefault("base-trap-2.receive", Collections.singletonList("player-effect: SPEED,1,15,base"));

            addDefaultDisplayItem("base-trap-3", "REDSTONE_TORCH_ON", 0, 1, false);
            yml.addDefault("base-trap-3.custom-announce", true);
            yml.addDefault("base-trap-3.receive", Collections.singletonList("remove-effect: INVISIBILITY,enemy"));

            addDefaultDisplayItem("base-trap-4", "IRON_PICKAXE", 0, 1, false);
            yml.addDefault("base-trap-4.receive", Collections.singletonList("player-effect: SLOW_DIGGING,1,15,enemy"));

            //noinspection ArraysAsListWithZeroOrOneArgument
            yml.addDefault("separator-back.on-click.player", Arrays.asList(""));
            //noinspection ArraysAsListWithZeroOrOneArgument
            yml.addDefault("separator-back.on-click.console", Arrays.asList(""));
            addDefaultDisplayItem("separator-back", "ARROW", 0, 1, false);
        }
        yml.options().copyDefaults(true);
        if (isFirstTime()) {
            yml.set(INTERVAL_VERSION, 1);
            save();
        } else if (migrateIntervals(yml)) {
            Path file = new File(dir, name + ".yml").toPath();
            Path backup = file.resolveSibling(file.getFileName() + ".pre-interval.bak");
            try {
                if (!Files.exists(backup)) Files.copy(file, backup);
                Path temporary = Files.createTempFile(file.getParent(), name + "-interval-", ".tmp");
                try {
                    Files.write(temporary, yml.saveToString().getBytes(StandardCharsets.UTF_8));
                    Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
                } finally {
                    Files.deleteIfExists(temporary);
                }
                plugin.getLogger().info("Migrated generator-edit delays to intervals; backup: " + backup);
            } catch (IOException ex) {
                throw new IllegalStateException("Could not migrate " + file + "; existing configuration is unchanged", ex);
            }
        } else {
            save();
        }
    }

    static boolean migrateIntervals(YamlConfiguration yml) {
        if (yml.getInt(INTERVAL_VERSION) >= 1) return false;
        for (String upgrade : yml.getKeys(false)) {
            if (!upgrade.startsWith("upgrade-") || !yml.isConfigurationSection(upgrade)) continue;
            for (String tier : yml.getConfigurationSection(upgrade).getKeys(false)) {
                String path = upgrade + "." + tier + ".receive";
                if (!yml.isList(path)) continue;
                List<String> actions = yml.getStringList(path);
                List<String> migrated = new ArrayList<>(actions.size());
                for (String action : actions) {
                    Matcher matcher = GENERATOR_EDIT.matcher(action);
                    if (!matcher.matches()) {
                        migrated.add(action);
                        continue;
                    }
                    int oldDelay = Integer.parseInt(matcher.group(2));
                    if (oldDelay < 0 || oldDelay == Integer.MAX_VALUE) throw new IllegalArgumentException("Generator delay cannot be migrated at " + path);
                    migrated.add(matcher.group(1) + (oldDelay + 1) + matcher.group(3));
                }
                if (!actions.equals(migrated)) yml.set(path, migrated);
            }
        }
        yml.set(INTERVAL_VERSION, 1);
        return true;
    }

    private void addDefaultDisplayItem(String path, String material, int data, int amount, boolean enchanted) {
        getYml().addDefault(path + ".display-item.material", material);
        getYml().addDefault(path + ".display-item.data", data);
        getYml().addDefault(path + ".display-item.amount", amount);
        getYml().addDefault(path + ".display-item.enchanted", enchanted);
    }

}
