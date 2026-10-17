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

package com.andrei1058.bedwars.api.language;

import com.andrei1058.bedwars.api.configuration.ConfigPath;

@SuppressWarnings("WeakerAccess")
public class Messages {

    public static String PREFIX = "prefix";

    public static String LOBBY_MENU_TITLE = "lobby-menu.title";
    public static String LOBBY_MENU_ITEM_NAME = "lobby-menu.item-name";
    public static String LOBBY_MENU_ITEM_LORE = "lobby-menu.item-lore";
    public static String LOBBY_MENU_STATS = "lobby-menu.stats";
    public static String LOBBY_MENU_QUICK_BUY = "lobby-menu.quick-buy";
    public static String QUICK_BUY_EDITOR_TITLE = "quick-buy-editor.title";
    public static String QUICK_BUY_EDITOR_SELECT_SLOT_TITLE = "quick-buy-editor.select-slot-title";
    public static String QUICK_BUY_EDITOR_LOADING = "quick-buy-editor.loading";
    public static String QUICK_BUY_EDITOR_LOAD_FAILED = "quick-buy-editor.load-failed";
    public static String QUICK_BUY_EDITOR_ADD = "quick-buy-editor.add";
    public static String QUICK_BUY_EDITOR_ADDED = "quick-buy-editor.added";
    public static String QUICK_BUY_EDITOR_REMOVE = "quick-buy-editor.remove";
    public static String QUICK_BUY_EDITOR_SELECT_SLOT = "quick-buy-editor.select-slot";
    public static String QUICK_BUY_EDITOR_BACK = "quick-buy-editor.back";

    public static String COMMAND_READY_SUCCESS = "cmd-ready-success";
    public static String COMMAND_UNREADY_SUCCESS = "cmd-unready-success";
    public static String COMMAND_READY_COUNTDOWN_SHORTENED = "cmd-ready-countdown-shortened";
    public static String COMMAND_TOGGLE_READY_ENABLED = "cmd-toggle-ready-enabled";
    public static String COMMAND_TOGGLE_READY_DISABLED = "cmd-toggle-ready-disabled";
    public static String COMMAND_READY_DISABLED = "cmd-ready-disabled";
    public static String COMMAND_READY_NOT_IN_GAME = "cmd-ready-not-in-game";
    public static String COMMAND_READY_NOT_WAITING = "cmd-ready-not-waiting";
    public static String COMMAND_READY_SPECTATOR = "cmd-ready-spectator";
    public static String COMMAND_READY_ITEM_NAME_READY = "cmd-ready-item-name-ready";
    public static String COMMAND_READY_ITEM_NAME_NOT_READY = "cmd-ready-item-name-not-ready";
    public static String COMMAND_READY_ITEM_LORE_READY = "cmd-ready-item-lore-ready";
    public static String COMMAND_READY_ITEM_LORE_NOT_READY = "cmd-ready-item-lore-not-ready";

    public static String TEAM_SELECTOR_TITLE = "team-selector.title";
    public static String TEAM_SELECTOR_ITEM_NAME = "team-selector.item-name";
    public static String TEAM_SELECTOR_ITEM_LORE = "team-selector.item-lore";
    public static String TEAM_SELECTOR_NONE = "team-selector.none";
    public static String TEAM_SELECTOR_TEAM_LORE = "team-selector.team-lore";
    public static String TEAM_SELECTOR_SELECTED = "team-selector.selected";
    public static String TEAM_SELECTOR_CANCEL_NAME = "team-selector.cancel-name";
    public static String TEAM_SELECTOR_CANCEL_LORE = "team-selector.cancel-lore";
    public static String TEAM_SELECTOR_FULL = "team-selector.full";
    public static String TEAM_SELECTOR_PREVIOUS_PAGE = "team-selector.previous-page";
    public static String TEAM_SELECTOR_NEXT_PAGE = "team-selector.next-page";

    /**
     * next event related
     */
    public static String NEXT_EVENT_DIAMOND_UPGRADE_II = "next-event-diamondII";
    public static String NEXT_EVENT_DIAMOND_UPGRADE_III = "next-event-diamondIII";
    public static String NEXT_EVENT_EMERALD_UPGRADE_II = "next-event-emeraldII";
    public static String NEXT_EVENT_EMERALD_UPGRADE_III = "next-event-emeraldIII";
    public static String NEXT_EVENT_BEDS_DESTROY = "next-event-beds-destroy";
    public static String NEXT_EVENT_DRAGON_SPAWN = "next-event-dragon-spawn";
    public static String NEXT_EVENT_GAME_END = "next-event-game-end";
    public static String NEXT_EVENT_TITLE_ANNOUNCE_BEDS_DESTROYED = "next-event-beds-destroy-title";
    public static String NEXT_EVENT_SUBTITLE_ANNOUNCE_BEDS_DESTROYED = "next-event-beds-destroy-sub-title";
    public static String NEXT_EVENT_CHAT_ANNOUNCE_BEDS_DESTROYED = "next-event-beds-destroy-chat";
    public static String NEXT_EVENT_TITLE_ANNOUNCE_SUDDEN_DEATH = "next-event-sudden-death-title";
    public static String NEXT_EVENT_SUBTITLE_ANNOUNCE_SUDDEN_DEATH = "next-event-sudden-death-sub-title";
    public static String NEXT_EVENT_CHAT_ANNOUNCE_SUDDEN_DEATH = "next-event-sudden-death-chat";

    /**
     * General commands reply
     */
    public static String COMMAND_MAIN = "cmd-main-list";
    public static String COMMAND_LANG_LIST_HEADER = "cmd-lang-list-header";
    public static String COMMAND_LANG_LIST_FORMAT = "cmd-lang-list-format";
    public static String COMMAND_LANG_USAGE = "cmd-lang-usage";
    public static String COMMAND_LANG_SELECTED_NOT_EXIST = "cmd-lang-not-exist";
    public static String COMMAND_LANG_SELECTED_SUCCESSFULLY = "cmd-lang-set";
    public static String COMMAND_LANG_USAGE_DENIED = "cmd-lang-not-set";
    public static String COMMAND_JOIN_USAGE = "cmd-join-usage";
    public static String COMMAND_JOIN_GROUP_OR_ARENA_NOT_FOUND = "cmd-join-not-found";
    public static String COMMAND_JOIN_DENIED_IS_FULL = "cmd-join-arena-full";
    public static String COMMAND_JOIN_NO_EMPTY_FOUND = "cmd-join-arenas-full";
    public static String COMMAND_JOIN_DENIED_IS_FULL_OF_VIPS = "cmd-join-arena-full-vips";
    public static String COMMAND_JOIN_DENIED_PARTY_TOO_BIG = "cmd-join-party-big";
    public static String COMMAND_JOIN_DENIED_NOT_PARTY_LEADER = "cmd-join-not-leader";
    public static String COMMAND_JOIN_PLAYER_JOIN_MSG = "cmd-join-success";
    public static String COMMAND_JOIN_SPECTATOR_MSG = "cmd-join-spectate";
    public static String COMMAND_JOIN_SPECTATOR_DENIED_MSG = "cmd-join-spectate-denied";
    public static String COMMAND_TP_PLAYER_NOT_FOUND = "cmd-tp-player-not-found";
    public static String COMMAND_TP_NOT_IN_ARENA = "cmd-tp-not-in-arena";
    public static String COMMAND_TP_NOT_STARTED = "cmd-tp-not-started";
    public static String COMMAND_TP_USAGE = "cmd-tp-usage";
    public static String REJOIN_NO_ARENA = "cmd-rejoin-no-arena";
    public static String REJOIN_DENIED = "cmd-rejoin-denied";
    public static String REJOIN_ALLOWED = "cmd-rejoin-allowed";
    public static String COMMAND_REJOIN_PLAYER_RECONNECTED = "cmd-rejoin-player-reconnected";
    public static String COMMAND_LEAVE_MSG = "cmd-leave";
    public static String COMMAND_NOT_ALLOWED_IN_GAME = "cmd-blocked-in-game";
    public static String COMMAND_LEAVE_DENIED_NOT_IN_ARENA = "cmd-not-in-arena";
    public static String COMMAND_PARTY_HELP = "cmd-party-help";
    public static String COMMAND_PARTY_INVITE_USAGE = "cmd-party-invite-usage";
    public static String COMMAND_PARTY_INVITE_SENT = "cmd-party-invite";
    public static String COMMAND_PARTY_INVITE_DENIED_PLAYER_OFFLINE = "cmd-party-offline";
    public static String COMMAND_PARTY_INVITE_SENT_TARGET_RECEIVE_MSG = "cmd-party-invite-received";
    public static String COMMAND_PARTY_INVITE_DENIED_CANNOT_INVITE_YOURSELF = "cmd-party-invite-self";
    public static String COMMAND_PARTY_ACCEPT_DENIED_NO_INVITE = "cmd-party-no-invite";
    public static String COMMAND_PARTY_ACCEPT_DENIED_ALREADY_IN_PARTY = "cmd-party-already-in";
    public static String COMMAND_PARTY_INSUFFICIENT_PERMISSIONS = "cmd-party-no-perm";
    public static String COMMAND_PARTY_ACCEPT_USAGE = "cmd-party-accept-usage";
    public static String COMMAND_PARTY_ACCEPT_SUCCESS = "cmd-party-join";
    public static String COMMAND_PARTY_GENERAL_DENIED_NOT_IN_PARTY = "cmd-party-not-in";
    public static String COMMAND_PARTY_LEAVE_DENIED_IS_OWNER_NEEDS_DISBAND = "cmd-party-cant-leave";
    public static String COMMAND_PARTY_LEAVE_SUCCESS = "cmd-party-leave";
    public static String COMMAND_PARTY_DISBAND_SUCCESS = "cmd-party-disband";
    public static String COMMAND_PARTY_REMOVE_USAGE = "cmd-party-remove-usage";
    public static String COMMAND_PARTY_REMOVE_SUCCESS = "cmd-party-remove";
    public static String COMMAND_PARTY_REMOVE_DENIED_TARGET_NOT_PARTY_MEMBER = "cmd-party-remove-not-in";
    public static String COMMAND_PARTY_PROMOTE_SUCCESS = "cmd-party-promote-owner";
    public static String COMMAND_PARTY_PROMOTE_OWNER = "cmd-party-owner";
    public static String COMMAND_PARTY_PROMOTE_NEW_OWNER = "cmd-party-owner-changed";
    public static String COMMAND_PARTY_INFO_OWNER = "cmd-party-info-owner";
    public static String COMMAND_PARTY_INFO_PLAYERS = "cmd-party-info-players";
    public static String COMMAND_PARTY_INFO_PLAYER = "cmd-party-info-player";
    public static String COMMAND_NOT_FOUND_OR_INSUFF_PERMS = "cmd-not-found";
    public static String COMMAND_FORCESTART_NOT_IN_GAME = "cmd-start-no-game";
    public static String COMMAND_FORCESTART_SUCCESS = "cmd-start";
    public static String COMMAND_START_NOT_ENOUGH_PLAYERS = "cmd-start-not-enough-players";
    public static String COMMAND_FORCESTART_NO_PERM = "cmd-start-no-perm";
    public static String COMMAND_ECHO_USAGE = "cmd-echo-usage";
    public static String COMMAND_ECHO_SENT = "cmd-echo-sent";
    public static String COMMAND_ECHO_NOT_IN_ARENA = "cmd-echo-not-in-arena";
    public static String COMMAND_ECHOTO_USAGE = "cmd-echoto-usage";
    public static String COMMAND_ECHOTO_SENT = "cmd-echoto-sent";
    public static String COMMAND_ECHOALL_USAGE = "cmd-echoall-usage";
    public static String COMMAND_ECHOALL_SENT = "cmd-echoall-sent";
    public static String COMMAND_ARENA_NOT_FOUND = "cmd-arena-not-found";
    public static String COMMAND_NOT_PLAYER = "cmd-not-player";
    public static String COMMAND_COOLDOWN = "cmd-cooldown";
    public static String COMMAND_SPECTATOR_MODE_SWITCH_VANILLA = "cmd-specmode-vanilla";
    public static String COMMAND_SPECTATOR_MODE_SWITCH_NORMAL = "cmd-specmode-normal";

