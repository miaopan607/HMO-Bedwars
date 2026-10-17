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

package com.andrei1058.bedwars.listeners.chat;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.api.server.ServerType;
import com.andrei1058.bedwars.arena.Arena;
import com.andrei1058.bedwars.commands.shout.ShoutCommand;
import com.andrei1058.bedwars.configuration.Permissions;
import com.andrei1058.bedwars.support.papi.SupportPAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.andrei1058.bedwars.BedWars.*;
import static com.andrei1058.bedwars.api.language.Language.getMsg;
import static com.andrei1058.bedwars.api.language.Language.getPlayerLanguage;

public class ChatFormatting implements Listener {
    private final Map<UUID, Long> globalCooldowns = new HashMap<>();
    private long nextCooldownCleanup;

    // 提示和实际聊天发送共用权限规则，显式拒绝优先于通配权限。
    public static boolean canSendArenaShout(Player player) {
        return !(player.isPermissionSet(Permissions.PERMISSION_SHOUT_COMMAND)
                && !player.hasPermission(Permissions.PERMISSION_SHOUT_COMMAND))
                && Permissions.hasPermission(player, Permissions.PERMISSION_SHOUT_COMMAND, Permissions.PERMISSION_ALL);
    }

    public static boolean canSendGlobalChat(Player player) {
        return player.hasPermission(Permissions.PERMISSION_GLOBAL_CHAT)
                || (!player.isPermissionSet(Permissions.PERMISSION_GLOBAL_CHAT)
                && (!config.getBoolean(ConfigPath.GLOBAL_CHAT_REQUIRE_PERMISSION)
                || player.hasPermission(Permissions.PERMISSION_ALL)));
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        if (e == null) return;
        Player p = e.getPlayer();

        // Global chat must be routed before shared-world, spectator and arena isolation.
        if (e.getMessage().startsWith("@")) {
            handleGlobalChat(e, p);
            return;
        }
        if (!config.getBoolean(ConfigPath.GENERAL_CHAT_FORMATTING)) return;

        // in shared mode we don't want messages from outside the arena to be seen in game
        if (getServerType() == ServerType.SHARED && Arena.getArenaByPlayer(p) == null) {
            e.getRecipients().removeIf(pl -> Arena.getArenaByPlayer(pl) != null);
            return;
        }

        // handle chat color. we would need to work on permission inheritance
        if (Permissions.hasPermission(p, Permissions.PERMISSION_CHAT_COLOR, Permissions.PERMISSION_VIP, Permissions.PERMISSION_ALL)) {
            e.setMessage(ChatColor.translateAlternateColorCodes('&', e.getMessage()));
        }

        Language language = getPlayerLanguage(p);

        // handle lobby world for multi arena
        if (getServerType() == ServerType.MULTIARENA && p.getWorld().getName().equalsIgnoreCase(BedWars.getLobbyWorld())) {
            setRecipients(e, p.getWorld().getPlayers());
        }

        // handle arena chat
        if (Arena.getArenaByPlayer(p) != null) {
            IArena a = Arena.getArenaByPlayer(p);

            // 结算期普通聊天面向整局玩家和旁观者。
            if (a.getStatus() == GameState.restarting) {
                setRecipients(e, a.getPlayers(), a.getSpectators());
                e.setFormat(parsePHolders(language.m(Messages.FORMATTING_CHAT_SHOUT), p, a.getTeam(p)));
                return;
            }

            // 赛前普通聊天面向整场玩家和旁观者。
            if (a.getStatus() == GameState.waiting || a.getStatus() == GameState.starting) {
                setRecipients(e, a.getPlayers(), a.getSpectators());
                e.setFormat(parsePHolders(language.m(a.isSpectator(p)
                        ? Messages.FORMATTING_CHAT_SPECTATOR
                        : Messages.FORMATTING_CHAT_WAITING), p, null));
                return;
            }

            // spectator chat
            if (a.isSpectator(p)) {
                setRecipients(e, a.getSpectators());
                e.setFormat(parsePHolders(language.m(Messages.FORMATTING_CHAT_SPECTATOR), p, null));
                return;
            }

            ITeam team = a.getTeam(p);
            String msg = e.getMessage();

            // shout format
            if (isShouting(msg, language)) {
                // Explicit shout denial takes precedence over wildcard permission.
                if (!canSendArenaShout(p)) {
                    e.setCancelled(true);
                    p.sendMessage(Language.getMsg(p, Messages.COMMAND_NOT_FOUND_OR_INSUFF_PERMS));
                    return;
                }
                if (ShoutCommand.isShoutCooldown(p)) {
                    e.setCancelled(true);
                    p.sendMessage(language.m(Messages.COMMAND_COOLDOWN)
                            .replace("{seconds}", String.valueOf(Math.round(ShoutCommand.getShoutCooldown(p))))
                    );
                    return;
                }
                ShoutCommand.updateShout(p);
                setRecipients(e, a.getPlayers(), a.getSpectators());
                msg = clearShout(msg, language);
                if (msg.isEmpty()) {
                    e.setCancelled(true);
                    return;
                }
                e.setMessage(msg);
                e.setFormat(parsePHolders(language.m(Messages.FORMATTING_CHAT_SHOUT), p, team));
                return;
            }

            // player team chat
            if (a.getMaxInTeam() == 1) {
                setRecipients(e, a.getPlayers(), a.getSpectators());
                // 单人模式普通聊天面向整局，使用公屏标签。
                e.setFormat(parsePHolders(language.m(Messages.FORMATTING_CHAT_SHOUT), p, team));
            } else {
                setRecipients(e, team.getMembers());
                e.setFormat(parsePHolders(language.m(Messages.FORMATTING_CHAT_TEAM), p, team));
            }
            return;
        }

        // multi arena lobby chat
        e.setFormat(parsePHolders(language.m(Messages.FORMATTING_CHAT_LOBBY), p, null));
    }

