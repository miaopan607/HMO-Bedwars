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

import com.andrei1058.bedwars.api.BedWars;
import com.andrei1058.bedwars.api.configuration.ConfigManager;
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import com.andrei1058.bedwars.api.events.player.PlayerLangChangeEvent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.*;

public class Language extends ConfigManager {

    private final String iso;
    private final Plugin owner;
    private ConfigManager overrides;
    private File overrideFile;
    private YamlConfiguration baseConfiguration;
    private YamlConfiguration overrideConfiguration;
    private boolean overrideReadFailed;
    private final Map<String, Object> runtimeDefaults = new HashMap<>();
    private String prefix = "";
    private static String prefixStatic = "";
    private static final HashMap<UUID, Language> langByPlayer = new HashMap<>();
    private static final List<Language> languages = new ArrayList<>();
    private static Language defaultLanguage;
    private static Language englishLanguage;
    private static final String MISSING_LANGUAGE_MESSAGE = "MISSING_LANG";

    private static Language fallbackLanguage() {
        if (defaultLanguage != null) return defaultLanguage;
        if (englishLanguage != null) return englishLanguage;
        return languages.isEmpty() ? null : languages.get(0);
    }

    private static Language languageFor(UUID uuid) {
        Language language = uuid == null ? null : langByPlayer.get(uuid);
        if (language != null && languages.contains(language)) return language;
        return fallbackLanguage();
    }

    private static Object rawValue(Language language, String path) {
        if (path == null) return null;
        Language[] candidates = new Language[]{language, defaultLanguage, englishLanguage};
        for (Language candidate : candidates) {
            if (candidate != null && candidate.getYml() != null && candidate.getYml().get(path) != null) {
                return candidate.getYml().get(path);
            }
        }
        return null;
    }

    private static String rawMessage(Language language, String path) {
        if (path == null) return null;
        Language[] candidates = new Language[]{language, defaultLanguage, englishLanguage};
        for (Language candidate : candidates) {
            if (candidate != null && candidate.getYml() != null) {
                String message = candidate.getYml().getString(path);
                if (message != null) return message;
            }
        }
        return null;
    }

    private static List<String> rawList(Language language, String path) {
        if (path == null) return null;
        Language[] candidates = new Language[]{language, defaultLanguage, englishLanguage};
        for (Language candidate : candidates) {
            if (candidate != null && candidate.getYml() != null && candidate.getYml().get(path) != null) {
                return candidate.getYml().getStringList(path);
            }
        }
        return null;
    }
    private String serverIp;

