package com.andrei1058.bedwars.shop.quickbuy;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/** 数据库读取留在异步线程，偏好列表只在主线程应用。 */
public class QuickBuyTask extends BukkitRunnable {
    private final UUID uuid;
    private final PlayerQuickBuyCache cache;
    private final Map<Integer, String> defaults = new HashMap<>();

    public QuickBuyTask(UUID uuid) {
        this.uuid = uuid;
        this.cache = PlayerQuickBuyCache.getQuickBuyCache(uuid);
        ConfigurationSection section = BedWars.shop.getYml().getConfigurationSection(ConfigPath.SHOP_QUICK_DEFAULTS_PATH);
        if (section != null) {
            for (String key : section.getKeys(false)) {
                String path = section.getString(key + ".path");
                String slot = section.getString(key + ".slot");
                if (path == null || slot == null) continue;
                try {
                    defaults.put(Integer.parseInt(slot), path);
                } catch (NumberFormatException ex) {
                    BedWars.debug(slot + " must be an integer!");
                }
            }
        }
        runTaskAsynchronously(BedWars.plugin);
    }

    @Override
    public void run() {
        try {
            boolean existing = BedWars.getRemoteDatabase().hasQuickBuy(uuid);
            Map<Integer, String> slots = existing
                    ? BedWars.getRemoteDatabase().getQuickBuySlots(uuid, PlayerQuickBuyCache.quickSlots)
                    : defaults;
            if (slots == null) throw new IllegalStateException("Unable to load quick buy slots");
            Bukkit.getScheduler().runTask(BedWars.plugin, () -> {
                if (PlayerQuickBuyCache.getQuickBuyCache(uuid) != cache) return;
                for (Map.Entry<Integer, String> entry : slots.entrySet()) {
                    if (entry.getValue() == null || entry.getValue().trim().isEmpty()) continue;
                    QuickBuyElement element = new QuickBuyElement(entry.getValue(), entry.getKey());
                    if (!element.isLoaded()) continue;
                    if (existing) cache.addQuickElement(element);
                    else cache.setElement(entry.getKey(), element.getCategoryContent());
                }
                cache.finishLoading(false);
            });
        } catch (Exception ex) {
            BedWars.plugin.getLogger().log(Level.SEVERE, "Unable to load quick buy for " + uuid, ex);
            Bukkit.getScheduler().runTask(BedWars.plugin, () -> {
                if (PlayerQuickBuyCache.getQuickBuyCache(uuid) == cache) cache.finishLoading(true);
            });
        }
    }
}