    private void handleGlobalChat(AsyncPlayerChatEvent event, Player player) {
        String message = event.getMessage().substring(1).trim();
        if (message.isEmpty()) {
            event.setCancelled(true);
            player.sendMessage(getMsg(player, Messages.CHAT_GLOBAL_USAGE));
            return;
        }
        if (!canSendGlobalChat(player)) {
            event.setCancelled(true);
            player.sendMessage(getMsg(player, Messages.COMMAND_NOT_FOUND_OR_INSUFF_PERMS));
            return;
        }
        long remaining = claimGlobalCooldown(player);
        if (remaining > 0) {
            event.setCancelled(true);
            player.sendMessage(getMsg(player, Messages.COMMAND_COOLDOWN)
                    .replace("{seconds}", String.valueOf((remaining + 999) / 1000)));
            return;
        }
        if (Permissions.hasPermission(player, Permissions.PERMISSION_CHAT_COLOR, Permissions.PERMISSION_VIP, Permissions.PERMISSION_ALL)) {
            message = ChatColor.translateAlternateColorCodes('&', message);
        }
        event.setMessage(message);
        event.setFormat(parsePHolders(getMsg(player, Messages.FORMATTING_CHAT_SERVER), player, null));
        // Keep the event uncancelled so downstream moderation can still block delivery.
        event.getRecipients().clear();
        event.getRecipients().addAll(Bukkit.getOnlinePlayers());
    }

    private synchronized long claimGlobalCooldown(Player player) {
        long now = System.currentTimeMillis();
        if (now >= nextCooldownCleanup) {
            globalCooldowns.values().removeIf(deadline -> deadline <= now);
            nextCooldownCleanup = now + 60_000;
        }
        if (player.hasPermission(Permissions.PERMISSION_GLOBAL_CHAT_BYPASS)) return 0;
        int seconds = config.getInt(ConfigPath.GLOBAL_CHAT_COOLDOWN);
        if (seconds <= 0) return 0;
        UUID playerId = player.getUniqueId();
        Long deadline = globalCooldowns.get(playerId);
        if (deadline != null && deadline > now) return deadline - now;
        globalCooldowns.put(playerId, now + seconds * 1000L);
        return 0;
    }

    private static String parsePHolders(String content, Player player, @Nullable ITeam team) {
        boolean showTeam = team != null && content.contains("{team}");
        String playerName = showTeam
                ? team.getColor().chat() + player.getDisplayName()
                : player.getDisplayName();
        content = content
                .replace("{playername}", playerName)
                .replace("{player}", playerName);
        if (team != null) {
            String teamFormat = getMsg(player, Messages.FORMAT_PAPI_PLAYER_TEAM_TEAM)
                    .replace("{TeamColor}", team.getColor().chat() + "")
                    .replace("{TeamName}", team.getDisplayName(Language.getPlayerLanguage(player)).toUpperCase());
            content = content.replace("{team}", teamFormat);
        } else {
            content = content.replace("{team}", "");
        }
        // 正文尚未插入，只整理格式中可选等级和身份留下的多余空格。
        return SupportPAPI.getSupportPAPI().replace(player, content).replaceAll(" {2,}", " ").trim().replace("{message}", "%2$s");
    }

    private static boolean isShouting(String msg, Language lang) {
        return msg.startsWith("!") || msg.startsWith("！") || msg.startsWith("shout") ||
                msg.startsWith("SHOUT") || msg.startsWith(lang.m(Messages.MEANING_SHOUT));
    }

    private static String clearShout(String msg, Language lang) {
        if (msg.startsWith("!") || msg.startsWith("！")) msg = msg.substring(1);
        if (msg.startsWith("SHOUT")) msg = msg.replaceFirst("SHOUT", "");
        if (msg.startsWith("shout")) msg = msg.replaceFirst("shout", "");
        if (msg.startsWith(lang.m(Messages.MEANING_SHOUT))) {
            msg = msg.replaceFirst(lang.m(Messages.MEANING_SHOUT), "");
        }
        return msg.trim();
    }

    @SafeVarargs
    public static void setRecipients(AsyncPlayerChatEvent event, List<Player>... target) {
        if (!config.getBoolean(ConfigPath.GENERAL_CHAT_GLOBAL)) {
            event.getRecipients().clear();
            for (List<Player> list : target) {
                event.getRecipients().addAll(list);
            }
        }
    }
}