    /**
     * Arena join/ leave related
     */
    public static String ARENA_JOIN_VIP_KICK = "arena-kicked-by-vip";
    public static String ARENA_START_COUNTDOWN_STOPPED_INSUFF_PLAYERS_CHAT = "arena-countdown-stopped";
    //public static String ARENA_PLAYER_QUIT = "player.quit";
    public static String ARENA_RESTART_PLAYER_KICK = "arena-restart-kick";
    public static String ARENA_JOIN_DENIED_SELECTOR = "arena-join-denied-selector";
    public static String ARENA_JOIN_DENIED_NO_PROXY = "arena-join-denied-no-bwp";
    public static String ARENA_SPECTATE_DENIED_SELECTOR = "arena-spectate-denied-selector";
    public static String ARENA_LEAVE_PARTY_DISBANDED = "arena-leave-party-disbanded";

    /**
     * Arena status/ status change related
     */
    public static String ARENA_STATUS_WAITING_NAME = "arena-status-waiting";
    public static String ARENA_STATUS_STARTING_NAME = "arena-status-starting";
    public static String ARENA_STATUS_PLAYING_NAME = "arena-status-playing";
    public static String ARENA_STATUS_RESTARTING_NAME = "arena-status-restarting";
    public static String ARENA_STATUS_START_PLAYER_TITLE = "arena-start-title";
    public static String ARENA_STATUS_START_PLAYER_TUTORIAL = "arena-start-tutorial";
    public static String ARENA_STATUS_START_COUNTDOWN_CHAT = "arena-start-countdown";
    public static String ARENA_STATUS_START_COUNTDOWN_TITLE = "arena-start-countdown-title";
    public static String ARENA_STATUS_START_COUNTDOWN_SUB_TITLE = "arena-start-countdown-subtitle";
    public static String ARENA_STATUS_START_COUNTDOWN_CANCELLED_TITLE = "arena-countdown-stopped-title";
    public static String ARENA_STATUS_START_COUNTDOWN_CANCELLED_SUB_TITLE = "arena-countdown-stopped-subtitle";
    public static String ARENA_DISPLAY_GROUP_PATH = "display-group-";

    /**
     * Arena GUI related
     */
    public static String ARENA_GUI_INV_NAME = "arena-selector-gui-name";
    public static String ARENA_GUI_ARENA_CONTENT_NAME = "arena-selector-content-name";
    public static String ARENA_GUI_ARENA_CONTENT_LORE = "arena-selector-content-lore";
    public static String ARENA_GUI_SKIPPED_ITEM_NAME = "arena-selector-skipped-item-name";
    public static String ARENA_GUI_SKIPPED_ITEM_LORE = "arena-selector-skipped-item-lore";

    /**
     * Spectator related
     */
    public static String ARENA_SPECTATOR_TELEPORTER_GUI_NAME = "spectator-tp-gui-name";
    //{player} - returns display name, {prefix} - returns the player rank
    public static String ARENA_SPECTATOR_TELEPORTER_GUI_HEAD_NAME = "spectator-tp-gui-head-name";
    //{health}, {food}
    public static String ARENA_SPECTATOR_TELEPORTER_GUI_HEAD_LORE = "spectator-tp-gui-head-lore";

    public static String ARENA_SPECTATOR_LEAVE_ITEM_NAME = "spectator-tp-name";
    public static String ARENA_SPECTATOR_LEAVE_ITEM_LORE = "spectator-tp-lore";

    public static String ARENA_SPECTATOR_FIRST_PERSON_ENTER_TITLE = "spectator-first-person-enter-title";
    public static String ARENA_SPECTATOR_FIRST_PERSON_ENTER_SUBTITLE = "spectator-first-person-enter-subtitle";
    public static String ARENA_SPECTATOR_FIRST_PERSON_LEAVE_TITLE = "spectator-first-person-quit-title";
    public static String ARENA_SPECTATOR_FIRST_PERSON_LEAVE_SUBTITLE = "spectator-first-person-quit-subtitle";

    /**
     * Stats related
     */
    public static String PLAYER_STATS_GUI_PATH = "stats";
    public static String PLAYER_STATS_GUI_INV_NAME = PLAYER_STATS_GUI_PATH + "-inv-name";

    /**
     * Arena generators related
     */
    public static String GENERATOR_HOLOGRAM_TIER = "generator-tier";
    public static String GENERATOR_HOLOGRAM_TYPE_DIAMOND = "generator-diamond";
    public static String GENERATOR_HOLOGRAM_TYPE_EMERALD = "generator-emerald";
    public static String GENERATOR_HOLOGRAM_TIMER = "generator-timer";
    public static String GENERATOR_UPGRADE_CHAT_ANNOUNCEMENT = "generator-upgrade-announce";

    /**
     * General formatting
     */
    public static String FORMATTING_CHAT_LOBBY = "format-chat-lobby";
    public static String FORMATTING_CHAT_WAITING = "format-chat-waiting";
    public static String FORMATTING_CHAT_SHOUT = "format-chat-global";
    public static String FORMATTING_CHAT_SERVER = "format-chat-server";
    public static String CHAT_GLOBAL_USAGE = "chat-global-usage";
    public static String JOIN_WELCOME = "join-welcome";
    public static String HINT_CHAT_GLOBAL = "hint-chat-global";
    public static String HINT_REJOIN = "hint-rejoin";
    public static String HINT_READY_ITEM = "hint-ready-item";
    public static String HINT_CHAT_SHOUT = "hint-chat-shout";
    public static String HINT_SPECTATOR_MODE = "hint-spectator-mode";
    public static final String COMMAND_SPECTATORS_DISPLAY = "command-spectators-display";
    public static final String COMMAND_SPECTATORS_ONLY = "command-spectators-only";
    public static final String SPECTATOR_VISIBILITY_SHOWN = "spectator-visibility-shown";
    public static final String SPECTATOR_VISIBILITY_HIDDEN = "spectator-visibility-hidden";
    public static final String SPECTATOR_VISIBILITY_STATE_SHOWN = "spectator-visibility-state-shown";
    public static final String SPECTATOR_VISIBILITY_STATE_HIDDEN = "spectator-visibility-state-hidden";
    public static final String SPECTATOR_VISIBILITY_ACTION_SHOW = "spectator-visibility-action-show";
    public static final String SPECTATOR_VISIBILITY_ACTION_HIDE = "spectator-visibility-action-hide";
    public static final String SPECTATOR_VISIBILITY_SAVE_FAILED = "spectator-visibility-save-failed";
    public static String FORMATTING_CHAT_TEAM = "format-chat-team";
    public static String FORMATTING_CHAT_SPECTATOR = "format-chat-spectator";
    public static String FORMATTING_SCOREBOARD_DATE = "format-sb-date";
    public static String FORMATTING_SCOREBOARD_TEAM_GENERIC = "format-sb-team-generic";
    public static String FORMATTING_SCOREBOARD_HEALTH = "format-sb-health";

    // TAB
    public static String FORMATTING_SB_TAB_BASE = "format-tab";

    // TAB LOBBY
    public static String FORMATTING_SB_TAB_LOBBY_HEADER = FORMATTING_SB_TAB_BASE + ".lobby.header";
    public static String FORMATTING_SB_TAB_LOBBY_FOOTER = FORMATTING_SB_TAB_BASE + ".lobby.footer";
    public static String FORMATTING_SB_TAB_LOBBY_PREFIX = FORMATTING_SB_TAB_BASE + ".lobby.prefix";
    public static String FORMATTING_SB_TAB_LOBBY_SUFFIX = FORMATTING_SB_TAB_BASE + ".lobby.suffix";

    // TAB WAITING
    public static String FORMATTING_SB_TAB_WAITING_HEADER = FORMATTING_SB_TAB_BASE+".waiting.player.header";
    public static String FORMATTING_SB_TAB_WAITING_FOOTER = FORMATTING_SB_TAB_BASE+".waiting.player.footer";
    public static String FORMATTING_SB_TAB_WAITING_PREFIX = FORMATTING_SB_TAB_BASE+".waiting.player.prefix";
    public static String FORMATTING_SB_TAB_WAITING_SUFFIX = FORMATTING_SB_TAB_BASE+".waiting.player.suffix";
    // TAB WAITING FOR SPECTATORS
    public static String FORMATTING_SB_TAB_WAITING_HEADER_SPEC = FORMATTING_SB_TAB_BASE+".waiting.spectator.header";
    public static String FORMATTING_SB_TAB_WAITING_FOOTER_SPEC = FORMATTING_SB_TAB_BASE+".waiting.spectator.footer";
    public static String FORMATTING_SB_TAB_WAITING_PREFIX_SPEC = FORMATTING_SB_TAB_BASE+".waiting.spectator.prefix";
    public static String FORMATTING_SB_TAB_WAITING_SUFFIX_SPEC = FORMATTING_SB_TAB_BASE+".waiting.spectator.suffix";

    // TAB STARTING
    public static String FORMATTING_SB_TAB_STARTING_HEADER = FORMATTING_SB_TAB_BASE+".starting.player.header";
    public static String FORMATTING_SB_TAB_STARTING_FOOTER = FORMATTING_SB_TAB_BASE+".starting.player.footer";
    public static String FORMATTING_SB_TAB_STARTING_PREFIX = FORMATTING_SB_TAB_BASE+".starting.player.prefix";
    public static String FORMATTING_SB_TAB_STARTING_SUFFIX = FORMATTING_SB_TAB_BASE+".starting.player.suffix";
    // TAB STARTING FOR SPECTATORS
    public static String FORMATTING_SB_TAB_STARTING_HEADER_SPEC = FORMATTING_SB_TAB_BASE+".starting.spectator.header";
    public static String FORMATTING_SB_TAB_STARTING_FOOTER_SPEC = FORMATTING_SB_TAB_BASE+".starting.spectator.footer";
    public static String FORMATTING_SB_TAB_STARTING_PREFIX_SPEC = FORMATTING_SB_TAB_BASE+".starting.spectator.prefix";
    public static String FORMATTING_SB_TAB_STARTING_SUFFIX_SPEC = FORMATTING_SB_TAB_BASE+".starting.spectator.suffix";

