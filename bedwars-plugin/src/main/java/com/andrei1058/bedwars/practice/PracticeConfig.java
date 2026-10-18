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
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.Set;
import java.util.UUID;

/** Only validated, successfully persisted candidates become live definitions. */
public final class PracticeConfig extends ConfigManager {
    public static final String POINTS = "points";
    public static final String NPC_LOCATION = "npc.location";
    public static final String NPC_UUID = "npc.entity-uuid";
    private Map<String, PracticePoint> points = Collections.emptyMap();

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
    public void commit(YamlConfiguration candidate) throws IOException {
        Map<String, PracticePoint> parsed = parse(candidate);
        saveConfiguration(candidate);
        replaceConfiguration(candidate);
        points = parsed;
    }
    public void install(YamlConfiguration candidate) {
        Map<String, PracticePoint> parsed = parse(candidate);
        replaceConfiguration(candidate);
        points = parsed;
    }
    public UUID getNpcUuid() {
        String value = getYml().getString(NPC_UUID);
        return value == null ? null : UUID.fromString(value);
    }

    private Map<String, PracticePoint> parse(YamlConfiguration candidate) {
        Map<String, PracticePoint> result = new TreeMap<>();
        ConfigurationSection section = candidate.getConfigurationSection(POINTS);
        if (section == null) throw new IllegalArgumentException("points must be a section");
        Set<String> ids = section.getKeys(false);
        File[] arenas = ids.isEmpty() ? null : new File(BedWars.plugin.getDataFolder(), "Arenas").listFiles();
        for (String id : ids) {
            if (!id.matches("[a-z0-9_-]+")) throw new IllegalArgumentException("Invalid point id: " + id);
            String root = POINTS + "." + id + ".";
            String name = candidate.getString(root + "world");
            validateWorld(name, arenas);
            PracticeMode mode = PracticeMode.parse(candidate.getString(root + "mode"));
            if (!(candidate.get(root + "enabled") instanceof Boolean)) {
                throw new IllegalArgumentException(id + ": enabled must be a boolean");
            }
            boolean enabled = candidate.getBoolean(root + "enabled");
            World world = Bukkit.getWorld(name);
            if (enabled && world == null) {
                File directory = new File(Bukkit.getWorldContainer(), name);
                if (new File(directory, "level.dat").isFile()) {
                    try { world = Bukkit.createWorld(new WorldCreator(name)); }
                    catch (RuntimeException ex) { BedWars.plugin.getLogger().warning("Practice " + id + " world " + name + ": " + ex.getMessage()); }
                }
                if (world == null) BedWars.plugin.getLogger().warning("Practice " + id + " world unavailable: " + name);
            }
            Location spawn = location(candidate.getString(root + "spawn"), world);
            Location pos1 = location(candidate.getString(root + "pos1"), world);
            Location pos2 = location(candidate.getString(root + "pos2"), world);
            Object fail = candidate.get(root + "fail-y");
            if (!(fail instanceof Integer)) throw new IllegalArgumentException(id + ": fail-y must be an integer");
            int failY = (Integer) fail;
            PracticePoint point = new PracticePoint(id, name, mode, spawn, pos1, pos2, failY, enabled);
            if (enabled) {
                if (!point.containsCoordinates(spawn) || spawn.getY() <= failY || failY > point.getMinY())
                    throw new IllegalArgumentException(id + ": incomplete or invalid point");
                if (world != null && !BedWars.nms.isPracticeSpawnSafe(spawn)) throw new IllegalArgumentException(id + ": unsafe spawn");
                for (PracticePoint other : result.values()) {
                    if (other.isEnabled() && point.overlaps(other)) throw new IllegalArgumentException(id + ": overlaps " + other.getId());
                }
            }
            result.put(id, point);
        }
        String npc = candidate.getString(NPC_LOCATION);
        if (npc != null) {
            String[] parts = npc.split(",", -1);
            if (parts.length != 6) throw new IllegalArgumentException("Invalid NPC location");
            location(String.join(",", java.util.Arrays.copyOf(parts, 5)), Bukkit.getWorld(parts[5]));
            if (!parts[5].equals(BedWars.config.getLobbyWorldName())) throw new IllegalArgumentException("NPC must be in lobby world");
        }
        String uuid = candidate.getString(NPC_UUID);
        if (uuid != null) UUID.fromString(uuid);
        return result;
    }

    private static void validateWorld(String name, File[] arenas) {
        if (name == null || name.isEmpty() || name.contains("/") || name.contains("\\") || name.contains("..") || name.contains("bw_temp_"))
            throw new IllegalArgumentException("Invalid practice world");
        if (name.equalsIgnoreCase(Bukkit.getWorlds().get(0).getName()) || name.equalsIgnoreCase(BedWars.config.getLobbyWorldName()))
            throw new IllegalArgumentException("Practice requires an independent world");
        if (arenas != null) for (File arena : arenas) {
            if (arena.getName().equalsIgnoreCase(name + ".yml")) throw new IllegalArgumentException("World belongs to an arena: " + name);
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
