package com.andrei1058.bedwars.practice;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.configuration.ConfigManager;
import com.andrei1058.bedwars.arena.SetupSession;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.*;

/** 每个练习点独立校验；正在使用的定义在重载时保持不变。 */
public final class PracticeConfig extends ConfigManager {
    public static final String POINTS = "points";
    public static final String NPC_LOCATION = "npc.location";
    public static final String NPC_UUID = "npc.entity-uuid";
    private Map<String, PracticePoint> points = new TreeMap<>();
    private final Map<String, String> errors = new TreeMap<>();

    public PracticeConfig() {
        super(BedWars.plugin, "practice", BedWars.plugin.getDataFolder().getPath());
        try {
            YamlConfiguration candidate = readConfiguration();
            if (!candidate.contains(POINTS)) {
                candidate.createSection(POINTS);
                saveConfiguration(candidate);
            }
            install(candidate);
        } catch (IOException | InvalidConfigurationException | IllegalArgumentException ex) {
            BedWars.plugin.getLogger().warning("Cannot load practice.yml: " + ex.getMessage());
        }
    }

    public Collection<PracticePoint> getPoints() { return Collections.unmodifiableCollection(points.values()); }
    public Map<String, String> getErrors() { return Collections.unmodifiableMap(errors); }
    public PracticePoint getPoint(String id) { return points.get(id); }
    public boolean usesWorld(String world) {
        for (PracticePoint point : points.values()) if (point.getWorldName().equals(world)) return true;
        return false;
    }
    public YamlConfiguration copyConfiguration() throws InvalidConfigurationException {
        YamlConfiguration copy = new YamlConfiguration();
        copy.loadFromString(getYml().saveToString());
        return copy;
    }
    public void commit(YamlConfiguration candidate, String id) throws IOException {
        validateNpc(candidate);
        PracticePoint changed = parsePoint(candidate, id);
        for (PracticePoint other : points.values()) {
            if (!other.getId().equals(id) && changed != null && changed.isEnabled() && other.isEnabled()
                    && changed.overlaps(other)) throw new IllegalArgumentException("Overlaps " + other.getId());
        }
        // 只保存本次点位，避免覆盖磁盘上尚未重载的其他修改。
        try {
            YamlConfiguration disk = readConfiguration();
            copySection(candidate, disk, "points." + id);
            saveConfiguration(disk);
        } catch (InvalidConfigurationException ex) { throw new IOException(ex); }
        replaceConfiguration(candidate);
        errors.remove(id);
        if (changed == null) points.remove(id); else points.put(id, changed);
    }
    public void commitNpc(YamlConfiguration candidate) throws IOException {
        validateNpc(candidate);
        try {
            YamlConfiguration disk = readConfiguration();
            copySection(candidate, disk, "npc");
            saveConfiguration(disk);
            copySection(candidate, getYml(), "npc");
        } catch (InvalidConfigurationException ex) { throw new IOException(ex); }
    }
    private static void copySection(YamlConfiguration source, YamlConfiguration destination, String path) {
        ConfigurationSection section = source.getConfigurationSection(path);
        destination.set(path, null);
        if (section != null) destination.createSection(path, section.getValues(true));
    }
    public void install(YamlConfiguration candidate) { install(candidate, Collections.emptySet()); }
    public void install(YamlConfiguration candidate, Set<String> retained) {
        validateNpc(candidate);
        ConfigurationSection section = candidate.getConfigurationSection(POINTS);
        if (section == null) throw new IllegalArgumentException("points must be a section");
        Map<String, PracticePoint> parsed = new TreeMap<>();
        Map<String, String> rejected = new TreeMap<>();
        for (String id : retained) {
            if (points.containsKey(id)) {
                parsed.put(id, points.get(id));
                copySection(getYml(), candidate, "points." + id);
            }
        }
        for (String id : candidate.getConfigurationSection(POINTS).getKeys(false)) {
            if (retained.contains(id)) continue;
            try {
                PracticePoint point = parsePoint(candidate, id);
                for (PracticePoint other : parsed.values()) {
                    if (point.isEnabled() && other.isEnabled() && point.overlaps(other))
                        throw new IllegalArgumentException("Overlaps " + other.getId());
                }
                parsed.put(id, point);
            } catch (IllegalArgumentException ex) {
                rejected.put(id, ex.getMessage());
                BedWars.plugin.getLogger().warning("Practice " + id + ": " + ex.getMessage());
            }
        }
        replaceConfiguration(candidate);
        points = parsed;
        errors.clear(); errors.putAll(rejected);
    }
    public void installPoint(YamlConfiguration candidate, String id) {
        PracticePoint changed = parsePoint(candidate, id);
        for (PracticePoint other : points.values()) {
            if (!other.getId().equals(id) && changed != null && changed.isEnabled() && other.isEnabled()
                    && changed.overlaps(other)) throw new IllegalArgumentException("Overlaps " + other.getId());
        }
        copySection(candidate, getYml(), "points." + id);
        errors.remove(id);
        if (changed == null) points.remove(id); else points.put(id, changed);
    }
    public UUID getNpcUuid() {
        String value = getYml().getString(NPC_UUID);
        return value == null ? null : UUID.fromString(value);
    }