    // TAB PLAYING
    public static String FORMATTING_SB_TAB_PLAYING_HEADER = FORMATTING_SB_TAB_BASE+".playing.alive.header";
    public static String FORMATTING_SB_TAB_PLAYING_FOOTER = FORMATTING_SB_TAB_BASE+".playing.alive.footer";
    public static String FORMATTING_SB_TAB_PLAYING_PREFIX = FORMATTING_SB_TAB_BASE+".playing.alive.prefix";
    public static String FORMATTING_SB_TAB_PLAYING_SUFFIX = FORMATTING_SB_TAB_BASE+".playing.alive.suffix";

    // TAB PLAYING-ELIMINATED
    public static String FORMATTING_SB_TAB_PLAYING_ELM_HEADER = FORMATTING_SB_TAB_BASE+".playing.eliminated.header";
    public static String FORMATTING_SB_TAB_PLAYING_ELM_FOOTER = FORMATTING_SB_TAB_BASE+".playing.eliminated.footer";
    public static String FORMATTING_SB_TAB_PLAYING_ELM_PREFIX = FORMATTING_SB_TAB_BASE+".playing.eliminated.prefix";
    public static String FORMATTING_SB_TAB_PLAYING_ELM_SUFFIX = FORMATTING_SB_TAB_BASE+".playing.eliminated.suffix";
    public static String FORMATTING_SB_TAB_PLAYING_ELM_DEPARTED = FORMATTING_SB_TAB_BASE+".playing.eliminated.departed";

    // TAB PLAYING FOR SPECTATORS
    public static String FORMATTING_SB_TAB_PLAYING_SPEC_HEADER = FORMATTING_SB_TAB_BASE+".playing.spectator.header";
    public static String FORMATTING_SB_TAB_PLAYING_SPEC_FOOTER = FORMATTING_SB_TAB_BASE+".playing.spectator.footer";
    public static String FORMATTING_SB_TAB_PLAYING_SPEC_PREFIX = FORMATTING_SB_TAB_BASE+".playing.spectator.prefix";
    public static String FORMATTING_SB_TAB_PLAYING_SPEC_SUFFIX = FORMATTING_SB_TAB_BASE+".playing.spectator.suffix";

    // TAB RESTARTING FOR WINNERS ALIVE
    public static String FORMATTING_SB_TAB_RESTARTING_WIN1_HEADER = FORMATTING_SB_TAB_BASE+".restarting.winner-alive.header";
    public static String FORMATTING_SB_TAB_RESTARTING_WIN1_FOOTER = FORMATTING_SB_TAB_BASE+".restarting.winner-alive.footer";
    public static String FORMATTING_SB_TAB_RESTARTING_WIN1_PREFIX = FORMATTING_SB_TAB_BASE+".restarting.winner-alive.prefix";
    public static String FORMATTING_SB_TAB_RESTARTING_WIN1_SUFFIX = FORMATTING_SB_TAB_BASE+".restarting.winner-alive.suffix";

    // TAB RESTARTING FOR WINNERS DEAD
    public static String FORMATTING_SB_TAB_RESTARTING_WIN2_HEADER = FORMATTING_SB_TAB_BASE+".restarting.winner-dead.header";
    public static String FORMATTING_SB_TAB_RESTARTING_WIN2_FOOTER = FORMATTING_SB_TAB_BASE+".restarting.winner-dead.footer";
    public static String FORMATTING_SB_TAB_RESTARTING_WIN2_PREFIX = FORMATTING_SB_TAB_BASE+".restarting.winner-dead.prefix";
    public static String FORMATTING_SB_TAB_RESTARTING_WIN2_SUFFIX = FORMATTING_SB_TAB_BASE+".restarting.winner-dead.suffix";


    // TAB RESTARTING FOR LOSERS
    public static String FORMATTING_SB_TAB_RESTARTING_ELM_HEADER = FORMATTING_SB_TAB_BASE+".restarting.loser.header";
    public static String FORMATTING_SB_TAB_RESTARTING_ELM_FOOTER = FORMATTING_SB_TAB_BASE+".restarting.loser.footer";
    public static String FORMATTING_SB_TAB_RESTARTING_ELM_PREFIX = FORMATTING_SB_TAB_BASE+".restarting.loser.prefix";
    public static String FORMATTING_SB_TAB_RESTARTING_ELM_SUFFIX = FORMATTING_SB_TAB_BASE+".restarting.loser.suffix";

    // TAB RESTARTING FOR SPECTATORS
    public static String FORMATTING_SB_TAB_RESTARTING_SPEC_HEADER = FORMATTING_SB_TAB_BASE+".restarting.spectator.header";
    public static String FORMATTING_SB_TAB_RESTARTING_SPEC_FOOTER = FORMATTING_SB_TAB_BASE+".restarting.spectator.footer";
    public static String FORMATTING_SB_TAB_RESTARTING_SPEC_PREFIX = FORMATTING_SB_TAB_BASE+".restarting.spectator.prefix";
    public static String FORMATTING_SB_TAB_RESTARTING_SPEC_SUFFIX = FORMATTING_SB_TAB_BASE+".restarting.spectator.suffix";

    public static String FORMATTING_SCOREBOARD_TEAM_ELIMINATED = "format-sb-team-eliminated";
    public static String FORMATTING_SCOREBOARD_BED_DESTROYED = "format-sb-bed-destroyed";
    public static String FORMATTING_SCOREBOARD_TEAM_ALIVE = "format-sb-team-alive";
    public static String FORMATTING_SCOREBOARD_NEXEVENT_TIMER = "format-sb-generator-timer";
    public static String FORMATTING_SCOREBOARD_YOUR_TEAM = "format-sb-you";
    public static String FORMATTING_ACTION_BAR_TRACKING = "format-action-tracking";
    public static String FORMATTING_TEAM_WINNER_FORMAT = "format-winner-team";
    public static String FORMATTING_SOLO_WINNER_FORMAT = "format-winner-solo";
    //public static String FORMATTING_TAB_LIST = "format.tablist";
    public static String FORMATTING_GENERATOR_TIER1 = "format-tier1";
    public static String FORMATTING_GENERATOR_TIER2 = "format-tier2";
    public static String FORMATTING_GENERATOR_TIER3 = "format-tier3";
    public static String FORMATTING_DESPAWNABLE_UTILITY_NPC_HEALTH = "format-despawnable-health";
    public static String FORMATTING_STATS_DATE_FORMAT = "format-stats-time";
    public static String FORMAT_PAPI_PLAYER_TEAM_TEAM = "format-papi-player-team";
    public static String FORMAT_PAPI_PLAYER_TEAM_SHOUT = "format-papi-player-shout";
    public static String FORMAT_PAPI_PLAYER_TEAM_SPECTATOR = "format-papi-player-spectator";
    public static String FORMAT_UPGRADE_TIER_LOCKED = "format-tier-color-locked";
    public static String FORMAT_UPGRADE_TIER_UNLOCKED = "format-tier-color-unlocked";
    public static String FORMAT_UPGRADE_COLOR_CANT_AFFORD = "format-upgrade-color-cant-afford";
    public static String FORMAT_UPGRADE_COLOR_CAN_AFFORD = "format-upgrade-color-can-afford";
    public static String FORMAT_UPGRADE_COLOR_UNLOCKED = "format-upgrade-color-unlocked";
    public static String FORMAT_UPGRADE_TRAP_COST = "format-upgrade-trap-cost";
    public static String FORMAT_SPECTATOR_TARGET = "format-spectator-target";

    /**
     * Meaning/ Translations
     */
    public static String MEANING_FULL = "meaning-full";
    public static String MEANING_SHOUT = "meaning-shout";
    public static String MEANING_NEVER = "meaning-never";
    public static String MEANING_NOBODY = "meaning-nobody";
    public static String MEANING_IRON_SINGULAR = "meaning-iron-singular";
    public static String MEANING_IRON_PLURAL = "meaning-iron-plural";
    public static String MEANING_GOLD_SINGULAR = "meaning-gold-singular";
    public static String MEANING_GOLD_PLURAL = "meaning-gold-plural";
    public static String MEANING_EMERALD_SINGULAR = "meaning-emerald-singular";
    public static String MEANING_EMERALD_PLURAL = "meaning-emerald-plural";
    public static String MEANING_DIAMOND_SINGULAR = "meaning-diamond-singular";
    public static String MEANING_DIAMOND_PLURAL = "meaning-diamond-plural";
    public static String MEANING_NO_TRAP = "meaning-no-trap";

    /**
     * Scoreboard related
     */
    public static String SCOREBOARD_LOBBY = "sidebar.lobby";
    public static String SCOREBOARD_DEFAULT_WAITING = "sidebar.Default.waiting.player";
    public static String SCOREBOARD_DEFAULT_WAITING_SPEC = "sidebar.Default.waiting.spectator";
    public static String SCOREBOARD_DEFAULT_STARTING = "sidebar.Default.starting.player";
    public static String SCOREBOARD_DEFAULT_STARTING_SPEC = "sidebar.Default.starting.spectator";
    public static String SCOREBOARD_DEFAULT_PLAYING = "sidebar.Default.playing.alive";
    public static String SCOREBOARD_DEFAULT_PLAYING_SPEC = "sidebar.Default.playing.spectator";
    public static String SCOREBOARD_DEFAULT_PLAYING_SPEC_ELIMINATED = "sidebar.Default.playing.eliminated";

    public static String SCOREBOARD_DEFAULT_RESTARTING_SPEC = "sidebar.Default.restarting.spectator";
    public static String SCOREBOARD_DEFAULT_RESTARTING_WIN1 = "sidebar.Default.restarting.winner-alive";
    public static String SCOREBOARD_DEFAULT_RESTARTING_WIN2 = "sidebar.Default.restarting.winner-eliminated";
    public static String SCOREBOARD_DEFAULT_RESTARTING_LOSER = "sidebar.Default.restarting.loser";

    /**
     * Player interact related
     */
    public static String INTERACT_CANNOT_PLACE_BLOCK = "interact-cant-place";
    public static String INTERACT_CANNOT_BREAK_BLOCK = "interact-cant-break";
    public static String INTERACT_CANNOT_BREAK_OWN_BED = "interact-cant-destroy-bed";
    public static String INTERACT_BED_DESTROY_CHAT_ANNOUNCEMENT = "interact-bed-destroy-chat";
    public static String INTERACT_BED_DESTROY_TITLE_ANNOUNCEMENT = "interact-bed-destroy-title";
    public static String INTERACT_BED_DESTROY_SUBTITLE_ANNOUNCEMENT = "interact-bed-destroy-subtitle";
    public static String INTERACT_BED_DESTROY_CHAT_ANNOUNCEMENT_TO_VICTIM = "interact-bed-destroy-team";
    public static String INTERACT_CHEST_CANT_OPEN_TEAM_ELIMINATED = "interact-cant-open-chest";
    public static String INTERACT_INVISIBILITY_REMOVED_DAMGE_TAKEN = "interact-invisibility-removed-damaged";

