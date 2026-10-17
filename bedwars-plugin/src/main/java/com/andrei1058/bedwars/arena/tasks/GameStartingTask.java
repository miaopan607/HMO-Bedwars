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

package com.andrei1058.bedwars.arena.tasks;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.NextEvent;
import com.andrei1058.bedwars.api.arena.generator.GeneratorType;
import com.andrei1058.bedwars.api.arena.generator.IGenerator;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.api.tasks.StartingTask;
import com.andrei1058.bedwars.arena.Arena;
import com.andrei1058.bedwars.arena.team.BedWarsTeam;
import com.andrei1058.bedwars.configuration.Sounds;
import com.andrei1058.bedwars.support.papi.SupportPAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

import static com.andrei1058.bedwars.BedWars.nms;
import static com.andrei1058.bedwars.api.language.Language.getList;
import static com.andrei1058.bedwars.api.language.Language.getMsg;

public class GameStartingTask implements Runnable, StartingTask {

    private int countdown;
    private final IArena arena;
    private final BukkitTask task;

    public GameStartingTask(Arena arena) {
        this.arena = arena;
        countdown = arena.getGameConfiguration().getInt(ConfigPath.GENERAL_CONFIGURATION_START_COUNTDOWN_REGULAR);
        task = Bukkit.getScheduler().runTaskTimer(BedWars.plugin, this, 0, 20L);
    }


    /**
     * Get countdown value
     */
    public int getCountdown() {
        return countdown;
    }

    public void setCountdown(int countdown) {
        this.countdown = countdown;
    }

    /**
     * Get arena
     */
    public IArena getArena() {
        return arena;
    }

    /**
     * Get task ID
     */
    public int getTask() {
        return task.getTaskId();
    }

    @Override
    public BukkitTask getBukkitTask() {
        return task;
    }

    @Override
    public void run() {
        if (!canContinueStarting()) {
            return;
        }
        if (countdown == 0) {

            Arena concreteArena = getArena() instanceof Arena ? (Arena) getArena() : null;
            if (concreteArena != null) concreteArena.prepareTeamAssignment();
            getArena().getTeamAssigner().assignTeams(getArena());
            if (concreteArena != null) concreteArena.finishTeamAssignment();

            if (!canContinueStarting()) {
                return;
            }

            //Color bed block if possible
            //Destroy bed if team is empty
            //Spawn shops and upgrades
            //Disable generators for empty teams if required
            for (ITeam team : getArena().getTeams()) {
                nms.colorBed(team);
                if (team.getMembers().isEmpty()) {
                    team.setBedDestroyed(true);
                    if (getArena().getConfig().getBoolean(ConfigPath.ARENA_DISABLE_GENERATOR_FOR_EMPTY_TEAMS)) {
                        for (IGenerator gen : team.getGenerators()) {
                            gen.disable();
                        }
                    }
                }
            }

            Bukkit.getScheduler().runTaskLater(BedWars.plugin, () -> {
                //Enable diamond/ emerald generators
                for (IGenerator og : getArena().getOreGenerators()) {
                    if (og.getType() == GeneratorType.EMERALD || og.getType() == GeneratorType.DIAMOND)
                        og.enableRotation();
                }
            }, 60L);

            //Spawn players
            spawnPlayers();

            if (!canContinueStarting()) {
                return;
            }

            //Lobby removal
            BedWars.getAPI().getRestoreAdapter().onLobbyRemoval(arena);

            if (!canContinueStarting()) {
                return;
            }

            for (Player spectator : getArena().getSpectators()) {
                sendStart(spectator);
            }
            task.cancel();
            getArena().changeStatus(GameState.playing);

            // Check if emerald should be first based on time
            if (getArena().getUpgradeDiamondsCount() < getArena().getUpgradeEmeraldsCount()) {
                getArena().setNextEvent(NextEvent.DIAMOND_GENERATOR_TIER_II);
            } else {
                getArena().setNextEvent(NextEvent.EMERALD_GENERATOR_TIER_II);
            }

            //Spawn shopkeepers
            for (ITeam bwt : getArena().getTeams()) {
                bwt.spawnNPCs();
            }
            return;
        }

        //Send countdown
        if (getCountdown() % 10 == 0 || getCountdown() <= 5) {
            String soundPath = getCountdown() < 5
                    ? ConfigPath.SOUNDS_COUNTDOWN_TICK_X + getCountdown()
                    : ConfigPath.SOUNDS_COUNTDOWN_TICK;
            sendCountdown(getArena().getPlayers(), soundPath);
            sendCountdown(getArena().getSpectators(), soundPath);
        }
        countdown--;
    }

    private void sendCountdown(List<Player> recipients, String soundPath) {
        Sounds.playSound(soundPath, recipients);
        for (Player player : recipients) {
            Language playerLang = Language.getPlayerLanguage(player);
            String[] titleSubtitle = Language.getCountDownTitle(playerLang, getCountdown());
            nms.sendTitle(player, titleSubtitle[0], titleSubtitle[1], 0, 20, 10);
            player.sendMessage(getMsg(player, Messages.ARENA_STATUS_START_COUNTDOWN_CHAT).replace("{time}", String.valueOf(getCountdown())));
        }
    }

    //Spawn players
    private void spawnPlayers() {
        for (ITeam bwt : getArena().getTeams()) {
            for (Player p : new ArrayList<>(bwt.getMembers())) {
                if (!canContinueStarting()) {
                    return;
                }
                if (!getArena().getPlayers().contains(p)) {
                    continue;
                }
                BedWarsTeam.reSpawnInvulnerability.put(p.getUniqueId(), System.currentTimeMillis() + 2000L);
                bwt.firstSpawn(p);
                if (!canContinueStarting()) {
                    return;
                }
                if (!getArena().getPlayers().contains(p)) {
                    continue;
                }
                sendStart(p);
                for (String tut : getList(p, Messages.ARENA_STATUS_START_PLAYER_TUTORIAL)) {
                    p.sendMessage(SupportPAPI.getSupportPAPI().replace(p, tut));
                }
            }
        }
    }

    private void sendStart(Player player) {
        Sounds.playSound(ConfigPath.SOUND_GAME_START, player);
        nms.sendTitle(player, getMsg(player, Messages.ARENA_STATUS_START_PLAYER_TITLE), null, 0, 30, 10);
    }

    /**
     * 检查游戏是否仍处于 starting 状态且存在玩家，否则取消任务或重置为 waiting。
     */
    private boolean canContinueStarting() {
        if (getArena().getStatus() != GameState.starting) {
            cancel();
            return false;
        }
        if (getArena().isStartBlocked()) {
            getArena().changeStatus(GameState.waiting);
            return false;
        }
        if (getArena().getPlayers().isEmpty()) {
            getArena().changeStatus(GameState.waiting);
            cancel();
            return false;
        }
        return true;
    }

    public void cancel() {
        task.cancel();
    }
}