    public Language(Plugin plugin, String iso) {
        super(plugin, "messages_" + iso, plugin.getDataFolder().getPath() + "/Languages");
        this.iso = iso;
        this.owner = plugin;
        if ("en".equalsIgnoreCase(iso)) {
            englishLanguage = this;
        }
        // replace old placeholders
        List<String> oldMsg = getYml().getStringList(Messages.GAME_END_TOP_PLAYER_CHAT);
        if (!oldMsg.isEmpty()) {
            String[] oldTop1 = new String[]{"{firstName}", "{secondName}", "{thirdName}"};
            String[] oldTop2 = new String[]{"{firstKills}", "{secondKills}", "{thirdKills}"};

            List<String> newMsg = new ArrayList<>();
            for (String oldString : oldMsg) {
                for (String oldPlaceholder : oldTop1) {
                    oldString = oldString.replace(oldPlaceholder, "{topPlayerName}");
                }
                for (String oldPlaceholder : oldTop2) {
                    oldString = oldString.replace(oldPlaceholder, "{topValue}");
                }
                newMsg.add(oldString);
            }

            getYml().set(Messages.GAME_END_TOP_PLAYER_CHAT, newMsg);
        }

        if (null != getYml().get("scoreboard")) {
            for (String group : getYml().getConfigurationSection("scoreboard").getKeys(false)) {
                if (group.equalsIgnoreCase("lobby")) {
                    relocate("scoreboard." + group, "sidebar." + group);
                } else {
                    Map<String, String[]> stages = new HashMap<>();
                    stages.put("waiting", new String[]{Messages.SCOREBOARD_DEFAULT_WAITING, Messages.SCOREBOARD_DEFAULT_WAITING_SPEC});
                    stages.put("starting", new String[]{Messages.SCOREBOARD_DEFAULT_STARTING, Messages.SCOREBOARD_DEFAULT_STARTING_SPEC});

                    for (Map.Entry<String, String[]> stage : stages.entrySet()) {
                        if (exists("scoreboard." + group + "." + stage.getKey() + ".player")) {
                            relocate("scoreboard." + group + "." + stage.getKey() + ".player", stage.getValue()[0].replace("Default", group));
                        } else {
                            relocate("scoreboard." + group + "." + stage.getKey(), stage.getValue()[0].replace("Default", group));
                        }
                        if (exists("scoreboard." + group + "." + stage.getKey() + ".spectator")) {
                            relocate("scoreboard." + group + "." + stage.getKey() + ".spectator", stage.getValue()[1].replace("Default", group));
                        }
                    }
                    if (exists("scoreboard." + group + ".playing.alive")) {
                        relocate("scoreboard." + group + ".playing.alive", Messages.SCOREBOARD_DEFAULT_PLAYING.replace("Default", group));
                        relocate("scoreboard." + group + ".playing.spectator", Messages.SCOREBOARD_DEFAULT_PLAYING_SPEC.replace("Default", group));
                    } else {
                        relocate("scoreboard." + group + ".playing", Messages.SCOREBOARD_DEFAULT_PLAYING.replace("Default", group));
                    }
                }
            }
            getYml().set("scoreboard", null);
        }

        languages.add(this);
    }

    /** 默认文件由内置文案生成，自定义文案只从覆盖文件读取。 */
    public void initializeOverrides() {
        if (overrides != null) return;
        Configuration defaults = getYml().getDefaults();
        baseConfiguration = copyConfiguration(defaults == null ? getYml() : defaults);
        if (defaults != null) baseConfiguration.setDefaults(defaults);
        overrideFile = new File(owner.getDataFolder(), "Languages/overrides/" + getName() + ".yml");
        overrides = new ConfigManager(owner, getName(), overrideFile.getParent());
        try {
            replaceConfiguration(readConfiguration());
        } catch (IOException | InvalidConfigurationException e) {
            owner.getLogger().log(Level.SEVERE, "Could not initialize language " + iso + " with overrides " + overrideFile, e);
            replaceConfiguration(copyConfiguration(baseConfiguration));
        }
        applyRuntimeDefaults();
        refreshConfiguration();
    }

    public boolean hasOverride(String path) {
        return overrideConfiguration != null && path != null && overrideConfiguration.isSet(path);
    }

    public void setRuntimeDefault(String path, Object value) {
        if (path == null || getYml() == null) return;
        if (hasOverride(path)) {
            runtimeDefaults.remove(path);
            return;
        }
        Object copy = copyValue(value);
        runtimeDefaults.put(path, copy);
        getYml().set(path, copyValue(copy));
    }

    @Override
    public void set(String path, Object value) {
        runtimeDefaults.remove(path);
        super.set(path, value);
    }

    @Override
    public YamlConfiguration readConfiguration() throws IOException, InvalidConfigurationException {
        // super 构造器也会调用此方法，此时子类字段尚未初始化。
        if (overrides == null) {
            return getClass() == Language.class ? super.readConfiguration() : new YamlConfiguration();
        }
        if (baseConfiguration.getDefaults() != null) super.saveConfiguration(baseConfiguration);
        try {
            YamlConfiguration loaded = overrideFile.exists() ? overrides.readConfiguration() : new YamlConfiguration();
            YamlConfiguration merged = copyConfiguration(baseConfiguration);
            configurationLeaves(loaded).forEach(merged::set);
            overrideConfiguration = loaded;
            overrideReadFailed = false;
            return merged;
        } catch (IOException | InvalidConfigurationException e) {
            overrideReadFailed = true;
            throw e;
        }
    }

