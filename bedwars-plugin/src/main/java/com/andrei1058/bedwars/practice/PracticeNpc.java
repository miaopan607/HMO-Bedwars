package com.andrei1058.bedwars.practice;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.arena.Arena;
import com.andrei1058.bedwars.arena.SetupSession;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class PracticeNpc implements Listener {
    private final PracticeManager manager;
    private final Set<UUID> retired = new HashSet<>();
    private Entity entity;
    public PracticeNpc(PracticeManager manager) { this.manager = manager; }
    public boolean isNpc(Entity candidate) { return entity != null && candidate.getUniqueId().equals(entity.getUniqueId()); }
    public void start() {
        UUID removed = entity == null ? null : entity.getUniqueId();
        shutdown();
        try {
            String encoded = manager.getConfig().getYml().getString(PracticeConfig.NPC_LOCATION);
            if (encoded == null) return;
            String name = encoded.split(",", -1)[5];
            World world = Bukkit.getWorld(name);
            if (world == null && new File(Bukkit.getWorldContainer(), name + "/level.dat").isFile()) world = Bukkit.createWorld(new WorldCreator(name));
            if (world == null) throw new IllegalStateException("Lobby world unavailable: " + name);
            Location location = manager.getConfig().getConfigLoc(PracticeConfig.NPC_LOCATION);
            UUID previous = manager.getConfig().getNpcUuid();
            if (previous != null && !previous.equals(removed)) retired.add(previous);
            location.getChunk().load();
            if (!retired.isEmpty()) {
                for (Entity candidate : world.getEntities()) if (retired.remove(candidate.getUniqueId())) candidate.remove();
            }
            entity = BedWars.nms.spawnPracticeNpc(location, Language.getMsg(null, Messages.PRACTICE_NPC_NAME));
            YamlConfiguration candidate = manager.getConfig().copyConfiguration();
            candidate.set(PracticeConfig.NPC_UUID, entity.getUniqueId().toString());
            manager.getConfig().commit(candidate);
        } catch (Exception ex) {
            shutdown(); BedWars.plugin.getLogger().warning("Practice NPC unavailable: " + ex.getMessage());
        }
    }
    public boolean set(Player player) {
        if (!player.getWorld().getName().equals(BedWars.config.getLobbyWorldName()) || Arena.isInArena(player)
                || SetupSession.isInSetupSession(player.getUniqueId()) || manager.isPracticing(player)) {
            PracticeManager.message(player, Messages.PRACTICE_INVALID_POINT, null, "NPC must be placed in the lobby"); return false;
        }
        try {
            YamlConfiguration candidate = manager.getConfig().copyConfiguration();
            candidate.set(PracticeConfig.NPC_LOCATION, manager.getConfig().stringLocationConfigFormat(player.getLocation()));
            manager.getConfig().commit(candidate); start();
            if (entity == null) { PracticeManager.message(player, Messages.PRACTICE_SAVE_FAILED); return false; }
            PracticeManager.message(player, Messages.PRACTICE_ADMIN_SUCCESS); return true;
        } catch (Exception ex) { PracticeManager.message(player, Messages.PRACTICE_SAVE_FAILED, null, ex.getMessage()); return false; }
    }
    public boolean remove(CommandSender sender) {
        try {
            UUID previous = manager.getConfig().getNpcUuid();
            YamlConfiguration candidate = manager.getConfig().copyConfiguration();
            candidate.set("npc", null); manager.getConfig().commit(candidate);
            if (previous != null) retired.add(previous);
            shutdown(); PracticeManager.message(sender, Messages.PRACTICE_ADMIN_SUCCESS); return true;
        } catch (Exception ex) { PracticeManager.message(sender, Messages.PRACTICE_SAVE_FAILED, null, ex.getMessage()); return false; }
    }
    public void shutdown() {
        if (entity != null) {
            if (entity.isValid()) retired.remove(entity.getUniqueId());
            else retired.add(entity.getUniqueId());
            entity.remove();
            entity = null;
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void interact(PlayerInteractEntityEvent event) {
        if (!isNpc(event.getRightClicked())) return;
        event.setCancelled(true); manager.openMenu(event.getPlayer());
    }
    @EventHandler(priority = EventPriority.HIGHEST) public void damage(EntityDamageEvent event) { if (isNpc(event.getEntity())) event.setCancelled(true); }
    @EventHandler public void chunk(ChunkLoadEvent event) {
        if (retired.isEmpty()) return;
        for (Entity candidate : event.getChunk().getEntities()) if (retired.remove(candidate.getUniqueId())) candidate.remove();
    }
}
