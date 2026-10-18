package com.andrei1058.bedwars.practice;

import com.andrei1058.bedwars.arena.Misc;
import org.bukkit.Location;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class PracticePoint {
    private final String id, worldName;
    private final PracticeMode mode;
    private final Location spawn, min, max;
    private final int failY;
    private final boolean enabled;
    private final Map<String, PracticeTarget> targets = new LinkedHashMap<>();

    public PracticePoint(String id, String worldName, PracticeMode mode, Location spawn,
                         Location pos1, Location pos2, int failY, boolean enabled) {
        this.id = id;
        this.worldName = worldName;
        this.mode = mode;
        this.spawn = spawn == null ? null : spawn.clone();
        this.min = pos1 == null || pos2 == null ? null : Misc.minLoc(pos1, pos2);
        this.max = pos1 == null || pos2 == null ? null : Misc.maxLoc(pos1, pos2);
        this.failY = failY;
        this.enabled = enabled;
    }

    public String getId() { return id; }
    public String getWorldName() { return worldName; }
    public PracticeMode getMode() { return mode; }
    public Location getSpawn() { return spawn == null ? null : spawn.clone(); }
    public int getFailY() { return failY; }
    public boolean isEnabled() { return enabled; }
    public boolean isAvailable() {
        return enabled && spawn != null && spawn.getWorld() != null && min != null
                && org.bukkit.Bukkit.getWorld(worldName) == spawn.getWorld();
    }
    public void addTarget(PracticeTarget target) { targets.put(target.getId(), target); }
    public PracticeTarget getTarget(String id) { return id == null ? null : targets.get(id); }
    public Collection<PracticeTarget> getTargets() { return Collections.unmodifiableCollection(targets.values()); }

    public boolean containsPlayer(Location location) {
        return location != null && location.getWorld() != null && min != null
                && worldName.equals(location.getWorld().getName())
                && location.getX() >= min.getBlockX() && location.getX() < max.getBlockX() + 1.0
                && location.getZ() >= min.getBlockZ() && location.getZ() < max.getBlockZ() + 1.0
                && location.getY() >= failY && location.getY() < max.getBlockY() + 1.0;
    }

    public boolean contains(Location location) {
        return location != null && location.getWorld() != null
                && worldName.equals(location.getWorld().getName()) && containsCoordinates(location);
    }

    boolean containsCoordinates(Location location) {
        return min != null && location != null
                && location.getX() >= min.getBlockX() && location.getX() < max.getBlockX() + 1.0
                && location.getY() >= min.getBlockY() && location.getY() < max.getBlockY() + 1.0
                && location.getZ() >= min.getBlockZ() && location.getZ() < max.getBlockZ() + 1.0;
    }

    public boolean overlaps(PracticePoint other) {
        return min != null && other.min != null && worldName.equals(other.worldName)
                && min.getBlockX() <= other.max.getBlockX() && max.getBlockX() >= other.min.getBlockX()
                && Math.min(min.getBlockY(), failY) <= other.max.getBlockY()
                && max.getBlockY() >= Math.min(other.min.getBlockY(), other.failY)
                && min.getBlockZ() <= other.max.getBlockZ() && max.getBlockZ() >= other.min.getBlockZ();
    }
}
