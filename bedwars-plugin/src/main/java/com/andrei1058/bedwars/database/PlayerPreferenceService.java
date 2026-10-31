package com.andrei1058.bedwars.database;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.logging.Level;

/** 两项个人偏好共用读写队列，重连加载排在此前的保存之后。 */
public final class PlayerPreferenceService {
    private static PlayerPreferenceService instance;
    private final Map<UUID, Boolean> shopModes = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> spectatorVisibility = new ConcurrentHashMap<>();
    private final ExecutorService database = Executors.newSingleThreadExecutor(task -> new Thread(task, "HMO-PlayerPreferences"));

    public static void init() { instance = new PlayerPreferenceService(); }
    public static PlayerPreferenceService getInstance() { return instance; }

    /** 仅在异步预登录阶段等待，进入菜单前确定玩家偏好。 */
    public void load(UUID player) {
        try {
            database.submit(() -> {
                boolean bulk = false;
                boolean visible = true;
                try {
                    bulk = BedWars.getRemoteDatabase().getShiftBulkBuy(player);
                } catch (SQLException e) {
                    BedWars.plugin.getLogger().log(Level.WARNING, "Cannot load shop preference for " + player, e);
                }
                try {
                    visible = BedWars.getRemoteDatabase().getSpectatorVisibility(player);
                } catch (SQLException e) {
                    BedWars.plugin.getLogger().log(Level.WARNING, "Cannot load spectator preference for " + player, e);
                }
                shopModes.put(player, bulk);
                spectatorVisibility.put(player, visible);
            }).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            BedWars.plugin.getLogger().log(Level.WARNING, "Interrupted while loading player preferences for " + player, e);
        } catch (ExecutionException e) {
            BedWars.plugin.getLogger().log(Level.WARNING, "Cannot load player preferences for " + player, e.getCause());
        }
    }

    public boolean isShiftBulkBuy(UUID player) { return shopModes.getOrDefault(player, false); }
    public boolean isShowingSpectators(UUID player) { return spectatorVisibility.getOrDefault(player, true); }

    public void toggleShiftBulkBuy(Player player) {
        UUID uuid = player.getUniqueId();
        boolean enabled = !isShiftBulkBuy(uuid);
        shopModes.put(uuid, enabled);
        database.execute(() -> {
            try {
                BedWars.getRemoteDatabase().setShiftBulkBuy(uuid, enabled);
            } catch (SQLException e) {
                saveFailed(player, Messages.SHOP_MODE_SAVE_FAILED, e);
            }
        });
    }

    public boolean toggleSpectatorVisibility(Player player) {
        UUID uuid = player.getUniqueId();
        boolean visible = !isShowingSpectators(uuid);
        spectatorVisibility.put(uuid, visible);
        database.execute(() -> {
            try {
                BedWars.getRemoteDatabase().setSpectatorVisibility(uuid, visible);
            } catch (SQLException e) {
                saveFailed(player, Messages.SPECTATOR_VISIBILITY_SAVE_FAILED, e);
            }
        });
        return visible;
    }

    private void saveFailed(Player player, String message, SQLException error) {
        UUID uuid = player.getUniqueId();
        BedWars.plugin.getLogger().log(Level.WARNING, "Cannot save player preference " + message + " for " + uuid, error);
        Bukkit.getScheduler().runTask(BedWars.plugin, () -> {
            if (Bukkit.getPlayer(uuid) == player && player.isOnline())
                player.sendMessage(Language.getMsg(player, message));
        });
    }

    public void forget(UUID player) {
        shopModes.remove(player);
        spectatorVisibility.remove(player);
    }

    public void shutdown() {
        database.shutdown();
        try {
            if (!database.awaitTermination(10, TimeUnit.SECONDS))
                BedWars.plugin.getLogger().warning("Player preference writes have not drained after 10 seconds");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            BedWars.plugin.getLogger().log(Level.WARNING, "Interrupted while saving player preferences", e);
        }
    }
}
