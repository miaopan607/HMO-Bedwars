package com.andrei1058.bedwars.practice;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;
import java.util.ArrayList;
import java.util.List;

/** 原状态只捕获一次，模式切换和重试不会覆盖。 */
final class PracticeSnapshot {
    final YamlConfiguration yaml;

    PracticeSnapshot(YamlConfiguration yaml) { this.yaml = yaml; }

    static PracticeSnapshot capture(Player player) {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("contents", copy(player.getInventory().getContents()));
        yaml.set("armor", copy(player.getInventory().getArmorContents()));
        ItemStack cursor = player.getItemOnCursor();
        yaml.set("cursor", cursor == null ? null : cursor.clone());
        Location location = player.getLocation();
        yaml.set("world", location.getWorld().getName());
        yaml.set("x", location.getX()); yaml.set("y", location.getY()); yaml.set("z", location.getZ());
        yaml.set("yaw", location.getYaw()); yaml.set("pitch", location.getPitch());
        yaml.set("held-slot", player.getInventory().getHeldItemSlot());
        yaml.set("food", player.getFoodLevel()); yaml.set("health", player.getHealth());
        yaml.set("health-scale", player.getHealthScale()); yaml.set("health-scaled", player.isHealthScaled());
        yaml.set("saturation", player.getSaturation()); yaml.set("exhaustion", player.getExhaustion());
        yaml.set("fire-ticks", player.getFireTicks()); yaml.set("fall-distance", player.getFallDistance());
        yaml.set("game-mode", player.getGameMode().name());
        yaml.set("allow-flight", player.getAllowFlight()); yaml.set("flying", player.isFlying());
        yaml.set("velocity", player.getVelocity());
        yaml.set("effects", new ArrayList<>(player.getActivePotionEffects()));
        yaml.set("level", player.getLevel()); yaml.set("exp", player.getExp());
        yaml.set("total-exp", player.getTotalExperience());
        return new PracticeSnapshot(yaml);
    }

    Location location() {
        return new Location(Bukkit.getWorld(yaml.getString("world")), yaml.getDouble("x"), yaml.getDouble("y"),
                yaml.getDouble("z"), (float) yaml.getDouble("yaw"), (float) yaml.getDouble("pitch"));
    }

    void restore(Player player) {
        player.setItemOnCursor(null); player.closeInventory();
        for (PotionEffect effect : player.getActivePotionEffects()) player.removePotionEffect(effect.getType());
        for (Object effect : yaml.getList("effects")) player.addPotionEffect((PotionEffect) effect, true);
        player.getInventory().setContents(items("contents", 36));
        player.getInventory().setArmorContents(items("armor", 4));
        player.getInventory().setHeldItemSlot(yaml.getInt("held-slot"));
        player.setGameMode(GameMode.valueOf(yaml.getString("game-mode")));
        player.setAllowFlight(yaml.getBoolean("allow-flight")); player.setFlying(yaml.getBoolean("flying"));
        player.setHealthScale(yaml.getDouble("health-scale")); player.setHealthScaled(yaml.getBoolean("health-scaled"));
        player.setHealth(Math.min(yaml.getDouble("health"), player.getMaxHealth()));
        player.setFoodLevel(yaml.getInt("food")); player.setSaturation((float) yaml.getDouble("saturation"));
        player.setExhaustion((float) yaml.getDouble("exhaustion"));
        player.setFireTicks(yaml.getInt("fire-ticks")); player.setFallDistance((float) yaml.getDouble("fall-distance"));
        player.setVelocity((Vector) yaml.get("velocity"));
        player.setTotalExperience(yaml.getInt("total-exp"));
        player.setLevel(yaml.getInt("level")); player.setExp((float) yaml.getDouble("exp"));
        ItemStack cursor = yaml.getItemStack("cursor");
        player.setItemOnCursor(cursor == null ? null : cursor.clone()); player.updateInventory();
    }

    private ItemStack[] items(String key, int size) {
        List<?> values = yaml.getList(key);
        ItemStack[] result = new ItemStack[size];
        for (int index = 0; index < size; index++) {
            ItemStack value = (ItemStack) values.get(index);
            result[index] = value == null ? null : value.clone();
        }
        return result;
    }

    private static List<ItemStack> copy(ItemStack[] items) {
        List<ItemStack> result = new ArrayList<>(items.length);
        for (ItemStack item : items) result.add(item == null ? null : item.clone());
        return result;
    }
}
