package com.andrei1058.bedwars.arena.spectator;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.logging.Level;

/** 主线程立即应用视图；读写偏好共用队列，保持切换与重连的顺序。 */
public final class SpectatorVisibilityService {
    private static SpectatorVisibilityService instance;
    private final Map<UUID, Boolean> preferences = new ConcurrentHashMap<>();
    private final ExecutorService database = Executors.newSingleThreadExecutor(task -> new Thread(task, "HMO-SpectatorPreferences"));

    public static void init() { instance = new SpectatorVisibilityService(); }
    public static SpectatorVisibilityService getInstance() { return instance; }

    public void loadPreference(UUID player) {
        try {
            boolean visible = database.submit(() -> BedWars.getRemoteDatabase().getSpectatorVisibility(player)).get();
            preferences.put(player, visible);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            preferences.put(player, true);
            BedWars.plugin.getLogger().log(Level.WARNING, "Cannot load spectator preference for " + player, e);
        } catch (ExecutionException e) {
            preferences.put(player, true);
            BedWars.plugin.getLogger().log(Level.WARNING, "Cannot load spectator preference for " + player, e.getCause());
        }
    }

    public boolean isShowingSpectators(UUID player) { return preferences.getOrDefault(player, true); }

    public boolean toggle(Player player) {
        UUID uuid = player.getUniqueId();
        boolean visible = !isShowingSpectators(uuid);
        preferences.put(uuid, visible);
        IArena arena = Arena.getArenaByPlayer(player);
        if (arena != null)
            BedWars.nms.updateSpectatorView(player, new ArrayList<>(arena.getSpectators()), visible);
        database.execute(() -> {
            try {
                BedWars.getRemoteDatabase().setSpectatorVisibility(uuid, visible);
            } catch (java.sql.SQLException e) {
                BedWars.plugin.getLogger().log(Level.WARNING, "Cannot save spectator preference for " + uuid, e);
                Bukkit.getScheduler().runTask(BedWars.plugin, () -> {
                    if (Bukkit.getPlayer(uuid) == player && player.isOnline())
                        player.sendMessage(Language.getMsg(player, Messages.SPECTATOR_VISIBILITY_SAVE_FAILED));
                });
            }
        });
        return visible;
    }

    public void refreshArena(IArena arena) {
        List<Player> spectators = new ArrayList<>(arena.getSpectators());
        for (Player viewer : spectators)
            BedWars.nms.updateSpectatorView(viewer, spectators, isShowingSpectators(viewer.getUniqueId()));
    }

    public void removeSpectator(Player player, IArena arena) {
        BedWars.nms.clearSpectatorView(player);
        refreshArena(arena);
    }

    public void forget(UUID player) { preferences.remove(player); }

    public void shutdown() {
        BedWars.nms.clearSpectatorViews();
        database.shutdown();
        try {
            if (!database.awaitTermination(10, TimeUnit.SECONDS))
                BedWars.plugin.getLogger().warning("Spectator preference writes have not drained after 10 seconds");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            BedWars.plugin.getLogger().log(Level.WARNING, "Interrupted while saving spectator preferences", e);
        }
    }
}
