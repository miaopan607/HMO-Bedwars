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

package com.andrei1058.bedwars.language;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.andrei1058.bedwars.BedWars.mainCmd;

public class SimplifiedChinese extends Language {

    public SimplifiedChinese() {
        super(BedWars.plugin, "zh_cn");

        YamlConfiguration yml = getYml();
        yml.options().copyDefaults(true);
        yml.addDefault(Messages.PREFIX, "");
        yml.addDefault("name", "简体中文");

        // this must stay here
        // move message to new path
        if (yml.get("player-die-knocked-regular") != null && yml.get(Messages.PLAYER_DIE_KNOCKED_IN_VOID_REGULAR_KILL) == null) {
            yml.set(Messages.PLAYER_DIE_KNOCKED_IN_VOID_REGULAR_KILL, yml.getString("player-die-knocked-regular"));
            yml.set("player-die-knocked-regular", null);
        }
        if (yml.get("player-die-knocked-final") != null && yml.get(Messages.PLAYER_DIE_KNOCKED_IN_VOID_FINAL_KILL) == null) {
            yml.set(Messages.PLAYER_DIE_KNOCKED_IN_VOID_FINAL_KILL, yml.getString("player-die-knocked-final"));
            yml.set("player-die-knocked-final", null);
        }

        yml.addDefault(Messages.TEAM_SELECTOR_TITLE, "&8选择队伍");
        yml.addDefault(Messages.TEAM_SELECTOR_ITEM_NAME, "&e选择队伍");
        yml.addDefault(Messages.TEAM_SELECTOR_ITEM_LORE, Arrays.asList("&7当前队伍：{team}", "&f右键选择队伍"));
        yml.addDefault(Messages.TEAM_SELECTOR_NONE, "&f未选择");
        yml.addDefault(Messages.TEAM_SELECTOR_TEAM_LORE, Arrays.asList("&7人数：{selected}/{max}", "&e点击选择", "{state}"));
        yml.addDefault(Messages.TEAM_SELECTOR_SELECTED, "&a已选择此队伍");
        yml.addDefault(Messages.TEAM_SELECTOR_CANCEL_NAME, "&c取消选择队伍");
        yml.addDefault(Messages.TEAM_SELECTOR_CANCEL_LORE, Collections.singletonList("&7开场时自动分配队伍"));
        yml.addDefault(Messages.TEAM_SELECTOR_FULL, "&c该队伍已满，请选择其他队伍。");
        yml.addDefault(Messages.TEAM_SELECTOR_PREVIOUS_PAGE, "&e上一页");
        yml.addDefault(Messages.TEAM_SELECTOR_NEXT_PAGE, "&e下一页");
        yml.addDefault("team-color-name.RED", "红队");
        yml.addDefault("team-color-name.BLUE", "蓝队");
        yml.addDefault("team-color-name.GREEN", "绿队");
        yml.addDefault("team-color-name.YELLOW", "黄队");
        yml.addDefault("team-color-name.AQUA", "青队");
        yml.addDefault("team-color-name.WHITE", "白队");
        yml.addDefault("team-color-name.PINK", "粉队");
        yml.addDefault("team-color-name.GRAY", "灰队");
        yml.addDefault("team-color-name.DARK_GREEN", "深绿队");
        yml.addDefault("team-color-name.DARK_GRAY", "深灰队");

        yml.addDefault(Messages.JOIN_WELCOME, Collections.emptyList());
        yml.addDefault(Messages.HINT_CHAT_GLOBAL, "&7使用&e@&7开头发送全服消息。");
        yml.addDefault(Messages.HINT_REJOIN, "&7使用&e/rejoin&7重新加入刚刚的对局。");
        yml.addDefault(Messages.HINT_READY_ITEM, "&7可右键快捷栏&e第 2 格&7的&c红石块&7准备。");
        yml.addDefault(Messages.HINT_CHAT_SHOUT, "&7使用&e!&7开头发送全局消息。");
        yml.addDefault(Messages.HINT_SPECTATOR_MODE, "&7使用&e/{command} spec&7切换原版旁观模式。");

        yml.addDefault(Messages.COMMAND_MAIN, Arrays.asList("", "&a▪ &7/" + mainCmd + " stats", "&a▪ &7/" + mainCmd + " join &o<游戏/模式>", "&a▪ &7/" + mainCmd + " leave", "&a▪ &7/" + mainCmd + " lang", "&a▪ &7/" + mainCmd + " gui", "&a▪ &7/" + mainCmd + " start &b（赞助者）", "&a▪ &7/" + mainCmd + " specmode &b（旁观者）"));
        yml.addDefault(Messages.SERVER_SPAWN_PROTECTION_WARNING, "server.properties 中启用了 spawn-protection，这可能会影响 BedWars 竞技场，建议将其设为 0。");
        yml.addDefault(Messages.SERVER_MULTIVERSE_WARNING, "检测到 Multiverse-Core；请将其移除，或确保它不会修改 BedWars 地图。");
        yml.addDefault(Messages.RESTORE_LOAD_WORLD, "正在从 Bukkit 世界容器加载 {world}。");
        yml.addDefault(Messages.RESTORE_CREATE_VOID_MAP, "正在创建新的虚空地图：{world}");
        yml.addDefault(Messages.CUSTOMER_DETAILS_HEADER, "&7[&f{plugin} v{version}&7]&7&m---------------------------");
        yml.addDefault(Messages.CUSTOMER_DETAILS_USER_ID, "&7用户 ID：&f%%__USER__%%");
        yml.addDefault(Messages.CUSTOMER_DETAILS_DOWNLOAD_ID, "&7下载 ID：&f%%__NONCE__%%");
        yml.addDefault(Messages.COMMAND_ARENA_GROUP_ARENA_NOT_FOUND, "&c▪ &7竞技场 {arena} 不存在！");
        yml.addDefault(Messages.COMMAND_ARENA_GROUP_ASSIGNED, "&6 ▪ &7已将 {arena} 加入分组：{group}");
        yml.addDefault(Messages.COMMAND_ARENA_GROUP_CREATED, "&6 ▪ &7分组已创建！");
        yml.addDefault(Messages.COMMAND_ARENA_GROUP_CREATE_HOVER, "创建竞技场分组。详情参见 Wiki。");
        yml.addDefault(Messages.COMMAND_ARENA_GROUP_CREATE_USAGE, "&6 ▪ &7/{command} {subcommand} create &o<分组名>");
        yml.addDefault(Messages.COMMAND_ARENA_GROUP_DEFAULT, "&6 ▪ &f默认");
        yml.addDefault(Messages.COMMAND_ARENA_GROUP_DELETED, "&6 ▪ &7分组已删除！");
        yml.addDefault(Messages.COMMAND_ARENA_GROUP_DISPLAY, "&6 ▪ &7/{command} {subcommand} &7- &e点击查看详情");
        yml.addDefault(Messages.COMMAND_ARENA_GROUP_DISPLAY_HOVER, "&f管理竞技场分组。");
        yml.addDefault(Messages.COMMAND_ARENA_GROUP_ENTRY, "&6 ▪ &f{group}");
        yml.addDefault(Messages.COMMAND_ARENA_GROUP_EXISTS, "&c▪ &7该分组已存在！");
        yml.addDefault(Messages.COMMAND_ARENA_GROUP_HEADER, "&7可用竞技场分组：");
        yml.addDefault(Messages.COMMAND_ARENA_GROUP_LIST_HOVER, "查看可用分组。");
        yml.addDefault(Messages.COMMAND_ARENA_GROUP_LIST_USAGE, "&6 ▪ &7/{command} {subcommand} list");
        yml.addDefault(Messages.COMMAND_ARENA_GROUP_NOT_FOUND, "&c▪ &7该分组不存在！");
        yml.addDefault(Messages.COMMAND_ARENA_GROUP_REMOVE_HOVER, "删除竞技场分组。详情参见 Wiki。");
        yml.addDefault(Messages.COMMAND_ARENA_GROUP_REMOVE_USAGE, "&6 ▪ &7/{command} {subcommand} remove &o<分组名>");
        yml.addDefault(Messages.COMMAND_ARENA_GROUP_SET_HOVER, "设置竞技场分组。详情参见 Wiki。");
        yml.addDefault(Messages.COMMAND_ARENA_GROUP_SET_USAGE, "&6 ▪ &7/{command} {subcommand} set &o<竞技场名> <分组名>");
        yml.addDefault(Messages.COMMAND_ARENA_GROUP_UNKNOWN, "&6 ▪ &7不存在名为 {group} 的分组。");
        yml.addDefault(Messages.COMMAND_ARENA_LIST_DISPLAY, "&6 ▪ &7/{command} {subcommand} {countColor}（已设置 {count}）");
        yml.addDefault(Messages.COMMAND_ARENA_LIST_DISPLAY_HOVER, "&f显示可用竞技场。");
        yml.addDefault(Messages.COMMAND_ARENA_LIST_EMPTY, "&c没有可显示的竞技场。");
        yml.addDefault(Messages.COMMAND_ARENA_LIST_ENTRY, "ID: &e{arena} &f分组：&e{group} &f人数：&e{players} &f状态：{status} &f世界已加载：&e{worldLoaded}");
        yml.addDefault(Messages.COMMAND_ARENA_LIST_HEADER, " \n&9|| &b{plugin}&7 已创建的游戏：\n ");
        yml.addDefault(Messages.COMMAND_ARENA_LIST_NEXT_PAGE, "&7输入 /&a{command} arenaList {page}&7 查看下一页。");
        yml.addDefault(Messages.COMMAND_ARENA_LIST_SPACER, " ");
        yml.addDefault(Messages.COMMAND_ARENA_USAGE, "&c▪ &7用法：&o/{command} {subcommand} <地图名>");
        yml.addDefault(Messages.COMMAND_BUILD_DISABLED, "&6 ▪ &7你现在无法放置或破坏方块！");
        yml.addDefault(Messages.COMMAND_BUILD_DISPLAY, "&6 ▪ &7/{command} {subcommand}         &7 - &e建造权限");
        yml.addDefault(Messages.COMMAND_BUILD_DISPLAY_HOVER, "&f打开或关闭建造模式，\n&f以便放置或破坏方块。");
        yml.addDefault(Messages.COMMAND_BUILD_ENABLED, "&6 ▪ &7你现在可以放置或破坏方块。");
        yml.addDefault(Messages.COMMAND_CLONE_ARENA_COPY_ERROR, "&c▪ &7复制地图配置时发生错误。请检查控制台。");
        yml.addDefault(Messages.COMMAND_CLONE_ARENA_DISABLE_FIRST, "&c▪ &7请先禁用 {arena}！");
        yml.addDefault(Messages.COMMAND_CLONE_ARENA_DISPLAY, "&6 ▪ &7/{command} {subcommand} &6<世界名> <新名称>");
        yml.addDefault(Messages.COMMAND_CLONE_ARENA_DISPLAY_HOVER, "&f复制已有竞技场。");
        yml.addDefault(Messages.COMMAND_CLONE_ARENA_EXISTS, "&c▪ &7{arena} 已存在！");
        yml.addDefault(Messages.COMMAND_CLONE_ARENA_SUCCESS, "&6 ▪ &7完成 :D。");
        yml.addDefault(Messages.COMMAND_CLONE_ARENA_USAGE, "&c▪ &7用法：&o/{command} {subcommand} <地图名> <新竞技场名>");
        yml.addDefault(Messages.COMMAND_DEL_ARENA_CONFIRM, "&6 ▪ &7请再次输入以确认。");
        yml.addDefault(Messages.COMMAND_DEL_ARENA_DISPLAY, "&6 ▪ &7/{command} {subcommand} &6<世界名>");
        yml.addDefault(Messages.COMMAND_DEL_ARENA_DISPLAY_HOVER, "&f删除地图及其配置。");
        yml.addDefault(Messages.COMMAND_DEL_ARENA_NOT_FOUND, "&c▪ &7该竞技场不存在！");
        yml.addDefault(Messages.COMMAND_DEL_ARENA_SUCCESS, "&c▪ &7已删除 {arena}！");
        yml.addDefault(Messages.COMMAND_DEL_ARENA_USAGE, "&c▪ &7用法：&o/{command} delArena <地图名>");
        yml.addDefault(Messages.COMMAND_DEL_ARENA_WORLD_NOT_FOUND, "&c▪ &7{arena} 不存在对应的世界文件夹！");
        yml.addDefault(Messages.COMMAND_DISABLE_ARENA_ALREADY_DISABLED, "&c▪ &7该竞技场已禁用！");
        yml.addDefault(Messages.COMMAND_DISABLE_ARENA_DISPLAY, "&6 ▪ &7/{command} {subcommand} &6<世界名>");
        yml.addDefault(Messages.COMMAND_DISABLE_ARENA_DISPLAY_HOVER, "&f禁用竞技场。\n禁用前将移出竞技场中的玩家。");
        yml.addDefault(Messages.COMMAND_DISABLE_ARENA_PLAYING, "&6 ▪ &7竞技场正在游戏，请在结束后禁用！");
        yml.addDefault(Messages.COMMAND_DISABLE_ARENA_START, "&6 ▪ &7正在禁用竞技场……");
        yml.addDefault(Messages.COMMAND_ENABLE_ARENA_ALREADY_ENABLED, "&c▪ &7该竞技场已启用！");
        yml.addDefault(Messages.COMMAND_ENABLE_ARENA_DISPLAY, "&6 ▪ &7/{command} {subcommand} &6<世界名>");
        yml.addDefault(Messages.COMMAND_ENABLE_ARENA_DISPLAY_HOVER, "&f启用竞技场。");
        yml.addDefault(Messages.COMMAND_ENABLE_ARENA_QUEUED, "&c▪ &7该竞技场已在启用队列中！");
        yml.addDefault(Messages.COMMAND_ENABLE_ARENA_START, "&6 ▪ &7正在启用竞技场……");
        yml.addDefault(Messages.COMMAND_ENABLE_ARENA_USAGE, "&c▪ &7用法：&o/{command} enableRotation <地图名>");

        yml.addDefault(Messages.COMMAND_NPC_ADD_HOVER, "点击使用。");
        yml.addDefault(Messages.COMMAND_NPC_ADD_USAGE, "§f\n§c▪ §7用法：§e§o/{command} {subcommand} add <皮肤> <竞技场分组> <§7第一行§9\\n§7第二行§e>\n§7你可以用 §e{players} §7表示分组内的玩家数量。");
        yml.addDefault(Messages.COMMAND_NPC_CREATED, "§a§c▪ §bNPC：{name} §b已设置！");
        yml.addDefault(Messages.COMMAND_NPC_DISPLAY, "§6 ▪ §7/{command} {subcommand}         §7   - §e创建加入游戏的 NPC");
        yml.addDefault(Messages.COMMAND_NPC_DISPLAY_HOVER, "§f创建用于加入游戏的 NPC。\n§f点击查看详情。");
        yml.addDefault(Messages.COMMAND_NPC_MAIN_ADD_HOVER, "§f使用此命令创建加入游戏的 NPC。\n§f点击查看语法。");
        yml.addDefault(Messages.COMMAND_NPC_MAIN_ADD_USAGE, "§f\n§c▪ §7用法：§e/{command} {subcommand} add");
        yml.addDefault(Messages.COMMAND_NPC_MAIN_REMOVE_HOVER, "§f站在 NPC 前方将其移除。");
        yml.addDefault(Messages.COMMAND_NPC_MAIN_REMOVE_USAGE, "§c▪ §7用法：§e/{command} {subcommand} remove");
        yml.addDefault(Messages.COMMAND_NPC_NONE_NEARBY, "§c▪ §b附近没有 NPC。");
        yml.addDefault(Messages.COMMAND_NPC_NONE_SET, "§c▪ §b尚未设置 NPC！");
        yml.addDefault(Messages.COMMAND_NPC_REMOVED, "§c▪ §b已移除目标 NPC！");
        yml.addDefault(Messages.COMMAND_NPC_TARGET_GROUPS, "§a§c▪ §b目标分组：§6{groups}");
        yml.addDefault(Messages.COMMAND_RELOAD_ARENA, "§6 ▪ §7竞技场配置 {config} 已重新加载！");
        yml.addDefault(Messages.COMMAND_RELOAD_CONFIG, "§6 ▪ §7{config} 已重新加载！");
        yml.addDefault(Messages.COMMAND_RELOAD_DISPLAY, "§6 ▪ §7/{command} {subcommand}       §7 - §e重新加载消息");
        yml.addDefault(Messages.COMMAND_RELOAD_DISPLAY_HOVER, "§f重新加载消息。\n§c不建议这样做！");
        yml.addDefault(Messages.COMMAND_RELOAD_LANGUAGE, "§6 ▪ §7语言 {language} 已重新加载！");
        yml.addDefault(Messages.COMMAND_SETUP_ALREADY_IN_SESSION, "§c ▪ §7你已在设置流程中！");
        yml.addDefault(Messages.COMMAND_SETUP_ARENA_CAPITALS, "§c▪ §c{arena}§7 不得包含大写字母！请将文件夹重命名为：§a{lowercase}");
        yml.addDefault(Messages.COMMAND_SETUP_ARENA_DISABLE_FIRST, "&c▪ &7请先禁用它！");
        yml.addDefault(Messages.COMMAND_SETUP_ARENA_DISPLAY, "§6 ▪ §7/{command} setupArena §6<世界名>");
        yml.addDefault(Messages.COMMAND_SETUP_ARENA_DISPLAY_HOVER, "§f创建或编辑竞技场。\n竞技场名称中不显示“_”和“-”。");
        yml.addDefault(Messages.COMMAND_SETUP_ARENA_SYMBOL, "§c▪ §7{arena} 不能包含此符号：§c+");
        yml.addDefault(Messages.COMMAND_SETUP_ARENA_USAGE, "§c▪ §7用法：§o/{command} {subcommand} <地图名>");
        yml.addDefault(Messages.COMMAND_SETUP_AUTO_TEAMS_CONFIRM, "§6 ▪ §7§l点击此处创建找到的队伍。");
        yml.addDefault(Messages.COMMAND_SETUP_AUTO_TEAMS_CONFIRM_HOVER, "§f点击创建找到的队伍！");
        yml.addDefault(Messages.COMMAND_SETUP_AUTO_TEAMS_FOUND_ENTRY, "§f ▪ {team}");
        yml.addDefault(Messages.COMMAND_SETUP_AUTO_TEAMS_FOUND_HEADER, "§6§l发现新队伍：");
        yml.addDefault(Messages.COMMAND_SETUP_AUTO_TEAMS_NONE, "§6 ▪ §7未找到新队伍。\n§6 ▪ §7可使用 §6/{command} createTeam 手动创建队伍。");
        yml.addDefault(Messages.COMMAND_SETUP_AUTO_TEAMS_SEARCH, "§6 ▪ §7正在查找队伍，可能会造成卡顿。");
        yml.addDefault(Messages.COMMAND_SETUP_AUTO_TEAMS_TIMEOUT, "§c ▪ §7查找超时。再次输入以重新查找队伍。");
        yml.addDefault(Messages.COMMAND_SETUP_AUTO_TEAMS_WAITING_DESCRIPTION, "§f如果希望游戏开始时等待大厅消失，");
        yml.addDefault(Messages.COMMAND_SETUP_AUTO_TEAMS_WAITING_HEADER, "§6§l移除等待大厅：");
        yml.addDefault(Messages.COMMAND_SETUP_AUTO_TEAMS_WAITING_OPTIONAL, "§7此步骤可选。若要跳过，请输入 §6/{command}");
        yml.addDefault(Messages.COMMAND_SETUP_AUTO_TEAMS_WAITING_SELECTION, "§f请使用下列命令，像使用 WorldEdit 一样选择区域。");
        yml.addDefault(Messages.COMMAND_SETUP_AVAILABLE_TEAMS, "可用队伍：");
        yml.addDefault(Messages.COMMAND_SETUP_BED_HOLOGRAM, "{team} §6床已设置");
        yml.addDefault(Messages.COMMAND_SETUP_BED_MANUAL_HOVER, "设置队伍床。");
        yml.addDefault(Messages.COMMAND_SETUP_BED_SET, "已设置队伍 {team} 的床。");
        yml.addDefault(Messages.COMMAND_SETUP_BED_SET_TITLE, "§a已设置队伍 {team} 的床。");
        yml.addDefault(Messages.COMMAND_SETUP_BED_SPAWN_HOVER, "§f设置队伍床。");
        yml.addDefault(Messages.COMMAND_SETUP_BED_STAND_ON_BED, "§c使用此命令时你必须站在床上！");
        yml.addDefault(Messages.COMMAND_SETUP_BED_STAND_ON_BED_TITLE, "§c你必须站在床上。");
        yml.addDefault(Messages.COMMAND_SETUP_BED_TEAM_HOVER, "§f设置队伍 {team} 的床。");
        yml.addDefault(Messages.COMMAND_SETUP_BUILD_HEIGHT_SET, "§6 ▪ §7最高建造高度 Y 已设置为 §e{height}§7！");
        yml.addDefault(Messages.COMMAND_SETUP_BUILD_HEIGHT_USAGE, "§c▪ §7用法：/{command} setMaxBuildHeight <整数>");
        yml.addDefault(Messages.COMMAND_SETUP_CREATE_TEAM_COLORS, "§6 ▪ §7可用颜色：{colors}");
        yml.addDefault(Messages.COMMAND_SETUP_CREATE_TEAM_CREATED, "§6 ▪ §7队伍 {team} §7已创建！");
        yml.addDefault(Messages.COMMAND_SETUP_CREATE_TEAM_EXISTS, "§c▪ §7队伍 {team} 已存在！");
        yml.addDefault(Messages.COMMAND_SETUP_CREATE_TEAM_INVALID_COLOR, "§c▪ §7颜色无效！");
        yml.addDefault(Messages.COMMAND_SETUP_CREATE_TEAM_USAGE, "§c▪ §7用法：/{command} createTeam §o<名称> §o<颜色>");
        yml.addDefault(Messages.COMMAND_SETUP_GENERATOR_ADDED, "{generator} 生成器已添加！");
        yml.addDefault(Messages.COMMAND_SETUP_GENERATOR_ADDED_TITLE, "§6{generator}§a 生成器已添加！");
        yml.addDefault(Messages.COMMAND_SETUP_GENERATOR_ADVANCED_AUTO_HOVER, "添加队伍生成器。\n自动识别对应队伍。");
        yml.addDefault(Messages.COMMAND_SETUP_GENERATOR_ADVANCED_AUTO_USAGE, "/bw addGenerator <iron/gold/upgrade>");
        yml.addDefault(Messages.COMMAND_SETUP_GENERATOR_ADVANCED_HINT, "试试：/bw addGenerator <iron/gold/upgrade> <队伍名>");
        yml.addDefault(Messages.COMMAND_SETUP_GENERATOR_ADVANCED_HOLOGRAM, "§6已为队伍 {team} 添加 {generator} 生成器。");
        yml.addDefault(Messages.COMMAND_SETUP_GENERATOR_ADVANCED_SET, "已为队伍 {team} 添加 {generator} 生成器。");
        yml.addDefault(Messages.COMMAND_SETUP_GENERATOR_ADVANCED_TITLE, "§6{generator}§a 生成器已为队伍 {team}§a 添加！");
        yml.addDefault(Messages.COMMAND_SETUP_GENERATOR_ADVANCED_USAGE, "/bw addGenerator <iron/gold/upgrade> <队伍名>");
        yml.addDefault(Messages.COMMAND_SETUP_GENERATOR_AUTO_USAGE, "/bw addGenerator（自动识别队伍）");
        yml.addDefault(Messages.COMMAND_SETUP_GENERATOR_CREATE_TEAM_HINT, "如果要创建队伍，请使用：/bw createTeam。");
        yml.addDefault(Messages.COMMAND_SETUP_GENERATOR_EXISTS, "§c此生成器已设置！");
        yml.addDefault(Messages.COMMAND_SETUP_GENERATOR_EXISTS_TITLE, "§c此生成器已设置！");
        yml.addDefault(Messages.COMMAND_SETUP_GENERATOR_HOLOGRAM, "§6{generator} 已设置");
        yml.addDefault(Messages.COMMAND_SETUP_GENERATOR_OTHER_HINT, "其他用法：/bw addGenerator <emerald/diamond>");
        yml.addDefault(Messages.COMMAND_SETUP_GENERATOR_OTHER_HOVER, "添加绿宝石或钻石生成器。");
        yml.addDefault(Messages.COMMAND_SETUP_GENERATOR_RESOURCE_USAGE, "/bw addGenerator <emerald/diamond>");
        yml.addDefault(Messages.COMMAND_SETUP_GENERATOR_TEAM_HOLOGRAM, "§6已为队伍 {team} 设置生成器。");
        yml.addDefault(Messages.COMMAND_SETUP_GENERATOR_TEAM_HOVER, "添加队伍生成器。");
        yml.addDefault(Messages.COMMAND_SETUP_GENERATOR_TEAM_NOT_FOUND, "§c找不到队伍：{team}");
        yml.addDefault(Messages.COMMAND_SETUP_GENERATOR_TEAM_SET, "已为队伍 {team} 设置生成器。");
        yml.addDefault(Messages.COMMAND_SETUP_GENERATOR_TEAM_SET_TITLE, "§a已为队伍 {team} 设置生成器。");
        yml.addDefault(Messages.COMMAND_SETUP_GENERATOR_TEAM_USAGE, "/bw addGenerator <队伍>");
        yml.addDefault(Messages.COMMAND_SETUP_KILL_DROPS_CREATE_TEAMS, "请先创建队伍！");
        yml.addDefault(Messages.COMMAND_SETUP_KILL_DROPS_CREATE_TEAMS_TITLE, "§c请先创建队伍！");
        yml.addDefault(Messages.COMMAND_SETUP_KILL_DROPS_HOLOGRAM, "§6队伍 {team} 的击杀掉落物");
        yml.addDefault(Messages.COMMAND_SETUP_KILL_DROPS_HOVER, "设置队伍击杀掉落物的位置。");
        yml.addDefault(Messages.COMMAND_SETUP_KILL_DROPS_SET, "已为队伍 {team} 设置击杀掉落物位置。");
        yml.addDefault(Messages.COMMAND_SETUP_KILL_DROPS_TEAM_ENTRY, "§6 ▪ 队伍 {team} 的击杀掉落物（点击设置）");
        yml.addDefault(Messages.COMMAND_SETUP_KILL_DROPS_TEAM_HOVER, "§f设置队伍 {team} 的击杀掉落物位置。");
        yml.addDefault(Messages.COMMAND_SETUP_KILL_DROPS_TEAM_SET, "已为队伍 {team} 设置击杀掉落物位置。");
        yml.addDefault(Messages.COMMAND_SETUP_KILL_DROPS_TEAM_SET_TITLE, "§a已为队伍 {team} 设置击杀掉落物位置。");
        yml.addDefault(Messages.COMMAND_SETUP_KILL_DROPS_USAGE, "§c用法：/{command} setKillDrops <队伍名>");
        yml.addDefault(Messages.COMMAND_SETUP_MAX_IN_TEAM_SET, "§6 ▪ §7每队最大人数已设置！");
        yml.addDefault(Messages.COMMAND_SETUP_MAX_IN_TEAM_USAGE, "§c▪ §7用法：/{command} setMaxInTeam <整数>");
        yml.addDefault(Messages.COMMAND_SETUP_NEARBY_TEAM_NOT_FOUND, "§c附近没有找到队伍。");
        yml.addDefault(Messages.COMMAND_SETUP_NEARBY_TEAM_NOT_FOUND_TITLE, "§c附近没有找到队伍。");
        yml.addDefault(Messages.COMMAND_SETUP_NOT_IN_SESSION, "§c ▪ §7你不在设置流程中！");
        yml.addDefault(Messages.COMMAND_SETUP_REMOVE_GENERATOR_NOT_FOUND, "附近未找到生成器（搜索范围 2×2）。");
        yml.addDefault(Messages.COMMAND_SETUP_REMOVE_GENERATOR_NOT_FOUND_TITLE, "§c附近未找到生成器。");
        yml.addDefault(Messages.COMMAND_SETUP_REMOVE_GENERATOR_REMOVED_AT, "已移除位置 X:{x} Y:{y} Z:{z} 的 {generator} 生成器。");
        yml.addDefault(Messages.COMMAND_SETUP_REMOVE_GENERATOR_REMOVED_TITLE, "§a{generator} 生成器已移除。");
        yml.addDefault(Messages.COMMAND_SETUP_REMOVE_GENERATOR_STAND_CLOSE, "你必须站在需要移除的生成器全息文字附近。");
        yml.addDefault(Messages.COMMAND_SETUP_REMOVE_GENERATOR_TEAM_GENERATOR_REMOVED_TITLE, "队伍 {team} 的 §a{generator} 生成器已移除。");
        yml.addDefault(Messages.COMMAND_SETUP_REMOVE_GENERATOR_TEAM_REMOVED, "队伍 {team} 的生成器已移除！");
        yml.addDefault(Messages.COMMAND_SETUP_REMOVE_GENERATOR_TEAM_REMOVED_AT, "已移除位置 X:{x} Y:{y} Z:{z} 的队伍 {team} {generator} 生成器。");
        yml.addDefault(Messages.COMMAND_SETUP_REMOVE_GENERATOR_TEAM_REMOVED_TITLE, "队伍 {team} 的生成器已移除。");
        yml.addDefault(Messages.COMMAND_SETUP_REMOVE_TEAM_HOVER, "§7移除队伍 {team} §7（点击移除）");
        yml.addDefault(Messages.COMMAND_SETUP_REMOVE_TEAM_NOT_FOUND, "队伍 {team} 不存在。");
        yml.addDefault(Messages.COMMAND_SETUP_REMOVE_TEAM_NOT_FOUND_TITLE, "§c找不到队伍：{team}");
        yml.addDefault(Messages.COMMAND_SETUP_REMOVE_TEAM_SUCCESS, "已移除队伍：{team}");
        yml.addDefault(Messages.COMMAND_SETUP_REMOVE_TEAM_SUCCESS_TITLE, "§a已移除队伍：{team}");
        yml.addDefault(Messages.COMMAND_SETUP_REMOVE_TEAM_USAGE, "§c用法：/{command} removeTeam <队伍名>");
        yml.addDefault(Messages.COMMAND_SETUP_SAVE_ENABLE_HINT, "现在可以使用以下命令启用竞技场：");
        yml.addDefault(Messages.COMMAND_SETUP_SAVE_ENABLE_HOVER, "§a启用此竞技场。");
        yml.addDefault(Messages.COMMAND_SETUP_SAVE_ENABLE_OPTION, "§6/{command} enableArena {arena}§7（点击启用）");
        yml.addDefault(Messages.COMMAND_SETUP_SAVE_SUCCESS, "竞技场修改已保存！");
        yml.addDefault(Messages.COMMAND_SETUP_SHOP_HOLOGRAM, "{team} §6商店已设置");
        yml.addDefault(Messages.COMMAND_SETUP_SHOP_HOVER, "设置队伍商店。");
        yml.addDefault(Messages.COMMAND_SETUP_SHOP_OTHER_USAGE, "其他用法：/bw setShop <队伍名>");
        yml.addDefault(Messages.COMMAND_SETUP_SHOP_SET, "已设置队伍 {team} 的商店。");
        yml.addDefault(Messages.COMMAND_SETUP_SHOP_TEAM_HOVER, "§7设置队伍 {team} 的商店。");
        yml.addDefault(Messages.COMMAND_SETUP_SPAWN_HOLOGRAM, "{team} §6出生点已设置");
        yml.addDefault(Messages.COMMAND_SETUP_SPAWN_REMAINING, "剩余队伍：{teams}");
        yml.addDefault(Messages.COMMAND_SETUP_SPAWN_SET, "§6 ▪ 已设置队伍 {team} 的出生点。");
        yml.addDefault(Messages.COMMAND_SETUP_SPAWN_TEAMS_HEADER, "队伍列表：");
        yml.addDefault(Messages.COMMAND_SETUP_SPAWN_TEAM_ENTRY, "设置队伍 {team} 的出生点（点击设置）");
        yml.addDefault(Messages.COMMAND_SETUP_SPAWN_TEAM_HOVER, "§f设置队伍 {team} 的出生点。");
        yml.addDefault(Messages.COMMAND_SETUP_SPAWN_TEAM_NOT_FOUND, "§c找不到目标队伍：§c{team}");
        yml.addDefault(Messages.COMMAND_SETUP_SPAWN_USAGE, "§c用法：/{command} setSpawn <队伍>");
        yml.addDefault(Messages.COMMAND_SETUP_SPECTATOR_SET, "旁观者出生点已设置！");
        yml.addDefault(Messages.COMMAND_SETUP_SPECTATOR_USAGE, "§c用法：/{command} {subcommand}");
        yml.addDefault(Messages.COMMAND_SETUP_TEAM_ENTRY, "§6 ▪ {team}");
        yml.addDefault(Messages.COMMAND_SETUP_TEAM_MANUAL_HINT, "如果已设置出生点，但未自动找到队伍，请尝试：/bw {subcommand} <队伍>");
        yml.addDefault(Messages.COMMAND_SETUP_TEAM_NOT_FOUND, "§c该队伍不存在！");
        yml.addDefault(Messages.COMMAND_SETUP_TEAM_SELECT_ENTRY, "§6 ▪ {team} §7（点击设置）");
        yml.addDefault(Messages.COMMAND_SETUP_TEAM_SET_SPAWN_FIRST, "请先设置该队伍的出生点！");
        yml.addDefault(Messages.COMMAND_SETUP_TEAM_SPAWN_HOVER, "§f设置队伍出生点。");
        yml.addDefault(Messages.COMMAND_SETUP_TYPE_AVAILABLE, "§9可用类型：");
        yml.addDefault(Messages.COMMAND_SETUP_TYPE_CHANGED, "§6 ▪ §7竞技场分组已更改为：§d{type}");
        yml.addDefault(Messages.COMMAND_SETUP_TYPE_OPTION, "§9 ▪ §e{type} §7（点击设置）");
        yml.addDefault(Messages.COMMAND_SETUP_TYPE_OPTION_HOVER, "§d点击将竞技场设为 {type} 类型。");
        yml.addDefault(Messages.COMMAND_SETUP_TYPE_USAGE, "§9 ▪ §7用法：{command} {subcommand} <类型>");
        yml.addDefault(Messages.COMMAND_SETUP_UPGRADE_HOLOGRAM, "{team} §6升级商店已设置");
        yml.addDefault(Messages.COMMAND_SETUP_UPGRADE_HOVER, "设置队伍升级商店 NPC。");
        yml.addDefault(Messages.COMMAND_SETUP_UPGRADE_SET, "已设置队伍 {team} 的升级商店 NPC。");
        yml.addDefault(Messages.COMMAND_SETUP_UPGRADE_TEAM_HOVER, "§f队伍 {team} 的升级商店 NPC 已设置。");
        yml.addDefault(Messages.COMMAND_SETUP_WAITING_POS_OPTION, "§c ▪ §7/{command} waitingPos {position}");
        yml.addDefault(Messages.COMMAND_SETUP_WAITING_POS_OPTION_HOVER, "§d设置位置 {position}");
        yml.addDefault(Messages.COMMAND_SETUP_WAITING_POS_REMAINING, "§c ▪ §7设置剩余位置：");
        yml.addDefault(Messages.COMMAND_SETUP_WAITING_POS_SET, "§6 ▪ §7位置 {position} 已设置！");
        yml.addDefault(Messages.COMMAND_SETUP_WAITING_POS_TEAMS_REMINDER, "§6 ▪ §7请确认已设置队伍出生点！");
        yml.addDefault(Messages.COMMAND_SETUP_WAITING_POS_USAGE, "§c▪ §7用法：/{command} {subcommand} 1 或 2");
        yml.addDefault(Messages.COMMAND_SETUP_WAITING_SPAWN_SET, "§6 ▪ §7已设置 §e{arena}§7 的等待出生点！");
        yml.addDefault(Messages.COMMAND_SET_LOBBY_DISPLAY, "§6 ▪ §7/{command} setLobby{status}");
        yml.addDefault(Messages.COMMAND_SET_LOBBY_DISPLAY_HOVER, "§a设置主大厅。§f此项必需，但如果服务器运行在 §eBUNGEE §f模式下，大厅位置将§e不会§f使用。\n§e再次输入可替换旧出生点。");
        yml.addDefault(Messages.COMMAND_SET_LOBBY_IN_ARENA, "§6 ▪ §c此命令不能在竞技场使用，只能在主大厅使用！");
        yml.addDefault(Messages.COMMAND_SET_LOBBY_NOT_SET, " §c（未设置）");
        yml.addDefault(Messages.COMMAND_SET_LOBBY_SET, " §a（已设置）");
        yml.addDefault(Messages.COMMAND_SET_LOBBY_SUCCESS, "§6 ▪ §7大厅位置已设置！");
        yml.addDefault(Messages.COMMAND_TOGGLE_READY_DISPLAY, "§e/bw toggleReady §7- 开启或关闭准备确认功能。");
        yml.addDefault(Messages.COMMAND_ECHO_DISPLAY, "&6 ▪ &7/{command} {subcommand} &7- &e向当前竞技场发送消息");
        yml.addDefault(Messages.COMMAND_ECHO_HOVER, "&f向当前竞技场中的所有玩家发送消息。\n&f用法：/{command} echo <消息>");
        yml.addDefault(Messages.COMMAND_ECHO_PREFIX, "&7[&c管理员&7] &f");
        yml.addDefault(Messages.COMMAND_ECHOTO_DISPLAY, "&6 ▪ &7/{command} {subcommand} &7- &e向指定竞技场发送消息");
        yml.addDefault(Messages.COMMAND_ECHOTO_HOVER, "&f向指定竞技场中的所有玩家发送消息。\n&f用法：/{command} echoto <竞技场> <消息>");
        yml.addDefault(Messages.COMMAND_ECHOALL_DISPLAY, "&6 ▪ &7/{command} {subcommand} &7- &e向所有竞技场发送消息");
        yml.addDefault(Messages.COMMAND_ECHOALL_HOVER, "&f向所有竞技场的玩家发送消息。\n&f用法：/{command} echoall <消息>");
        yml.addDefault(Messages.COMMAND_FORCE_END_GAME_DISPLAY, "&6 ▪ &7/{command} {subcommand} &7 - &e强制结束竞技场");
        yml.addDefault(Messages.COMMAND_FORCE_END_GAME_DISPLAY_HOVER, "&f强制结束竞技场，不产生获胜者。\n&f权限：&c{permission}");
        yml.addDefault(Messages.COMMAND_FORCE_END_GAME_NOT_PLAYING, "&c游戏尚未开始。");
        yml.addDefault(Messages.COMMAND_FORCE_END_GAME_SUCCESS, "&a正在强制结束游戏……");
        yml.addDefault(Messages.COMMAND_FORCE_START_DISPLAY, "&6 ▪ &7/{command} {subcommand} &7 - &e强制开始竞技场");
        yml.addDefault(Messages.COMMAND_FORCE_START_DISPLAY_HOVER, "&f强制开始竞技场。\n&f权限：&c{permission}");
        yml.addDefault(Messages.COMMAND_START_DISPLAY, "&6 ▪ &7/{command} {subcommand} &7 - &e将开始倒计时缩短至 5 秒");
        yml.addDefault(Messages.COMMAND_START_DISPLAY_HOVER, "&f将竞技场的开始倒计时缩短至 5 秒。\n&f权限：&c{permission}");
        yml.addDefault(Messages.COMMAND_READY_DISPLAY, "&e/{command} {subcommand} &7- 切换准备状态。");
        yml.addDefault(Messages.COMMAND_GUI_DISPLAY, "&6 ▪ &7/{command} {subcommand}");
        yml.addDefault(Messages.COMMAND_GUI_DISPLAY_HOVER, "&f打开竞技场菜单。");
        yml.addDefault(Messages.COMMAND_JOIN_DISPLAY, "&6 ▪ &7/{command} {subcommand} &e<random/竞技场名/分组名>");
        yml.addDefault(Messages.COMMAND_JOIN_DISPLAY_HOVER, "&f按名称或分组加入竞技场。\n&f/{command} join random - 随机加入竞技场。");
        yml.addDefault(Messages.COMMAND_LANG_DISPLAY, "&6 ▪ &7/{command} {subcommand}");
        yml.addDefault(Messages.COMMAND_LANG_DISPLAY_HOVER, "&f切换你的语言。");
        yml.addDefault(Messages.COMMAND_LEAVE_DISPLAY, "&6 ▪ &7/{command} {subcommand}");
        yml.addDefault(Messages.COMMAND_LEAVE_DISPLAY_HOVER, "&f离开竞技场。");
        yml.addDefault(Messages.COMMAND_STATS_DISPLAY, "&6 ▪ &7/{command} {subcommand}");
        yml.addDefault(Messages.COMMAND_STATS_DISPLAY_HOVER, "&f打开战绩菜单。");
        yml.addDefault(Messages.COMMAND_SPEC_MODE_DISPLAY, "&6 ▪ &7/{command} specmode");
        yml.addDefault(Messages.COMMAND_SPEC_MODE_DISPLAY_HOVER, "&f切换原版旁观模式与插件旁观模式。");
        yml.addDefault(Messages.COMMAND_ADMIN_HEADER, "&7&l{dot} &6{plugin} v{version} &7- &c 管理员命令");
        yml.addDefault(Messages.COMMAND_CONSOLE_SAFEMODE, "&f   bw safemode &e启用/禁用");
        yml.addDefault(Messages.COMMAND_CONSOLE_NO_COMMANDS, "&f目前没有可用的控制台命令。");
        yml.addDefault(Messages.COMMAND_LOBBY_NOT_SET, "&c❌ &7请先设置大厅位置！");
        yml.addDefault(Messages.ARENA_UNEXPECTED_PLAYER_KICK, "你不应出现在这里。");
        yml.addDefault(Messages.COMMAND_LIST_DISPLAY, "&6 ▪ &7/{command} {subcommand}         &7 - &e 查看玩家命令");
        yml.addDefault(Messages.COMMAND_LIST_DISPLAY_HOVER, "&f查看玩家命令。");
        yml.addDefault(Messages.COMMAND_LIST_SETUP_HEADER, "&7&l{dot}&6{plugin} v{version}&7- &a{world} 命令");
        yml.addDefault(Messages.COMMAND_LIST_CREDITS, "&9&l{dot} &6{plugin} &7v{version} 由 andrei1058 开发");
        yml.addDefault(Messages.COMMAND_LIST_CREDITS_HOVER, "&7竞技场：{arenaColor}{arenas}");
        yml.addDefault(Messages.COMMAND_LIST_NOT_SET, "&c（未设置）");
        yml.addDefault(Messages.COMMAND_LIST_SET, "&a（已设置）");
        yml.addDefault(Messages.COMMAND_LIST_OPTIONAL, "&7&o可选");
        yml.addDefault(Messages.COMMAND_LIST_ALL_SET, "&a（全部已设置）");
        yml.addDefault(Messages.COMMAND_LIST_REMAINING, "&c（剩余：{remaining}&c）");
        yml.addDefault(Messages.COMMAND_LIST_POS1_NOT_SET, "&c（位置 1 未设置）");
        yml.addDefault(Messages.COMMAND_LIST_POS2_NOT_SET, "&c（位置 2 未设置）");
        yml.addDefault(Messages.COMMAND_LIST_WAITING_SPAWN, "{dot}{strike}setWaitingSpawn&r {status}");
        yml.addDefault(Messages.COMMAND_LIST_WAITING_POS, "{dot}{strike}waitingPos 1/2&r {status}");
        yml.addDefault(Messages.COMMAND_LIST_SET_SPAWN, "{dot}{strike}setSpawn <队伍名>&r {status}");
        yml.addDefault(Messages.COMMAND_LIST_SET_BED, "{dot}{strike}setBed&r {status}");
        yml.addDefault(Messages.COMMAND_LIST_SET_SHOP, "{dot}{strike}setShop&r {status}");
        yml.addDefault(Messages.COMMAND_LIST_SET_KILL_DROPS, "{dot}{strike}setKillDrops&r {status}");
        yml.addDefault(Messages.COMMAND_LIST_SET_UPGRADE, "{dot}{strike}setUpgrade&r {status}");
        yml.addDefault(Messages.COMMAND_LIST_ADD_GENERATOR, "{dot}addGenerator {remaining}&e(&a绿宝石{iron} &b钻石{gold}&e)");
        yml.addDefault(Messages.COMMAND_LIST_SET_SPECTATOR_SPAWN, "{dot}{strike}setSpectSpawn&r {status}");
        yml.addDefault(Messages.COMMAND_LIST_AUTO_CREATE_TEAMS, "{dot}autoCreateTeams &e（自动识别）");
        yml.addDefault(Messages.COMMAND_LIST_CREATE_TEAM, "{dot}createTeam <名称> <颜色> &e（已创建 {teams} 队）");
        yml.addDefault(Messages.COMMAND_LIST_REMOVE_TEAM, "{dot}removeTeam <名称>");
        yml.addDefault(Messages.COMMAND_LIST_REMOVE_GENERATOR, "{dot}removeGenerator");
        yml.addDefault(Messages.COMMAND_LIST_SET_MAX_IN_TEAM, "{dot}setMaxInTeam <人数> （当前：{size}）");
        yml.addDefault(Messages.COMMAND_LIST_ARENA_GROUP, "{dot}arenaGroup {group}");
        yml.addDefault(Messages.COMMAND_LIST_SET_TYPE, "{dot}setType <类型> {group}");
        yml.addDefault(Messages.COMMAND_LIST_SAVE, "{dot}save");
        yml.addDefault(Messages.COMMAND_LIST_HOVER_WAITING_SPAWN, "&f设置玩家在游戏开始前的等待位置。");
        yml.addDefault(Messages.COMMAND_LIST_HOVER_WAITING_POS, "&f设置游戏开始后消失的等待大厅区域。\n&f请按 WorldEdit 区域选择方式选取。");
        yml.addDefault(Messages.COMMAND_LIST_HOVER_SPECTATOR_SPAWN, "&f设置旁观者出生点。");
        yml.addDefault(Messages.COMMAND_LIST_HOVER_AUTO_CREATE_TEAMS, "&f根据岛屿颜色自动创建队伍。");
        yml.addDefault(Messages.COMMAND_LIST_HOVER_CREATE_TEAM, "&f创建队伍。");
        yml.addDefault(Messages.COMMAND_LIST_HOVER_REMOVE_TEAM, "&f按名称删除队伍。");
        yml.addDefault(Messages.COMMAND_LIST_HOVER_SET_SPAWN, "&f设置队伍出生点。\n&f尚未设置出生点的队伍：\n{spawnNames}");
        yml.addDefault(Messages.COMMAND_LIST_HOVER_SET_BED, "&f设置队伍床的位置。\n&f无需指定队伍名称。");
        yml.addDefault(Messages.COMMAND_LIST_HOVER_SET_SHOP, "&f设置队伍商店 NPC。\n&f无需指定队伍名称。\n&f仅在游戏开始后生成。");
        yml.addDefault(Messages.COMMAND_LIST_HOVER_SET_UPGRADE, "&f设置队伍升级 NPC。\n&f无需指定队伍名称。\n&f仅在游戏开始后生成。");
        yml.addDefault(Messages.COMMAND_LIST_HOVER_SET_KILL_DROPS, "&f设置击杀敌人后掉落物品的位置。");
        yml.addDefault(Messages.COMMAND_LIST_HOVER_ADD_GENERATOR_ADVANCED, "&f添加生成器出生点。\n&e/{command} addGenerator <Iron/ Gold/ Emerald, Diamond>\n&f站在钻石块上设置钻石生成器。\n&f站在绿宝石块上设置绿宝石生成器。");
        yml.addDefault(Messages.COMMAND_LIST_HOVER_ADD_GENERATOR_ASSISTED, "&f添加生成器出生点。\n&e站在队伍岛屿上设置队伍生成器。\n&f站在钻石块上设置钻石生成器。\n&f站在绿宝石块上设置绿宝石生成器。");
        yml.addDefault(Messages.COMMAND_LIST_HOVER_SET_MAX_IN_TEAM, "&f设置每队最大人数。");
        yml.addDefault(Messages.COMMAND_LIST_HOVER_ARENA_GROUP, "&f设置竞技场分组。");
        yml.addDefault(Messages.COMMAND_LIST_HOVER_SET_TYPE, "&f将竞技场加入分组。");
        yml.addDefault(Messages.COMMAND_LIST_HOVER_SAVE, "&f保存竞技场并返回大厅。");
        yml.addDefault(Messages.ARENA_LOAD_ALREADY_QUEUED, "&c尝试加载竞技场 {arena}，但它已在启用队列中。");
        yml.addDefault(Messages.ARENA_LOAD_ALREADY_ENABLED, "&c尝试加载竞技场 {arena}，但它已启用。");
        yml.addDefault(Messages.ARENA_LOAD_NO_TEAMS, "竞技场 {arena} 尚未设置任何队伍。");
        yml.addDefault(Messages.ARENA_LOAD_NOT_ENOUGH_TEAMS, "&c竞技场 {arena} 至少需要设置两个队伍。");
        yml.addDefault(Messages.ARENA_LOAD_WORLD_MISSING, "&c未找到名为 {arena} 的地图。");
        yml.addDefault(Messages.ARENA_LOAD_INVALID_TEAM_COLOR, "&c竞技场 {arena} 的队伍 {team} 颜色无效。");
        yml.addDefault(Messages.ARENA_LOAD_TEAM_SETTING_MISSING, "&c竞技场 {arena} 的队伍 {team} 尚未设置 {setting}。");
        yml.addDefault(Messages.ARENA_LOAD_DIAMOND_GENERATOR_MISSING, "&c竞技场 {arena} 尚未设置钻石生成器。");
        yml.addDefault(Messages.ARENA_LOAD_EMERALD_GENERATOR_MISSING, "&c竞技场 {arena} 尚未设置绿宝石生成器。");
        yml.addDefault(Messages.ARENA_LOAD_WAITING_SPAWN_MISSING, "&c竞技场 {arena} 尚未设置等待出生点。");
        yml.addDefault(Messages.SETUP_GUI_TITLE, "&8选择设置方式");
        yml.addDefault(Messages.SETUP_GUI_ASSISTED_NAME, "&e&l引导式设置");
        yml.addDefault(Messages.SETUP_GUI_ASSISTED_LORE, Arrays.asList("", "&a简单快速地设置！", "&7适合新手和想省事的管理员 :D", "", "&3精简设置选项。"));
        yml.addDefault(Messages.SETUP_GUI_ADVANCED_NAME, "&c&l高级设置");
        yml.addDefault(Messages.SETUP_GUI_ADVANCED_LORE, Arrays.asList("", "&a详细设置！", "&7适合熟悉配置的管理员 :D", "", "&3更多高级选项。"));
        yml.addDefault(Messages.SETUP_LOADING, "&6 ▪ &7正在加载 {arena}");
        yml.addDefault(Messages.SETUP_CANCELLED, "&6 ▪ &7已取消设置竞技场 {arena}！");
        yml.addDefault(Messages.SETUP_TELEPORTED, "&a已将你传送到 &6{arena}&a 的出生点。");
        yml.addDefault(Messages.SETUP_GREETING, "&a你好，{player}！");
        yml.addDefault(Messages.SETUP_WAITING_SPAWN_PROMPT, "&f请设置等待出生点。");
        yml.addDefault(Messages.SETUP_WAITING_SPAWN_EXPLANATION, "&f玩家将在此等待游戏开始。");
        yml.addDefault(Messages.SETUP_WAITING_SPAWN_CLICK, "&9     ▪     &6点击此处设置等待大厅    &9 ▪");
        yml.addDefault(Messages.SETUP_WAITING_SPAWN_HOVER, "&d点击设置等待出生点。");
        yml.addDefault(Messages.SETUP_COMMAND_LIST_HINT, "&e或输入：&7/{command} 查看命令列表。");
        yml.addDefault(Messages.SETUP_COMMAND_LIST_HOVER, "&f查看命令列表。");
        yml.addDefault(Messages.SETUP_TEAM_GENERATOR_MARKER, "&6已为队伍 {teamColor}{team} 添加 {generator} 生成器。");
        yml.addDefault(Messages.SETUP_TEAM_SPAWN_MARKER, "{teamColor}{team} &6出生点已设置");
        yml.addDefault(Messages.SETUP_TEAM_BED_MARKER, "{teamColor}{team} &6床已设置");
        yml.addDefault(Messages.SETUP_TEAM_SHOP_MARKER, "{teamColor}{team} &6商店已设置");
        yml.addDefault(Messages.SETUP_TEAM_UPGRADE_MARKER, "{teamColor}{team} &6升级商店已设置");
        yml.addDefault(Messages.SETUP_TEAM_KILL_DROPS_MARKER, "&6击杀掉落物 {team}");
        yml.addDefault(Messages.SETUP_GENERATOR_MARKER, "&6{generator} 已设置");
        yml.addDefault(Messages.SETUP_GENERATOR_IRON, "铁");
        yml.addDefault(Messages.SETUP_GENERATOR_GOLD, "金");
        yml.addDefault(Messages.SETUP_GENERATOR_EMERALD, "绿宝石");
        yml.addDefault(Messages.SETUP_GENERATOR_DIAMOND, "钻石");
        yml.addDefault(Messages.SETUP_AVAILABLE_TEAMS, "可用队伍：");
        yml.addDefault(Messages.SETUP_START_FAILED, "&c无法启动设置流程。请检查控制台。");
        yml.addDefault(Messages.SIGN_SAVED, "&a▪ &7已保存竞技场 {arena} 的告示牌。");
        yml.addDefault(Messages.SIGN_NO_ARENAS, "&c▪ &7你还没有设置任何竞技场！");
        yml.addDefault(Messages.PLUGIN_RELOAD_KICK, "HMO-Bedwars 已重新加载！请勿重新加载插件。");

        yml.addDefault(Messages.COMMAND_ECHO_USAGE, "{prefix}&c用法：/bw echo <消息>");
        yml.addDefault(Messages.COMMAND_ECHO_SENT, "{prefix}&a消息已发送至竞技场 {arena}");
        yml.addDefault(Messages.COMMAND_ECHO_NOT_IN_ARENA, "{prefix}&c你不在一个竞技场中。");
        yml.addDefault(Messages.COMMAND_ECHOTO_USAGE, "{prefix}&c用法：/bw echoto <竞技场> <消息>");
        yml.addDefault(Messages.COMMAND_ECHOTO_SENT, "{prefix}&a消息已发送至竞技场 {arena}");
        yml.addDefault(Messages.COMMAND_ECHOALL_USAGE, "{prefix}&c用法：/bw echoall <消息>");
        yml.addDefault(Messages.COMMAND_ECHOALL_SENT, "{prefix}&a消息已发送至所有竞技场。");
        yml.addDefault(Messages.COMMAND_ARENA_NOT_FOUND, "{prefix}&c未找到竞技场：{arena}");
        yml.addDefault(Messages.COMMAND_NOT_PLAYER, "{prefix}&c该命令仅限玩家使用。");
        yml.addDefault(Messages.COMMAND_LANG_LIST_HEADER, "{prefix} &a可用的语言：");
        yml.addDefault(Messages.COMMAND_LANG_LIST_FORMAT, "&a▪  &7{iso} - &f{name}");
        yml.addDefault(Messages.COMMAND_LANG_USAGE, "{prefix}&7用法：/lang &f&o<iso>");
        yml.addDefault(Messages.COMMAND_LANG_SELECTED_NOT_EXIST, "{prefix}&c该语言不存在！");
        yml.addDefault(Messages.COMMAND_LANG_SELECTED_SUCCESSFULLY, "{prefix}&a语言已设置！");
        yml.addDefault(Messages.COMMAND_LANG_USAGE_DENIED, "{prefix}&c你不能在游戏进行时修改语言。");
        yml.addDefault(Messages.COMMAND_READY_SUCCESS, "{prefix}&a你现在准备好了！");
        yml.addDefault(Messages.COMMAND_UNREADY_SUCCESS, "{prefix}&e你取消了准备。");
        yml.addDefault(Messages.COMMAND_READY_COUNTDOWN_SHORTENED, "{prefix}&a所有玩家都已准备！倒计时缩短至10秒！");
        yml.addDefault(Messages.COMMAND_TOGGLE_READY_ENABLED, "{prefix}&a准备确认功能已启用。");
        yml.addDefault(Messages.COMMAND_TOGGLE_READY_DISABLED, "{prefix}&c准备确认功能已禁用。");
        yml.addDefault(Messages.COMMAND_READY_DISABLED, "{prefix}&c该命令已禁用。");
        yml.addDefault(Messages.COMMAND_READY_NOT_IN_GAME, "{prefix}&c你不在游戏中！");
        yml.addDefault(Messages.COMMAND_READY_NOT_WAITING, "{prefix}&c你现在不能使用此命令。");
        yml.addDefault(Messages.COMMAND_READY_SPECTATOR, "{prefix}&c旁观者不能使用此命令。");
        yml.addDefault(Messages.COMMAND_READY_ITEM_NAME_READY, "&a准备");
        yml.addDefault(Messages.COMMAND_READY_ITEM_NAME_NOT_READY, "&c未准备");
        yml.addDefault(Messages.COMMAND_READY_ITEM_LORE_READY, Arrays.asList("&7右键点击以准备！"));
        yml.addDefault(Messages.COMMAND_READY_ITEM_LORE_NOT_READY, Arrays.asList("&7右键点击以取消准备！"));

        yml.addDefault(Messages.COMMAND_JOIN_USAGE, "&a▪ &7用法：/" + BedWars.mainCmd + " join &o<游戏/模式>");
        yml.addDefault(Messages.COMMAND_JOIN_GROUP_OR_ARENA_NOT_FOUND, "{prefix}&c游戏{name}不存在！");
        yml.addDefault(Messages.COMMAND_JOIN_DENIED_IS_FULL, "{prefix}&c游戏已满！\n&a请考虑赞助以支持我们！ &7&o(点击查看)");
        yml.addDefault(Messages.COMMAND_JOIN_NO_EMPTY_FOUND, "{prefix}&c现在没有可用的游戏:(");
        yml.addDefault(Messages.COMMAND_JOIN_DENIED_IS_FULL_OF_VIPS, "{prefix}&c很抱歉，虽然我们已知道你已赞助，但该游戏已满。\n&c此游戏中全是赞助者或管理员。");
        yml.addDefault(Messages.COMMAND_JOIN_DENIED_PARTY_TOO_BIG, "{prefix}&c你的队伍人数太多了，不能作为一个队伍加入该游戏:(");
        yml.addDefault(Messages.COMMAND_JOIN_DENIED_NOT_PARTY_LEADER, "{prefix}&c只有队长才能选择游戏。");
        yml.addDefault(Messages.COMMAND_JOIN_PLAYER_JOIN_MSG, "{prefix}&7{player}&e加入了游戏(&b{on}&e/&b{max}&e)！");
        yml.addDefault(Messages.COMMAND_JOIN_SPECTATOR_MSG, "{prefix}&6你正在观战&9{arena}&6。\n{prefix}&e输入 &c/leave &e离开。");
        yml.addDefault(Messages.COMMAND_JOIN_SPECTATOR_DENIED_MSG, "&c该游戏不允许旁观！");
        yml.addDefault(Messages.COMMAND_TP_PLAYER_NOT_FOUND, "{prefix}&c无法找到这位玩家！");
        yml.addDefault(Messages.COMMAND_TP_NOT_IN_ARENA, "{prefix}&c该玩家不在任何一场起床战争游戏中！");
        yml.addDefault(Messages.COMMAND_TP_NOT_STARTED, "{prefix}&c该玩家所在的游戏还没开始！");
        yml.addDefault(Messages.COMMAND_TP_USAGE, "{prefix}&c用法：/bw tp <玩家名>");
        yml.addDefault(Messages.REJOIN_NO_ARENA, "{prefix}&c没有可以重新加入的游戏！");
        yml.addDefault(Messages.REJOIN_DENIED, "{prefix}&c由于你所属队伍的床被破坏或游戏已经结束，你不能重新加入。");
        yml.addDefault(Messages.REJOIN_ALLOWED, "{prefix}&e正在重新加入&a{arena}&e！");
        yml.addDefault(Messages.COMMAND_REJOIN_PLAYER_RECONNECTED, "{prefix}&7{player}&e重新连接。");
        yml.addDefault(Messages.COMMAND_LEAVE_DENIED_NOT_IN_ARENA, "{prefix}&c你不在一场起床战争游戏中！");
        yml.addDefault(Messages.COMMAND_LEAVE_MSG, "{prefix}&7{player}&e离开了！");
        yml.addDefault(Messages.COMMAND_NOT_ALLOWED_IN_GAME, "{prefix}&c你在游戏中不可以这么做。");
        yml.addDefault(Messages.COMMAND_NOT_FOUND_OR_INSUFF_PERMS, "{prefix}&c指令无效或你没有权限！");
        yml.addDefault(Messages.COMMAND_PARTY_HELP, Arrays.asList("&6▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "&a队伍指令：",
                "&e/party help &7- &b查看该提示",
                "&e/party invite <玩家> &7- &b邀请一位玩家到你的队伍",
                "&e/party leave &7- &b离开当前队伍",
                "&e/party info &7- &b查看队伍成员和队长",
                "&e/party promote <玩家> &7- &b转让队长身份",
                "&e/party remove <玩家> &7- &b将玩家移出队伍",
                "&e/party accept <玩家> &7- &b接受队伍邀请",
                "&e/party disband &7- &b解散队伍")
        );
        yml.addDefault(Messages.COMMAND_PARTY_INVITE_USAGE, "{prefix}&e用法：&7/party invite <玩家>");
        yml.addDefault(Messages.COMMAND_PARTY_INVITE_SENT, "{prefix}&e已向&7{player}&e发送邀请&6。");
        yml.addDefault(Messages.COMMAND_PARTY_INVITE_SENT_TARGET_RECEIVE_MSG, "{prefix}&b{player}&e邀请你加入队伍！ &o&7(点击接受)");
        yml.addDefault(Messages.COMMAND_PARTY_INVITE_DENIED_CANNOT_INVITE_YOURSELF, "{prefix}&c你不可以邀请你自己！");
        yml.addDefault(Messages.COMMAND_PARTY_INVITE_DENIED_PLAYER_OFFLINE, "{prefix}&7{player}&e不在线！");
        yml.addDefault(Messages.COMMAND_PARTY_ACCEPT_DENIED_NO_INVITE, "{prefix}&c没有可以接受的队伍邀请！");
        yml.addDefault(Messages.COMMAND_PARTY_ACCEPT_DENIED_ALREADY_IN_PARTY, "{prefix}&e你已经在队伍中了！");
        yml.addDefault(Messages.COMMAND_PARTY_INSUFFICIENT_PERMISSIONS, "{prefix}&c只有队长才可以这么做！");
        yml.addDefault(Messages.COMMAND_PARTY_ACCEPT_USAGE, "{prefix}&e用法：&7/party accept <玩家>");
        yml.addDefault(Messages.COMMAND_PARTY_ACCEPT_SUCCESS, "{prefix}&7{player}&e加入了队伍！");
        yml.addDefault(Messages.COMMAND_PARTY_GENERAL_DENIED_NOT_IN_PARTY, "{prefix}&c你不在队伍中！");
        yml.addDefault(Messages.COMMAND_PARTY_LEAVE_DENIED_IS_OWNER_NEEDS_DISBAND, "{prefix}&c你不可以离开由你带领的队伍！\n&e使用：&b/party disband &e来解散队伍。");
        yml.addDefault(Messages.COMMAND_PARTY_LEAVE_SUCCESS, "{prefix}&7{player}&e离开了队伍！");
        yml.addDefault(Messages.COMMAND_PARTY_DISBAND_SUCCESS, "{prefix}&e队伍已解散！");
        yml.addDefault(Messages.COMMAND_PARTY_REMOVE_USAGE, "{prefix}&7用法：&e/party remove <玩家>");
        yml.addDefault(Messages.COMMAND_PARTY_REMOVE_SUCCESS, "{prefix}&7{player}&e被移出了队伍。");
        yml.addDefault(Messages.COMMAND_PARTY_REMOVE_DENIED_TARGET_NOT_PARTY_MEMBER, "{prefix}&7{player}&e不在你的队伍中！");
        yml.addDefault(Messages.COMMAND_PARTY_PROMOTE_SUCCESS, "{prefix}&e你已将 {player} 设为队长");
        yml.addDefault(Messages.COMMAND_PARTY_PROMOTE_OWNER, "{prefix}&e你已成为队长");
        yml.addDefault(Messages.COMMAND_PARTY_PROMOTE_NEW_OWNER, "{prefix}&7 &e{player} 已成为队长");
        yml.addDefault(Messages.COMMAND_PARTY_INFO_OWNER, "\n{prefix}&e队长：&7{owner}");
        yml.addDefault(Messages.COMMAND_PARTY_INFO_PLAYERS,"{prefix}&e队伍成员：");
        yml.addDefault(Messages.COMMAND_PARTY_INFO_PLAYER, "&7{player}");
        yml.addDefault(Messages.COMMAND_FORCESTART_NOT_IN_GAME, "&c▪ &7你不在游戏中！");
        yml.addDefault(Messages.COMMAND_START_NOT_ENOUGH_PLAYERS, "&c▪ &7玩家人数不足以开始游戏！");
        yml.addDefault(Messages.COMMAND_FORCESTART_SUCCESS, "&c▪ &7游戏开始倒计时缩短！");
        yml.addDefault(Messages.COMMAND_FORCESTART_NO_PERM, "{prefix}&7你不可以强制开始游戏！\n&7请考虑赞助以得到对应权限！");
        yml.addDefault(Messages.COMMAND_COOLDOWN, "&c你不能这么做！ 请等待 {seconds} 秒！");
        yml.addDefault(Messages.COMMAND_SPECTATOR_MODE_SWITCH_VANILLA, "{prefix}&a已切换到原版旁观模式。");
        yml.addDefault(Messages.COMMAND_SPECTATOR_MODE_SWITCH_NORMAL, "{prefix}&a已切换到普通观战模式。");
        yml.addDefault(Messages.ARENA_JOIN_VIP_KICK, "{prefix}&c抱歉，由于有一位赞助者加入该游戏，因此你被移出了该游戏。\n&a请考虑赞助以支持我们！ &7&o(点击查看)");
        yml.addDefault(Messages.ARENA_START_COUNTDOWN_STOPPED_INSUFF_PLAYERS_CHAT, "{prefix}&c玩家不足！ 倒计时取消！");
        yml.addDefault(Messages.ARENA_RESTART_PLAYER_KICK, "{prefix}&e当前游戏正在重启。");
        yml.addDefault(Messages.ARENA_STATUS_PLAYING_NAME, "&c游戏中");
        yml.addDefault(Messages.ARENA_STATUS_RESTARTING_NAME, "&c重启中");
        yml.addDefault(Messages.ARENA_STATUS_WAITING_NAME, "&b等待中 &c{full}");
        yml.addDefault(Messages.ARENA_STATUS_STARTING_NAME, "&6即将开始 &c{full}");
        yml.addDefault(Messages.ARENA_GUI_INV_NAME, "&8点击加入");
        yml.addDefault(Messages.ARENA_GUI_ARENA_CONTENT_NAME, "&a&l{name}");
        yml.addDefault(Messages.ARENA_GUI_ARENA_CONTENT_LORE, Arrays.asList("", "&7状态：{status}", "&7玩家数：&f{on}&7/&f{max}", "&7观赛中：&f{spectators}", "&7模式：&a{group}", "", "&a左击加入", "&e右击观赛"));
        yml.addDefault(Messages.ARENA_GUI_SKIPPED_ITEM_NAME, "&r{serverIp}");
        yml.addDefault(Messages.ARENA_GUI_SKIPPED_ITEM_LORE, Collections.emptyList());
        yml.addDefault(Messages.ARENA_STATUS_START_COUNTDOWN_CHAT, "{prefix}&e游戏将在 &6{time}&e 秒后开始！");
        yml.addDefault(Messages.ARENA_STATUS_START_COUNTDOWN_TITLE, " ");
        yml.addDefault(Messages.ARENA_STATUS_START_COUNTDOWN_SUB_TITLE, "&a{second}");
        yml.addDefault(Messages.ARENA_STATUS_START_COUNTDOWN_SUB_TITLE + "-5", "&e❺");
        yml.addDefault(Messages.ARENA_STATUS_START_COUNTDOWN_SUB_TITLE + "-4", "&e❹");
        yml.addDefault(Messages.ARENA_STATUS_START_COUNTDOWN_SUB_TITLE + "-3", "&c❸");
        yml.addDefault(Messages.ARENA_STATUS_START_COUNTDOWN_SUB_TITLE + "-2", "&c❷");
        yml.addDefault(Messages.ARENA_STATUS_START_COUNTDOWN_SUB_TITLE + "-1", "&c❶");
        yml.addDefault(Messages.ARENA_STATUS_START_COUNTDOWN_CANCELLED_TITLE, " ");
        yml.addDefault(Messages.ARENA_STATUS_START_COUNTDOWN_CANCELLED_SUB_TITLE, "&c等待更多玩家……");
        yml.addDefault(Messages.ARENA_STATUS_START_PLAYER_TITLE, "&a游戏开始");
        yml.addDefault(Messages.ARENA_STATUS_START_PLAYER_TUTORIAL, Arrays.asList(
                "&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "&f                                  &l起床战争", "",
                "&e&l                            保护你的床并破坏敌人的床，",
                "&e&l                        从资源点收集铁锭、金锭、",
                "&e&l                        绿宝石和钻石，",
                "&e&l                        来购买强力装备和进行升级来使自己变强！", "",
                "&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        yml.addDefault(Messages.ARENA_JOIN_DENIED_SELECTOR, "{prefix}&c抱歉，你现在不能加入该游戏。右键来观赛！");
        yml.addDefault(Messages.ARENA_SPECTATE_DENIED_SELECTOR, "{prefix}&c抱歉，你现在不能观赛。左键来加入游戏！");
        yml.addDefault(Messages.ARENA_JOIN_DENIED_NO_PROXY, "&c抱歉，你必须通过 BedWarsProxy 来加入游戏。 \n&e如果你想设置游戏，你可以给予你自己 bw.setup 权限来直接进入服务器！");
        yml.addDefault(Messages.ARENA_SPECTATOR_TELEPORTER_GUI_NAME, "&8传送");
        yml.addDefault(Messages.ARENA_SPECTATOR_TELEPORTER_GUI_HEAD_NAME, "{teamColor}{team} - {player}");
        yml.addDefault(Messages.ARENA_SPECTATOR_TELEPORTER_GUI_HEAD_LORE, Arrays.asList("&7生命值：&f{health}%", "&7饱食度：&f{food}", "", "&7左键传送"));
        yml.addDefault(Messages.ARENA_SPECTATOR_LEAVE_ITEM_NAME, "&c&l回到大厅");
        yml.addDefault(Messages.ARENA_SPECTATOR_LEAVE_ITEM_LORE, Collections.singletonList("&7右键返回大厅！"));
        yml.addDefault(Messages.ARENA_SPECTATOR_FIRST_PERSON_ENTER_TITLE, "&a正在旁观&7{player}");
        yml.addDefault(Messages.ARENA_SPECTATOR_FIRST_PERSON_ENTER_SUBTITLE, "&c潜行以退出！");
        yml.addDefault(Messages.ARENA_SPECTATOR_FIRST_PERSON_LEAVE_TITLE, "&e退出旁观者模式");
        yml.addDefault(Messages.ARENA_SPECTATOR_FIRST_PERSON_LEAVE_SUBTITLE, "");
        yml.addDefault(Messages.ARENA_LEAVE_PARTY_DISBANDED, "{prefix}&c由于队长离开了，队伍解散！");
        yml.addDefault(Messages.GENERATOR_HOLOGRAM_TIER, "&e等级&c{tier}");
        yml.addDefault(Messages.GENERATOR_HOLOGRAM_TYPE_DIAMOND, "&b&l钻石");
        yml.addDefault(Messages.GENERATOR_HOLOGRAM_TYPE_EMERALD, "&a&l绿宝石");
        yml.addDefault(Messages.GENERATOR_HOLOGRAM_TIMER, "&c{seconds}&e 秒后生成");
        yml.addDefault(Messages.GENERATOR_UPGRADE_CHAT_ANNOUNCEMENT, "{prefix}{generatorType}资源点&e升级到&c{tier}级。");
        yml.addDefault(Messages.FORMATTING_CHAT_LOBBY, "&7[大厅] &a{player}&f：{message}");
        yml.addDefault(Messages.FORMATTING_CHAT_WAITING, "&7[等待] &a{player}&f：{message}");
        yml.addDefault(Messages.FORMATTING_CHAT_SHOUT, "&6[公屏] {team} &a{player}&f：{message}");
        yml.addDefault(Messages.FORMATTING_CHAT_SERVER, "&6[全服] &a{player}&f：{message}");
        yml.addDefault(Messages.CHAT_GLOBAL_USAGE, "{prefix}&c请在 @ 后输入要发送到全服的消息。");
        yml.addDefault(Messages.FORMATTING_CHAT_TEAM, "&b[队内] {team} &a{player}&f：{message}");
        yml.addDefault(Messages.FORMATTING_CHAT_SPECTATOR, "&7[旁观] &a{player}&f：{message}");
        yml.addDefault(Messages.FORMATTING_SCOREBOARD_HEALTH, Arrays.asList("&c❤", "&a生命值"));

        yml.addDefault(Messages.FORMATTING_SCOREBOARD_DATE, "yy/MM/dd");
        yml.addDefault(Messages.FORMATTING_SCOREBOARD_TEAM_GENERIC, "{TeamColor}{TeamLetter}&f {TeamName}：{TeamStatus}");
        yml.addDefault(Messages.FORMATTING_SCOREBOARD_TEAM_ELIMINATED, "&c&l✘");
        yml.addDefault(Messages.FORMATTING_SCOREBOARD_BED_DESTROYED, "&a{remainingPlayers}");
        yml.addDefault(Messages.FORMATTING_SCOREBOARD_TEAM_ALIVE, "&a&l✓");
        yml.addDefault(Messages.FORMATTING_SCOREBOARD_NEXEVENT_TIMER, "mm:ss");
        yml.addDefault(Messages.FORMATTING_SCOREBOARD_YOUR_TEAM, "&7 你");
        yml.addDefault(Messages.FORMATTING_ACTION_BAR_TRACKING, "&f正在追踪：{team} &f- 距离：{distance}m");
        yml.addDefault(Messages.FORMATTING_TEAM_WINNER_FORMAT, "      {TeamColor}{TeamName} &7- {members}");
        yml.addDefault(Messages.FORMATTING_SOLO_WINNER_FORMAT, "                 {TeamColor}{TeamName} &7- {members}");
        yml.addDefault(Messages.FORMATTING_GENERATOR_TIER1, "I");
        yml.addDefault(Messages.FORMATTING_GENERATOR_TIER2, "II");
        yml.addDefault(Messages.FORMATTING_GENERATOR_TIER3, "III");
        yml.addDefault(Messages.FORMATTING_DESPAWNABLE_UTILITY_NPC_HEALTH, "▮ ");
        yml.addDefault(Messages.FORMATTING_STATS_DATE_FORMAT, "yyyy/MM/dd HH:mm");
        yml.addDefault(Messages.FORMAT_PAPI_PLAYER_TEAM_TEAM, "{TeamColor}[{TeamName}]");
        yml.addDefault(Messages.FORMAT_PAPI_PLAYER_TEAM_SHOUT, "&6[公屏]");
        yml.addDefault(Messages.FORMAT_PAPI_PLAYER_TEAM_SPECTATOR, "&7[旁观者]");
        yml.addDefault(Messages.MEANING_FULL, "已满");
        yml.addDefault(Messages.MEANING_SHOUT, "公屏");
        yml.addDefault(Messages.MEANING_NOBODY, "无玩家");
        yml.addDefault(Messages.MEANING_NEVER, "从不");
        yml.addDefault(Messages.MEANING_IRON_SINGULAR, "铁锭");
        yml.addDefault(Messages.MEANING_IRON_PLURAL, "铁锭");
        yml.addDefault(Messages.MEANING_GOLD_SINGULAR, "金锭");
        yml.addDefault(Messages.MEANING_GOLD_PLURAL, "金锭");
        yml.addDefault(Messages.MEANING_EMERALD_SINGULAR, "绿宝石");
        yml.addDefault(Messages.MEANING_EMERALD_PLURAL, "绿宝石");
        yml.addDefault(Messages.MEANING_DIAMOND_SINGULAR, "钻石");
        yml.addDefault(Messages.MEANING_DIAMOND_PLURAL, "钻石");
        yml.addDefault(Messages.INTERACT_CANNOT_PLACE_BLOCK, "{prefix}&c你不能在这里放置方块！");
        yml.addDefault(Messages.INTERACT_CANNOT_BREAK_BLOCK, "{prefix}&c你只能破坏由玩家放置的方块！");
        yml.addDefault(Messages.INTERACT_CANNOT_BREAK_OWN_BED, "&c你不能破坏自己的床！");
        yml.addDefault(Messages.INTERACT_BED_DESTROY_CHAT_ANNOUNCEMENT, "\n&f&l床被破坏 > {TeamColor}{TeamName}的床&7被{PlayerColor}{PlayerName}&7破坏了！\n");
        yml.addDefault(Messages.INTERACT_BED_DESTROY_TITLE_ANNOUNCEMENT, "&c床被破坏！");
        yml.addDefault(Messages.INTERACT_BED_DESTROY_SUBTITLE_ANNOUNCEMENT, "&f你不能再重生！");
        yml.addDefault(Messages.INTERACT_BED_DESTROY_CHAT_ANNOUNCEMENT_TO_VICTIM, "&f&l床被破坏 > &7你的床被{PlayerColor}{PlayerName}&7破坏了！\n");
        yml.addDefault(Messages.INTERACT_CHEST_CANT_OPEN_TEAM_ELIMINATED, "&c此队伍还未被团灭，因此你不能打开该团队箱子！");
        yml.addDefault(Messages.INTERACT_INVISIBILITY_REMOVED_DAMGE_TAKEN, "&c你因受到伤害而被迫退出隐身！");
        yml.addDefault(Messages.PLAYER_DIE_VOID_FALL_REGULAR_KILL, "{PlayerColor}{PlayerName}&7掉进了虚空。");
        yml.addDefault(Messages.PLAYER_DIE_VOID_FALL_FINAL_KILL, "{PlayerColor}{PlayerName}&7掉进了虚空。 &b&l最终击杀！");
        yml.addDefault(Messages.PLAYER_DIE_KNOCKED_IN_VOID_REGULAR_KILL, "{PlayerColor}{PlayerName}&7被{KillerColor}{KillerName}&7丢进了虚空。");
        yml.addDefault(Messages.PLAYER_DIE_KNOCKED_IN_VOID_FINAL_KILL, "{PlayerColor}{PlayerName}&7被{KillerColor}{KillerName}&7丢进了虚空。 &b&l最终击杀！");
        yml.addDefault(Messages.PLAYER_DIE_PVP_LOG_OUT_REGULAR, "{PlayerColor}{PlayerName}&7在与{KillerColor}{KillerName}&7战斗时断开连接。");
        yml.addDefault(Messages.PLAYER_DIE_PVP_LOG_OUT_FINAL, "{PlayerColor}{PlayerName}&7在与{KillerColor}{KillerName}&7战斗时断开连接。 &b&l最终击杀！");
        yml.addDefault(Messages.PLAYER_DIE_KNOCKED_BY_REGULAR_KILL, "{PlayerColor}{PlayerName}&7被{KillerColor}{KillerName}&7推落。");
        yml.addDefault(Messages.PLAYER_DIE_KNOCKED_BY_FINAL_KILL, "{PlayerColor}{PlayerName}&7被{KillerColor}{KillerName}&7推落。 &b&l最终击杀！");
        yml.addDefault(Messages.PLAYER_DIE_EXPLOSION_WITH_SOURCE_REGULAR_KILL, "{PlayerColor}{PlayerName}&7被{KillerColor}{KillerName}&7炸死了。");
        yml.addDefault(Messages.PLAYER_DIE_EXPLOSION_WITH_SOURCE_FINAL_KILL, "{PlayerColor}{PlayerName}&7被{KillerColor}{KillerName}&7炸死了。 &b&l最终击杀！");
        yml.addDefault(Messages.PLAYER_DIE_EXPLOSION_WITHOUT_SOURCE_REGULAR, "{PlayerColor}{PlayerName}&7爆炸了。");
        yml.addDefault(Messages.PLAYER_DIE_EXPLOSION_WITHOUT_SOURCE_FINAL_KILL, "{PlayerColor}{PlayerName}&7爆炸了。 &b&l最终击杀！");
        yml.addDefault(Messages.PLAYER_DIE_PVP_REGULAR_KILL, "{PlayerColor}{PlayerName}&7被{KillerColor}{KillerName}&7击杀。");
        yml.addDefault(Messages.PLAYER_DIE_PVP_FINAL_KILL, "{PlayerColor}{PlayerName}&7被{KillerColor}{KillerName}&7击杀。 &b&l最终击杀！");
        yml.addDefault(Messages.PLAYER_DIE_UNKNOWN_REASON_REGULAR, "{PlayerColor}{PlayerName}&7死了。");
        yml.addDefault(Messages.PLAYER_DIE_UNKNOWN_REASON_FINAL_KILL, "{PlayerColor}{PlayerName}&7死了。 &b&l最终击杀！");
        yml.addDefault(Messages.PLAYER_DIE_SHOOT_REGULAR, "{PlayerColor}{PlayerName}&7被{KillerColor}{KillerName}&7射死了！");
        yml.addDefault(Messages.PLAYER_DIE_SHOOT_FINAL_KILL, "{PlayerColor}{PlayerName}&7被{KillerColor}{KillerName}&7射死了！ &b&l最终击杀！");
        yml.addDefault(Messages.PLAYER_DIE_DEBUG_REGULAR, "{PlayerColor}{PlayerName}&7被{KillerColor}{KillerTeamName}&7的蠹虫杀死了！");
        yml.addDefault(Messages.PLAYER_DIE_DEBUG_FINAL_KILL, "{PlayerColor}{PlayerName}&7被{KillerColor}{KillerTeamName}&7的蠹虫杀死了！ &b&l最终击杀！");
        yml.addDefault(Messages.PLAYER_DIE_IRON_GOLEM_REGULAR, "{PlayerColor}{PlayerName}&7被{KillerColor}{KillerTeamName}&7的铁傀儡杀死了！");
        yml.addDefault(Messages.PLAYER_DIE_IRON_GOLEM_FINAL_KILL, "{PlayerColor}{PlayerName}&7被{KillerColor}{KillerTeamName}&7的铁傀儡杀死了！ &b&l最终击杀！");
        yml.addDefault(Messages.PLAYER_DIE_REWARD_DIAMOND, "{prefix}&b+{amount}{meaning}");
        yml.addDefault(Messages.PLAYER_DIE_REWARD_EMERALD, "{prefix}&a+{amount}{meaning}");
        yml.addDefault(Messages.PLAYER_DIE_REWARD_IRON, "{prefix}&f+{amount}{meaning}");
        yml.addDefault(Messages.PLAYER_DIE_REWARD_GOLD, "{prefix}&6+{amount}{meaning}");
        yml.addDefault(Messages.PLAYER_DIE_RESPAWN_TITLE, "&c你死了！");
        yml.addDefault(Messages.PLAYER_DIE_RESPAWN_SUBTITLE, "&e你将在 &a{time} &e秒后重生！");
        yml.addDefault(Messages.PLAYER_DIE_RESPAWN_CHAT, "{prefix}&e你将在 &a{time} &e秒后重生！");
        yml.addDefault(Messages.PLAYER_DIE_RESPAWNED_TITLE, "&a已重生！");
        yml.addDefault(Messages.PLAYER_DIE_ELIMINATED_CHAT, "{prefix}&c你已被淘汰！");
        yml.addDefault(Messages.PLAYER_HIT_BOW, "{prefix}{TeamColor}{PlayerName}&7还有 &e{amount} &c生命值！");
        yml.addDefault(Messages.GAME_END_GAME_OVER_PLAYER_TITLE, "&c&l游戏结束！");
        yml.addDefault(Messages.GAME_END_VICTORY_PLAYER_TITLE, "&6&l胜利！");
        yml.addDefault(Messages.GAME_END_TEAM_WON_CHAT, "{prefix}{TeamColor}{TeamName}&a赢得了这场游戏！");
        yml.addDefault(Messages.GAME_END_TOP_PLAYER_CHAT, Arrays.asList("",
                "&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "&f                                   &l起床战争", "", "{winnerFormat}", "", "",
                "&6                      &6⭐ &l击杀第一名 &7- {topTeamColor}{topPlayerDisplayName} &7- &l{topValue}",
                "&e                        &l击杀第二名 &7- {topTeamColor}{topPlayerDisplayName} &7- &l{topValue}",
                "&c                        &l击杀第三名 &7- {topTeamColor}{topPlayerDisplayName} &7- &l{topValue}", "",
                "&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        yml.addDefault(Messages.BED_HOLOGRAM_DEFEND, "&c&l保护你的床！");
        yml.addDefault(Messages.BED_HOLOGRAM_DESTROYED, "&c&l你的床被破坏了！");
        yml.addDefault(Messages.NPC_NAME_TEAM_UPGRADES, "&b队伍升级,&e&l右键点击");
        yml.addDefault(Messages.NPC_NAME_SOLO_UPGRADES, "&b单人模式升级,&e&l右键点击");
        yml.addDefault(Messages.NPC_NAME_TEAM_SHOP, "&b队伍商店,&e&l右键点击");
        yml.addDefault(Messages.NPC_NAME_SOLO_SHOP, "&b物品商店,&e&l右键点击");
        yml.addDefault(Messages.TEAM_ELIMINATED_CHAT, "\n&f&l队伍团灭 > {TeamColor}{TeamName}&c已被团灭！\n");
        yml.addDefault(Messages.NEXT_EVENT_BEDS_DESTROY, "&c床被破坏");
        yml.addDefault(Messages.NEXT_EVENT_DIAMOND_UPGRADE_II, "&f钻石II级");
        yml.addDefault(Messages.NEXT_EVENT_DIAMOND_UPGRADE_III, "&f钻石III级");
        yml.addDefault(Messages.NEXT_EVENT_DRAGON_SPAWN, "&f绝杀模式");
        yml.addDefault(Messages.NEXT_EVENT_EMERALD_UPGRADE_II, "&f绿宝石II级");
        yml.addDefault(Messages.NEXT_EVENT_EMERALD_UPGRADE_III, "&f绿宝石III级");
        yml.addDefault(Messages.NEXT_EVENT_GAME_END, "&4游戏结束！");
        yml.addDefault(Messages.NEXT_EVENT_TITLE_ANNOUNCE_BEDS_DESTROYED, "&c床被破坏！");
        yml.addDefault(Messages.NEXT_EVENT_SUBTITLE_ANNOUNCE_BEDS_DESTROYED, "&f所有床已被破坏！");
        yml.addDefault(Messages.NEXT_EVENT_CHAT_ANNOUNCE_BEDS_DESTROYED, "&c&l所有床已被破坏！");
        yml.addDefault(Messages.NEXT_EVENT_TITLE_ANNOUNCE_SUDDEN_DEATH, "&c绝杀模式");
        yml.addDefault(Messages.NEXT_EVENT_SUBTITLE_ANNOUNCE_SUDDEN_DEATH, "");
        yml.addDefault(Messages.NEXT_EVENT_CHAT_ANNOUNCE_SUDDEN_DEATH, "&c绝杀模式：&6&b{TeamDragons} {TeamColor}{TeamName}的龙！");


        /* Lobby Command Items */
        yml.addDefault(Messages.LOBBY_MENU_TITLE, "&8主城菜单");
        yml.addDefault(Messages.LOBBY_MENU_ITEM_NAME, "&e玩家菜单");
        yml.addDefault(Messages.LOBBY_MENU_ITEM_LORE, Collections.singletonList("&f右键查看战绩或编辑快速购买！"));
        yml.addDefault(Messages.LOBBY_MENU_STATS, "&e查看战绩");
        yml.addDefault(Messages.LOBBY_MENU_QUICK_BUY, "&e编辑快速购买");
        yml.addDefault(Messages.QUICK_BUY_EDITOR_TITLE, "&8快速购买编辑");
        yml.addDefault(Messages.QUICK_BUY_EDITOR_SELECT_SLOT_TITLE, "&8选择快捷槽位");
        yml.addDefault(Messages.QUICK_BUY_EDITOR_LOADING, "&e正在加载……");
        yml.addDefault(Messages.QUICK_BUY_EDITOR_LOAD_FAILED, "&c加载失败，请重新打开编辑器。");
        yml.addDefault(Messages.QUICK_BUY_EDITOR_ADD, "&e点击添加到快速购买。");
        yml.addDefault(Messages.QUICK_BUY_EDITOR_ADDED, "&a已添加到快速购买。");
        yml.addDefault(Messages.QUICK_BUY_EDITOR_REMOVE, "&eShift 点击移除。");
        yml.addDefault(Messages.QUICK_BUY_EDITOR_SELECT_SLOT, "&e点击选择或替换此槽位。");
        yml.addDefault(Messages.QUICK_BUY_EDITOR_BACK, "&e返回");
        yml.addDefault(Messages.GENERAL_CONFIGURATION_LOBBY_ITEMS_NAME.replace("%path%", "stats"), "&e战绩");
        yml.addDefault(Messages.GENERAL_CONFIGURATION_LOBBY_ITEMS_LORE.replace("%path%", "stats"), Collections.singletonList("&f右键显示你的战绩！"));
        yml.addDefault(Messages.GENERAL_CONFIGURATION_LOBBY_ITEMS_NAME.replace("%path%", "arena-selector"), "&e选择游戏");
        yml.addDefault(Messages.GENERAL_CONFIGURATION_LOBBY_ITEMS_LORE.replace("%path%", "arena-selector"), Collections.singletonList("&f右键选择游戏！"));
        yml.addDefault(Messages.GENERAL_CONFIGURATION_LOBBY_ITEMS_NAME.replace("%path%", "leave"), "&e回到主大厅");
        yml.addDefault(Messages.GENERAL_CONFIGURATION_LOBBY_ITEMS_LORE.replace("%path%", "leave"), Collections.singletonList("&f右键离开起床战争！"));
        /* Pre Game Command Items */
        yml.addDefault(Messages.GENERAL_CONFIGURATION_WAITING_ITEMS_NAME.replace("%path%", "stats"), "&e战绩");
        yml.addDefault(Messages.GENERAL_CONFIGURATION_WAITING_ITEMS_LORE.replace("%path%", "stats"), Collections.singletonList("&f右键显示你的战绩！"));
        yml.addDefault(Messages.GENERAL_CONFIGURATION_WAITING_ITEMS_NAME.replace("%path%", "leave"), "&e返回大厅");
        yml.addDefault(Messages.GENERAL_CONFIGURATION_WAITING_ITEMS_LORE.replace("%path%", "leave"), Collections.singletonList("&f右键离开游戏！"));
        /* Spectator Command Items */
        yml.addDefault(Messages.GENERAL_CONFIGURATION_SPECTATOR_ITEMS_NAME.replace("%path%", "teleporter"), "&e传送");
        yml.addDefault(Messages.GENERAL_CONFIGURATION_SPECTATOR_ITEMS_NAME.replace("%path%", "leave"), "&e回到大厅");
        yml.addDefault(Messages.GENERAL_CONFIGURATION_SPECTATOR_ITEMS_LORE.replace("%path%", "leave"), Collections.singletonList("&f右键离开游戏！"));

        /* save default items messages for stats gui */
        yml.addDefault(Messages.PLAYER_STATS_GUI_INV_NAME, "&8{player}的战绩");
        addDefaultStatsMsg(yml, "wins", "&6胜场数", "&f{wins}");
        addDefaultStatsMsg(yml, "losses", "&6失败场数", "&f{losses}");
        addDefaultStatsMsg(yml, "kills", "&6击杀数", "&f{kills}");
        addDefaultStatsMsg(yml, "deaths", "&6死亡数", "&f{deaths}");
        addDefaultStatsMsg(yml, "final-kills", "&6最终击杀数", "&f{finalKills}");
        addDefaultStatsMsg(yml, "final-deaths", "&6最终死亡数", "&f{finalDeaths}");
        addDefaultStatsMsg(yml, "beds-destroyed", "&6破坏床数", "&f{bedsDestroyed}");
        addDefaultStatsMsg(yml, "first-play", "&6首次游玩", "&f{firstPlay}");
        addDefaultStatsMsg(yml, "last-play", "&6上次游玩", "&f{lastPlay}");
        addDefaultStatsMsg(yml, "games-played", "&6总游玩场数", "&f{gamesPlayed}");

        // Start of Sidebar
        yml.addDefault(Messages.SCOREBOARD_LOBBY, Arrays.asList(
                "&6&l起床战争,&4&l起&6&l床战争,&6&l起&4&l床&6&l战争,&6&l起床&4&l战&6&l争,&6&l起床战&4&l争,&6&l起床战争,&6&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争",
                "&7{player}",
                "",
                "&f总胜场：&a{wins}",
                "&f总击杀：&a{kills}",
                "", "&e{serverIp}")
        );
        yml.addDefault(Messages.SCOREBOARD_DEFAULT_WAITING, Arrays.asList(
                "&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争",
                "&7{date} &8{server}",
                "",
                "&f地图：&a{map}",
                "",
                "&f玩家数：&a{on}/{max}",
                "",
                "&f等待中,&f等待中.,&f等待中..,&f等待中...",
                "",
                "&f模式：&a{group}",
                "&f版本：&7{version}",
                "",
                "&e{serverIp}")
        );
        yml.addDefault(Messages.SCOREBOARD_DEFAULT_WAITING_SPEC, Arrays.asList(
                "&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争",
                "&7{date} &8{server}",
                "&o&7正在观战",
                "&f地图：&a{map}",
                "",
                "&f玩家数：&a{on}/{max}",
                "",
                "&f等待中,&f等待中.,&f等待中..,&f等待中...",
                "",
                "&f模式：&a{group}",
                "&f版本：&7{version}",
                "",
                "&e{serverIp}")
        );
        yml.addDefault(Messages.SCOREBOARD_DEFAULT_STARTING, Arrays.asList(
                "&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争",
                "&7{date} &8{server}",
                "",
                "&f地图：&a{map}",
                "",
                "&f玩家数：&a{on}/{max}",
                "",
                "&f &a{time} &f秒后开始",
                "",
                "&f模式：&a{group}",
                "&f版本：&7{version}",
                "",
                "&e{serverIp}")
        );
        yml.addDefault(Messages.SCOREBOARD_DEFAULT_STARTING_SPEC, Arrays.asList(
                "&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争",
                "&7{date} &8{server}",
                "&o&7正在观战",
                "&f地图：&a{map}",
                "",
                "&f玩家数：&a{on}/{max}",
                "",
                "&f &a{time} &f秒后开始",
                "",
                "&f模式：&a{group}",
                "&f版本：&7{version}",
                "",
                "&e{serverIp}")
        );
        yml.addDefault(Messages.SCOREBOARD_DEFAULT_PLAYING, Arrays.asList(
                "&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争",
                "&7{date}",
                "",
                "&f{nextEvent}：&a{time}",
                "",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "",
                "&e{serverIp}")
        );

        yml.addDefault(Messages.SCOREBOARD_DEFAULT_PLAYING_SPEC_ELIMINATED, Arrays.asList(
                "&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争",
                "&7{date}",
                "&o&7正在观战 {spectatorTarget}",
                "",
                "&f{nextEvent}：&a{time}",
                "",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "",
                "&e{serverIp}")
        );
        yml.addDefault(Messages.SCOREBOARD_DEFAULT_PLAYING_SPEC, Arrays.asList(
                "&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争",
                "&7{date}",
                "&o&7正在观战 {spectatorTarget}",
                "",
                "&f{nextEvent}：&a{time}",
                "",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "",
                "&e{serverIp}")
        );
        yml.addDefault(Messages.SCOREBOARD_DEFAULT_RESTARTING_SPEC, Arrays.asList(
                "&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争",
                "&7{date}",
                "",
                "&6获胜者：{winnerTeamColor}{winnerTeamName} &6⭐",
                "",
                "&7&l击杀榜：",
                "&f{topTeamColor}{topPlayerDisplayName}&7 - &l{topValue}",
                "&f{topTeamColor}{topPlayerDisplayName}&7 - &l{topValue}",
                "&f{topTeamColor}{topPlayerDisplayName}&7 - &l{topValue}",
                "&f{topTeamColor}{topPlayerDisplayName}&7 - &l{topValue}",
                "&f{topTeamColor}{topPlayerDisplayName}&7 - &l{topValue}",
                "&f{topTeamColor}{topPlayerDisplayName}&7 - &l{topValue}",
                "&f{topTeamColor}{topPlayerDisplayName}&7 - &l{topValue}",
                "&f{topTeamColor}{topPlayerDisplayName}&7 - &l{topValue}",
                "",
                "&e{serverIp}")
        );

        yml.addDefault(Messages.SCOREBOARD_DEFAULT_RESTARTING_WIN1, Arrays.asList(
                "&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争",
                "&7{date}",
                "",
                "&6获胜者：{winnerTeamColor}{winnerTeamName} &6⭐",
                "",
                "&7&l击杀榜：",
                "&f{topTeamColor}{topPlayerDisplayName}&7 - &l{topValue}",
                "&f{topTeamColor}{topPlayerDisplayName}&7 - &l{topValue}",
                "&f{topTeamColor}{topPlayerDisplayName}&7 - &l{topValue}",
                "&f{topTeamColor}{topPlayerDisplayName}&7 - &l{topValue}",
                "&f{topTeamColor}{topPlayerDisplayName}&7 - &l{topValue}",
                "&f{topTeamColor}{topPlayerDisplayName}&7 - &l{topValue}",
                "&f{topTeamColor}{topPlayerDisplayName}&7 - &l{topValue}",
                "&f{topTeamColor}{topPlayerDisplayName}&7 - &l{topValue}",
                "",
                "&e{serverIp}")
        );

        yml.addDefault(Messages.SCOREBOARD_DEFAULT_RESTARTING_WIN2, Arrays.asList(
                "&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争",
                "&7{date}",
                "",
                "&6获胜者：{winnerTeamColor}{winnerTeamName} &6⭐",
                "",
                "&7&l击杀榜：",
                "&f{topTeamColor}{topPlayerDisplayName}&7 - &l{topValue}",
                "&f{topTeamColor}{topPlayerDisplayName}&7 - &l{topValue}",
                "&f{topTeamColor}{topPlayerDisplayName}&7 - &l{topValue}",
                "&f{topTeamColor}{topPlayerDisplayName}&7 - &l{topValue}",
                "&f{topTeamColor}{topPlayerDisplayName}&7 - &l{topValue}",
                "&f{topTeamColor}{topPlayerDisplayName}&7 - &l{topValue}",
                "&f{topTeamColor}{topPlayerDisplayName}&7 - &l{topValue}",
                "&f{topTeamColor}{topPlayerDisplayName}&7 - &l{topValue}",
                "",
                "&e{serverIp}")
        );

        yml.addDefault(Messages.SCOREBOARD_DEFAULT_RESTARTING_LOSER, Arrays.asList(
                "&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争",
                "&7{date}",
                "",
                "&6获胜者：{winnerTeamColor}{winnerTeamName} &6⭐",
                "",
                "&7&l击杀榜：",
                "&f{topTeamColor}{topPlayerDisplayName}&7 - &l{topValue}",
                "&f{topTeamColor}{topPlayerDisplayName}&7 - &l{topValue}",
                "&f{topTeamColor}{topPlayerDisplayName}&7 - &l{topValue}",
                "&f{topTeamColor}{topPlayerDisplayName}&7 - &l{topValue}",
                "&f{topTeamColor}{topPlayerDisplayName}&7 - &l{topValue}",
                "&f{topTeamColor}{topPlayerDisplayName}&7 - &l{topValue}",
                "&f{topTeamColor}{topPlayerDisplayName}&7 - &l{topValue}",
                "&f{topTeamColor}{topPlayerDisplayName}&7 - &l{topValue}",
                "",
                "&e{serverIp}")
        );

        yml.addDefault(Messages.SCOREBOARD_DEFAULT_PLAYING.replaceFirst("Default", "Doubles"), Arrays.asList(
                "&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争",
                "&7{date}",
                "",
                "&f{nextEvent}：&a{time}",
                "",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "",
                "&e{serverIp}")
        );

        yml.addDefault(Messages.SCOREBOARD_DEFAULT_PLAYING_SPEC.replaceFirst("Default", "Doubles"), Arrays.asList(
                "&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争",
                "&7{date}",
                "&o&7正在观战 {spectatorTarget}",
                "&f{nextEvent}：&a{time}",
                "",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "",
                "&e{serverIp}")
        );
        yml.addDefault(Messages.SCOREBOARD_DEFAULT_PLAYING_SPEC_ELIMINATED.replaceFirst("Default", "Doubles"), Arrays.asList(
                "&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争",
                "&7{date}",
                "&o&7正在观战 {spectatorTarget}",
                "&f{nextEvent}：&a{time}",
                "",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "",
                "&e{serverIp}")
        );

        yml.addDefault(Messages.SCOREBOARD_DEFAULT_PLAYING.replaceFirst("Default", "3v3v3v3"), Arrays.asList(
                "&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争",
                "&7{date}",
                "",
                "&f{nextEvent}：&a{time}",
                "",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "",
                "&f击杀：&a{kills}",
                "&f最终击杀：&a{finalKills}",
                "&f破坏床：&a{beds}",
                "",
                "&e{serverIp}")
        );

        yml.addDefault(Messages.SCOREBOARD_DEFAULT_PLAYING_SPEC.replaceFirst("Default", "3v3v3v3"), Arrays.asList(
                "&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争",
                "&7{date}",
                "&o&7正在观战 {spectatorTarget}",
                "&f{nextEvent}：&a{time}",
                "",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "",
                "",
                "&e{serverIp}")
        );
        yml.addDefault(Messages.SCOREBOARD_DEFAULT_PLAYING_SPEC_ELIMINATED.replaceFirst("Default", "3v3v3v3"), Arrays.asList(
                "&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争",
                "&7{date}",
                "&o&7正在观战 {spectatorTarget}",
                "&f{nextEvent}：&a{time}",
                "",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "",
                "&f击杀：&a{kills}",
                "&f最终击杀：&a{finalKills}",
                "&f破坏床：&a{beds}",
                "",
                "&e{serverIp}")
        );

        yml.addDefault(Messages.SCOREBOARD_DEFAULT_PLAYING.replaceFirst("Default", "4v4v4v4"), Arrays.asList(
                "&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争",
                "&7{date}",
                "",
                "&f{nextEvent}：&a{time}",
                "",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "",
                "&f击杀：&a{kills}",
                "&f最终击杀：&a{finalKills}",
                "&f破坏床：&a{beds}",
                "",
                "&e{serverIp}")
        );

        yml.addDefault(Messages.SCOREBOARD_DEFAULT_PLAYING_SPEC.replaceFirst("Default", "4v4v4v4"), Arrays.asList(
                "&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争",
                "&7{date}",
                "&o&7正在观战 {spectatorTarget}",
                "&f{nextEvent}：&a{time}",
                "",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "",
                "",
                "&e{serverIp}")
        );
        yml.addDefault(Messages.SCOREBOARD_DEFAULT_PLAYING_SPEC_ELIMINATED.replaceFirst("Default", "4v4v4v4"), Arrays.asList(
                "&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&f&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争,&e&l起床战争",
                "&7{date}",
                "&o&7正在观战 {spectatorTarget}",
                "&f{nextEvent}：&a{time}",
                "",
                "{team}",
                "{team}",
                "{team}",
                "{team}",
                "",
                "&f击杀：&a{kills}",
                "&f最终击杀：&a{finalKills}",
                "&f破坏床：&a{beds}",
                "",
                "&e{serverIp}")
        );


        // End of Sidebar

        // TAB 使用统一静态页眉，玩家状态放在页脚与行尾。
        String tabSeparator = "&8&m--------------------------------";
        List<String> tabHeader = List.of(
                "&a&lHMO BEDWARS",
                "&7起床战争 &8· &7玩家名单",
                tabSeparator
        );
        yml.addDefault(Messages.FORMATTING_SB_TAB_LOBBY_HEADER, tabHeader);
        yml.addDefault(Messages.FORMATTING_SB_TAB_LOBBY_FOOTER, List.of(
                tabSeparator,
                "&7大厅玩家 &8· &f{on}"
        ));
        yml.addDefault(Messages.FORMATTING_SB_TAB_LOBBY_PREFIX, List.of(""));
        yml.addDefault(Messages.FORMATTING_SB_TAB_LOBBY_SUFFIX, List.of(""));

        yml.addDefault(Messages.FORMATTING_SB_TAB_WAITING_HEADER, tabHeader);
        yml.addDefault(Messages.FORMATTING_SB_TAB_WAITING_FOOTER, List.of(
                tabSeparator,
                "&7等待更多玩家 &8· &f{on}/{max}"
        ));
        yml.addDefault(Messages.FORMATTING_SB_TAB_WAITING_PREFIX, List.of(""));
        yml.addDefault(Messages.FORMATTING_SB_TAB_WAITING_SUFFIX, List.of(""));
        yml.addDefault(Messages.FORMATTING_SB_TAB_WAITING_HEADER_SPEC, tabHeader);
        yml.addDefault(Messages.FORMATTING_SB_TAB_WAITING_FOOTER_SPEC, List.of(
                tabSeparator,
                "&7正在观战 &8· &f{on}/{max}"
        ));
        yml.addDefault(Messages.FORMATTING_SB_TAB_WAITING_PREFIX_SPEC, List.of("&7[观] "));
        yml.addDefault(Messages.FORMATTING_SB_TAB_WAITING_SUFFIX_SPEC, List.of(""));

        yml.addDefault(Messages.FORMATTING_SB_TAB_STARTING_HEADER, tabHeader);
        yml.addDefault(Messages.FORMATTING_SB_TAB_STARTING_FOOTER, List.of(
                tabSeparator,
                "&7即将开始 &8· &f{time} 秒 &8· &f{on}/{max}"
        ));
        yml.addDefault(Messages.FORMATTING_SB_TAB_STARTING_PREFIX, List.of(""));
        yml.addDefault(Messages.FORMATTING_SB_TAB_STARTING_SUFFIX, List.of(""));
        yml.addDefault(Messages.FORMATTING_SB_TAB_STARTING_HEADER_SPEC, tabHeader);
        yml.addDefault(Messages.FORMATTING_SB_TAB_STARTING_FOOTER_SPEC, List.of(
                tabSeparator,
                "&7正在观战 &8· &f{time} 秒后开始"
        ));
        yml.addDefault(Messages.FORMATTING_SB_TAB_STARTING_PREFIX_SPEC, List.of("&7[观] "));
        yml.addDefault(Messages.FORMATTING_SB_TAB_STARTING_SUFFIX_SPEC, List.of(""));

        yml.addDefault(Messages.FORMATTING_SB_TAB_PLAYING_HEADER, tabHeader);
        yml.addDefault(Messages.FORMATTING_SB_TAB_PLAYING_FOOTER, List.of(
                tabSeparator,
                "&7游戏中 &8· {teamColor}{teamName}"
        ));
        yml.addDefault(Messages.FORMATTING_SB_TAB_PLAYING_PREFIX, List.of("{teamColor}[{teamLetter}] "));
        yml.addDefault(Messages.FORMATTING_SB_TAB_PLAYING_SUFFIX, List.of(""));
        yml.addDefault(Messages.FORMATTING_SB_TAB_PLAYING_ELM_HEADER, tabHeader);
        yml.addDefault(Messages.FORMATTING_SB_TAB_PLAYING_ELM_FOOTER, List.of(
                tabSeparator,
                "&7正在观战 &8· &7原{teamColor}{teamName} &8· &7已淘汰"
        ));
        yml.addDefault(Messages.FORMATTING_SB_TAB_PLAYING_ELM_PREFIX, List.of("{teamColor}[{teamLetter}] "));
        yml.addDefault(Messages.FORMATTING_SB_TAB_PLAYING_ELM_SUFFIX, List.of("&7 · 淘汰"));
        yml.addDefault(Messages.FORMATTING_SB_TAB_PLAYING_ELM_DEPARTED, "{teamColor}[{teamLetter}] {playerName}&7 · 淘汰·离场");
        yml.addDefault(Messages.FORMATTING_SB_TAB_PLAYING_SPEC_HEADER, tabHeader);
        yml.addDefault(Messages.FORMATTING_SB_TAB_PLAYING_SPEC_FOOTER, List.of(
                tabSeparator,
                "&7正在观战"
        ));
        yml.addDefault(Messages.FORMATTING_SB_TAB_PLAYING_SPEC_PREFIX, List.of("&7[观] "));
        yml.addDefault(Messages.FORMATTING_SB_TAB_PLAYING_SPEC_SUFFIX, List.of(""));

        yml.addDefault(Messages.FORMATTING_SB_TAB_RESTARTING_WIN1_HEADER, tabHeader);
        yml.addDefault(Messages.FORMATTING_SB_TAB_RESTARTING_WIN1_FOOTER, List.of(
                tabSeparator,
                "&7比赛结束 &8· {teamColor}{teamName} &7获胜"
        ));
        yml.addDefault(Messages.FORMATTING_SB_TAB_RESTARTING_WIN1_PREFIX, List.of("{teamColor}[{teamLetter}] "));
        yml.addDefault(Messages.FORMATTING_SB_TAB_RESTARTING_WIN1_SUFFIX, List.of("&7 · 获胜"));
        yml.addDefault(Messages.FORMATTING_SB_TAB_RESTARTING_WIN2_HEADER, tabHeader);
        yml.addDefault(Messages.FORMATTING_SB_TAB_RESTARTING_WIN2_FOOTER, List.of(
                tabSeparator,
                "&7正在观战 &8· {teamColor}{teamName} &7获胜"
        ));
        yml.addDefault(Messages.FORMATTING_SB_TAB_RESTARTING_WIN2_PREFIX, List.of("{teamColor}[{teamLetter}] "));
        yml.addDefault(Messages.FORMATTING_SB_TAB_RESTARTING_WIN2_SUFFIX, List.of("&7 · 淘汰"));
        yml.addDefault(Messages.FORMATTING_SB_TAB_RESTARTING_ELM_HEADER, tabHeader);
        yml.addDefault(Messages.FORMATTING_SB_TAB_RESTARTING_ELM_FOOTER, List.of(
                tabSeparator,
                "&7比赛结束 &8· &7胜方 {winnerTeamColor}{winnerTeamName}"
        ));
        yml.addDefault(Messages.FORMATTING_SB_TAB_RESTARTING_ELM_PREFIX, List.of("{teamColor}[{teamLetter}] "));
        yml.addDefault(Messages.FORMATTING_SB_TAB_RESTARTING_ELM_SUFFIX, List.of("&7 · 淘汰"));
        yml.addDefault(Messages.FORMATTING_SB_TAB_RESTARTING_SPEC_HEADER, tabHeader);
        yml.addDefault(Messages.FORMATTING_SB_TAB_RESTARTING_SPEC_FOOTER, List.of(
                tabSeparator,
                "&7比赛结束 &8· &7胜方 {winnerTeamColor}{winnerTeamName}"
        ));
        yml.addDefault(Messages.FORMATTING_SB_TAB_RESTARTING_SPEC_PREFIX, List.of("&7[观] "));
        yml.addDefault(Messages.FORMATTING_SB_TAB_RESTARTING_SPEC_SUFFIX, List.of(""));

        yml.addDefault(Messages.SHOP_INDEX_NAME, "&8快速购买");
        yml.addDefault(Messages.SHOP_QUICK_ADD_NAME, "&8添加到快速购买...");
        yml.addDefault(Messages.SHOP_INSUFFICIENT_MONEY, "{prefix}&c你没有足够的{currency}！ 还需要 {amount} 个！");
        yml.addDefault(Messages.SHOP_INSUFFICIENT_INVENTORY_SPACE, "{prefix}&c背包已满！");
        yml.addDefault(Messages.SHOP_NEW_PURCHASE, "{prefix}&a购买&6{item}");
        yml.addDefault(Messages.SHOP_ALREADY_BOUGHT, "{prefix}&c你已经购买过了！");
        yml.addDefault(Messages.SHOP_UTILITY_NPC_SILVERFISH_NAME, "{TeamColor}&l{TeamName} &r{TeamColor}蠹虫");
        yml.addDefault(Messages.SHOP_UTILITY_NPC_IRON_GOLEM_NAME, "{TeamColor}{despawn}秒 &8[ {TeamColor}{health}&8]");
        yml.addDefault(Messages.SHOP_SEPARATOR_NAME, "&8⇧ 分类");
        yml.addDefault(Messages.SHOP_SEPARATOR_LORE, Collections.singletonList("&8⇩ 物品"));
        yml.addDefault(Messages.SHOP_QUICK_BUY_NAME, "&b快速购买");
        yml.addDefault(Messages.SHOP_QUICK_BUY_LORE, new ArrayList<>());
        yml.addDefault(Messages.SHOP_QUICK_EMPTY_NAME, "&c空槽位！");
        yml.addDefault(Messages.SHOP_QUICK_EMPTY_LORE, Arrays.asList("&7这是快速购买槽位！", "&bShift+点击 &7商店中的物品", "&7来加到这里。"));
        yml.addDefault(Messages.SHOP_CAN_BUY_COLOR, "&a");
        yml.addDefault(Messages.SHOP_CANT_BUY_COLOR, "&c");
        yml.addDefault(Messages.SHOP_LORE_STATUS_CAN_BUY, "&e点击购买！");
        yml.addDefault(Messages.SHOP_LORE_STATUS_CANT_AFFORD, "&c你没有足够的{currency}！");
        yml.addDefault(Messages.SHOP_LORE_STATUS_MAXED, "&a满级！");
        yml.addDefault(Messages.SHOP_LORE_STATUS_ARMOR, "&a已装备！");
        yml.addDefault(Messages.SHOP_LORE_QUICK_ADD, "&bShift+点击 来添加快速购买");
        yml.addDefault(Messages.SHOP_LORE_QUICK_REMOVE, "&bShift+点击 来从快速购买中移除！");


        addCategoryMessages(yml, ConfigPath.SHOP_PATH_CATEGORY_BLOCKS, "&8方块", "&a方块", Collections.singletonList("&e点击查看！"));

        addContentMessages(yml, "wool", ConfigPath.SHOP_PATH_CATEGORY_BLOCKS, "{color}羊毛", Arrays.asList("&7花费：&f{cost} {currency}", "", "&7很好的搭路工具",
                "&7购买后将变为你队伍的颜色", "", "{quick_buy}", "{buy_status}"));
        addContentMessages(yml, "clay", ConfigPath.SHOP_PATH_CATEGORY_BLOCKS, "{color}硬化粘土", Arrays.asList("&7花费：{cost} {currency}", "", "&7保护床的基本方块。", "", "{quick_buy}", "{buy_status}"));
        addContentMessages(yml, "glass", ConfigPath.SHOP_PATH_CATEGORY_BLOCKS, "{color}防爆玻璃", Arrays.asList("&7花费：{cost} {currency}", "", "&7免疫爆炸。", "", "{quick_buy}", "{buy_status}"));
        addContentMessages(yml, "stone", ConfigPath.SHOP_PATH_CATEGORY_BLOCKS, "{color}末地石", Arrays.asList("&7花费：{cost} {currency}", "", "&7保护床的坚实方块。", "", "{quick_buy}", "{buy_status}"));
        addContentMessages(yml, "ladder", ConfigPath.SHOP_PATH_CATEGORY_BLOCKS, "{color}梯子", Arrays.asList("&7花费：{cost} {currency}", "", "&7爬树时很有用。", "", "{quick_buy}", "{buy_status}"));
        addContentMessages(yml, "obsidian", ConfigPath.SHOP_PATH_CATEGORY_BLOCKS, "{color}黑曜石", Arrays.asList("&7花费：{cost} {currency}", "", "&7对床的终极保护。", "", "{quick_buy}", "{buy_status}"));
        addContentMessages(yml, "wood", ConfigPath.SHOP_PATH_CATEGORY_BLOCKS, "{color}木板", Arrays.asList("&7花费：{cost} {currency}", "", "&7保护床的坚实方块。", "", "{quick_buy}", "{buy_status}"));

        addCategoryMessages(yml, ConfigPath.SHOP_PATH_CATEGORY_MELEE, "&8战斗", "&a战斗", Collections.singletonList("&e点击查看！"));

        addContentMessages(yml, "stone-sword", ConfigPath.SHOP_PATH_CATEGORY_MELEE, "{color}石剑", Arrays.asList("&7花费：{cost} {currency}", "", "{quick_buy}", "{buy_status}"));
        addContentMessages(yml, "iron-sword", ConfigPath.SHOP_PATH_CATEGORY_MELEE, "{color}铁剑", Arrays.asList("&7花费：{cost} {currency}", "", "{quick_buy}", "{buy_status}"));
        addContentMessages(yml, "diamond-sword", ConfigPath.SHOP_PATH_CATEGORY_MELEE, "{color}钻石剑", Arrays.asList("&7花费：{cost} {currency}", "", "{quick_buy}", "{buy_status}"));
        addContentMessages(yml, "stick", ConfigPath.SHOP_PATH_CATEGORY_MELEE, "{color}木棍(击退 I)", Arrays.asList("&7花费：{cost} {currency}", "", "{quick_buy}", "{buy_status}"));

        addCategoryMessages(yml, ConfigPath.SHOP_PATH_CATEGORY_ARMOR, "&8盔甲", "&a盔甲", Collections.singletonList("&e点击查看！"));

        addContentMessages(yml, "chainmail", ConfigPath.SHOP_PATH_CATEGORY_ARMOR, "{color}永久锁链护甲", Arrays.asList("&7花费：{cost} {currency}",
                "", "&7锁链靴子和护腿", "&7重生后仍会装备", "", "{quick_buy}", "{buy_status}"));
        addContentMessages(yml, "iron-armor", ConfigPath.SHOP_PATH_CATEGORY_ARMOR, "{color}永久铁护甲", Arrays.asList("&7花费：{cost} {currency}",
                "", "&7铁靴子和护腿", "&7重生后仍会装备", "", "{quick_buy}", "{buy_status}"));
        addContentMessages(yml, "diamond-armor", ConfigPath.SHOP_PATH_CATEGORY_ARMOR, "{color}永久钻石护甲", Arrays.asList("&7花费：{cost} {currency}",
                "", "&7钻石靴子和护腿", "&7重生后仍会装备", "", "{quick_buy}", "{buy_status}"));

        addCategoryMessages(yml, ConfigPath.SHOP_PATH_CATEGORY_TOOLS, "&8工具", "&a工具", Collections.singletonList("&e点击查看！"));

        addContentMessages(yml, "shears", ConfigPath.SHOP_PATH_CATEGORY_TOOLS, "{color}永久剪刀", Arrays.asList("&7花费：{cost} {currency}",
                "", "&7拆羊毛的利器", "&7重生后仍会获得", "", "{quick_buy}", "{buy_status}"));
        addContentMessages(yml, "pickaxe", ConfigPath.SHOP_PATH_CATEGORY_TOOLS, "{color}镐{tier}", Arrays.asList("&7花费：{cost} {currency}", "&7等级：&e{tier}",
                "", "&7该工具可升级。", "&7每次死亡都会降一级。", "", "&7降到最低级为止", "", "{quick_buy}", "{buy_status}"));
        addContentMessages(yml, "axe", ConfigPath.SHOP_PATH_CATEGORY_TOOLS, "{color}斧{tier}", Arrays.asList("&7花费：{cost} {currency}", "&7等级：&e{tier}",
                "", "&7该工具可升级。", "&7每次死亡都会降一级。", "", "&7降到最低级为止", "", "{quick_buy}", "{buy_status}"));

        addCategoryMessages(yml, ConfigPath.SHOP_PATH_CATEGORY_RANGED, "&8远程武器", "&a远程武器", Collections.singletonList("&e点击查看！"));

        addContentMessages(yml, "arrow", ConfigPath.SHOP_PATH_CATEGORY_RANGED, "{color}箭", Arrays.asList("&7花费：{cost} {currency}", "", "{quick_buy}", "{buy_status}"));
        addContentMessages(yml, "bow1", ConfigPath.SHOP_PATH_CATEGORY_RANGED, "{color}弓", Arrays.asList("&7花费：{cost} {currency}", "", "{quick_buy}", "{buy_status}"));
        addContentMessages(yml, "bow2", ConfigPath.SHOP_PATH_CATEGORY_RANGED, "{color}弓（力量 I）", Arrays.asList("&7花费：{cost} {currency}", "", "{quick_buy}", "{buy_status}"));
        addContentMessages(yml, "bow3", ConfigPath.SHOP_PATH_CATEGORY_RANGED, "{color}弓（力量 I, 冲击 I）", Arrays.asList("&7花费：{cost} {currency}", "", "{quick_buy}", "{buy_status}"));

        addCategoryMessages(yml, ConfigPath.SHOP_PATH_CATEGORY_POTIONS, "&8药水", "&a药水", Collections.singletonList("&e点击查看！"));

        addContentMessages(yml, "speed-potion", ConfigPath.SHOP_PATH_CATEGORY_POTIONS, "{color}速度 {level} 药水（{duration} 秒）", Arrays.asList("&7花费：{cost} {currency}", "", "{quick_buy}", "{buy_status}"));
        addContentMessages(yml, "jump-potion", ConfigPath.SHOP_PATH_CATEGORY_POTIONS, "{color}跳跃提升 {level} 药水（{duration} 秒）", Arrays.asList("&7花费：{cost} {currency}", "", "{quick_buy}", "{buy_status}"));
        addContentMessages(yml, "invisibility", ConfigPath.SHOP_PATH_CATEGORY_POTIONS, "{color}隐身药水（30 秒）", Arrays.asList("&7花费：{cost} {currency}", "", "{quick_buy}", "{buy_status}"));

        addCategoryMessages(yml, ConfigPath.SHOP_PATH_CATEGORY_UTILITY, "&8实用工具", "&a实用工具", Collections.singletonList("&e点击查看！"));

        addContentMessages(yml, "golden-apple", ConfigPath.SHOP_PATH_CATEGORY_UTILITY, "{color}金苹果", Arrays.asList("&7花费：{cost} {currency}", "", "&7均衡治疗", "", "{quick_buy}", "{buy_status}"));
        addContentMessages(yml, "bedbug", ConfigPath.SHOP_PATH_CATEGORY_UTILITY, "{color}蠹虫", Arrays.asList("&7花费：{cost} {currency}", "",
                "&7在雪球落下的地方召唤蠹虫", "&7来干扰敌人", "&7持续15秒。", "", "{quick_buy}", "{buy_status}"));
        addContentMessages(yml, "dream-defender", ConfigPath.SHOP_PATH_CATEGORY_UTILITY, "{color}梦境守卫者", Arrays.asList("&7花费：{cost} {currency}", "",
                "&7召唤铁傀儡来保护基地", "&7持续 4 分钟", "", "{quick_buy}", "{buy_status}"));
        addContentMessages(yml, "fireball", ConfigPath.SHOP_PATH_CATEGORY_UTILITY, "{color}火球", Arrays.asList("&7花费：{cost} {currency}", "", "&7右键发射！",
                "&7把在窄桥上的敌人打下去！", "", "{quick_buy}", "{buy_status}"));
        addContentMessages(yml, "tnt", ConfigPath.SHOP_PATH_CATEGORY_UTILITY, "{color}TNT", Arrays.asList("&7花费：{cost} {currency}", "",
                "&7立即点燃, 适合炸点东西", "", "{quick_buy}", "{buy_status}"));
        addContentMessages(yml, "ender-pearl", ConfigPath.SHOP_PATH_CATEGORY_UTILITY, "{color}末影珍珠", Arrays.asList("&7花费：{cost} {currency}", "",
                "&7偷家最快的方式", "", "{quick_buy}", "{buy_status}"));
        addContentMessages(yml, "water-bucket", ConfigPath.SHOP_PATH_CATEGORY_UTILITY, "{color}水桶", Arrays.asList("&7花费：{cost} {currency}", "",
                "&7减缓敌人的速度", "&7也可以防止 TNT 破坏方块", "", "{quick_buy}", "{buy_status}"));
        addContentMessages(yml, "bridge-egg", ConfigPath.SHOP_PATH_CATEGORY_UTILITY, "{color}搭桥蛋", Arrays.asList("&7花费：{cost} {currency}", "",
                "&7丢出后在其轨迹形成一座桥", "", "{quick_buy}", "{buy_status}"));
        addContentMessages(yml, "magic-milk", ConfigPath.SHOP_PATH_CATEGORY_UTILITY, "{color}魔法牛奶", Arrays.asList("&7花费：{cost} {currency}", "",
                "&7喝下后 60 秒内不会触发陷阱", "", "{quick_buy}", "{buy_status}"));
        addContentMessages(yml, "sponge", ConfigPath.SHOP_PATH_CATEGORY_UTILITY, "{color}海绵", Arrays.asList("&7花费：{cost} {currency}", "",
                "&7用来吸水不错", "", "{quick_buy}", "{buy_status}"));
        addContentMessages(yml, "Compact Pop-up Tower", ConfigPath.SHOP_PATH_CATEGORY_UTILITY, "{color}袖珍弹出塔", Arrays.asList("&7花费：{cost} {currency}", "", "&7放置后自动搭建", "&7一座防御塔。", "", "{quick_buy}", "{buy_status}"));

        yml.addDefault(Messages.MEANING_NO_TRAP, "无陷阱！");
        yml.addDefault(Messages.FORMAT_SPECTATOR_TARGET, "{targetTeamColor}{targetDisplayName}");
        yml.addDefault(Messages.FORMAT_UPGRADE_TRAP_COST, "&7花费：{currencyColor}{cost} {currency}");
        yml.addDefault(Messages.FORMAT_UPGRADE_COLOR_CAN_AFFORD, "&e");
        yml.addDefault(Messages.FORMAT_UPGRADE_COLOR_CANT_AFFORD, "&c");
        yml.addDefault(Messages.FORMAT_UPGRADE_COLOR_UNLOCKED, "&a");
        yml.addDefault(Messages.FORMAT_UPGRADE_TIER_LOCKED, "&7");
        yml.addDefault(Messages.FORMAT_UPGRADE_TIER_UNLOCKED, "&a");
        yml.addDefault(Messages.UPGRADES_LORE_REPLACEMENT_CLICK_TO_BUY, "{color}点击购买！");
        yml.addDefault(Messages.UPGRADES_LORE_REPLACEMENT_INSUFFICIENT_MONEY, "{color}你没有足够的{currency}！");
        yml.addDefault(Messages.UPGRADES_LORE_REPLACEMENT_LOCKED, "&c已锁定");
        yml.addDefault(Messages.UPGRADES_LORE_REPLACEMENT_UNLOCKED, "{color}已解锁");
        yml.addDefault(Messages.UPGRADES_UPGRADE_BOUGHT_CHAT, "&a{player}购买了&6{upgradeName}");
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_NAME.replace("{name}", "forge").replace("{tier}", "tier-1"), "{color}铁锻炉");
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_LORE.replace("{name}", "forge"),
                Arrays.asList("&7提高你方岛屿上的", "&7资源生成速度。", "", "{tier_1_color}等级 1：+50% 生成速率，&b{tier_1_cost} {tier_1_currency}",
                        "{tier_2_color}等级 2：+100% 生成速率，&b{tier_2_cost} {tier_2_currency}",
                        "{tier_3_color}等级 3：生成绿宝石，&b{tier_3_cost} {tier_3_currency}",
                        "{tier_4_color}等级 4：+200% 生成速率，&b{tier_4_cost} {tier_4_currency}", ""));
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_NAME.replace("{name}", "forge").replace("{tier}", "tier-2"), "{color}金锭熔炉");
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_NAME.replace("{name}", "forge").replace("{tier}", "tier-3"), "{color}绿宝石熔炉");
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_NAME.replace("{name}", "forge").replace("{tier}", "tier-4"), "{color}无尽熔炉");
        yml.addDefault(Messages.UPGRADES_CATEGORY_ITEM_NAME_PATH + "traps", "&e购买陷阱");
        yml.addDefault(Messages.UPGRADES_CATEGORY_ITEM_LORE_PATH + "traps", Arrays.asList("&7已购买的陷阱将会", "&7在右侧排队。", "", "&e点击查看！"));
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_NAME.replace("{name}", "swords").replace("{tier}", "tier-1"), "{color}锋利附魔");
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_LORE.replace("{name}", "swords"),
                Arrays.asList("&7你的队伍将永久获得", "&7所有剑的锋利附魔 I，以及", "&7斧的锋利附魔 I！", "", "{tier_1_color}花费：&b{tier_1_cost} {tier_1_currency}", ""));
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_NAME.replace("{name}", "armor").replace("{tier}", "tier-1"), "{color}护甲强化 I");
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_LORE.replace("{name}", "armor"),
                Arrays.asList("&7你的队伍将永久获得", "&7所有护甲的保护附魔！", "", "{tier_1_color}等级 1：保护 I， &b{tier_1_cost} {tier_1_currency}",
                        "{tier_2_color}等级 2：保护 II，&b{tier_2_cost} {tier_2_currency}", "{tier_3_color}等级 3：保护 III，&b{tier_3_cost} {tier_3_currency}",
                        "{tier_4_color}等级 4：保护 IV，&b{tier_4_cost} {tier_4_currency}", ""));
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_NAME.replace("{name}", "armor").replace("{tier}", "tier-2"), "{color}护甲强化 II");
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_NAME.replace("{name}", "armor").replace("{tier}", "tier-3"), "{color}护甲强化 III");
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_NAME.replace("{name}", "armor").replace("{tier}", "tier-4"), "{color}护甲强化 IV");
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_NAME.replace("{name}", "miner").replace("{tier}", "tier-1"), "{color}疯狂矿工 I");
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_LORE.replace("{name}", "miner"),
                Arrays.asList("&7你队伍中的所有玩家", "&7将永久获得急迫效果。", "", "{tier_1_color}等级 1：急迫 I, &b{tier_1_cost} {tier_1_currency}",
                        "{tier_2_color}等级 2：急迫 II, &b{tier_2_cost} {tier_2_currency}", ""));
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_NAME.replace("{name}", "miner").replace("{tier}", "tier-2"), "{color}疯狂矿工 II");
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_NAME.replace("{name}", "heal-pool").replace("{tier}", "tier-1"), "{color}治愈池");
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_LORE.replace("{name}", "heal-pool"),
                Arrays.asList("&7在你的基地周围", "&7生成治愈池！", "", "{tier_1_color}花费：&b{tier_1_cost} {tier_1_currency}", ""));
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_NAME.replace("{name}", "dragon").replace("{tier}", "tier-1"), "{color}末影龙升级");
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_LORE.replace("{name}", "dragon"),
                Arrays.asList("&7绝杀模式期间，你方将拥有", "&72 条龙，而不是 1 条。", "", "{tier_1_color}花费：&b{tier_1_cost} {tier_1_currency}", ""));
        yml.addDefault(Messages.UPGRADES_SEPARATOR_ITEM_NAME_PATH + "glass", "&8⬆&7可购买");
        yml.addDefault(Messages.UPGRADES_SEPARATOR_ITEM_LORE_PATH + "glass", Collections.singletonList("&8⬇&7陷阱队列"));
        yml.addDefault(Messages.UPGRADES_TRAP_SLOT_ITEM_NAME_PATH + "first", "{color}陷阱 #1：{name}");
        yml.addDefault(Messages.UPGRADES_TRAP_SLOT_ITEM_LORE1_PATH + "first", Arrays.asList("&7第一个进入你基地的敌人", "&7将触发这个", "&7陷阱！"));
        yml.addDefault(Messages.UPGRADES_TRAP_SLOT_ITEM_LORE2_PATH + "first",
                Arrays.asList("", "&7下个购买的陷阱将在此", "&7进入队列。", "&7陷阱的花费将会随着", "&7队列长度增加", "", "&7下个陷阱花费：&b{cost} {currency}"));
        yml.addDefault(Messages.UPGRADES_TRAP_SLOT_ITEM_NAME_PATH + "second", "{color}陷阱 #2：{name}");
        yml.addDefault(Messages.UPGRADES_TRAP_SLOT_ITEM_LORE1_PATH + "second", Arrays.asList("&7第二个进入你基地的敌人", "&7将触发这个", "&7陷阱！"));
        yml.addDefault(Messages.UPGRADES_TRAP_SLOT_ITEM_LORE2_PATH + "second",
                Arrays.asList("", "&7下个购买的陷阱将在此", "&7进入队列。", "&7陷阱的花费将会随着", "&7队列长度增加", "", "&7下个陷阱花费：&b{cost} {currency}"));
        yml.addDefault(Messages.UPGRADES_TRAP_SLOT_ITEM_NAME_PATH + "third", "{color}陷阱 #3：{name}");
        yml.addDefault(Messages.UPGRADES_TRAP_SLOT_ITEM_LORE1_PATH + "third", Arrays.asList("&7第三个进入你基地的敌人", "&7将触发这个", "&7陷阱！"));
        yml.addDefault(Messages.UPGRADES_TRAP_SLOT_ITEM_LORE2_PATH + "third",
                Arrays.asList("", "&7下个购买的陷阱将在此", "&7进入队列。", "&7陷阱的花费将会随着", "&7队列长度增加", "", "&7下个陷阱花费：&b{cost} {currency}"));
        yml.addDefault(Messages.UPGRADES_BASE_TRAP_ITEM_NAME_PATH + "1", "{color}致盲陷阱");
        yml.addDefault(Messages.UPGRADES_BASE_TRAP_ITEM_LORE_PATH + "1", Arrays.asList("&7使敌人获得持续 5 秒的", "&7失明与缓慢效果。", ""));
        yml.addDefault(Messages.UPGRADES_BASE_TRAP_ITEM_NAME_PATH + "2", "{color}反击陷阱");
        yml.addDefault(Messages.UPGRADES_BASE_TRAP_ITEM_LORE_PATH + "2", Arrays.asList("&7使基地附近的队友获得", "&7持续 15 秒的速度 I。", ""));
        yml.addDefault(Messages.UPGRADES_BASE_TRAP_ITEM_NAME_PATH + "3", "{color}报警陷阱");
        yml.addDefault(Messages.UPGRADES_BASE_TRAP_ITEM_LORE_PATH + "3", Arrays.asList("&7显示隐身的敌人，", "&7以及他们的名字和队伍。", ""));
        yml.addDefault(Messages.UPGRADES_BASE_TRAP_ITEM_NAME_PATH + "4", "{color}挖掘疲劳陷阱");
        yml.addDefault(Messages.UPGRADES_BASE_TRAP_ITEM_LORE_PATH + "4", Arrays.asList("&7使敌人获得持续 10 秒的", "&7挖掘疲劳效果。", ""));
        yml.addDefault(Messages.UPGRADES_SEPARATOR_ITEM_NAME_PATH + "back", "&a返回");
        yml.addDefault(Messages.UPGRADES_SEPARATOR_ITEM_LORE_PATH + "back", Collections.singletonList("&7回到队伍升级菜单"));
        yml.addDefault(Messages.UPGRADES_CATEGORY_GUI_NAME_PATH + "traps", "&8将陷阱加入队列");
        yml.addDefault(Messages.UPGRADES_TRAP_QUEUE_LIMIT, "&c陷阱队列已满！");
        yml.addDefault(Messages.UPGRADES_TRAP_DEFAULT_MSG, "&c&l{trap}被触发了！");
        yml.addDefault(Messages.UPGRADES_TRAP_DEFAULT_TITLE, "&c陷阱触发！");
        yml.addDefault(Messages.UPGRADES_TRAP_DEFAULT_SUBTITLE, "&f你队伍的{trap}被触发了！");
        yml.addDefault(Messages.UPGRADES_TRAP_CUSTOM_MSG + "3", "&c&l报警陷阱被{color}&l{team}的&7&l{player}&c&l触发了！");
        yml.addDefault(Messages.UPGRADES_TRAP_CUSTOM_TITLE + "3", "&c&l警报！！！");
        yml.addDefault(Messages.UPGRADES_TRAP_CUSTOM_SUBTITLE + "3", "{color}{team}&f触发了陷阱！");
        yml.addDefault(Messages.CONFIG_ITEM_NAME_NOT_SET, "&c名称未设置");
        yml.addDefault(Messages.CONFIG_ITEM_LORE_NOT_SET, Collections.singletonList("&c说明未设置"));
        yml.addDefault(Messages.UPGRADES_MENU_GUI_NAME_DEFAULT, "&8队伍升级");
        save();
        setPrefix(m(Messages.PREFIX));
        setPrefixStatic(m(Messages.PREFIX));
    }
}