    /**
     * PvP related
     */
    public static String PLAYER_DIE_RESPAWN_TITLE = "player-respawn-title";
    public static String PLAYER_DIE_RESPAWN_SUBTITLE = "player-respawn-subtitle";
    public static String PLAYER_DIE_RESPAWN_CHAT = "player-respawn-timer-chat";
    public static String PLAYER_DIE_RESPAWNED_TITLE = "player-respawned-title";
    public static String PLAYER_DIE_ELIMINATED_CHAT = "player-eliminated-chat";

    public static String PLAYER_DIE_VOID_FALL_REGULAR_KILL = "player-die-void-regular";
    public static String PLAYER_DIE_VOID_FALL_FINAL_KILL = "player-die-void-final";
    public static String PLAYER_DIE_KNOCKED_IN_VOID_REGULAR_KILL = "player-die-knocked-void-regular";
    public static String PLAYER_DIE_KNOCKED_IN_VOID_FINAL_KILL = "player-die-knocked-void-final";
    public static String PLAYER_DIE_KNOCKED_BY_REGULAR_KILL = "player-die-knocked-fall-regular";
    public static String PLAYER_DIE_KNOCKED_BY_FINAL_KILL = "player-die-knocked-fall-final";
    public static String PLAYER_DIE_EXPLOSION_WITH_SOURCE_REGULAR_KILL = "player-die-bomb-regular";
    public static String PLAYER_DIE_EXPLOSION_WITH_SOURCE_FINAL_KILL = "player-die-bomb-final";
    public static String PLAYER_DIE_EXPLOSION_WITHOUT_SOURCE_REGULAR = "player-die-bomb2-regular";
    public static String PLAYER_DIE_EXPLOSION_WITHOUT_SOURCE_FINAL_KILL = "player-die-bomb2-final";
    public static String PLAYER_DIE_PVP_REGULAR_KILL = "player-die-attack-regular";
    public static String PLAYER_DIE_PVP_FINAL_KILL = "player-die-attack-final";
    public static String PLAYER_DIE_UNKNOWN_REASON_REGULAR = "player-die-unknown-regular";
    public static String PLAYER_DIE_UNKNOWN_REASON_FINAL_KILL = "player-die-unknown-final";
    public static String PLAYER_DIE_SHOOT_REGULAR = "player-die-shoot-regular";
    public static String PLAYER_DIE_SHOOT_FINAL_KILL = "player-die-shoot-final";
    public static String PLAYER_DIE_DEBUG_REGULAR = "player-die-bedbug-regular";
    public static String PLAYER_DIE_DEBUG_FINAL_KILL = "player-die-bedbug-final";
    public static String PLAYER_DIE_IRON_GOLEM_REGULAR = "player-die-golem-regular";
    public static String PLAYER_DIE_IRON_GOLEM_FINAL_KILL = "player-die-golem-final";
    public static String PLAYER_DIE_PVP_LOG_OUT_REGULAR = "player-die-pvp-log-out-regular";
    public static String PLAYER_DIE_PVP_LOG_OUT_FINAL = "player-die-pvp-log-out-final";

    public static String PLAYER_DIE_REWARD_DIAMOND = "player-loot-diamond";
    public static String PLAYER_DIE_REWARD_IRON = "player-loot-iron";
    public static String PLAYER_DIE_REWARD_GOLD = "player-loot-gold";
    public static String PLAYER_DIE_REWARD_EMERALD = "player-loot-emerald";

    public static String PLAYER_HIT_BOW = "player-hit-bow";

    /**
     * Misc
     */
    public static String GAME_END_GAME_OVER_PLAYER_TITLE = "game-end-game-over-title";
    public static String GAME_END_VICTORY_PLAYER_TITLE = "game-end-victory-title";
    public static String GAME_END_TOP_PLAYER_CHAT = "game-end-top-chat";
    public static String GAME_END_TEAM_WON_CHAT = "game-end-winner-team";


    public static String BED_HOLOGRAM_DEFEND = "bed-hologram-defend";
    public static String BED_HOLOGRAM_DESTROYED = "bed-hologram-destroyed";
    public static String TEAM_ELIMINATED_CHAT = "team-eliminated";


    /**
     * Upgrades/ Shop
     */
    public static String NPC_NAME_TEAM_UPGRADES = "npc-team-upgrades";
    public static String NPC_NAME_TEAM_SHOP = "npc-team-shop";
    public static String NPC_NAME_SOLO_UPGRADES = "npc-solo-upgrades";
    public static String NPC_NAME_SOLO_SHOP = "npc-solo-shop";

    public static String UPGRADES_MENU_GUI_NAME_PATH = "upgrades-menu-gui-name-";
    public static String UPGRADES_MENU_GUI_NAME_DEFAULT = "upgrades-menu-gui-name";
    public static String UPGRADES_CATEGORY_GUI_NAME_PATH = "upgrades-category-gui-name-";
    public static String UPGRADES_CATEGORY_ITEM_NAME_PATH = "upgrades-category-item-name-";
    public static String UPGRADES_CATEGORY_ITEM_LORE_PATH = "upgrades-category-item-lore-";
    public static String UPGRADES_SEPARATOR_ITEM_NAME_PATH = "upgrades-separator-item-name-";
    public static String UPGRADES_SEPARATOR_ITEM_LORE_PATH = "upgrades-separator-item-lore-";
    public static String UPGRADES_TRAP_SLOT_ITEM_NAME_PATH = "upgrades-trap-slot-item-name-";
    public static String UPGRADES_TRAP_SLOT_ITEM_LORE1_PATH = "upgrades-trap-slot-item-lore1-";
    public static String UPGRADES_TRAP_SLOT_ITEM_LORE2_PATH = "upgrades-trap-slot-item-lore2-";
    public static String UPGRADES_UPGRADE_TIER_ITEM_NAME = "upgrades-upgrade-name-{name}-{tier}";
    public static String UPGRADES_UPGRADE_TIER_ITEM_LORE = "upgrades-upgrade-lore-{name}";
    public static String UPGRADES_BASE_TRAP_ITEM_NAME_PATH = "upgrades-base-trap-name-";
    public static String UPGRADES_BASE_TRAP_ITEM_LORE_PATH = "upgrades-base-trap-lore-";
    public static String UPGRADES_TRAP_CUSTOM_TITLE = "upgrades-base-trap-title-";
    public static String UPGRADES_TRAP_CUSTOM_SUBTITLE = "upgrades-base-trap-subtitle-";
    public static String UPGRADES_TRAP_CUSTOM_MSG = "upgrades-base-trap-msg-";

    public static String UPGRADES_LORE_REPLACEMENT_INSUFFICIENT_MONEY = "upgrades-lore-insuff-money";
    public static String UPGRADES_LORE_REPLACEMENT_CLICK_TO_BUY = "upgrades-lore-click-buy";
    public static String UPGRADES_LORE_REPLACEMENT_UNLOCKED = "upgrades-lore-unlocked";
    public static String UPGRADES_LORE_REPLACEMENT_LOCKED = "upgrades-lore-locked";
    public static String UPGRADES_UPGRADE_BOUGHT_CHAT = "upgrades-new-purchase";
    public static String UPGRADES_TRAP_QUEUE_LIMIT = "upgrades-trap-queue-full";
    public static String UPGRADES_TRAP_DEFAULT_TITLE = "upgrades-trap-default-title";
    public static String UPGRADES_TRAP_DEFAULT_SUBTITLE = "upgrades-trap-default-subtitle";
    public static String UPGRADES_TRAP_DEFAULT_MSG = "upgrades-trap-default-msg";

    public static String SHOP_NEW_PURCHASE = "shop-new-purchase";
    public static String SHOP_UTILITY_NPC_SILVERFISH_NAME = "shop-utility-silverfish";
    public static String SHOP_UTILITY_NPC_IRON_GOLEM_NAME = "shop-utility-iron-golem";
    public static String SHOP_INSUFFICIENT_MONEY = "shop-insuff-money";
    public static String SHOP_INSUFFICIENT_INVENTORY_SPACE = "shop-insuff-inv-space";
    public static String SHOP_ALREADY_BOUGHT = "shop-already-bought";
    public static final String SHOP_PATH = "shop-items-messages";
    public static final String SHOP_LORE_STATUS_CANT_AFFORD = "shop-lore-status-cant-afford";
    public static final String SHOP_LORE_STATUS_CAN_BUY = "shop-lore-status-can-buy";
    public static final String SHOP_LORE_STATUS_MAXED = "shop-lore-status-tier-maxed";
    public static final String SHOP_LORE_STATUS_ARMOR = "shop-lore-status-armor";
    public static final String SHOP_LORE_QUICK_ADD = "shop-lore-quick-add";
    public static final String SHOP_LORE_QUICK_REMOVE = "shop-lore-quick-remove";
    public static final String SHOP_INDEX_NAME = SHOP_PATH + ".inventory-name";
    public static final String SHOP_QUICK_ADD_NAME = SHOP_PATH + ".quick-buy-add-inventory-name";
    public static final String SHOP_SEPARATOR_NAME = SHOP_PATH + ".separator-item-name";
    public static final String SHOP_SEPARATOR_LORE = SHOP_PATH + ".separator-item-lore";
    public static final String SHOP_QUICK_BUY_NAME = SHOP_PATH + ".quick-buy-item-name";
    public static final String SHOP_QUICK_BUY_LORE = SHOP_PATH + ".quick-buy-item-lore";
    public static final String SHOP_QUICK_EMPTY_NAME = SHOP_PATH + ".quick-buy-empty-item-name";
    public static final String SHOP_QUICK_EMPTY_LORE = SHOP_PATH + ".quick-buy-empty-item-lore";

    public static final String SHOP_CATEGORY_INVENTORY_NAME = SHOP_PATH + ".%category%.inventory-name";
    public static final String SHOP_CATEGORY_ITEM_NAME = SHOP_PATH + ".%category%.category-item-name";
    public static final String SHOP_CATEGORY_ITEM_LORE = SHOP_PATH + ".%category%.category-item-lore";
    public static final String SHOP_CONTENT_TIER_ITEM_NAME = SHOP_PATH + ".%category%.content-item-%content%-name";
    public static final String SHOP_CONTENT_TIER_ITEM_LORE = SHOP_PATH + ".%category%.content-item-%content%-lore";
    public static final String SHOP_CAN_BUY_COLOR = SHOP_PATH + ".can-buy-color";
    public static final String SHOP_CANT_BUY_COLOR = SHOP_PATH + ".cant-buy-color";

