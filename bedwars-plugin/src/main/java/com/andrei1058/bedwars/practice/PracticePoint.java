package com.andrei1058.bedwars.practice;

import com.andrei1058.bedwars.arena.Misc;
import org.bukkit.Location;

public final class PracticePoint {
    private final String id, worldName;
    private final PracticeMode mode;
    private final Location spawn, min, max;
    private final int failY;
    private final boolean enabled;

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
    public boolean isAvailable() { return enabled && spawn != null && spawn.getWorld() != null && min != null; }
    public int getMinY() { return min == null ? 0 : min.getBlockY(); }

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
                && min.getBlockY() <= other.max.getBlockY() && max.getBlockY() >= other.min.getBlockY()
                && min.getBlockZ() <= other.max.getBlockZ() && max.getBlockZ() >= other.min.getBlockZ();
    }
}
