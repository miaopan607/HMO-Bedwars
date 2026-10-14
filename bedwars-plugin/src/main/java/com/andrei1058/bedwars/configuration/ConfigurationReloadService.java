package com.andrei1058.bedwars.configuration;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.TeamColor;
import com.andrei1058.bedwars.api.configuration.ConfigManager;
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.api.server.ServerType;
import com.andrei1058.bedwars.arena.Arena;
import com.andrei1058.bedwars.arena.ArenaGUI;
import com.andrei1058.bedwars.arena.upgrades.HealPoolTask;
import com.andrei1058.bedwars.lobbysocket.LoadedUser;
import com.andrei1058.bedwars.upgrades.UpgradesManager;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** File changes and runtime application share one policy for both admin commands. */
public final class ConfigurationReloadService {
    enum ApplyTiming { IMMEDIATE, NEXT_ARENA, RESTART }

    private static final Set<String> NEXT_ARENA_KEYS = new HashSet<>(Arrays.asList(
            ConfigPath.GENERAL_CONFIGURATION_START_COUNTDOWN_REGULAR,
            ConfigPath.GENERAL_CONFIGURATION_START_COUNTDOWN_HALF,
            ConfigPath.GENERAL_CONFIGURATION_START_COUNTDOWN_SHORTENED,
            ConfigPath.GENERAL_CONFIGURATION_RESTART,
            ConfigPath.GENERAL_CONFIGURATION_BEDS_DESTROY_COUNTDOWN,
            ConfigPath.GENERAL_CONFIGURATION_DRAGON_SPAWN_COUNTDOWN,
            ConfigPath.GENERAL_CONFIGURATION_GAME_END_COUNTDOWN,
            ConfigPath.GENERAL_CONFIGURATION_ARENA_GROUPS));

    /** Snapshot only per-game keys, not the unrelated main configuration. */
    public static YamlConfiguration snapshotArenaConfiguration(ConfigurationSection configuration) {
        return ConfigurationValues.copy(configuration, NEXT_ARENA_KEYS);
    }

    // These consumers create connections, register features, or schedule fixed-period tasks.
    private static final Set<String> RESTART_KEYS = new HashSet<>(Arrays.asList(
            "serverType", "language", "lobbyLoc",
            ConfigPath.GENERAL_CONFIGURATION_DISABLED_LANGUAGES,
            ConfigPath.GENERAL_ENABLE_PARTY_CMD, ConfigPath.GENERAL_CONFIGURATION_ALLOW_PARTIES,
            ConfigPath.GENERAL_CONFIGURATION_HEAL_POOL_ENABLE,
            ConfigPath.GENERAL_CONFIGURATION_PERFORMANCE_ROTATE_GEN,
            ConfigPath.GENERAL_CONFIGURATION_PERFORMANCE_SPOIL_TNT_PLAYERS,
            ConfigPath.GENERAL_CONFIGURATION_BUNGEE_MODE_GAMES_BEFORE_RESTART,
            ConfigPath.GENERAL_CONFIGURATION_BUNGEE_OPTION_LOBBY_SERVERS,
            ConfigPath.GENERAL_CONFIGURATION_NPC_LOC_STORAGE,
            ConfigPath.GENERAL_TNT_PROTECTION_END_STONE_BLAST,
            ConfigPath.GENERAL_TNT_PROTECTION_GLASS_BLAST,
            ConfigPath.SB_CONFIG_SIDEBAR_LIST_REFRESH,
            ConfigPath.SB_CONFIG_SIDEBAR_PLACEHOLDERS_REFRESH_INTERVAL,
            ConfigPath.SB_CONFIG_SIDEBAR_TITLE_REFRESH_INTERVAL,
            ConfigPath.SB_CONFIG_SIDEBAR_HEALTH_REFRESH,
            ConfigPath.SB_CONFIG_TAB_HEADER_FOOTER_ENABLE,
            ConfigPath.SB_CONFIG_TAB_HEADER_FOOTER_REFRESH_INTERVAL,
            ConfigPath.GENERAL_CONFIGURATION_INVISIBILITY_PARTICLES_INTERVAL,
            ConfigPath.GENERAL_CONFIGURATION_INVISIBILITY_FOOTSTEPS_INTERVAL));