    private PracticePoint parsePoint(YamlConfiguration candidate, String id) {
        if (!id.matches("[a-z0-9_-]+")) throw new IllegalArgumentException("Invalid point id");
        String root = POINTS + "." + id + ".";
        if (!candidate.contains(POINTS + "." + id)) return null;
        String name = candidate.getString(root + "world");
        validateWorld(name);
        PracticeMode mode = PracticeMode.parse(candidate.getString(root + "mode"));
        if (!(candidate.get(root + "enabled") instanceof Boolean)) throw new IllegalArgumentException("enabled must be a boolean");
        boolean enabled = candidate.getBoolean(root + "enabled");
        World world = Bukkit.getWorld(name);
        if (enabled && world == null && new File(Bukkit.getWorldContainer(), name + "/level.dat").isFile()) {
            world = Bukkit.createWorld(new WorldCreator(name));
        }
        Location spawn = location(candidate.getString(root + "spawn"), world);
        Location first = location(candidate.getString(root + "pos1"), world);
        Location second = location(candidate.getString(root + "pos2"), world);
        if (!(candidate.get(root + "fail-y") instanceof Integer)) throw new IllegalArgumentException("fail-y must be an integer");
        int failY = candidate.getInt(root + "fail-y");
        PracticePoint point = new PracticePoint(id, name, mode, spawn, first, second, failY, enabled);
        if (enabled && (spawn == null || !point.containsCoordinates(spawn) || failY < 0 || failY >= spawn.getY()))
            throw new IllegalArgumentException("Invalid bounds, spawn or fail-y");
        if (enabled && world != null && !BedWars.nms.isPracticeSpawnSafe(spawn)) throw new IllegalArgumentException("Unsafe spawn");
        ConfigurationSection targets = mode == PracticeMode.BRIDGE ? candidate.getConfigurationSection(root + "targets") : null;
        if (targets != null) for (String targetId : targets.getKeys(false)) {
            String targetRoot = root + "targets." + targetId + ".";
            Location start = location(candidate.getString(targetRoot + "spawn"), world);
            Location min = location(candidate.getString(targetRoot + "pos1"), world);
            Location max = location(candidate.getString(targetRoot + "pos2"), world);
            if (!targetId.matches("[a-z0-9_-]+") || start == null || min == null || max == null
                    || !point.containsCoordinates(start) || !point.containsCoordinates(min) || !point.containsCoordinates(max)
                    || start.getY() <= failY || Math.min(min.getY(), max.getY()) <= failY)
                throw new IllegalArgumentException("Invalid target: " + targetId);
            if (enabled && world != null && !BedWars.nms.isPracticeSpawnSafe(start))
                throw new IllegalArgumentException("Unsafe target spawn: " + targetId);
            point.addTarget(new PracticeTarget(targetId, candidate.getString(targetRoot + "name", targetId), start, min, max));
        }
        return point;
    }
    private void validateNpc(YamlConfiguration candidate) {
        String npc = candidate.getString(NPC_LOCATION);
        if (npc != null) {
            String[] parts = npc.split(",", -1);
            if (parts.length != 6) throw new IllegalArgumentException("Invalid NPC location");
            location(String.join(",", Arrays.copyOf(parts, 5)), Bukkit.getWorld(parts[5]));
            if (!parts[5].equals(BedWars.config.getLobbyWorldName())) throw new IllegalArgumentException("NPC must be in lobby world");
        }
        String uuid = candidate.getString(NPC_UUID);
        if (uuid != null) UUID.fromString(uuid);
    }
    private static void validateWorld(String name) {
        if (name == null || name.isEmpty() || name.contains("/") || name.contains("\\") || name.contains("..") || name.contains("bw_temp_"))
            throw new IllegalArgumentException("Invalid practice world");
        if (name.equalsIgnoreCase(Bukkit.getWorlds().get(0).getName()) || name.equalsIgnoreCase(BedWars.config.getLobbyWorldName()))
            throw new IllegalArgumentException("Practice requires an independent world");
        File[] arenas = new File(BedWars.plugin.getDataFolder(), "Arenas").listFiles();
        if (arenas != null) for (File arena : arenas) {
            if (arena.getName().equalsIgnoreCase(name + ".yml")) throw new IllegalArgumentException("World belongs to an arena");
        }
        for (SetupSession session : SetupSession.getSetupSessions()) {
            if (name.equalsIgnoreCase(session.getWorldName())) throw new IllegalArgumentException("World is in arena setup");
        }
    }
    private static Location location(String value, World world) {
        if (value == null) return null;
        String[] parts = value.split(",", -1);
        if (parts.length != 5) throw new IllegalArgumentException("Invalid location: " + value);
        double[] numbers = new double[5];
        for (int i = 0; i < 5; i++) {
            numbers[i] = Double.parseDouble(parts[i]);
            if (!Double.isFinite(numbers[i])) throw new IllegalArgumentException("Non-finite location");
        }
        if (numbers[1] < 0 || numbers[1] >= 256) throw new IllegalArgumentException("Location outside world height");
        return new Location(world, numbers[0], numbers[1], numbers[2], (float) numbers[3], (float) numbers[4]);
    }
}
