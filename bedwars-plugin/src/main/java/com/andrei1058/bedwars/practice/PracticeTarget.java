package com.andrei1058.bedwars.practice;

import com.andrei1058.bedwars.arena.Misc;
import org.bukkit.Location;

/** 目标区域使用玩家脚部坐标，不把目标岛的墙体算作着陆。 */
public final class PracticeTarget {
    private final String id, name;
    private final Location spawn, min, max;

    public PracticeTarget(String id, String name, Location spawn, Location first, Location second) {
        this.id = id;
        this.name = name;
        this.spawn = spawn.clone();
        min = Misc.minLoc(first, second);
        max = Misc.maxLoc(first, second);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public Location getSpawn() { return spawn.clone(); }
    public Location getMin() { return min.clone(); }
    public Location getMax() { return max.clone(); }
    public boolean contains(Location location) {
        return location.getWorld() == min.getWorld()
                && location.getX() >= min.getBlockX() && location.getX() < max.getBlockX() + 1.0
                && location.getY() >= min.getBlockY() && location.getY() < max.getBlockY() + 1.0
                && location.getZ() >= min.getBlockZ() && location.getZ() < max.getBlockZ() + 1.0;
    }
}