    @Override
    public void replaceConfiguration(YamlConfiguration configuration) {
        if (overrides != null && baseConfiguration.getDefaults() != null) {
            configuration.setDefaults(baseConfiguration.getDefaults());
        }
        super.replaceConfiguration(configuration);
    }

    @Override
    public void saveConfiguration(YamlConfiguration configuration) throws IOException {
        // 语言子类先注册全部 defaults，再由 initializeOverrides 同步默认文件。
        if (overrides == null) return;
        if (overrideReadFailed) throw new IOException("Unreadable language overrides: " + overrideFile);
        YamlConfiguration changes = new YamlConfiguration();
        configurationLeaves(configuration).forEach((path, value) -> {
            if (runtimeDefaults.containsKey(path) && !hasOverride(path)) return;
            if (hasOverride(path) || !Objects.equals(value, copyValue(baseConfiguration.get(path)))) changes.set(path, value);
        });
        overrides.saveConfiguration(changes);
        overrideConfiguration = changes;
    }

    @Override
    public void reload() {
        super.reload();
        applyRuntimeDefaults();
        refreshConfiguration();
    }

    private void applyRuntimeDefaults() {
        runtimeDefaults.forEach((path, value) -> {
            if (!hasOverride(path)) getYml().set(path, copyValue(value));
        });
    }

    private static Map<String, Object> configurationLeaves(ConfigurationSection configuration) {
        Map<String, Object> leaves = new LinkedHashMap<>();
        for (String path : configuration.getKeys(true)) {
            Object value = configuration.get(path);
            if (!(value instanceof ConfigurationSection)) leaves.put(path, copyValue(value));
        }
        return leaves;
    }

    private static YamlConfiguration copyConfiguration(Configuration source) {
        YamlConfiguration copy = new YamlConfiguration();
        configurationLeaves(source).forEach(copy::set);
        if (source.getDefaults() != null) copy.setDefaults(source.getDefaults());
        return copy;
    }

    private static Object copyValue(Object value) {
        if (value instanceof Object[]) value = Arrays.asList((Object[]) value);
        if (value instanceof List<?>) {
            List<Object> copy = new ArrayList<>(((List<?>) value).size());
            for (Object item : (List<?>) value) copy.add(copyValue(item));
            return copy;
        }
        if (value instanceof Map<?, ?>) {
            Map<Object, Object> copy = new LinkedHashMap<>();
            ((Map<?, ?>) value).forEach((key, item) -> copy.put(copyValue(key), copyValue(item)));
            return copy;
        }
        if (value instanceof Float) return Double.parseDouble(value.toString());
        return value;
    }

    /**
     * Set chat prefix.
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setPrefixStatic(String prefix) {
        prefixStatic = prefix;
    }

    /** Invalidate ordinary message caches after a successful configuration reload. */
    public void refreshConfiguration() {
        serverIp = null;
        String message = rawMessage(this, Messages.PREFIX);
        prefix = ChatColor.translateAlternateColorCodes('&', message == null ? "" : message);
        if (this == defaultLanguage) prefixStatic = prefix;
    }

    /**
     * Get scoreboard strings.
     */
    public static List<String> getScoreboard(Player player, String path, String alternative) {
        Language language = getPlayerLanguage(player);
        if (language == null) return Collections.emptyList();
        if (rawValue(language, path) != null) {
            return language.l(path);
        }
        if (path != null) {
            String[] sp = path.split("\\.", -1);
            if (sp.length == 3 && !sp[1].isEmpty()) {
                String path2 = sp[0] + "." + Character.toUpperCase(sp[1].charAt(0))
                        + sp[1].substring(1).toLowerCase(Locale.ROOT) + "." + sp[2];
                if (rawValue(language, path2) != null) return language.l(path2);
                path2 = sp[0] + "." + sp[1].toUpperCase(Locale.ROOT) + "." + sp[2];
                if (rawValue(language, path2) != null) return language.l(path2);
            }
        }
        return language.l(alternative);
    }