    /* MultiArena Lobby Item Messages */
    public static final String GENERAL_CONFIGURATION_LOBBY_ITEMS_NAME = ConfigPath.GENERAL_CONFIGURATION_LOBBY_ITEMS_PATH + "-%path%-name";
    public static final String GENERAL_CONFIGURATION_LOBBY_ITEMS_LORE = ConfigPath.GENERAL_CONFIGURATION_LOBBY_ITEMS_PATH + "-%path%-lore";

    /* Spectator Items Messages */
    public static final String GENERAL_CONFIGURATION_SPECTATOR_ITEMS_NAME = ConfigPath.GENERAL_CONFIGURATION_SPECTATOR_ITEMS_PATH + "-%path%-name";
    public static final String GENERAL_CONFIGURATION_SPECTATOR_ITEMS_LORE = ConfigPath.GENERAL_CONFIGURATION_SPECTATOR_ITEMS_PATH + "-%path%-lore";

    /* Arena waiting Items Messages */
    public static final String GENERAL_CONFIGURATION_WAITING_ITEMS_NAME = ConfigPath.GENERAL_CONFIGURATION_PRE_GAME_ITEMS_PATH + "-%path%-name";
    public static final String GENERAL_CONFIGURATION_WAITING_ITEMS_LORE = ConfigPath.GENERAL_CONFIGURATION_PRE_GAME_ITEMS_PATH + "-%path%-lore";



    // DEPRECATIONS FOR REMOVAL

    // LOBBY TAB
    @Deprecated(forRemoval = true)
    public static String FORMATTING_SIDEBAR_TAB_HEADER_LOBBY = FORMATTING_SB_TAB_LOBBY_HEADER;
    @Deprecated(forRemoval = true)
    public static String FORMATTING_SIDEBAR_TAB_FOOTER_LOBBY = FORMATTING_SB_TAB_LOBBY_FOOTER;
    @Deprecated(forRemoval = true)
    public static String FORMATTING_SCOREBOARD_TAB_PREFIX_LOBBY = FORMATTING_SB_TAB_LOBBY_PREFIX;
    @Deprecated(forRemoval = true)
    public static String FORMATTING_SCOREBOARD_TAB_SUFFIX_LOBBY = FORMATTING_SB_TAB_LOBBY_SUFFIX;
    // WAITING TAB
    @Deprecated(forRemoval = true)
    public static String FORMATTING_SIDEBAR_TAB_HEADER_WAITING = FORMATTING_SB_TAB_WAITING_HEADER;
    @Deprecated(forRemoval = true)
    public static String FORMATTING_SIDEBAR_TAB_FOOTER_WAITING = FORMATTING_SB_TAB_WAITING_FOOTER;
    @Deprecated(forRemoval = true)
    public static String FORMATTING_SCOREBOARD_TAB_PREFIX_WAITING = FORMATTING_SB_TAB_WAITING_PREFIX;
    @Deprecated(forRemoval = true)
    public static String FORMATTING_SCOREBOARD_TAB_SUFFIX_WAITING = FORMATTING_SB_TAB_WAITING_SUFFIX;
    // STARTING TAB
    @Deprecated(forRemoval = true)
    public static String FORMATTING_SIDEBAR_TAB_HEADER_STARTING = FORMATTING_SB_TAB_STARTING_HEADER;
    @Deprecated(forRemoval = true)
    public static String FORMATTING_SIDEBAR_TAB_FOOTER_STARTING = FORMATTING_SB_TAB_STARTING_FOOTER;
    @Deprecated(forRemoval = true)
    public static String FORMATTING_SCOREBOARD_TAB_PREFIX_STARTING = FORMATTING_SB_TAB_STARTING_PREFIX;
    @Deprecated(forRemoval = true)
    public static String FORMATTING_SCOREBOARD_TAB_SUFFIX_STARTING = FORMATTING_SB_TAB_STARTING_SUFFIX;
    // PLAYING TAB
    @Deprecated(forRemoval = true)
    public static String FORMATTING_SIDEBAR_TAB_HEADER_PLAYING = FORMATTING_SB_TAB_PLAYING_HEADER;
    @Deprecated(forRemoval = true)
    public static String FORMATTING_SIDEBAR_TAB_FOOTER_PLAYING = FORMATTING_SB_TAB_PLAYING_FOOTER;
    @Deprecated(forRemoval = true)
    public static String FORMATTING_SCOREBOARD_TAB_PREFIX_PLAYING = FORMATTING_SB_TAB_PLAYING_PREFIX;
    @Deprecated(forRemoval = true)
    public static String FORMATTING_SCOREBOARD_TAB_SUFFIX_PLAYING = FORMATTING_SB_TAB_PLAYING_SUFFIX;
    // SPECTATING TAB
    @Deprecated(forRemoval = true)
    public static String FORMATTING_SIDEBAR_TAB_HEADER_SPECTATOR = FORMATTING_SB_TAB_PLAYING_SPEC_HEADER;
    @Deprecated(forRemoval = true)
    public static String FORMATTING_SIDEBAR_TAB_FOOTER_SPECTATOR = FORMATTING_SB_TAB_PLAYING_SPEC_FOOTER;
    @Deprecated(forRemoval = true)
    public static String FORMATTING_SCOREBOARD_TAB_PREFIX_SPECTATOR = FORMATTING_SB_TAB_PLAYING_SPEC_PREFIX;
    @Deprecated(forRemoval = true)
    public static String FORMATTING_SCOREBOARD_TAB_SUFFIX_SPECTATOR = FORMATTING_SB_TAB_PLAYING_SPEC_SUFFIX;
    // RESTARTING TAB
    @Deprecated(forRemoval = true)
    public static String FORMATTING_SCOREBOARD_TAB_PREFIX_PRESTARTING = FORMATTING_SB_TAB_RESTARTING_WIN2_PREFIX;
    @Deprecated(forRemoval = true)
    public static String FORMATTING_SCOREBOARD_TAB_PREFIX_RESTARTING = FORMATTING_SB_TAB_RESTARTING_WIN2_PREFIX;
    @Deprecated(forRemoval = true)
    public static String FORMATTING_SCOREBOARD_TAB_SUFFIX_PRESTARTING = FORMATTING_SB_TAB_RESTARTING_WIN2_SUFFIX;
    @Deprecated(forRemoval = true)
    public static String FORMATTING_SCOREBOARD_TAB_SUFFIX_RESTARTING = FORMATTING_SB_TAB_RESTARTING_WIN2_SUFFIX;
    @Deprecated(forRemoval = true)
    public static String FORMATTING_SIDEBAR_TAB_HEADER_RESTARTING = FORMATTING_SB_TAB_RESTARTING_ELM_HEADER;
    @Deprecated(forRemoval = true)
    public static String FORMATTING_SIDEBAR_TAB_FOOTER_RESTARTING = FORMATTING_SB_TAB_PLAYING_SPEC_HEADER;

    public static String ARENA_LOAD_ALREADY_QUEUED = "arena-load-already-queued";
    public static String ARENA_LOAD_ALREADY_ENABLED = "arena-load-already-enabled";
    public static String ARENA_LOAD_NO_TEAMS = "arena-load-no-teams";
    public static String ARENA_LOAD_NOT_ENOUGH_TEAMS = "arena-load-not-enough-teams";
    public static String ARENA_LOAD_WORLD_MISSING = "arena-load-world-missing";
    public static String ARENA_LOAD_INVALID_TEAM_COLOR = "arena-load-invalid-team-color";
    public static String ARENA_LOAD_TEAM_SETTING_MISSING = "arena-load-team-setting-missing";
    public static String ARENA_LOAD_DIAMOND_GENERATOR_MISSING = "arena-load-diamond-generator-missing";
    public static String ARENA_LOAD_EMERALD_GENERATOR_MISSING = "arena-load-emerald-generator-missing";
    public static String ARENA_LOAD_WAITING_SPAWN_MISSING = "arena-load-waiting-spawn-missing";
    public static String SETUP_GUI_TITLE = "setup-gui-title";
    public static String SETUP_GUI_ASSISTED_NAME = "setup-gui-assisted-name";
    public static String SETUP_GUI_ASSISTED_LORE = "setup-gui-assisted-lore";
    public static String SETUP_GUI_ADVANCED_NAME = "setup-gui-advanced-name";
    public static String SETUP_GUI_ADVANCED_LORE = "setup-gui-advanced-lore";
    public static String SETUP_LOADING = "setup-loading";
    public static String SETUP_CANCELLED = "setup-cancelled";
    public static String SETUP_TELEPORTED = "setup-teleported";
    public static String SETUP_GREETING = "setup-greeting";
    public static String SETUP_WAITING_SPAWN_PROMPT = "setup-waiting-spawn-prompt";
    public static String SETUP_WAITING_SPAWN_EXPLANATION = "setup-waiting-spawn-explanation";
    public static String SETUP_WAITING_SPAWN_CLICK = "setup-waiting-spawn-click";
    public static String SETUP_WAITING_SPAWN_HOVER = "setup-waiting-spawn-hover";
    public static String SETUP_COMMAND_LIST_HINT = "setup-command-list-hint";
    public static String SETUP_COMMAND_LIST_HOVER = "setup-command-list-hover";
    public static String SETUP_TEAM_GENERATOR_MARKER = "setup-team-generator-marker";
    public static String SETUP_TEAM_SPAWN_MARKER = "setup-team-spawn-marker";
    public static String SETUP_TEAM_BED_MARKER = "setup-team-bed-marker";
    public static String SETUP_TEAM_SHOP_MARKER = "setup-team-shop-marker";
    public static String SETUP_TEAM_UPGRADE_MARKER = "setup-team-upgrade-marker";
    public static String SETUP_TEAM_KILL_DROPS_MARKER = "setup-team-kill-drops-marker";
    public static String SETUP_GENERATOR_MARKER = "setup-generator-marker";
    public static String SETUP_GENERATOR_IRON = "setup-generator-iron";
    public static String SETUP_GENERATOR_GOLD = "setup-generator-gold";
    public static String SETUP_GENERATOR_EMERALD = "setup-generator-emerald";
    public static String SETUP_GENERATOR_DIAMOND = "setup-generator-diamond";
    public static String SETUP_AVAILABLE_TEAMS = "setup-available-teams";
    public static String SETUP_START_FAILED = "setup-start-failed";
    public static String SIGN_SAVED = "sign-saved";
    public static String SIGN_NO_ARENAS = "sign-no-arenas";
    public static String PLUGIN_RELOAD_KICK = "plugin-reload-kick";
    public static String ARENA_UNEXPECTED_PLAYER_KICK = "arena-unexpected-player-kick";

