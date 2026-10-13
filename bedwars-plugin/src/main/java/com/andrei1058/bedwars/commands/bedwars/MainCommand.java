/*
 * BedWars1058 - A bed wars mini-game.
 * Copyright (C) 2021 Andrei Dascฤ�lu
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

package com.andrei1058.bedwars.commands.bedwars;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.command.ParentCommand;
import com.andrei1058.bedwars.api.command.SubCommand;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.api.server.ServerType;
import com.andrei1058.bedwars.arena.SetupSession;
import com.andrei1058.bedwars.commands.bedwars.subcmds.regular.*;
import com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.Level;
import com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.Reload;
import com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.setup.*;
import com.andrei1058.bedwars.support.citizens.JoinNPC;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.andrei1058.bedwars.BedWars.*;
import static com.andrei1058.bedwars.api.language.Language.getMsg;

public class MainCommand extends BukkitCommand implements ParentCommand {

    /* SubCommands ArenaList */
    private static List<SubCommand> subCommandList = new ArrayList<>();
    /* MainCommand instance*/
    private static MainCommand instance;
    /* Dot char */
    @SuppressWarnings("WeakerAccess")
    public static char dot = 254;

    public MainCommand(String name) {
        super(name);
        setAliases(Arrays.asList("bedwars", "bedwars1058"));
        instance = this;
        new CmdJoin(this, "join");
        new CmdReady(this, "ready");
        new CmdLeave(this, "leave");
        if (BedWars.getPracticeManager() != null) new CmdPractice(this, "practice");
        new CmdLang(this, "lang");
        new CmdTeleporter(this, "teleporter");
        new CmdSpecMode(this, "spec");
        if (getServerType() != ServerType.BUNGEE) {
            new CmdGUI(this, "gui");
        }
        new CmdStats(this, "stats");
        new CmdForceStart(this, "forceStart");
        new com.andrei1058.bedwars.commands.bedwars.subcmds.regular.CmdForceEndGame(this, "forceEndGame");
        new CmdStart(this, "start");
        if (BedWars.getServerType() != ServerType.BUNGEE) {
            new com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.SetLobby(this, "setLobby"); //priority 1
        }
        new com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.SetupArena(this, "setupArena"); //priority 2
        new com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.ArenaList(this, "arenaList"); //priority 3
        new com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.DelArena(this, "delArena"); //priority 4
        new com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.EnableArena(this, "enableArena"); //priority 5
        new com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.DisableArena(this, "disableArena"); //priority 6
        new com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.CloneArena(this, "cloneArena"); //priority 7
        new com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.ArenaGroup(this, "arenaGroup"); //priority 8
        new com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.Build(this, "build"); //priority 9
        new Level(this, "level");
        new Reload(this, "reload"); //priority 11
        new com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.Config(this, "config");
        new CmdList(this, "cmds"); //priority 20
        new CmdEcho(this, "echo"); //priority 21
        new CmdEchoTo(this, "echoto"); //priority 22
        new CmdEchoAll(this, "echoall"); //priority 23

        /* Arena setup commands (in world) */
        new AutoCreateTeams(this, "autoCreateTeams");
        new SetWaitingSpawn(this, "setWaitingSpawn");
        new SetSpectatorPos(this, "setSpectSpawn");
        new CreateTeam(this, "createTeam");
        new com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.setup.WaitingPos(this, "waitingPos");
        new com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.setup.RemoveTeam(this, "removeTeam");
        new com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.setup.SetMaxInTeam(this, "setMaxInTeam");
        new com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.setup.SetBuildHeight(this, "setMaxBuildHeight");
        new com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.setup.SetSpawn(this, "setSpawn");
        new com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.setup.SetBed(this, "setBed");
        new SetShop(this, "setShop");
        new com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.setup.SetUpgrade(this, "setUpgrade");
        new com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.setup.AddGenerator(this, "addGenerator");
        new RemoveGenerator(this, "removeGenerator");
        new com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.setup.SetType(this, "setType");
        new com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.setup.Save(this, "save");
        if (JoinNPC.isCitizensSupport() && BedWars.getServerType() != ServerType.BUNGEE) {
            new com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.NPC(this, "npc");
        }
        new com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.CmdToggleReady(this, "toggleReady");
        new CmdTpStaff(this, "tp");
        new SetKillDropsLoc(this, "setKillDrops");
    }

    @Override
    public boolean execute(CommandSender s, String st, String[] args) {

        if (args.length == 0) {
            /* Set op commands*/
            if ((s.isOp() || s.hasPermission(BedWars.mainCmd + ".*"))) {
                if (s instanceof Player) {
                    if (SetupSession.isInSetupSession(((Player) s).getUniqueId())) {
                        Bukkit.dispatchCommand(s, getName() + " cmds");
                    } else {
                        s.sendMessage("");
                        s.sendMessage(getMsg((Player) s, Messages.COMMAND_ADMIN_HEADER).replace("{dot}", String.valueOf(dot)).replace("{plugin}", plugin.getDescription().getName()).replace("{version}", plugin.getDescription().getVersion()));
                        s.sendMessage("");
                        sendSubCommands((Player) s);
                    }
                } else {
                    s.sendMessage(getMsg(null, Messages.COMMAND_CONSOLE_SAFEMODE));
                }
            } else {
                if (s instanceof ConsoleCommandSender) {
                    s.sendMessage(getMsg(null, Messages.COMMAND_CONSOLE_NO_COMMANDS));
                    return true;
                }
                /* Send player commands */
                Bukkit.dispatchCommand(s, mainCmd + " cmds");
            }
            return true;
        }

        boolean commandFound = false;
        for (SubCommand sb : getSubCommands()) {
            if (sb.getSubCommandName().equalsIgnoreCase(args[0])) {
                if (sb.hasPermission(s)) {
                    commandFound = sb.execute(Arrays.copyOfRange(args, 1, args.length), s);
                }
            }
        }

        if (!commandFound) {
            if (s instanceof Player) {
                s.sendMessage(getMsg((Player) s, Messages.COMMAND_NOT_FOUND_OR_INSUFF_PERMS));
            } else {
                s.sendMessage(Language.getDefaultLanguage().m(Messages.COMMAND_NOT_FOUND_OR_INSUFF_PERMS));
            }
        }
        return true;
    }

    public static boolean isArenaGroup(String var) {
        if (config.getYml().get("arenaGroups") != null) {
            return config.getYml().getStringList("arenaGroups").contains(var);
        }
        return var.equalsIgnoreCase("default");
    }

    public static TextComponent createTC(String text, String suggest, String shot_text) {
        TextComponent tx = new TextComponent(text);
        tx.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggest));
        tx.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(shot_text).create()));
        return tx;
    }

    @Override
    public void addSubCommand(SubCommand subCommand) {
        subCommandList.add(subCommand);
    }

    @Override
    public void sendSubCommands(Player p) {
        List<SubCommand> commands = new ArrayList<>(getSubCommands());
        for (int priority = 0; priority <= 20; priority++) {
            for (SubCommand sb : commands) {
                if (sb.getPriority() != priority || !sb.isShow() || !sb.canSee(p, BedWars.getAPI())) continue;
                String displayKey = displayKey(sb.getSubCommandName(), false);
                if (displayKey == null) {
                    if (sb.getDisplayInfo() != null) p.spigot().sendMessage(sb.getDisplayInfo());
                    continue;
                }
                String hoverKey = displayKey(sb.getSubCommandName(), true);
                String text = getMsg(p, displayKey).replace("{command}", getName()).replace("{subcommand}", sb.getSubCommandName());
                String hover = hoverKey == null ? null : getMsg(p, hoverKey);
                if ("setLobby".equalsIgnoreCase(sb.getSubCommandName())) {
                    text = text.replace("{status}", getMsg(p, config.getLobbyWorldName().isEmpty() ? Messages.COMMAND_SET_LOBBY_NOT_SET : Messages.COMMAND_SET_LOBBY_SET));
                } else if ("arenaList".equalsIgnoreCase(sb.getSubCommandName())) {
                    int count = com.andrei1058.bedwars.arena.Arena.getArenas().size();
                    text = text.replace("{countColor}", count == 0 ? "§c" : "§a").replace("{count}", String.valueOf(count));
                }
                if (hover != null) {
                    if (sb instanceof CmdForceEndGame) {
                        hover = hover.replace("{permission}", com.andrei1058.bedwars.configuration.Permissions.PERMISSION_FORCE_END_GAME);
                    } else if (sb instanceof CmdForceStart) {
                        hover = hover.replace("{permission}", com.andrei1058.bedwars.configuration.Permissions.PERMISSION_FORCESTART);
                    } else if (sb instanceof CmdStart) {
                        hover = hover.replace("{permission}", com.andrei1058.bedwars.configuration.Permissions.PERMISSION_START);
                    }
                    hover = hover.replace("{command}", getName());
                }
                TextComponent display = hover == null ? new TextComponent(text) : createTC(text, "/" + getName() + " " + sb.getSubCommandName(), hover);
                if (sb.getDisplayInfo() != null && sb.getDisplayInfo().getClickEvent() != null) {
                    display.setClickEvent(sb.getDisplayInfo().getClickEvent());
                }
                p.spigot().sendMessage(display);
            }
        }
    }

    private static String displayKey(String command, boolean hover) {
        switch (command) {
            case "practice": return hover ? Messages.PRACTICE_COMMAND_DISPLAY_HOVER : Messages.PRACTICE_COMMAND_DISPLAY;
            case "setLobby": return hover ? Messages.COMMAND_SET_LOBBY_DISPLAY_HOVER : Messages.COMMAND_SET_LOBBY_DISPLAY;
            case "setupArena": return hover ? Messages.COMMAND_SETUP_ARENA_DISPLAY_HOVER : Messages.COMMAND_SETUP_ARENA_DISPLAY;
            case "arenaList": return hover ? Messages.COMMAND_ARENA_LIST_DISPLAY_HOVER : Messages.COMMAND_ARENA_LIST_DISPLAY;
            case "delArena": return hover ? Messages.COMMAND_DEL_ARENA_DISPLAY_HOVER : Messages.COMMAND_DEL_ARENA_DISPLAY;
            case "enableArena": return hover ? Messages.COMMAND_ENABLE_ARENA_DISPLAY_HOVER : Messages.COMMAND_ENABLE_ARENA_DISPLAY;
            case "disableArena": return hover ? Messages.COMMAND_DISABLE_ARENA_DISPLAY_HOVER : Messages.COMMAND_DISABLE_ARENA_DISPLAY;
            case "cloneArena": return hover ? Messages.COMMAND_CLONE_ARENA_DISPLAY_HOVER : Messages.COMMAND_CLONE_ARENA_DISPLAY;
            case "arenaGroup": return hover ? Messages.COMMAND_ARENA_GROUP_DISPLAY_HOVER : Messages.COMMAND_ARENA_GROUP_DISPLAY;
            case "build": return hover ? Messages.COMMAND_BUILD_DISPLAY_HOVER : Messages.COMMAND_BUILD_DISPLAY;
            case "level": return hover ? Messages.COMMAND_LEVEL_DISPLAY_HOVER : Messages.COMMAND_LEVEL_DISPLAY;
            case "npc": return hover ? Messages.COMMAND_NPC_DISPLAY_HOVER : Messages.COMMAND_NPC_DISPLAY;
            case "reload": return hover ? Messages.COMMAND_RELOAD_DISPLAY_HOVER : Messages.COMMAND_RELOAD_DISPLAY;
            case "config": return hover ? null : Messages.CONFIG_SET_DISPLAY;
            case "cmds": return hover ? Messages.COMMAND_LIST_DISPLAY_HOVER : Messages.COMMAND_LIST_DISPLAY;
            case "forceStart": return hover ? Messages.COMMAND_FORCE_START_DISPLAY_HOVER : Messages.COMMAND_FORCE_START_DISPLAY;
            case "start": return hover ? Messages.COMMAND_START_DISPLAY_HOVER : Messages.COMMAND_START_DISPLAY;
            case "forceEndGame": return hover ? Messages.COMMAND_FORCE_END_GAME_DISPLAY_HOVER : Messages.COMMAND_FORCE_END_GAME_DISPLAY;
            case "ready": return hover ? null : Messages.COMMAND_READY_DISPLAY;
            case "echo": return hover ? Messages.COMMAND_ECHO_HOVER : Messages.COMMAND_ECHO_DISPLAY;
            case "echoto": return hover ? Messages.COMMAND_ECHOTO_HOVER : Messages.COMMAND_ECHOTO_DISPLAY;
            case "echoall": return hover ? Messages.COMMAND_ECHOALL_HOVER : Messages.COMMAND_ECHOALL_DISPLAY;
            case "gui": return hover ? Messages.COMMAND_GUI_DISPLAY_HOVER : Messages.COMMAND_GUI_DISPLAY;
            case "join": return hover ? Messages.COMMAND_JOIN_DISPLAY_HOVER : Messages.COMMAND_JOIN_DISPLAY;
            case "lang": return hover ? Messages.COMMAND_LANG_DISPLAY_HOVER : Messages.COMMAND_LANG_DISPLAY;
            case "leave": return hover ? Messages.COMMAND_LEAVE_DISPLAY_HOVER : Messages.COMMAND_LEAVE_DISPLAY;
            case "stats": return hover ? Messages.COMMAND_STATS_DISPLAY_HOVER : Messages.COMMAND_STATS_DISPLAY;
            case "spec": return hover ? Messages.COMMAND_SPEC_MODE_DISPLAY_HOVER : Messages.COMMAND_SPEC_MODE_DISPLAY;
        }
        return null;
    }

    public List<String> tabComplete(CommandSender s, String alias, String[] args, Location location) throws IllegalArgumentException {
        if (args.length >= 2 && args[0].equalsIgnoreCase("practice")
                && getSubCommand("practice") instanceof CmdPractice) {
            return ((CmdPractice) getSubCommand("practice")).tabComplete(s, args);
        }
        if (args.length == 1) {
            List<String> sub = new ArrayList<>();
            for (SubCommand sb : getSubCommands()) {
                if (sb.canSee(s, BedWars.getAPI())) sub.add(sb.getSubCommandName());
            }
            return sub;
        } else if (args.length == 2) {
            if (hasSubCommand(args[0])) {
                if (getSubCommand(args[0]).canSee(s, BedWars.getAPI()))
                    return getSubCommand(args[0]).getTabComplete();
            }
        }
        return null;
    }


    @Override
    public List<SubCommand> getSubCommands() {
        return subCommandList;
    }

    /**
     * Get instance
     */
    public static MainCommand getInstance() {
        return instance;
    }

    /**
     * Check if lobby location is set, else send a error message to the player
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isLobbySet(Player p) {
        if (BedWars.getServerType() == ServerType.BUNGEE) return true;
        if (config.getLobbyWorldName().isEmpty()) {
            if (p != null) {
                p.sendMessage(getMsg(p, Messages.COMMAND_LOBBY_NOT_SET));
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean hasSubCommand(String name) {
        for (SubCommand sc : getSubCommands()) {
            if (sc.getSubCommandName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get sub-command by name
     */
    @SuppressWarnings("WeakerAccess")
    public SubCommand getSubCommand(String name) {
        for (SubCommand sc : getSubCommands()) {
            if (sc.getSubCommandName().equalsIgnoreCase(name)) {
                return sc;
            }
        }
        return null;
    }

    /**
     * Get a dot symbol
     */
    public static char getDot() {
        return dot;
    }
}