    private static final Set<String> DYNAMIC_SHOP_KEYS = new HashSet<>(Arrays.asList(
            ConfigPath.SHOP_SPECIAL_TOWER_ENABLE, ConfigPath.SHOP_SPECIAL_TOWER_MATERIAL,
            ConfigPath.SHOP_SPECIAL_SILVERFISH_ENABLE, ConfigPath.SHOP_SPECIAL_SILVERFISH_MATERIAL,
            ConfigPath.SHOP_SPECIAL_SILVERFISH_DATA, ConfigPath.SHOP_SPECIAL_SILVERFISH_HEALTH,
            ConfigPath.SHOP_SPECIAL_SILVERFISH_DAMAGE, ConfigPath.SHOP_SPECIAL_SILVERFISH_SPEED,
            ConfigPath.SHOP_SPECIAL_SILVERFISH_DESPAWN,
            ConfigPath.SHOP_SPECIAL_IRON_GOLEM_ENABLE, ConfigPath.SHOP_SPECIAL_IRON_GOLEM_MATERIAL,
            ConfigPath.SHOP_SPECIAL_IRON_GOLEM_DATA, ConfigPath.SHOP_SPECIAL_IRON_GOLEM_HEALTH,
            ConfigPath.SHOP_SPECIAL_IRON_GOLEM_DESPAWN, ConfigPath.SHOP_SPECIAL_IRON_GOLEM_SPEED));

    private final Map<String, YamlConfiguration> accepted = new HashMap<>();
    private final Map<String, Object> mainDefaults;
    private Map<String, Object> mainRunning;

    public ConfigurationReloadService() {
        mainDefaults = ConfigurationValues.leaves(BedWars.config.getYml().getDefaults());
        configurations().forEach((file, manager) -> accepted.put(file, ConfigurationValues.copy(manager.getYml())));
        mainRunning = ConfigurationValues.leaves(BedWars.config.getYml());
    }

    private Map<String, ConfigManager> configurations() {
        Map<String, ConfigManager> files = new TreeMap<>();
        add(files, BedWars.config, "config.yml");
        add(files, BedWars.signs, "signs.yml");
        add(files, BedWars.generators, "generators.yml");
        add(files, BedWars.shop, "shop.yml");
        ConfigManager upgrades = UpgradesManager.getConfiguration();
        if (upgrades != null) add(files, upgrades, upgrades.getName() + ".yml");
        for (Language language : Language.getLanguages()) add(files, language, "Languages/overrides/" + language.getName() + ".yml");
        for (IArena arena : Arena.getArenas()) add(files, arena.getConfig(), "Arenas/" + arena.getArenaName() + ".yml");
        return files;
    }

    private static void add(Map<String, ConfigManager> files, ConfigManager manager, String name) {
        if (manager != null) files.putIfAbsent(name, manager);
    }

    /** A newly validated arena has consumed its file, including normal round rebuilds. */
    public void registerArena(ConfigManager manager) {
        accepted.put("Arenas/" + manager.getName() + ".yml", ConfigurationValues.copy(manager.getYml()));
    }

    public void reload(CommandSender sender) {
        Report report = new Report();
        configurations().forEach((file, manager) -> reloadFile(file, manager, report));
        refreshCaches();
        report.send(sender);
    }

    public void reloadMainConfiguration() {
        if (reloadFile("config.yml", BedWars.config, new Report())) refreshCaches();
    }

    private boolean reloadFile(String file, ConfigManager manager, Report report) {
        accepted.computeIfAbsent(file, ignored -> ConfigurationValues.copy(manager.getYml()));
        try {
            YamlConfiguration candidate = manager.readConfiguration();
            validate(file, candidate, accepted.get(file));
            commit(file, manager, candidate, report);
            return true;
        } catch (IOException | InvalidConfigurationException | IllegalArgumentException e) {
            fail(report, file, e);
            return false;
        }
    }