    // Sensitive commands and arena setup messages.
    public static String COMMAND_ARENA_GROUP_ARENA_NOT_FOUND = "command-arena-group-arena-not-found";
    public static String COMMAND_ARENA_GROUP_ASSIGNED = "command-arena-group-assigned";
    public static String COMMAND_ARENA_GROUP_CREATED = "command-arena-group-created";
    public static String COMMAND_ARENA_GROUP_CREATE_HOVER = "command-arena-group-create-hover";
    public static String COMMAND_ARENA_GROUP_CREATE_USAGE = "command-arena-group-create-usage";
    public static String COMMAND_ARENA_GROUP_DEFAULT = "command-arena-group-default";
    public static String COMMAND_ARENA_GROUP_DELETED = "command-arena-group-deleted";
    public static String COMMAND_ARENA_GROUP_DISPLAY = "command-arena-group-display";
    public static String COMMAND_ARENA_GROUP_DISPLAY_HOVER = "command-arena-group-display-hover";
    public static String COMMAND_ARENA_GROUP_ENTRY = "command-arena-group-entry";
    public static String COMMAND_ARENA_GROUP_EXISTS = "command-arena-group-exists";
    public static String COMMAND_ARENA_GROUP_HEADER = "command-arena-group-header";
    public static String COMMAND_ARENA_GROUP_LIST_HOVER = "command-arena-group-list-hover";
    public static String COMMAND_ARENA_GROUP_LIST_USAGE = "command-arena-group-list-usage";
    public static String COMMAND_ARENA_GROUP_NOT_FOUND = "command-arena-group-not-found";
    public static String COMMAND_ARENA_GROUP_REMOVE_HOVER = "command-arena-group-remove-hover";
    public static String COMMAND_ARENA_GROUP_REMOVE_USAGE = "command-arena-group-remove-usage";
    public static String COMMAND_ARENA_GROUP_SET_HOVER = "command-arena-group-set-hover";
    public static String COMMAND_ARENA_GROUP_SET_USAGE = "command-arena-group-set-usage";
    public static String COMMAND_ARENA_GROUP_UNKNOWN = "command-arena-group-unknown";
    public static String COMMAND_ARENA_LIST_DISPLAY = "command-arena-list-display";
    public static String COMMAND_ARENA_LIST_DISPLAY_HOVER = "command-arena-list-display-hover";
    public static String COMMAND_ARENA_LIST_EMPTY = "command-arena-list-empty";
    public static String COMMAND_ARENA_LIST_ENTRY = "command-arena-list-entry";
    public static String COMMAND_ARENA_LIST_HEADER = "command-arena-list-header";
    public static String COMMAND_ARENA_LIST_NEXT_PAGE = "command-arena-list-next-page";
    public static String COMMAND_ARENA_LIST_SPACER = "command-arena-list-spacer";
    public static String COMMAND_ARENA_USAGE = "command-arena-usage";
    public static String COMMAND_BUILD_DISABLED = "command-build-disabled";
    public static String COMMAND_BUILD_DISPLAY = "command-build-display";
    public static String COMMAND_BUILD_DISPLAY_HOVER = "command-build-display-hover";
    public static String COMMAND_BUILD_ENABLED = "command-build-enabled";
    public static String COMMAND_CLONE_ARENA_COPY_ERROR = "command-clone-arena-copy-error";
    public static String COMMAND_CLONE_ARENA_DISABLE_FIRST = "command-clone-arena-disable-first";
    public static String COMMAND_CLONE_ARENA_DISPLAY = "command-clone-arena-display";
    public static String COMMAND_CLONE_ARENA_DISPLAY_HOVER = "command-clone-arena-display-hover";
    public static String COMMAND_CLONE_ARENA_EXISTS = "command-clone-arena-exists";
    public static String COMMAND_CLONE_ARENA_SUCCESS = "command-clone-arena-success";
    public static String COMMAND_CLONE_ARENA_USAGE = "command-clone-arena-usage";
    public static String COMMAND_DEL_ARENA_CONFIRM = "command-del-arena-confirm";
    public static String COMMAND_DEL_ARENA_DISPLAY = "command-del-arena-display";
    public static String COMMAND_DEL_ARENA_DISPLAY_HOVER = "command-del-arena-display-hover";
    public static String COMMAND_DEL_ARENA_NOT_FOUND = "command-del-arena-not-found";
    public static String COMMAND_DEL_ARENA_SUCCESS = "command-del-arena-success";
    public static String COMMAND_DEL_ARENA_USAGE = "command-del-arena-usage";
    public static String COMMAND_DEL_ARENA_WORLD_NOT_FOUND = "command-del-arena-world-not-found";
    public static String COMMAND_DISABLE_ARENA_ALREADY_DISABLED = "command-disable-arena-already-disabled";
    public static String COMMAND_DISABLE_ARENA_DISPLAY = "command-disable-arena-display";
    public static String COMMAND_DISABLE_ARENA_DISPLAY_HOVER = "command-disable-arena-display-hover";
    public static String COMMAND_DISABLE_ARENA_PLAYING = "command-disable-arena-playing";
    public static String COMMAND_DISABLE_ARENA_START = "command-disable-arena-start";
    public static String COMMAND_ENABLE_ARENA_ALREADY_ENABLED = "command-enable-arena-already-enabled";
    public static String COMMAND_ENABLE_ARENA_DISPLAY = "command-enable-arena-display";
    public static String COMMAND_ENABLE_ARENA_DISPLAY_HOVER = "command-enable-arena-display-hover";
    public static String COMMAND_ENABLE_ARENA_QUEUED = "command-enable-arena-queued";
    public static String COMMAND_ENABLE_ARENA_START = "command-enable-arena-start";
    public static String COMMAND_ENABLE_ARENA_USAGE = "command-enable-arena-usage";
    public static String COMMAND_NPC_ADD_HOVER = "command-npc-add-hover";
    public static String COMMAND_NPC_ADD_USAGE = "command-npc-add-usage";
    public static String COMMAND_NPC_CREATED = "command-npc-created";
    public static String COMMAND_NPC_DISPLAY = "command-npc-display";
    public static String COMMAND_NPC_DISPLAY_HOVER = "command-npc-display-hover";
    public static String COMMAND_NPC_MAIN_ADD_HOVER = "command-npc-main-add-hover";
    public static String COMMAND_NPC_MAIN_ADD_USAGE = "command-npc-main-add-usage";
    public static String COMMAND_NPC_MAIN_REMOVE_HOVER = "command-npc-main-remove-hover";
    public static String COMMAND_NPC_MAIN_REMOVE_USAGE = "command-npc-main-remove-usage";
    public static String COMMAND_NPC_NONE_NEARBY = "command-npc-none-nearby";
    public static String COMMAND_NPC_NONE_SET = "command-npc-none-set";
    public static String COMMAND_NPC_REMOVED = "command-npc-removed";
    public static String COMMAND_NPC_TARGET_GROUPS = "command-npc-target-groups";
    public static String COMMAND_RELOAD_ARENA = "command-reload-arena";
    public static String COMMAND_RELOAD_CONFIG = "command-reload-config";
    public static String COMMAND_RELOAD_DISPLAY = "command-reload-display";
    public static String COMMAND_RELOAD_DISPLAY_HOVER = "command-reload-display-hover";
    public static String COMMAND_RELOAD_LANGUAGE = "command-reload-language";
    public static String COMMAND_SETUP_ALREADY_IN_SESSION = "command-setup-already-in-session";
    public static String COMMAND_SETUP_ARENA_CAPITALS = "command-setup-arena-capitals";
    public static String COMMAND_SETUP_ARENA_DISABLE_FIRST = "command-setup-arena-disable-first";
    public static String COMMAND_SETUP_ARENA_DISPLAY = "command-setup-arena-display";
    public static String COMMAND_SETUP_ARENA_DISPLAY_HOVER = "command-setup-arena-display-hover";
    public static String COMMAND_SETUP_ARENA_SYMBOL = "command-setup-arena-symbol";
    public static String COMMAND_SETUP_ARENA_USAGE = "command-setup-arena-usage";
    public static String COMMAND_SETUP_AUTO_TEAMS_CONFIRM = "command-setup-auto-teams-confirm";
    public static String COMMAND_SETUP_AUTO_TEAMS_CONFIRM_HOVER = "command-setup-auto-teams-confirm-hover";
    public static String COMMAND_SETUP_AUTO_TEAMS_FOUND_ENTRY = "command-setup-auto-teams-found-entry";
    public static String COMMAND_SETUP_AUTO_TEAMS_FOUND_HEADER = "command-setup-auto-teams-found-header";
    public static String COMMAND_SETUP_AUTO_TEAMS_NONE = "command-setup-auto-teams-none";
    public static String COMMAND_SETUP_AUTO_TEAMS_SEARCH = "command-setup-auto-teams-search";
    public static String COMMAND_SETUP_AUTO_TEAMS_TIMEOUT = "command-setup-auto-teams-timeout";
    public static String COMMAND_SETUP_AUTO_TEAMS_WAITING_DESCRIPTION = "command-setup-auto-teams-waiting-description";
    public static String COMMAND_SETUP_AUTO_TEAMS_WAITING_HEADER = "command-setup-auto-teams-waiting-header";
    public static String COMMAND_SETUP_AUTO_TEAMS_WAITING_OPTIONAL = "command-setup-auto-teams-waiting-optional";
    public static String COMMAND_SETUP_AUTO_TEAMS_WAITING_SELECTION = "command-setup-auto-teams-waiting-selection";
    public static String COMMAND_SETUP_AVAILABLE_TEAMS = "command-setup-available-teams";
    public static String COMMAND_SETUP_BED_HOLOGRAM = "command-setup-bed-hologram";
    public static String COMMAND_SETUP_BED_MANUAL_HOVER = "command-setup-bed-manual-hover";
    public static String COMMAND_SETUP_BED_SET = "command-setup-bed-set";
    public static String COMMAND_SETUP_BED_SET_TITLE = "command-setup-bed-set-title";
    public static String COMMAND_SETUP_BED_SPAWN_HOVER = "command-setup-bed-spawn-hover";
    public static String COMMAND_SETUP_BED_STAND_ON_BED = "command-setup-bed-stand-on-bed";
    public static String COMMAND_SETUP_BED_STAND_ON_BED_TITLE = "command-setup-bed-stand-on-bed-title";
    public static String COMMAND_SETUP_BED_TEAM_HOVER = "command-setup-bed-team-hover";
    public static String COMMAND_SETUP_BUILD_HEIGHT_SET = "command-setup-build-height-set";
    public static String COMMAND_SETUP_BUILD_HEIGHT_USAGE = "command-setup-build-height-usage";
    public static String COMMAND_SETUP_CREATE_TEAM_COLORS = "command-setup-create-team-colors";
    public static String COMMAND_SETUP_CREATE_TEAM_CREATED = "command-setup-create-team-created";
    public static String COMMAND_SETUP_CREATE_TEAM_EXISTS = "command-setup-create-team-exists";
    public static String COMMAND_SETUP_CREATE_TEAM_INVALID_COLOR = "command-setup-create-team-invalid-color";
    public static String COMMAND_SETUP_CREATE_TEAM_USAGE = "command-setup-create-team-usage";
    public static String COMMAND_SETUP_KILL_DROPS_CREATE_TEAMS = "command-setup-kill-drops-create-teams";
    public static String COMMAND_SETUP_KILL_DROPS_CREATE_TEAMS_TITLE = "command-setup-kill-drops-create-teams-title";
    public static String COMMAND_SETUP_KILL_DROPS_HOLOGRAM = "command-setup-kill-drops-hologram";
    public static String COMMAND_SETUP_KILL_DROPS_HOVER = "command-setup-kill-drops-hover";
    public static String COMMAND_SETUP_KILL_DROPS_SET = "command-setup-kill-drops-set";
    public static String COMMAND_SETUP_KILL_DROPS_TEAM_ENTRY = "command-setup-kill-drops-team-entry";
    public static String COMMAND_SETUP_KILL_DROPS_TEAM_HOVER = "command-setup-kill-drops-team-hover";
    public static String COMMAND_SETUP_KILL_DROPS_TEAM_SET = "command-setup-kill-drops-team-set";
    public static String COMMAND_SETUP_KILL_DROPS_TEAM_SET_TITLE = "command-setup-kill-drops-team-set-title";
    public static String COMMAND_SETUP_KILL_DROPS_USAGE = "command-setup-kill-drops-usage";
    public static String COMMAND_SETUP_MAX_IN_TEAM_SET = "command-setup-max-in-team-set";
    public static String COMMAND_SETUP_MAX_IN_TEAM_USAGE = "command-setup-max-in-team-usage";
    public static String COMMAND_SETUP_NEARBY_TEAM_NOT_FOUND = "command-setup-nearby-team-not-found";
    public static String COMMAND_SETUP_NEARBY_TEAM_NOT_FOUND_TITLE = "command-setup-nearby-team-not-found-title";
    public static String COMMAND_SETUP_NOT_IN_SESSION = "command-setup-not-in-session";
    public static String COMMAND_SETUP_SHOP_HOLOGRAM = "command-setup-shop-hologram";
    public static String COMMAND_SETUP_SHOP_HOVER = "command-setup-shop-hover";
    public static String COMMAND_SETUP_SHOP_OTHER_USAGE = "command-setup-shop-other-usage";
    public static String COMMAND_SETUP_SHOP_SET = "command-setup-shop-set";
    public static String COMMAND_SETUP_SHOP_TEAM_HOVER = "command-setup-shop-team-hover";
    public static String COMMAND_SETUP_TEAM_ENTRY = "command-setup-team-entry";
    public static String COMMAND_SETUP_TEAM_MANUAL_HINT = "command-setup-team-manual-hint";
    public static String COMMAND_SETUP_TEAM_NOT_FOUND = "command-setup-team-not-found";
    public static String COMMAND_SETUP_TEAM_SELECT_ENTRY = "command-setup-team-select-entry";
    public static String COMMAND_SETUP_TEAM_SET_SPAWN_FIRST = "command-setup-team-set-spawn-first";
    public static String COMMAND_SETUP_TEAM_SPAWN_HOVER = "command-setup-team-spawn-hover";
    public static String COMMAND_SETUP_TYPE_AVAILABLE = "command-setup-type-available";
    public static String COMMAND_SETUP_TYPE_CHANGED = "command-setup-type-changed";
    public static String COMMAND_SETUP_TYPE_OPTION = "command-setup-type-option";
    public static String COMMAND_SETUP_TYPE_OPTION_HOVER = "command-setup-type-option-hover";
    public static String COMMAND_SETUP_TYPE_USAGE = "command-setup-type-usage";
    public static String COMMAND_SETUP_UPGRADE_HOLOGRAM = "command-setup-upgrade-hologram";
    public static String COMMAND_SETUP_UPGRADE_HOVER = "command-setup-upgrade-hover";
    public static String COMMAND_SETUP_UPGRADE_SET = "command-setup-upgrade-set";
    public static String COMMAND_SETUP_UPGRADE_TEAM_HOVER = "command-setup-upgrade-team-hover";
    public static String COMMAND_SETUP_WAITING_POS_OPTION = "command-setup-waiting-pos-option";
    public static String COMMAND_SETUP_WAITING_POS_OPTION_HOVER = "command-setup-waiting-pos-option-hover";
    public static String COMMAND_SETUP_WAITING_POS_REMAINING = "command-setup-waiting-pos-remaining";
    public static String COMMAND_SETUP_WAITING_POS_SET = "command-setup-waiting-pos-set";
    public static String COMMAND_SETUP_WAITING_POS_TEAMS_REMINDER = "command-setup-waiting-pos-teams-reminder";
    public static String COMMAND_SETUP_WAITING_POS_USAGE = "command-setup-waiting-pos-usage";
    public static String COMMAND_SETUP_WAITING_SPAWN_SET = "command-setup-waiting-spawn-set";
    public static String COMMAND_SET_LOBBY_DISPLAY = "command-set-lobby-display";
    public static String COMMAND_SET_LOBBY_DISPLAY_HOVER = "command-set-lobby-display-hover";
    public static String COMMAND_SET_LOBBY_IN_ARENA = "command-set-lobby-in-arena";
    public static String COMMAND_SET_LOBBY_NOT_SET = "command-set-lobby-not-set";
    public static String COMMAND_SET_LOBBY_SET = "command-set-lobby-set";
    public static String COMMAND_SET_LOBBY_SUCCESS = "command-set-lobby-success";