    /**
     * Get language display name.
     */
    public String getLangName() {
        if (getYml() == null) return iso;
        return getYml().getString("name", iso);
    }

    /**
     * Get message in player's language.
     */
    public static String getMsg(Player player, String path) {
        Language language = getPlayerLanguage(player);
        String message = language == null ? MISSING_LANGUAGE_MESSAGE : language.m(path);
        return message.replace("{prefix}", prefixStatic == null ? "" : prefixStatic);
    }

    /**
     * Retrieve a player language.
     */
    public static Language getPlayerLanguage(@NotNull Player player) {
        return languageFor(player == null ? null : player.getUniqueId());
    }

    public static Language getPlayerLanguage(UUID p) {
        return languageFor(p);
    }

    /**
     * Check if a message was set.
     */
    public boolean exists(String path) {
        return path != null && getYml() != null && getYml().get(path) != null;
    }
    /**
     * Ensure a dynamic item name exists and migrate old English placeholder text.
     */
    public void ensureLocalizedName(String path) {
        Object current = getYml().get(path);
        Object configuredDefault = getYml().getDefaults() == null ? null : getYml().getDefaults().get(path);
        String replacement = configuredDefault instanceof String ? (String) configuredDefault : m(Messages.CONFIG_ITEM_NAME_NOT_SET);
        if (current == null || (!hasOverride(path) && isLegacyUnsetName(current))) setRuntimeDefault(path, replacement);
    }

    /**
     * Ensure dynamic lore exists and migrate old English placeholder text.
     */
    @SuppressWarnings("unchecked")
    public void ensureLocalizedLore(String path) {
        Object current = getYml().get(path);
        Object configuredDefault = getYml().getDefaults() == null ? null : getYml().getDefaults().get(path);
        List<String> replacement = configuredDefault instanceof List ? new ArrayList<>((List<String>) configuredDefault)
                : l(Messages.CONFIG_ITEM_LORE_NOT_SET);
        if (current == null || (!hasOverride(path) && isLegacyUnsetLore(current))) setRuntimeDefault(path, replacement);
    }

    /**
     * Get a string list in player's language.
     */
    public static List<String> getList(@NotNull Player player, String path) {
        Language language = getPlayerLanguage(player);
        return language == null ? Collections.emptyList() : language.l(path);
    }


    @SuppressWarnings("unused")
    public void relocate(String from, String to) {
        Object fromData = getYml().get(from);
        if (null != fromData) {
            this.getYml().set(to, fromData);
            this.getYml().set(from, null);
        }
    }

    /**
     * Save a value to file if not exists.
     */
    public static void saveIfNotExists(String path, Object data) {
        if (path == null) return;
        for (Language l : languages) {
            if (l != null && l.getYml() != null && l.getYml().get(path) == null) {
                l.set(path, data);
            }
        }
    }

    /**
     * Get a color translated message.
     */
    public String m(String path) {
        String message = rawMessage(this, path);
        if (message == null) message = MISSING_LANGUAGE_MESSAGE;
        if (serverIp == null && Bukkit.getServicesManager().getRegistration(BedWars.class) != null) {
            BedWars api = Bukkit.getServicesManager().getRegistration(BedWars.class).getProvider();
            if (api != null && api.getConfigs() != null && api.getConfigs().getMainConfig() != null) {
                serverIp = api.getConfigs().getMainConfig().getString(
                        ConfigPath.GENERAL_CONFIG_PLACEHOLDERS_REPLACEMENTS_SERVER_IP);
            }
        }
        return ChatColor.translateAlternateColorCodes('&', message
                .replace("{prefix}", prefix == null ? "" : prefix)
                .replace("{serverIp}", serverIp == null ? "" : serverIp));
    }