    public void setMainConfiguration(CommandSender sender, String path, String yamlValue) {
        if (!sender.isOp()) {
            sender.sendMessage(message(sender, Messages.CONFIG_SET_OP_ONLY));
            return;
        }
        Report report = new Report();
        try {
            YamlConfiguration candidate = BedWars.config.readConfiguration();
            YamlConfiguration previous = accepted.get("config.yml");
            if (!ConfigurationValues.leaves(candidate).equals(ConfigurationValues.leaves(previous))) {
                sender.sendMessage(message(sender, Messages.CONFIG_SET_CONFLICT));
                return;
            }
            if (!candidate.contains(path) || candidate.isConfigurationSection(path)) {
                sender.sendMessage(message(sender, Messages.CONFIG_SET_INVALID_KEY));
                return;
            }
            try {
                Object value = ConfigurationValues.convert(ConfigurationValues.parseValue(yamlValue), candidate.get(path), mainDefaults.get(path));
                candidate.set(path, value);
                validate("config.yml", candidate, previous);
            } catch (InvalidConfigurationException | IllegalArgumentException e) {
                sender.sendMessage(message(sender, Messages.CONFIG_SET_INVALID_VALUE));
                return;
            }
            if (Objects.equals(candidate.get(path), previous.get(path))) {
                sender.sendMessage(message(sender, Messages.CONFIG_RELOAD_UNCHANGED));
                return;
            }
            BedWars.config.saveConfiguration(candidate);
            commit("config.yml", BedWars.config, candidate, report);
            refreshCaches();
        } catch (IOException | InvalidConfigurationException e) {
            fail(report, "config.yml", e);
        }
        report.send(sender);
    }

    private ApplyTiming timing(String file, String path) {
        if (file.startsWith("Arenas/") || file.equals("generators.yml")) return ApplyTiming.NEXT_ARENA;
        if (file.equals("config.yml")) {
            if (path.startsWith("database.") || RESTART_KEYS.contains(path)) return ApplyTiming.RESTART;
            if (NEXT_ARENA_KEYS.contains(path)) return ApplyTiming.NEXT_ARENA;
            // Unknown extension keys may be cached by third-party API consumers.
            if (mainDefaults.containsKey(path) || path.equals("storeLink") || path.equals("lobbyServer")) return ApplyTiming.IMMEDIATE;
            return ApplyTiming.RESTART;
        }
        if (file.startsWith("Languages/")) {
            if (path.equals(Messages.FORMATTING_SCOREBOARD_DATE) || path.equals(Messages.FORMATTING_SCOREBOARD_NEXEVENT_TIMER)
                    || path.startsWith("shop-") || path.startsWith("upgrades-")) return ApplyTiming.RESTART;
            return ApplyTiming.IMMEDIATE;
        }
        if (file.equals("signs.yml")) {
            return path.equals("format") || path.startsWith("status-block.") ? ApplyTiming.IMMEDIATE : ApplyTiming.RESTART;
        }
        if (file.equals("shop.yml")) {
            if (DYNAMIC_SHOP_KEYS.contains(path)) return ApplyTiming.IMMEDIATE;
            return ApplyTiming.RESTART;
        }
        if (file.equals("upgrades2.yml") && (path.endsWith("-upgrades-settings.trap-queue-limit")
                || path.endsWith("-upgrades-settings.trap-start-price")
                || path.endsWith("-upgrades-settings.trap-increment-price"))) return ApplyTiming.IMMEDIATE;
        // Menu indexes and upgrade definitions are built once; do not re-register their listeners.
        return ApplyTiming.RESTART;
    }

    private void commit(String file, ConfigManager manager, YamlConfiguration candidate, Report report) {
        Map<String, Object> before = ConfigurationValues.leaves(accepted.get(file));
        Map<String, Object> after = ConfigurationValues.leaves(candidate);
        if (before.equals(after)) return;
        Set<String> paths = new TreeSet<>(before.keySet());
        paths.addAll(after.keySet());
        Map<String, Object> runningValues = null;
        for (String path : paths) {
            if (Objects.equals(before.get(path), after.get(path)) && before.containsKey(path) == after.containsKey(path)) continue;
            ApplyTiming timing = timing(file, path);
            String operation = !before.containsKey(path) ? Messages.CONFIG_RELOAD_ADDED
                    : !after.containsKey(path) ? Messages.CONFIG_RELOAD_REMOVED : Messages.CONFIG_RELOAD_MODIFIED;
            report.changes.get(timing).add(new Change(file, path, operation, before.get(path), after.get(path)));
            // Existing arenas own copies of all NEXT_ARENA data; new arenas see the new globals.
            if (timing != ApplyTiming.RESTART && !file.startsWith("Arenas/")) {
                if (runningValues == null) runningValues = ConfigurationValues.leaves(manager.getYml());
                if (after.containsKey(path)) runningValues.put(path, after.get(path));
                else runningValues.remove(path);
            }
        }
        if (runningValues != null) {
            YamlConfiguration running = new YamlConfiguration();
            runningValues.forEach(running::set);
            manager.replaceConfiguration(running);
            if (file.equals("config.yml")) mainRunning = ConfigurationValues.leaves(running);
        }
        accepted.put(file, ConfigurationValues.copy(candidate));
    }