    public static String COMMAND_ADMIN_HEADER = "cmd-admin-header";
    public static String COMMAND_CONSOLE_SAFEMODE = "cmd-console-safemode";
    public static String COMMAND_CONSOLE_NO_COMMANDS = "cmd-console-no-commands";
    public static String COMMAND_LOBBY_NOT_SET = "cmd-lobby-not-set";
    public static String COMMAND_FORCE_END_GAME_DISPLAY = "cmd-force-end-game-display";
    public static String COMMAND_FORCE_END_GAME_DISPLAY_HOVER = "cmd-force-end-game-display-hover";
    public static String COMMAND_FORCE_END_GAME_NOT_PLAYING = "cmd-force-end-game-not-playing";
    public static String COMMAND_FORCE_END_GAME_SUCCESS = "cmd-force-end-game-success";
    public static String COMMAND_FORCE_START_DISPLAY = "cmd-force-start-display";
    public static String COMMAND_FORCE_START_DISPLAY_HOVER = "cmd-force-start-display-hover";
    public static String COMMAND_START_DISPLAY = "cmd-start-display";
    public static String COMMAND_START_DISPLAY_HOVER = "cmd-start-display-hover";
    public static String COMMAND_READY_DISPLAY = "cmd-ready-display";
    public static String COMMAND_GUI_DISPLAY = "cmd-gui-display";
    public static String COMMAND_GUI_DISPLAY_HOVER = "cmd-gui-display-hover";
    public static String COMMAND_JOIN_DISPLAY = "cmd-join-display";
    public static String COMMAND_JOIN_DISPLAY_HOVER = "cmd-join-display-hover";
    public static String COMMAND_LANG_DISPLAY = "cmd-lang-display";
    public static String COMMAND_LANG_DISPLAY_HOVER = "cmd-lang-display-hover";
    public static String COMMAND_LEAVE_DISPLAY = "cmd-leave-display";
    public static String COMMAND_LEAVE_DISPLAY_HOVER = "cmd-leave-display-hover";
    public static String COMMAND_STATS_DISPLAY = "cmd-stats-display";
    public static String COMMAND_STATS_DISPLAY_HOVER = "cmd-stats-display-hover";
    public static String COMMAND_SPEC_MODE_DISPLAY = "cmd-spec-mode-display";
    public static String COMMAND_SPEC_MODE_DISPLAY_HOVER = "cmd-spec-mode-display-hover";
    public static String COMMAND_ECHO_DISPLAY = "cmd-echo-display";
    public static String COMMAND_ECHO_HOVER = "cmd-echo-hover";
    public static String COMMAND_ECHO_PREFIX = "cmd-echo-prefix";
    public static String COMMAND_ECHOTO_DISPLAY = "cmd-echoto-display";
    public static String COMMAND_ECHOTO_HOVER = "cmd-echoto-hover";
    public static String COMMAND_ECHOALL_DISPLAY = "cmd-echoall-display";
    public static String COMMAND_ECHOALL_HOVER = "cmd-echoall-hover";
    public static String COMMAND_LIST_DISPLAY = "cmd-list-display";
    public static String COMMAND_LIST_DISPLAY_HOVER = "cmd-list-display-hover";
    public static String COMMAND_LIST_SETUP_HEADER = "cmd-list-setup-header";
    public static String COMMAND_LIST_CREDITS = "cmd-list-credits";
    public static String COMMAND_LIST_CREDITS_HOVER = "cmd-list-credits-hover";
    public static String COMMAND_LIST_NOT_SET = "cmd-list-not-set";
    public static String COMMAND_LIST_SET = "cmd-list-set";
    public static String COMMAND_LIST_OPTIONAL = "cmd-list-optional";
    public static String COMMAND_LIST_ALL_SET = "cmd-list-all-set";
    public static String COMMAND_LIST_REMAINING = "cmd-list-remaining";
    public static String COMMAND_LIST_POS1_NOT_SET = "cmd-list-pos1-not-set";
    public static String COMMAND_LIST_POS2_NOT_SET = "cmd-list-pos2-not-set";
    public static String COMMAND_LIST_WAITING_SPAWN = "cmd-list-waiting-spawn";
    public static String COMMAND_LIST_WAITING_POS = "cmd-list-waiting-pos";
    public static String COMMAND_LIST_SET_SPAWN = "cmd-list-set-spawn";
    public static String COMMAND_LIST_SET_BED = "cmd-list-set-bed";
    public static String COMMAND_LIST_SET_SHOP = "cmd-list-set-shop";
    public static String COMMAND_LIST_SET_KILL_DROPS = "cmd-list-set-kill-drops";
    public static String COMMAND_LIST_SET_UPGRADE = "cmd-list-set-upgrade";
    public static String COMMAND_LIST_ADD_GENERATOR = "cmd-list-add-generator";
    public static String COMMAND_LIST_SET_SPECTATOR_SPAWN = "cmd-list-set-spectator-spawn";
    public static String COMMAND_LIST_AUTO_CREATE_TEAMS = "cmd-list-auto-create-teams";
    public static String COMMAND_LIST_CREATE_TEAM = "cmd-list-create-team";
    public static String COMMAND_LIST_REMOVE_TEAM = "cmd-list-remove-team";
    public static String COMMAND_LIST_REMOVE_GENERATOR = "cmd-list-remove-generator";
    public static String COMMAND_LIST_SET_MAX_IN_TEAM = "cmd-list-set-max-in-team";
    public static String COMMAND_LIST_ARENA_GROUP = "cmd-list-arena-group";
    public static String COMMAND_LIST_SET_TYPE = "cmd-list-set-type";
    public static String COMMAND_LIST_SAVE = "cmd-list-save";
    public static String COMMAND_LIST_HOVER_WAITING_SPAWN = "cmd-list-hover-waiting-spawn";
    public static String COMMAND_LIST_HOVER_WAITING_POS = "cmd-list-hover-waiting-pos";
    public static String COMMAND_LIST_HOVER_SPECTATOR_SPAWN = "cmd-list-hover-spectator-spawn";
    public static String COMMAND_LIST_HOVER_AUTO_CREATE_TEAMS = "cmd-list-hover-auto-create-teams";
    public static String COMMAND_LIST_HOVER_CREATE_TEAM = "cmd-list-hover-create-team";
    public static String COMMAND_LIST_HOVER_REMOVE_TEAM = "cmd-list-hover-remove-team";
    public static String COMMAND_LIST_HOVER_SET_SPAWN = "cmd-list-hover-set-spawn";
    public static String COMMAND_LIST_HOVER_SET_BED = "cmd-list-hover-set-bed";
    public static String COMMAND_LIST_HOVER_SET_SHOP = "cmd-list-hover-set-shop";
    public static String COMMAND_LIST_HOVER_SET_UPGRADE = "cmd-list-hover-set-upgrade";
    public static String COMMAND_LIST_HOVER_SET_KILL_DROPS = "cmd-list-hover-set-kill-drops";
    public static String COMMAND_LIST_HOVER_ADD_GENERATOR_ADVANCED = "cmd-list-hover-add-generator-advanced";
    public static String COMMAND_LIST_HOVER_ADD_GENERATOR_ASSISTED = "cmd-list-hover-add-generator-assisted";
    public static String COMMAND_LIST_HOVER_SET_MAX_IN_TEAM = "cmd-list-hover-set-max-in-team";
    public static String COMMAND_LIST_HOVER_ARENA_GROUP = "cmd-list-hover-arena-group";
    public static String COMMAND_LIST_HOVER_SET_TYPE = "cmd-list-hover-set-type";
    public static String COMMAND_LIST_HOVER_SAVE = "cmd-list-hover-save";
    public static String COMMAND_SETUP_GENERATOR_ADDED = "command-setup-generator-added";
    public static String COMMAND_SETUP_GENERATOR_ADDED_TITLE = "command-setup-generator-added-title";
    public static String COMMAND_SETUP_GENERATOR_ADVANCED_HINT = "command-setup-generator-advanced-hint";
    public static String COMMAND_SETUP_GENERATOR_ADVANCED_HOLOGRAM = "command-setup-generator-advanced-hologram";
    public static String COMMAND_SETUP_GENERATOR_ADVANCED_SET = "command-setup-generator-advanced-set";
    public static String COMMAND_SETUP_GENERATOR_CREATE_TEAM_HINT = "command-setup-generator-create-team-hint";
    public static String COMMAND_SETUP_GENERATOR_EXISTS = "command-setup-generator-exists";
    public static String COMMAND_SETUP_GENERATOR_EXISTS_TITLE = "command-setup-generator-exists-title";
    public static String COMMAND_SETUP_GENERATOR_HOLOGRAM = "command-setup-generator-hologram";
    public static String COMMAND_SETUP_GENERATOR_OTHER_HINT = "command-setup-generator-other-hint";
    public static String COMMAND_SETUP_GENERATOR_OTHER_HOVER = "command-setup-generator-other-hover";
    public static String COMMAND_SETUP_GENERATOR_TEAM_HOLOGRAM = "command-setup-generator-team-hologram";
    public static String COMMAND_SETUP_GENERATOR_TEAM_NOT_FOUND = "command-setup-generator-team-not-found";
    public static String COMMAND_SETUP_GENERATOR_TEAM_SET = "command-setup-generator-team-set";
    public static String COMMAND_SETUP_GENERATOR_TEAM_SET_TITLE = "command-setup-generator-team-set-title";
    public static String COMMAND_SETUP_REMOVE_GENERATOR_NOT_FOUND = "command-setup-remove-generator-not-found";
    public static String COMMAND_SETUP_REMOVE_GENERATOR_NOT_FOUND_TITLE = "command-setup-remove-generator-not-found-title";
    public static String COMMAND_SETUP_REMOVE_GENERATOR_REMOVED_AT = "command-setup-remove-generator-removed-at";
    public static String COMMAND_SETUP_REMOVE_GENERATOR_REMOVED_TITLE = "command-setup-remove-generator-removed-title";
    public static String COMMAND_SETUP_REMOVE_GENERATOR_STAND_CLOSE = "command-setup-remove-generator-stand-close";
    public static String COMMAND_SETUP_REMOVE_GENERATOR_TEAM_GENERATOR_REMOVED_TITLE = "command-setup-remove-generator-team-generator-removed-title";
    public static String COMMAND_SETUP_REMOVE_GENERATOR_TEAM_REMOVED = "command-setup-remove-generator-team-removed";
    public static String COMMAND_SETUP_REMOVE_GENERATOR_TEAM_REMOVED_AT = "command-setup-remove-generator-team-removed-at";
    public static String COMMAND_SETUP_REMOVE_GENERATOR_TEAM_REMOVED_TITLE = "command-setup-remove-generator-team-removed-title";
    public static String COMMAND_SETUP_REMOVE_TEAM_HOVER = "command-setup-remove-team-hover";
    public static String COMMAND_SETUP_REMOVE_TEAM_NOT_FOUND = "command-setup-remove-team-not-found";
    public static String COMMAND_SETUP_REMOVE_TEAM_NOT_FOUND_TITLE = "command-setup-remove-team-not-found-title";
    public static String COMMAND_SETUP_REMOVE_TEAM_SUCCESS = "command-setup-remove-team-success";
    public static String COMMAND_SETUP_REMOVE_TEAM_SUCCESS_TITLE = "command-setup-remove-team-success-title";
    public static String COMMAND_SETUP_REMOVE_TEAM_USAGE = "command-setup-remove-team-usage";
    public static String COMMAND_SETUP_SAVE_ENABLE_HINT = "command-setup-save-enable-hint";
    public static String COMMAND_SETUP_SAVE_ENABLE_HOVER = "command-setup-save-enable-hover";
    public static String COMMAND_SETUP_SAVE_ENABLE_OPTION = "command-setup-save-enable-option";
    public static String COMMAND_SETUP_SAVE_SUCCESS = "command-setup-save-success";
    public static String COMMAND_SETUP_SPAWN_HOLOGRAM = "command-setup-spawn-hologram";
    public static String COMMAND_SETUP_SPAWN_REMAINING = "command-setup-spawn-remaining";
    public static String COMMAND_SETUP_SPAWN_SET = "command-setup-spawn-set";
    public static String COMMAND_SETUP_SPAWN_TEAMS_HEADER = "command-setup-spawn-teams-header";
    public static String COMMAND_SETUP_SPAWN_TEAM_ENTRY = "command-setup-spawn-team-entry";
    public static String COMMAND_SETUP_SPAWN_TEAM_HOVER = "command-setup-spawn-team-hover";
    public static String COMMAND_SETUP_SPAWN_TEAM_NOT_FOUND = "command-setup-spawn-team-not-found";
    public static String COMMAND_SETUP_SPAWN_USAGE = "command-setup-spawn-usage";
    public static String COMMAND_SETUP_SPECTATOR_SET = "command-setup-spectator-set";
    public static String COMMAND_SETUP_SPECTATOR_USAGE = "command-setup-spectator-usage";
    public static String COMMAND_TOGGLE_READY_DISPLAY = "command-toggle-ready-display";