    /**
     * Get a color translated list.
     */
    public List<String> l(String path) {
        List<String> lines = rawList(this, path);
        List<String> result = new ArrayList<>();
        if (lines == null) lines = Collections.emptyList();
        for (String line : lines) {
            if (line != null) result.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        return result;
    }

    public static HashMap<UUID, Language> getLangByPlayer() {
        return langByPlayer;
    }

    /**
     * Check if a language exists.
     */
    public static boolean isLanguageExist(String iso) {
        return findLang(iso) != null;
    }

    /**
     * Find an explicitly loaded language.
     *
     * @return the matching language, or null when the ISO is unknown.
     */
    public static Language findLang(String iso) {
        if (iso == null) return null;
        for (Language language : languages) {
            if (language != null && language.iso != null && language.iso.equalsIgnoreCase(iso)) return language;
        }
        return null;
    }

    /**
     * Get a language by ISO, falling back to the server language for compatibility.
     */
    public static Language getLang(String iso) {
        Language language = findLang(iso);
        return language == null ? fallbackLanguage() : language;
    }

    /**
     * Get language iso code.
     */
    public String getIso() {
        return iso;
    }

    /**
     * Get loaded languages list.
     */
    public static List<Language> getLanguages() {
        return languages;
    }

    private static String getDefaultMessage(Language language, String path, String fallback) {
        String message = rawMessage(language, path);
        return message == null ? fallback : message;
    }

    private static List<String> getDefaultMessageList(Language language, String path, List<String> fallback) {
        List<String> messages = rawList(language, path);
        return messages == null ? fallback : messages;
    }

    private static String normalizeLegacyPlaceholder(String message) {
        return message.replaceAll("(?i)[&§][0-9A-FK-OR]", "").trim().toLowerCase(Locale.ROOT);
    }
    private static String plainMessage(Object value) {
        if (value instanceof List) {
            StringBuilder message = new StringBuilder();
            for (Object line : (List<?>) value) {
                if (line != null) message.append(line).append(' ');
            }
            return normalizeLegacyPlaceholder(message.toString());
        }
        return value instanceof String ? normalizeLegacyPlaceholder((String) value) : null;
    }

    private static boolean sameMessage(Object first, Object second) {
        String firstMessage = plainMessage(first);
        return firstMessage != null && firstMessage.equals(plainMessage(second));
    }

    private static boolean isLegacyUnsetName(Object value) {
        if (!(value instanceof String)) return false;
        String message = normalizeLegacyPlaceholder((String) value);
        return message.equals("name not set") || message.startsWith("name not set at:");
    }

    private static boolean isLegacyUnsetLore(Object value) {
        if (value instanceof String) return isLegacyUnsetLoreText((String) value);
        if (value instanceof List) {
            StringBuilder message = new StringBuilder();
            for (Object line : (List<?>) value) {
                if (line != null) message.append(line).append(' ');
            }
            return isLegacyUnsetLoreText(message.toString());
        }
        return false;
    }

    private static boolean isLegacyUnsetLoreText(String value) {
        String message = normalizeLegacyPlaceholder(value);
        return message.equals("lore not set") || message.startsWith("lore not set at:");
    }

    private static void addLocalizedDefaultName(Language language, String path, String fallback) {
        String message = getDefaultMessage(language, Messages.CONFIG_ITEM_NAME_NOT_SET, fallback);
        Object current = language.getYml().get(path);
        if (current == null || (!language.hasOverride(path) && isLegacyUnsetName(current) && !sameMessage(current, message))) {
            language.setRuntimeDefault(path, message);
        }
    }

    private static void addLocalizedDefaultLore(Language language, String path, List<String> fallback) {
        List<String> messages = getDefaultMessageList(language, Messages.CONFIG_ITEM_LORE_NOT_SET, fallback);
        Object current = language.getYml().get(path);
        if (current == null || (!language.hasOverride(path) && isLegacyUnsetLore(current) && !sameMessage(current, messages))) {
            language.setRuntimeDefault(path, messages);
        }
    }

    /**
     * Register runtime localized messages for unset stats items.
     */
    public static void setupCustomStatsMessages() {
        if (Bukkit.getServer().getServicesManager().getRegistration(BedWars.class) == null) return;
        BedWars api = Bukkit.getServer().getServicesManager().getRegistration(BedWars.class).getProvider();
        if (api == null || api.getConfigs() == null || api.getConfigs().getMainConfig() == null) return;
        YamlConfiguration main = api.getConfigs().getMainConfig().getYml();
        if (main == null || main.get(ConfigPath.GENERAL_CONFIGURATION_STATS_PATH) == null) return;
        ConfigurationSection stats = main.getConfigurationSection(ConfigPath.GENERAL_CONFIGURATION_STATS_PATH);
        if (stats == null) return;
        for (Language l : getLanguages()) {
            if (l == null || l.getYml() == null) continue;
            for (String item : stats.getKeys(false)) {
                if (ConfigPath.GENERAL_CONFIGURATION_STATS_GUI_SIZE.contains(item)) continue;
                String namePath = Messages.PLAYER_STATS_GUI_PATH + "-" + item + "-name";
                String lorePath = Messages.PLAYER_STATS_GUI_PATH + "-" + item + "-lore";
                addLocalizedDefaultName(l, namePath, "&cName not set");
                addLocalizedDefaultLore(l, lorePath, Collections.singletonList("&cLore not set"));
            }
        }
    }

    @SuppressWarnings("WeakerAccess")
    public void addDefaultStatsMsg(@NotNull YamlConfiguration yml, String path, String name, String... lore) {
        if (yml.getDefaults() == null || !yml.getDefaults().contains(Messages.PLAYER_STATS_GUI_PATH + "-" + path + "-name"))
            yml.addDefault(Messages.PLAYER_STATS_GUI_PATH + "-" + path + "-name", name);
        if (yml.getDefaults() == null || !yml.getDefaults().contains(Messages.PLAYER_STATS_GUI_PATH + "-" + path + "-lore"))
            yml.addDefault(Messages.PLAYER_STATS_GUI_PATH + "-" + path + "-lore", lore);
    }

    private static void addLocalizedItemDefaults(Language language, ConfigurationSection section,
                                                  String nameTemplate, String loreTemplate) {
        if (section == null) return;
        for (String item : section.getKeys(false)) {
            if (item.isEmpty()) continue;
            addLocalizedDefaultName(language, nameTemplate.replace("%path%", item), "&cName not set");
            addLocalizedDefaultLore(language, loreTemplate.replace("%path%", item),
                    Collections.singletonList("&cLore not set"));
        }
    }

    /**
     * Create missing name/ lore for items: multi arena lobby, waiting, spectating
     */
    public static void addDefaultMessagesCommandItems(Language language) {
        if (language == null || language.getYml() == null
                || Bukkit.getServer().getServicesManager().getRegistration(BedWars.class) == null) return;
        BedWars api = Bukkit.getServer().getServicesManager().getRegistration(BedWars.class).getProvider();
        if (api == null || api.getConfigs() == null || api.getConfigs().getMainConfig() == null) return;
        YamlConfiguration main = api.getConfigs().getMainConfig().getYml();
        if (main == null) return;
        addLocalizedItemDefaults(language,
                main.getConfigurationSection(ConfigPath.GENERAL_CONFIGURATION_LOBBY_ITEMS_PATH),
                Messages.GENERAL_CONFIGURATION_LOBBY_ITEMS_NAME,
                Messages.GENERAL_CONFIGURATION_LOBBY_ITEMS_LORE);
        addLocalizedItemDefaults(language,
                main.getConfigurationSection(ConfigPath.GENERAL_CONFIGURATION_SPECTATOR_ITEMS_PATH),
                Messages.GENERAL_CONFIGURATION_SPECTATOR_ITEMS_NAME,
                Messages.GENERAL_CONFIGURATION_SPECTATOR_ITEMS_LORE);
        addLocalizedItemDefaults(language,
                main.getConfigurationSection(ConfigPath.GENERAL_CONFIGURATION_PRE_GAME_ITEMS_PATH),
                Messages.GENERAL_CONFIGURATION_WAITING_ITEMS_NAME,
                Messages.GENERAL_CONFIGURATION_WAITING_ITEMS_LORE);
    }

    /**
     * Create messages paths for new shop categories.
     */
    @SuppressWarnings("DuplicateExpressions")
    public void setupUnSetCategories() {
        if (getYml() == null || Bukkit.getServer().getServicesManager().getRegistration(BedWars.class) == null) return;
        BedWars api = Bukkit.getServer().getServicesManager().getRegistration(BedWars.class).getProvider();
        if (api == null || api.getConfigs() == null || api.getConfigs().getShopConfig() == null
                || api.getConfigs().getShopConfig().getYml() == null) return;
        YamlConfiguration shop = api.getConfigs().getShopConfig().getYml();
        for (String s : shop.getKeys(false)) {
            if (s.equalsIgnoreCase(ConfigPath.SHOP_SETTINGS_PATH)
                    || s.equalsIgnoreCase(ConfigPath.SHOP_SPECIALS_PATH)
                    || s.equals(ConfigPath.SHOP_QUICK_DEFAULTS_PATH)) continue;
            String nameFallback = getDefaultMessage(this, Messages.CONFIG_ITEM_NAME_NOT_SET, "&8Name not set");
            List<String> loreFallback = getDefaultMessageList(this, Messages.CONFIG_ITEM_LORE_NOT_SET,
                    Collections.singletonList("&8Lore not set"));
            String inventoryNamePath = Messages.SHOP_CATEGORY_INVENTORY_NAME.replace("%category%", s);
            String itemNamePath = Messages.SHOP_CATEGORY_ITEM_NAME.replace("%category%", s);
            String itemLorePath = Messages.SHOP_CATEGORY_ITEM_LORE.replace("%category%", s);
            Object inventoryName = getYml().get(inventoryNamePath);
            Object itemName = getYml().get(itemNamePath);
            Object itemLore = getYml().get(itemLorePath);
            if (inventoryName == null || (!hasOverride(inventoryNamePath) && isLegacyUnsetName(inventoryName) && !sameMessage(inventoryName, nameFallback))) setRuntimeDefault(inventoryNamePath, nameFallback);
            if (itemName == null || (!hasOverride(itemNamePath) && isLegacyUnsetName(itemName) && !sameMessage(itemName, nameFallback))) setRuntimeDefault(itemNamePath, nameFallback);
            if (itemLore == null || (!hasOverride(itemLorePath) && isLegacyUnsetLore(itemLore) && !sameMessage(itemLore, loreFallback))) setRuntimeDefault(itemLorePath, loreFallback);
            ConfigurationSection content = shop.getConfigurationSection(s + ConfigPath.SHOP_CATEGORY_CONTENT_PATH);
            if (content == null) continue;
            for (String c : content.getKeys(false)) {
                String contentNamePath = Messages.SHOP_CONTENT_TIER_ITEM_NAME.replace("%category%", s).replace("%content%", c);
                String contentLorePath = Messages.SHOP_CONTENT_TIER_ITEM_LORE.replace("%category%", s).replace("%content%", c);
                Object contentName = getYml().get(contentNamePath);
                Object contentLore = getYml().get(contentLorePath);
                if (contentName == null || (!hasOverride(contentNamePath) && isLegacyUnsetName(contentName) && !sameMessage(contentName, nameFallback))) setRuntimeDefault(contentNamePath, nameFallback);
                if (contentLore == null || (!hasOverride(contentLorePath) && isLegacyUnsetLore(contentLore) && !sameMessage(contentLore, loreFallback))) setRuntimeDefault(contentLorePath, loreFallback);
            }
        }
    }

    /**
     * Add required messages for a shop category to the given yml.
     */
    public static void addCategoryMessages(@NotNull YamlConfiguration yml, String categoryName, String invName, String itemName, List<String> itemLore) {
        if (yml.getDefaults() == null || !yml.getDefaults().contains(Messages.SHOP_CATEGORY_INVENTORY_NAME.replace("%category%", categoryName)))
            yml.addDefault(Messages.SHOP_CATEGORY_INVENTORY_NAME.replace("%category%", categoryName), invName);
        if (yml.getDefaults() == null || !yml.getDefaults().contains(Messages.SHOP_CATEGORY_ITEM_NAME.replace("%category%", categoryName)))
            yml.addDefault(Messages.SHOP_CATEGORY_ITEM_NAME.replace("%category%", categoryName), itemName);
        if (yml.getDefaults() == null || !yml.getDefaults().contains(Messages.SHOP_CATEGORY_ITEM_LORE.replace("%category%", categoryName)))
            yml.addDefault(Messages.SHOP_CATEGORY_ITEM_LORE.replace("%category%", categoryName), itemLore);
    }

    /**
     * Add required messages for a shop category content to the given yml.
     */
    public static void addContentMessages(@NotNull YamlConfiguration yml, String contentName, String categoryName, String itemName, List<String> itemLore) {
        if (yml == null || contentName == null || categoryName == null) return;
        final String path1 = Messages.SHOP_CONTENT_TIER_ITEM_NAME.replace("%category%", categoryName).replace("%content%", contentName),
                path2 = Messages.SHOP_CONTENT_TIER_ITEM_LORE.replace("%category%", categoryName).replace("%content%", contentName);
        if (yml.getDefaults() == null || !yml.getDefaults().contains(path1)) yml.addDefault(path1, itemName);
        if (yml.getDefaults() == null || !yml.getDefaults().contains(path2)) yml.addDefault(path2, itemLore);
    }

    /**
     * Change a player language and refresh scoreboard and custom join items.
     */
    public static boolean setPlayerLanguage(UUID uuid, String iso) {
        if (uuid == null) return false;
        Language current = langByPlayer.get(uuid);
        if (iso == null) {
            if (current == null) return true;
            Language reset = fallbackLanguage();
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline() && reset != null) {
                PlayerLangChangeEvent event = new PlayerLangChangeEvent(player, current.getIso(), reset.getIso());
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) return false;
            }
            langByPlayer.remove(uuid);
            return true;
        }
        Language newLang = Language.findLang(iso.trim());
        if (newLang == null) return false;
        Language oldLang = current == null ? fallbackLanguage() : current;
        if (oldLang != null && oldLang.getIso().equalsIgnoreCase(newLang.getIso())) return false;
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline() && oldLang != null) {
            PlayerLangChangeEvent event = new PlayerLangChangeEvent(player, oldLang.getIso(), newLang.getIso());
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) return false;
        }
        if (defaultLanguage != null && defaultLanguage.getIso().equalsIgnoreCase(newLang.getIso())) {
            langByPlayer.remove(uuid);
        } else {
            langByPlayer.put(uuid, newLang);
        }
        return true;
    }
    public static String @NotNull [] getCountDownTitle(@NotNull Language playerLang, int second) {
        Language language = playerLang == null ? fallbackLanguage() : playerLang;
        String[] paths = {
                Messages.ARENA_STATUS_START_COUNTDOWN_TITLE,
                Messages.ARENA_STATUS_START_COUNTDOWN_SUB_TITLE
        };
        String[] result = new String[2];
        for (int i = 0; i < paths.length; i++) {
            String specific = rawMessage(language, paths[i] + "-" + second);
            String message = specific == null ? rawMessage(language, paths[i]) : specific;
            if (message == null) message = MISSING_LANGUAGE_MESSAGE;
            result[i] = ChatColor.translateAlternateColorCodes('&', message)
                    .replace("{second}", String.valueOf(second));
            if (result[i].isEmpty()) result[i] = " ";
        }
        return result;
    }

    /**
     * Change server default language.
     */
    public static void setDefaultLanguage(Language defaultLanguage) {
        if (defaultLanguage != null) Language.defaultLanguage = defaultLanguage;
    }

    /**
     * Get server default language.
     */
    public static Language getDefaultLanguage() {
        return defaultLanguage;
    }
}