    /** Existing lobby/setup setters must not overwrite values waiting for a restart. */
    public void saveMainConfiguration(ConfigManager manager) throws IOException, InvalidConfigurationException {
        YamlConfiguration candidate = manager.readConfiguration();
        if (!ConfigurationValues.leaves(candidate).equals(ConfigurationValues.leaves(accepted.get("config.yml")))) {
            throw new IOException("Unloaded configuration changes");
        }
        Map<String, Object> current = ConfigurationValues.leaves(manager.getYml());
        Set<String> paths = new TreeSet<>(mainRunning.keySet());
        paths.addAll(current.keySet());
        for (String path : paths) {
            if (!Objects.equals(current.get(path), mainRunning.get(path))) candidate.set(path, current.get(path));
        }
        manager.saveConfiguration(candidate);
        accepted.put("config.yml", ConfigurationValues.copy(candidate));
        mainRunning = current;
    }

    private void validate(String file, YamlConfiguration candidate, YamlConfiguration previous) {
        Map<String, Object> before = ConfigurationValues.leaves(previous);
        Map<String, Object> after = ConfigurationValues.leaves(candidate);
        for (Map.Entry<String, Object> entry : after.entrySet()) {
            if (entry.getValue() instanceof Number && !Double.isFinite(((Number) entry.getValue()).doubleValue())) {
                throw new IllegalArgumentException("invalid-number: " + entry.getKey());
            }
            Object reference = before.get(entry.getKey());
            if (reference != null && !Objects.equals(reference, entry.getValue())) {
                if (reference instanceof List<?> && ((List<?>) reference).stream().anyMatch(value -> value instanceof Map<?, ?>)) {
                    if (!(entry.getValue() instanceof List<?>)) throw new IllegalArgumentException("invalid-type");
                } else {
                    ConfigurationValues.convert(entry.getValue(), reference, mainDefaults.get(entry.getKey()));
                }
            }
        }
        if (file.equals("config.yml")) {
            for (String key : mainDefaults.keySet()) {
                if (!candidate.contains(key)) throw new IllegalArgumentException("missing-key: " + key);
            }
            String serverType = candidate.getString("serverType");
            if (!"BUNGEE_LEGACY".equalsIgnoreCase(serverType)) ServerType.valueOf(serverType.toUpperCase(Locale.ROOT));
            requirePositive(candidate, "heal-pool-particle-density");
            requirePositive(candidate, ConfigPath.GENERAL_TNT_JUMP_STRENGTH_REDUCTION);
            requirePositive(candidate, ConfigPath.GENERAL_TNT_JUMP_Y_REDUCTION);
            for (String key : NEXT_ARENA_KEYS) {
                if (candidate.get(key) instanceof Number) requireNonNegative(candidate, key);
            }
            if (candidate.getLong(ConfigPath.GENERAL_CONFIGURATION_RESTART) > Integer.MAX_VALUE - 5L) {
                throw new IllegalArgumentException("invalid-restart-countdown");
            }
            validateInventorySize(candidate, ConfigPath.GENERAL_CONFIGURATION_STATS_GUI_SIZE);
            validateInventorySize(candidate, ConfigPath.GENERAL_CONFIGURATION_ARENA_SELECTOR_SETTINGS_SIZE);
            validateLocation(candidate, "lobbyLoc", 6);
        } else if (file.equals("generators.yml")) {
            for (String key : before.keySet()) {
                if (key.startsWith("Default.") && !candidate.contains(key)) throw new IllegalArgumentException("missing-key: " + key);
            }
            for (String key : Arrays.asList(ConfigPath.GENERATOR_STACK_ITEMS, ConfigPath.GENERATOR_ENABLE_GEN_SPLIT,
                    ConfigPath.GENERATOR_GEN_SPLIT_RADIUS)) {
                if (!candidate.contains(key)) throw new IllegalArgumentException("missing-key: " + key);
            }
            for (String key : after.keySet()) if (after.get(key) instanceof Number) requireNonNegative(candidate, key);
        } else if (file.startsWith("Arenas/")) {
            requirePositive(candidate, "maxInTeam");
            requirePositive(candidate, "minPlayers");
            requirePositive(candidate, "worldBorder");
            ConfigurationSection teams = candidate.getConfigurationSection("Team");
            if (teams == null || teams.getKeys(false).size() < 2) throw new IllegalArgumentException("invalid-teams");
            long capacity = candidate.getLong("maxInTeam") * teams.getKeys(false).size();
            if (capacity > Integer.MAX_VALUE || candidate.getLong("minPlayers") > capacity) {
                throw new IllegalArgumentException("invalid-minPlayers");
            }
            validateLocation(candidate, "waiting.Loc", 5);
            if (!candidate.contains("waiting.Loc")) throw new IllegalArgumentException("missing-waiting-location");
            validateLocation(candidate, ConfigPath.ARENA_SPEC_LOC, 5);
            validateLocation(candidate, ConfigPath.ARENA_WAITING_POS1, 5);
            validateLocation(candidate, ConfigPath.ARENA_WAITING_POS2, 5);
            for (String team : teams.getKeys(false)) {
                String root = "Team." + team;
                TeamColor.valueOf(candidate.getString(root + ".Color", "").toUpperCase(Locale.ROOT));
                for (String key : Arrays.asList("Spawn", "Bed", "Shop", "Upgrade")) {
                    if (!candidate.contains(root + "." + key)) throw new IllegalArgumentException("missing-team-location");
                    validateLocation(candidate, root + "." + key, 5);
                }
                for (String key : Arrays.asList("Iron", "Gold")) {
                    if (!candidate.contains(root + "." + key)) throw new IllegalArgumentException("missing-team-generator");
                    validateLocations(candidate, root + "." + key);
                }
            }
            validateLocations(candidate, "generator.Diamond");
            validateLocations(candidate, "generator.Emerald");
        } else if (file.equals("signs.yml") && candidate.getStringList("format").size() != 4) {
            throw new IllegalArgumentException("invalid-sign-format");
        }
        for (Map.Entry<String, Object> entry : after.entrySet()) {
            if (entry.getKey().endsWith(".material") && entry.getValue() instanceof String) {
                String material = (String) entry.getValue();
                if (!material.equalsIgnoreCase("none")) Material.valueOf(material.toUpperCase(Locale.ROOT));
            }
        }
    }