    public static String COMMAND_SETUP_GENERATOR_ADVANCED_AUTO_HOVER = "command-setup-generator-advanced-auto-hover";
    public static String COMMAND_SETUP_GENERATOR_ADVANCED_AUTO_USAGE = "command-setup-generator-advanced-auto-usage";
    public static String COMMAND_SETUP_GENERATOR_ADVANCED_TITLE = "command-setup-generator-advanced-title";
    public static String COMMAND_SETUP_GENERATOR_ADVANCED_USAGE = "command-setup-generator-advanced-usage";
    public static String COMMAND_SETUP_GENERATOR_AUTO_USAGE = "command-setup-generator-auto-usage";
    public static String COMMAND_SETUP_GENERATOR_RESOURCE_USAGE = "command-setup-generator-resource-usage";
    public static String COMMAND_SETUP_GENERATOR_TEAM_HOVER = "command-setup-generator-team-hover";
    public static String COMMAND_SETUP_GENERATOR_TEAM_USAGE = "command-setup-generator-team-usage";
    public static String CONFIG_ITEM_NAME_NOT_SET = "config-item-name-not-set";
    public static String CONFIG_ITEM_LORE_NOT_SET = "config-item-lore-not-set";
    public static String SERVER_SPAWN_PROTECTION_WARNING = "server-spawn-protection-warning";
    public static String SERVER_MULTIVERSE_WARNING = "server-multiverse-warning";
    public static String RESTORE_LOAD_WORLD = "restore-load-world";
    public static String RESTORE_CREATE_VOID_MAP = "restore-create-void-map";
    public static String CUSTOMER_DETAILS_HEADER = "customer-details-header";
    public static String CUSTOMER_DETAILS_USER_ID = "customer-details-user-id";
    public static String CUSTOMER_DETAILS_DOWNLOAD_ID = "customer-details-download-id";
    public static final String CONFIG_RELOAD_IMMEDIATE = "command.config-reload.immediate";
    public static final String CONFIG_RELOAD_NEXT_ARENA = "command.config-reload.next-arena";
    public static final String CONFIG_RELOAD_RESTART = "command.config-reload.restart";
    public static final String CONFIG_RELOAD_FAILED = "command.config-reload.failed";
    public static final String CONFIG_RELOAD_ENTRY = "command.config-reload.entry";
    public static final String CONFIG_RELOAD_FAILURE = "command.config-reload.failure";
    public static final String CONFIG_RELOAD_UNCHANGED = "command.config-reload.unchanged";
    public static final String CONFIG_RELOAD_ADDED = "command.config-reload.added";
    public static final String CONFIG_RELOAD_MODIFIED = "command.config-reload.modified";
    public static final String CONFIG_RELOAD_REMOVED = "command.config-reload.removed";
    public static final String CONFIG_SET_USAGE = "command.config-set.usage";
    public static final String CONFIG_SET_DISPLAY = "command.config-set.display";
    public static final String CONFIG_SET_OP_ONLY = "command.config-set.op-only";
    public static final String CONFIG_SET_CONFLICT = "command.config-set.conflict";
    public static final String CONFIG_SET_INVALID_KEY = "command.config-set.invalid-key";
    public static final String CONFIG_SET_INVALID_VALUE = "command.config-set.invalid-value";
    @Deprecated(forRemoval = true)
    public static String FORMATTING_SPECTATOR_TEAM = "format-spectator-team";
    @Deprecated(forRemoval = true)
    public static String FORMATTING_SPECTATOR_COLOR = FORMATTING_SPECTATOR_TEAM;
}
