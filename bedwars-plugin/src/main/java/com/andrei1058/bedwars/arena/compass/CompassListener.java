package com.andrei1058.bedwars.arena.compass;

import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent;
import com.andrei1058.bedwars.api.events.player.PlayerFirstSpawnEvent;
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent;
import com.andrei1058.bedwars.api.events.player.PlayerLeaveArenaEvent;
import com.andrei1058.bedwars.api.events.player.PlayerReSpawnEvent;
import com.andrei1058.bedwars.api.events.server.ArenaDisableEvent;
import com.andrei1058.bedwars.arena.Arena;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/** 接入指南针物品交互和竞技场生命周期。 */
public final class CompassListener implements Listener {

    @EventHandler
    public void onFirstSpawn(PlayerFirstSpawnEvent event) {
        CompassManager.restoreCompass(event.getArena(), event.getPlayer());
    }

    @EventHandler
    public void onRespawn(PlayerReSpawnEvent event) {
        CompassManager.restoreCompass(event.getArena(), event.getPlayer());
    }

    @EventHandler
    public void onKill(PlayerKillEvent event) {
        CompassManager.clearState(event.getVictim());
    }

    @EventHandler
    public void onLeave(PlayerLeaveArenaEvent event) {
        CompassManager.clearState(event.getPlayer());
        event.getPlayer().closeInventory();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        CompassManager.clearState(event.getPlayer());
    }

    @EventHandler
    public void onGameStateChange(GameStateChangeEvent event) {
        if (event.getNewState() != GameState.playing) {
            CompassManager.clearArena(event.getArena());
        }
    }

    @EventHandler
    public void onArenaDisable(ArenaDisableEvent event) {
        IArena arena = Arena.getArenaByName(event.getArenaName());
        if (arena != null) CompassManager.clearArena(arena);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        IArena arena = Arena.getArenaByPlayer(player);
        if (!CompassManager.isActiveSupportedPlayer(arena, player)
                || player.getGameMode() == GameMode.SPECTATOR) return;
        if (!CompassManager.isCompass(event.getItem())) return;
        event.setCancelled(true);
        CompassManager.openMainMenu(player);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        CompassManager.handleInventoryClick(event);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        CompassManager.handleInventoryDrag(event);
    }

}