    private static void requirePositive(YamlConfiguration configuration, String key) {
        if (!(configuration.get(key) instanceof Number) || configuration.getDouble(key) <= 0) throw new IllegalArgumentException("invalid-number: " + key);
    }

    private static void requireNonNegative(YamlConfiguration configuration, String key) {
        if (!(configuration.get(key) instanceof Number) || configuration.getDouble(key) < 0) throw new IllegalArgumentException("invalid-number: " + key);
    }

    private static void validateInventorySize(YamlConfiguration configuration, String key) {
        int size = configuration.getInt(key);
        if (size < 9 || size > 54 || size % 9 != 0) throw new IllegalArgumentException("invalid-inventory-size: " + key);
    }

    private static void validateLocation(YamlConfiguration configuration, String key, int length) {
        if (!configuration.contains(key)) return;
        validateLocationValue(configuration.getString(key, ""), key, length);
    }

    private static void validateLocations(YamlConfiguration configuration, String key) {
        if (!configuration.contains(key)) return;
        if (!(configuration.get(key) instanceof List<?>)) throw new IllegalArgumentException("invalid-locations: " + key);
        for (Object value : configuration.getList(key)) {
            if (!(value instanceof String)) throw new IllegalArgumentException("invalid-location: " + key);
            validateLocationValue((String) value, key, 5);
        }
    }

    private static void validateLocationValue(String value, String key, int length) {
        String[] parts = value.replace("[", "").replace("]", "").split(",");
        if (parts.length != length) throw new IllegalArgumentException("invalid-location: " + key);
        for (int index = 0; index < 5; index++) {
            if (!Double.isFinite(Double.parseDouble(parts[index]))) throw new IllegalArgumentException("invalid-location: " + key);
        }
    }

    private static void refreshCaches() {
        BedWars.setDebug(BedWars.config.getBoolean("debug"));
        ArenaGUI.refreshConfiguration();
        LoadedUser.refreshConfiguration();
        HealPoolTask.refreshConfiguration();
        Set<Listener> refreshed = Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        for (RegisteredListener registration : HandlerList.getRegisteredListeners(BedWars.plugin)) {
            Listener listener = registration.getListener();
            if (listener instanceof ReloadableConfiguration && refreshed.add(listener)) {
                ((ReloadableConfiguration) listener).refreshConfiguration();
            }
        }
        for (Language language : Language.getLanguages()) language.refreshConfiguration();
    }

