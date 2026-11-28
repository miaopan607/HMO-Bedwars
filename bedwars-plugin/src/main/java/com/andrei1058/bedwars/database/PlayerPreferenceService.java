package com.andrei1058.bedwars.database;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.shop.hotbar.HotbarCategory;
import com.andrei1058.bedwars.shop.hotbar.HotbarLayout;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.logging.Level;

/** 个人偏好共用读写队列，重连加载排在此前的保存之后。 */
public final class PlayerPreferenceService {
    private static PlayerPreferenceService instance;
    private final Map<UUID, Boolean> shopModes = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> spectatorVisibility = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> quickDeposit = new ConcurrentHashMap<>();
    private final Map<UUID, HotbarLayout> hotbarLayouts = new ConcurrentHashMap<>();
    private final ExecutorService database = Executors.newSingleThreadExecutor(task -> new Thread(task, "HMO-PlayerPreferences"));

    public static void init() { instance = new PlayerPreferenceService(); }
    public static PlayerPreferenceService getInstance() { return instance; }

    /** 仅在异步预登录阶段等待，进入菜单前确定玩家偏好。 */
    public void load(UUID player) {
        try {
            database.submit(() -> {
                boolean bulk = false;
                boolean visible = true;
                boolean deposit = false;
                HotbarLayout hotbar = HotbarLayout.EMPTY;
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
                try {
                    deposit = BedWars.getRemoteDatabase().getQuickDeposit(player);
                } catch (SQLException e) {
                    BedWars.plugin.getLogger().log(Level.WARNING, "Cannot load chest quick deposit preference for " + player, e);
                }
                try {
                    hotbar = HotbarLayout.deserialize(BedWars.getRemoteDatabase().getHotbarLayout(player));
                } catch (SQLException e) {
                    BedWars.plugin.getLogger().log(Level.WARNING, "Cannot load hotbar layout for " + player, e);
                }
                shopModes.put(player, bulk);
                spectatorVisibility.put(player, visible);
                quickDeposit.put(player, deposit);
                hotbarLayouts.put(player, hotbar);
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
    public boolean isQuickDeposit(UUID player) { return quickDeposit.getOrDefault(player, false); }
    public HotbarLayout getHotbarLayout(UUID player) { return hotbarLayouts.getOrDefault(player, HotbarLayout.EMPTY); }

    public HotbarLayout setHotbarSlot(Player player, int slot, HotbarCategory category) {
        HotbarLayout updated = getHotbarLayout(player.getUniqueId()).withSlot(slot, category);
        saveHotbarLayout(player, updated);
        return updated;
    }

    public HotbarLayout clearHotbarCategory(Player player, HotbarCategory category) {
        HotbarLayout updated = getHotbarLayout(player.getUniqueId()).withoutCategory(category);
        saveHotbarLayout(player, updated);
        return updated;
    }

    public HotbarLayout resetHotbarLayout(Player player) {
        saveHotbarLayout(player, HotbarLayout.EMPTY);
        return HotbarLayout.EMPTY;
    }

    public void saveHotbarLayout(Player player, HotbarLayout layout) {
        UUID uuid = player.getUniqueId();
        HotbarLayout safe = layout == null ? HotbarLayout.EMPTY : layout;
        HotbarLayout previous = hotbarLayouts.put(uuid, safe);
        if (Objects.equals(previous, safe)) return;
        String serialized = safe.serialize();
        database.execute(() -> {
            try {
                BedWars.getRemoteDatabase().setHotbarLayout(uuid, serialized);
            } catch (SQLException e) {
                saveFailed(player, Messages.HOTBAR_MANAGER_SAVE_FAILED, e);
            }
        });
    }

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

    public void toggleQuickDeposit(Player player) {
        UUID uuid = player.getUniqueId();
        boolean enabled = !isQuickDeposit(uuid);
        quickDeposit.put(uuid, enabled);
        database.execute(() -> {
            try {
                BedWars.getRemoteDatabase().setQuickDeposit(uuid, enabled);
            } catch (SQLException e) {
                saveFailed(player, Messages.CHEST_QUICK_DEPOSIT_SAVE_FAILED, e);
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
        quickDeposit.remove(player);
        hotbarLayouts.remove(player);
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
