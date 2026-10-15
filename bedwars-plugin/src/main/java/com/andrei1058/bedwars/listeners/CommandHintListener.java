package com.andrei1058.bedwars.listeners;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.arena.Arena;
import com.andrei1058.bedwars.arena.SetupSession;
import com.andrei1058.bedwars.commands.bedwars.MainCommand;
import com.andrei1058.bedwars.configuration.Permissions;
import com.andrei1058.bedwars.listeners.chat.ChatFormatting;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CommandHintListener implements Listener {
    private final Map<UUID, BukkitTask> pendingHints = new HashMap<>();
    private final Set<UUID> pendingLoginHints = new HashSet<>();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        pendingLoginHints.add(event.getPlayer().getUniqueId());
        scheduleHints(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        scheduleHints(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        BukkitTask task = pendingHints.remove(playerId);
        if (task != null) task.cancel();
        pendingLoginHints.remove(playerId);
    }

    private void scheduleHints(Player player) {
        UUID playerId = player.getUniqueId();
        BukkitTask previous = pendingHints.remove(playerId);
        if (previous != null) previous.cancel();

        // 自动传送可以替换任务，但不能丢掉尚未发送的进服提示。
        BukkitTask task = Bukkit.getScheduler().runTaskLater(BedWars.plugin, () -> {
            pendingHints.remove(playerId);
            boolean sendLogin = pendingLoginHints.remove(playerId);
            if (!player.isOnline()) return;
            if (sendLogin) sendLoginHints(player);
            sendArenaHints(player);
            // 自定义欢迎语放在操作提示之后发送。
            if (sendLogin) {
                for (String line : Language.getList(player, Messages.JOIN_WELCOME)) {
                    player.sendMessage(line.replace("{player}", player.getName()));
                }
            }
        }, 20L);
        pendingHints.put(playerId, task);
    }

    private void sendLoginHints(Player player) {
        if (ChatFormatting.canSendGlobalChat(player)) {
            player.sendMessage(Language.getMsg(player, Messages.HINT_CHAT_GLOBAL));
        }
        if (player.hasPermission(Permissions.PERMISSION_REJOIN)) {
            player.sendMessage(Language.getMsg(player, Messages.HINT_REJOIN));
        }
    }

    private void sendArenaHints(Player player) {
        if (SetupSession.isInSetupSession(player.getUniqueId())) return;
        IArena arena = Arena.getArenaByPlayer(player);
        if (arena == null) return;

        if (arena.isSpectator(player)) {
            player.sendMessage(Language.getMsg(player, Messages.HINT_SPECTATOR_MODE)
                    .replace("{command}", MainCommand.getInstance().getName()));
            return;
        }
        if (!arena.isPlayer(player)) return;

        GameState status = arena.getStatus();
        if (status == GameState.waiting || status == GameState.starting) {
            if (BedWars.config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_ENABLE_READY_CHECK)) {
                player.sendMessage(Language.getMsg(player, Messages.HINT_READY_ITEM));
            }
        } else if (status != GameState.playing) {
            return;
        }

        if (BedWars.config.getBoolean(ConfigPath.GENERAL_CHAT_FORMATTING)
                && ChatFormatting.canSendArenaShout(player)) {
            player.sendMessage(Language.getMsg(player, Messages.HINT_CHAT_SHOUT));
        }
    }
}