    private static String message(CommandSender sender, String key) {
        return Language.getMsg(sender instanceof Player ? (Player) sender : null, key);
    }

    private static void fail(Report report, String file, Exception exception) {
        // YAML exceptions can contain the source line, including credentials.
        String reason = exception instanceof InvalidConfigurationException ? "YAML 语法错误"
                : exception instanceof IOException ? "文件读取或保存失败（" + exception.getClass().getSimpleName() + "）"
                : "配置类型或取值不符合要求";
        report.failures.put(file, reason);
        BedWars.plugin.getLogger().warning("Configuration reload failed: " + file + " (" + exception.getClass().getSimpleName() + ")");
    }

    private static final class Change {
        final String file, path, operation;
        final Object before, after;

        Change(String file, String path, String operation, Object before, Object after) {
            this.file = file;
            this.path = path;
            this.operation = operation;
            this.before = before;
            this.after = after;
        }
    }

    private static final class Report {
        private static final String[] HEADINGS = {
                Messages.CONFIG_RELOAD_IMMEDIATE, Messages.CONFIG_RELOAD_NEXT_ARENA, Messages.CONFIG_RELOAD_RESTART};
        private static final Pattern ENTRY_FIELDS = Pattern.compile("\\{(file|key|operation|old|new)\\}");
        final EnumMap<ApplyTiming, List<Change>> changes = new EnumMap<>(ApplyTiming.class);
        final Map<String, String> failures = new TreeMap<>();

        Report() {
            for (ApplyTiming timing : ApplyTiming.values()) changes.put(timing, new ArrayList<>());
        }

        void send(CommandSender sender) {
            boolean changed = false;
            for (ApplyTiming timing : ApplyTiming.values()) {
                List<Change> entries = changes.get(timing);
                if (entries.isEmpty()) continue;
                changed = true;
                sender.sendMessage(message(sender, HEADINGS[timing.ordinal()]).replace("{count}", String.valueOf(entries.size())));
                entries.sort(java.util.Comparator.comparing((Change change) -> change.file).thenComparing(change -> change.path));
                for (Change change : entries) {
                    String lower = change.path.toLowerCase(Locale.ROOT);
                    boolean sensitive = lower.contains("password") || lower.contains("passwd") || lower.contains("secret")
                            || lower.contains("token") || lower.contains("credential") || lower.equals("database.pass");
                    String text = renderEntry(sender, change, sensitive);
                    for (int start = 0; start < text.length();) {
                        int end = Math.min(start + 200, text.length());
                        if (end < text.length() && Character.isHighSurrogate(text.charAt(end - 1))) end--;
                        sender.sendMessage(text.substring(start, end));
                        start = end;
                    }
                }
            }
            if (!failures.isEmpty()) {
                sender.sendMessage(message(sender, Messages.CONFIG_RELOAD_FAILED).replace("{count}", String.valueOf(failures.size())));
                failures.forEach((file, reason) -> sender.sendMessage(message(sender, Messages.CONFIG_RELOAD_FAILURE)
                        .replace("{file}", safe(file)).replace("{reason}", reason)));
            } else if (!changed) {
                sender.sendMessage(message(sender, Messages.CONFIG_RELOAD_UNCHANGED));
            }
        }

        private static String renderEntry(CommandSender sender, Change change, boolean sensitive) {
            Matcher fields = ENTRY_FIELDS.matcher(message(sender, Messages.CONFIG_RELOAD_ENTRY));
            StringBuilder result = new StringBuilder();
            while (fields.find()) {
                String value;
                switch (fields.group(1)) {
                    case "file": value = safe(change.file); break;
                    case "key": value = safe(change.path); break;
                    case "operation": value = message(sender, change.operation); break;
                    case "old": value = sensitive ? "<隐藏>" : ConfigurationValues.display(change.before); break;
                    case "new": value = sensitive ? "<隐藏>" : ConfigurationValues.display(change.after); break;
                    default: throw new IllegalStateException("Unknown configuration report field");
                }
                // Inserted values are literals, not another pass of template placeholders.
                fields.appendReplacement(result, Matcher.quoteReplacement(value));
            }
            fields.appendTail(result);
            return result.toString();
        }

        private static String safe(String text) {
            return text.replace("§", "\\u00a7").replace("&", "\\u0026").replace("\n", "\\n").replace("\r", "\\r");
        }
    }
}
