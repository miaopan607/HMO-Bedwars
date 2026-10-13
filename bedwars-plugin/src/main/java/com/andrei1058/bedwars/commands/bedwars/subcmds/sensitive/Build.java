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

package com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive;

import com.andrei1058.bedwars.api.BedWars;
import com.andrei1058.bedwars.api.command.ParentCommand;
import com.andrei1058.bedwars.api.command.SubCommand;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.arena.Arena;
import com.andrei1058.bedwars.arena.Misc;
import com.andrei1058.bedwars.arena.SetupSession;
import com.andrei1058.bedwars.commands.bedwars.MainCommand;
import com.andrei1058.bedwars.configuration.Permissions;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static com.andrei1058.bedwars.listeners.BreakPlace.*;

public class Build extends SubCommand {

    public Build(ParentCommand parent, String name) {
        super(parent, name);
        setPriority(9);
        showInList(true);
        setPermission(Permissions.PERMISSION_BUILD);
        setDisplayInfo(Misc.msgHoverClick(Language.getMsg(null, Messages.COMMAND_BUILD_DISPLAY).replace("{command}", getParent().getName()).replace("{subcommand}", getSubCommandName()), Language.getMsg(null, Messages.COMMAND_BUILD_DISPLAY_HOVER),
                "/" + getParent().getName() + " "+getSubCommandName(), ClickEvent.Action.RUN_COMMAND));
    }

    @Override
    public boolean execute(String[] args, CommandSender s) {
        if (s instanceof ConsoleCommandSender) return false;
        Player p = (Player) s;
        if (com.andrei1058.bedwars.BedWars.getPracticeManager() != null
                && com.andrei1058.bedwars.BedWars.getPracticeManager().isPracticing(p)) {
            com.andrei1058.bedwars.practice.PracticeManager.message(p, Messages.PRACTICE_LEAVE_FIRST);
            return true;
        }
        if (!MainCommand.isLobbySet(p)) return true;
        if (isBuildSession(p)) {
            p.sendMessage(Language.getMsg(p, Messages.COMMAND_BUILD_DISABLED));
            removeBuildSession(p);
        } else {
            p.sendMessage(Language.getMsg(p, Messages.COMMAND_BUILD_ENABLED));
            addBuildSession(p);
        }
        return true;
    }

    @Override
    public List<String> getTabComplete() {
        return null;
    }

    @Override
    public boolean canSee(CommandSender s, BedWars api) {
        if (s instanceof ConsoleCommandSender) return false;

        Player p = (Player) s;
        if (Arena.isInArena(p)) return false;

        if (SetupSession.isInSetupSession(p.getUniqueId())) return false;
        return hasPermission(s);
    }
}
