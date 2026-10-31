package com.andrei1058.bedwars.arena.spectator;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.database.PlayerPreferenceService;
import com.andrei1058.bedwars.arena.Arena;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** 主线程应用观战视图，个人偏好由统一存取服务管理。 */
public final class SpectatorVisibilityService {
    private static SpectatorVisibilityService instance;

    public static void init() { instance = new SpectatorVisibilityService(); }
    public static SpectatorVisibilityService getInstance() { return instance; }

    public boolean isShowingSpectators(UUID player) {
        return PlayerPreferenceService.getInstance().isShowingSpectators(player);
    }

    public boolean toggle(Player player) {
        boolean visible = PlayerPreferenceService.getInstance().toggleSpectatorVisibility(player);
        IArena arena = Arena.getArenaByPlayer(player);
        if (arena != null)
            refreshArena(arena);
        return visible;
    }

    public void refreshArena(IArena arena) {
        List<Player> spectators = new ArrayList<>(arena.getSpectators());
        List<Player> visibleSpectators = new ArrayList<>();
        List<Player> attachedSpectators = new ArrayList<>();
        for (Player spectator : spectators) {
            if (spectator.getSpectatorTarget() == null)
                visibleSpectators.add(spectator);
            else
                attachedSpectators.add(spectator);
        }
        for (Player viewer : spectators) {
            BedWars.nms.updateSpectatorView(viewer, visibleSpectators, isShowingSpectators(viewer.getUniqueId()));
            for (Player attachedSpectator : attachedSpectators)
                if (attachedSpectator != viewer)
                    BedWars.nms.spigotHidePlayer(attachedSpectator, viewer);
        }
    }

    public void removeSpectator(Player player, IArena arena) {
        BedWars.nms.clearSpectatorView(player);
        refreshArena(arena);
    }

    public void shutdown() {
        BedWars.nms.clearSpectatorViews();
    }
}
