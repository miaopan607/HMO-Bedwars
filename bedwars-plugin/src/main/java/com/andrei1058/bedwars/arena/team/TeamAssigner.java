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

package com.andrei1058.bedwars.arena.team;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.arena.team.ITeamAssigner;
import com.andrei1058.bedwars.api.events.gameplay.TeamAssignEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.andrei1058.bedwars.arena.Arena;

import java.util.*;

public class TeamAssigner implements ITeamAssigner {

    public void assignTeams(IArena arena) {
        List<ITeam> teams = new ArrayList<>(arena.getTeams());
        List<Player> waiting = new ArrayList<>();
        Set<UUID> processed = new HashSet<>();
        for (Player player : new ArrayList<>(arena.getPlayers())) {
            if (arena.getTeam(player) != null || arena instanceof Arena && ((Arena) arena).getSelectedTeam(player) != null) {
                processed.add(player.getUniqueId());
            } else {
                waiting.add(player);
            }
        }
        Collections.shuffle(teams);
        Collections.shuffle(waiting);
        int[] fixed = new int[teams.size()];
        for (int i = 0; i < fixed.length; i++) fixed[i] = teams.get(i).getMembers().size();
        int[] targets = TeamAllocation.targetSizes(fixed, waiting.size(), arena.getMaxInTeam());
        Map<UUID, Player> available = new HashMap<>();
        for (Player player : waiting) available.put(player.getUniqueId(), player);
        List<List<Player>> groups = new ArrayList<>();
        Map<UUID, ITeam> preferred = new HashMap<>();
        Set<UUID> grouped = new HashSet<>();
        for (Player player : waiting) {
            if (grouped.contains(player.getUniqueId())) continue;
            List<Player> members = BedWars.getParty().getMembers(player);
            List<Player> party = members == null ? new ArrayList<>() : new ArrayList<>(members);
            party.add(player);
            LinkedHashMap<UUID, Player> unique = new LinkedHashMap<>();
            ITeam companionTeam = null;
            boolean multipleTeams = false;
            for (Player member : party) {
                if (member == null) continue;
                if (available.containsKey(member.getUniqueId()) && !grouped.contains(member.getUniqueId())) {
                    unique.put(member.getUniqueId(), available.get(member.getUniqueId()));
                } else if (arena instanceof Arena && arena.isPlayer(member)) {
                    ITeam selected = ((Arena) arena).getSelectedTeam(member);
                    if (selected != null) {
                        if (companionTeam != null && companionTeam != selected) multipleTeams = true;
                        companionTeam = selected;
                    }
                }
            }
            List<Player> group = new ArrayList<>(unique.values());
            for (Player member : group) {
                grouped.add(member.getUniqueId());
                if (!multipleTeams && companionTeam != null) preferred.put(member.getUniqueId(), companionTeam);
            }
            groups.add(group);
        }
        groups.sort(Comparator.comparingInt((List<Player> group) -> group.size()).reversed());
        for (List<Player> group : groups) {
            int target = choose(teams, targets, group.size(), group.isEmpty() ? null : preferred.get(group.get(0).getUniqueId()));
            for (Player player : group) {
                int destination = target < 0 ? choose(teams, targets, 1, null) : target;
                if (!processed.add(player.getUniqueId()) || !arena.isPlayer(player) || arena.getTeam(player) != null) continue;
                if (destination < 0) continue; // An external listener may have consumed the planned capacity.
                ITeam team = teams.get(destination);
                if (team.getMembers().size() >= targets[destination] || team.getMembers().size() >= arena.getMaxInTeam()) continue;
                TeamAssignEvent event = new TeamAssignEvent(player, team, arena);
                Bukkit.getPluginManager().callEvent(event);
                player.closeInventory();
                if (!event.isCancelled() && arena.isPlayer(player) && arena.getTeam(player) == null
                        && team.getMembers().size() < targets[destination] && team.getMembers().size() < arena.getMaxInTeam()) {
                    team.addPlayers(player);
                }
            }
        }
    }

    private static int choose(List<ITeam> teams, int[] targets, int size, ITeam preferred) {
        int result = -1;
        for (int i = 0; i < teams.size(); i++) {
            int current = teams.get(i).getMembers().size();
            if (targets[i] - current < size) continue;
            if (teams.get(i) == preferred) return i;
            if (result < 0 || current < teams.get(result).getMembers().size()) result = i;
        }
        return result;
    }
}
