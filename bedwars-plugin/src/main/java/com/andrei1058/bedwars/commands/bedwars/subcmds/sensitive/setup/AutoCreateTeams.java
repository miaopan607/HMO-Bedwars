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

package com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.setup;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.team.TeamColor;
import com.andrei1058.bedwars.api.command.ParentCommand;
import com.andrei1058.bedwars.api.command.SubCommand;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.api.server.SetupType;
import com.andrei1058.bedwars.arena.Misc;
import com.andrei1058.bedwars.arena.SetupSession;
import com.andrei1058.bedwars.configuration.Permissions;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AutoCreateTeams extends SubCommand {

    public AutoCreateTeams(ParentCommand parent, String name) {
        super(parent, name);
        setArenaSetupCommand(true);
        setPermission(Permissions.PERMISSION_SETUP_ARENA);
    }

    private static HashMap<Player, Long> timeOut = new HashMap<>();
    private static HashMap<Player, List<Byte>> teamsFoundOld = new HashMap<>();

    @SuppressWarnings("deprecation")
    @Override
    public boolean execute(String[] args, CommandSender s) {
        if (s instanceof ConsoleCommandSender) return false;
        Player p = (Player) s;
        SetupSession ss = SetupSession.getSession(p.getUniqueId());
        if (ss == null) {
            s.sendMessage(Language.getMsg(p, Messages.COMMAND_SETUP_NOT_IN_SESSION));
            return true;
        }
        if (ss.getSetupType() == SetupType.ASSISTED) {
            if (timeOut.containsKey(p) && timeOut.get(p) >= System.currentTimeMillis() && teamsFoundOld.containsKey(p)) {
                for (Byte tf : teamsFoundOld.get(p)) {
                    Bukkit.dispatchCommand(s, BedWars.mainCmd + " createTeam " + TeamColor.enName(tf) + " " + TeamColor.enName(tf));
                }
                if (ss.getConfig().getYml().get("waiting.Pos1") == null) {
                    s.sendMessage("");
                    s.sendMessage(Language.getMsg(p, Messages.COMMAND_SETUP_AUTO_TEAMS_WAITING_HEADER));
                    s.sendMessage(Language.getMsg(p, Messages.COMMAND_SETUP_AUTO_TEAMS_WAITING_DESCRIPTION));
                    s.sendMessage(Language.getMsg(p, Messages.COMMAND_SETUP_AUTO_TEAMS_WAITING_SELECTION));
                    p.spigot().sendMessage(Misc.msgHoverClick(Language.getMsg(p, Messages.COMMAND_SETUP_WAITING_POS_OPTION).replace("{command}", BedWars.mainCmd).replace("{position}", "1"), Language.getMsg(p, Messages.COMMAND_SETUP_WAITING_POS_OPTION_HOVER).replace("{position}", "1"), "/" + getParent().getName() + " waitingPos 1", ClickEvent.Action.RUN_COMMAND));
                    p.spigot().sendMessage(Misc.msgHoverClick(Language.getMsg(p, Messages.COMMAND_SETUP_WAITING_POS_OPTION).replace("{command}", BedWars.mainCmd).replace("{position}", "2"), Language.getMsg(p, Messages.COMMAND_SETUP_WAITING_POS_OPTION_HOVER).replace("{position}", "2"), "/" + getParent().getName() + " waitingPos 2", ClickEvent.Action.RUN_COMMAND));
                    s.sendMessage("");
                    s.sendMessage(Language.getMsg(p, Messages.COMMAND_SETUP_AUTO_TEAMS_WAITING_OPTIONAL).replace("{command}", BedWars.mainCmd));
                }
                return true;
            }
            List<Byte> found = new ArrayList<>();
            World w = p.getWorld();
            if (ss.getConfig().getYml().get("Team") == null) {
                p.sendMessage(Language.getMsg(p, Messages.COMMAND_SETUP_AUTO_TEAMS_SEARCH));
                for (int x = -200; x < 200; x++) {
                    for (int y = 50; y < 130; y++) {
                        for (int z = -200; z < 200; z++) {
                            Block b = w.getBlockAt(x, y, z);
                            if (b.getType() != Material.WOOL) continue;
                            byte color = b.getData();
                            if (found.contains(color)) continue;
                            String teamName = TeamColor.enName(color);
                            if (!teamName.isEmpty() && ss.getConfig().getYml().get("Team." + teamName) == null) {
                                found.add(color);
                            }
                        }
                    }
                }
            }
            if (found.isEmpty()) {
                p.sendMessage(Language.getMsg(p, Messages.COMMAND_SETUP_AUTO_TEAMS_NONE).replace("{command}", BedWars.mainCmd));
            } else {
                if (timeOut.containsKey(p)) {
                    p.sendMessage(Language.getMsg(p, Messages.COMMAND_SETUP_AUTO_TEAMS_TIMEOUT));
                    timeOut.remove(p);
                    return true;
                }
                timeOut.put(p, System.currentTimeMillis() + 16000);
                teamsFoundOld.put(p, found);
                p.sendMessage(Language.getMsg(p, Messages.COMMAND_SETUP_AUTO_TEAMS_FOUND_HEADER));
                for (Byte tf : found) {
                    String name = TeamColor.enName(tf);
                    p.sendMessage(Language.getMsg(p, Messages.COMMAND_SETUP_AUTO_TEAMS_FOUND_ENTRY).replace("{team}", TeamColor.getChatColor(name) + name.replace('_', ' ')));
                }
                p.spigot().sendMessage(Misc.msgHoverClick(Language.getMsg(p, Messages.COMMAND_SETUP_AUTO_TEAMS_CONFIRM), Language.getMsg(p, Messages.COMMAND_SETUP_AUTO_TEAMS_CONFIRM_HOVER), "/" + getParent().getName() + " " + getSubCommandName(), ClickEvent.Action.RUN_COMMAND));
            }
        } else return false;
        return true;
    }

    @Override
    public List<String> getTabComplete() {
        return null;
    }

    @Override
    public boolean canSee(CommandSender s, com.andrei1058.bedwars.api.BedWars api) {
        if (s instanceof ConsoleCommandSender) return false;

        Player p = (Player) s;
        if (!SetupSession.isInSetupSession(p.getUniqueId())) return false;

        return hasPermission(s);
    }
}
