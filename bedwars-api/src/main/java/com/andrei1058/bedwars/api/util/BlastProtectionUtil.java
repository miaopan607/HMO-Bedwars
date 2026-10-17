package com.andrei1058.bedwars.api.util;

import com.andrei1058.bedwars.api.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import com.andrei1058.bedwars.api.server.VersionSupport;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BlastProtectionUtil {

    private final VersionSupport versionSupport;
    private final BedWars api;

    public BlastProtectionUtil(VersionSupport versionSupport, BedWars api) {
        this.versionSupport = versionSupport;
        this.api = api;
    }

    /**
     * Checks blast protection for one block in the POV's world.
     *
     * @param arena owning arena, required
     * @param pov explosion point of view, required
     * @param block block in the same world as the POV
     * @param step ray sampling distance (0.25 - 0.5 recommended)
     * @return whether the block is protected
     */
    public boolean isProtected(@NotNull IArena arena, Location pov, @NotNull Block block, double step) {
        return isProtected(createContext(arena, pov, step), block);
    }

    /**
     * Filters one explosion's same-world candidates with a shared classification cache.
     */
    public void filterBlocks(@NotNull IArena arena, @NotNull Location pov, @NotNull List<Block> blocks, double step) {
        if (blocks.isEmpty()) return;
        ExplosionContext context = createContext(arena, pov, step);
        blocks.removeIf(block -> isProtected(context, block));
    }

    private ExplosionContext createContext(IArena arena, Location pov, double step) {
        boolean rayBlockedByGlass = api.getConfigs().getMainConfig()
                .getBoolean(ConfigPath.GENERAL_TNT_RAY_BLOCKED_BY_GLASS);
        return new ExplosionContext(arena, pov, step, rayBlockedByGlass);
    }

    private boolean isProtected(ExplosionContext context, Block block) {
        Location location = block.getLocation();
        if (context.arena.isProtected(location) || context.arena.isTeamBed(location)) return true;

        Vector target = location.toVector();
        int protectedWeight = 0;
        int processedWeight = 0;
        for (Map.Entry<Vector, Integer> entry : context.origins.entrySet()) {
            Vector origin = entry.getKey();
            int weight = entry.getValue();
            boolean blocked = false;
            // Same-block rays remain unblocked votes in the original 27-ray threshold.
            if (!origin.equals(target)) {
                BlockRay ray = new BlockRay(context.world, origin, target, context.step);
                while (ray.hasNext()) {
                    if (isBlocking(context, ray.next())) {
                        blocked = true;
                        break;
                    }
                }
            }
            processedWeight += weight;
            if (blocked) protectedWeight += weight;
            if (protectedWeight >= 22) return true;
            if (protectedWeight + (27 - processedWeight) < 22) return false;
        }
        return protectedWeight >= 22;
    }

    private boolean isBlocking(ExplosionContext context, Block block) {
        BlockCoordinate coordinate = new BlockCoordinate(block.getX(), block.getY(), block.getZ());
        Boolean cached = context.blocking.get(coordinate);
        if (cached != null) return cached;

        Material type = block.getType();
        boolean blocked = type != Material.AIR
                && ((context.rayBlockedByGlass && versionSupport.isGlass(type))
                || (!context.allowMapBreak && !context.placed.contains(coordinate)));
        context.blocking.put(coordinate, blocked);
        return blocked;
    }

    private static final class ExplosionContext {
        private final IArena arena;
        private final World world;
        private final double step;
        private final boolean rayBlockedByGlass;
        private final boolean allowMapBreak;
        private final Set<BlockCoordinate> placed;
        private final Map<BlockCoordinate, Boolean> blocking = new HashMap<>();
        private final Map<Vector, Integer> origins = new LinkedHashMap<>();

        private ExplosionContext(IArena arena, Location pov, double step, boolean rayBlockedByGlass) {
            this.arena = arena;
            this.world = pov.getWorld();
            this.step = step;
            this.rayBlockedByGlass = rayBlockedByGlass;
            this.allowMapBreak = arena.isAllowMapBreak();
            this.placed = arena.getPlaced();
            // Group actual integer origins, retaining all 27 votes and their visitation order.
            for (double x = -0.73; x <= 0.73; x += 0.73) {
                for (double y = -0.73; y <= 0.73; y += 0.73) {
                    for (double z = -0.73; z <= 0.73; z += 0.73) {
                        Vector origin = new Vector(
                                NumberConversions.floor(pov.getX() + x),
                                NumberConversions.floor(pov.getY() + y),
                                NumberConversions.floor(pov.getZ() + z));
                        origins.merge(origin, 1, Integer::sum);
                    }
                }
            }
        }
    }
}
