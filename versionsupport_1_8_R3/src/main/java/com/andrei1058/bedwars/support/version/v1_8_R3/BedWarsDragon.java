package com.andrei1058.bedwars.support.version.v1_8_R3;

import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import net.minecraft.server.v1_8_R3.EntityEnderDragon;
import net.minecraft.server.v1_8_R3.MathHelper;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.ArrayList;
import java.util.List;

public class BedWarsDragon extends EntityEnderDragon {

    public BedWarsDragon(World world) {
        super(world);
    }

    @Override
    public void m() {
        super.m();
        if (world.isClientSide || !isAlive() || getHealth() <= 0 || ce()
                || !world.getGameRules().getBoolean("mobGriefing")) {
            return;
        }

        List<Block> blocks = findProtectedBlocks();
        if (blocks == null) {
            return;
        }

        EntityExplodeEvent event = new EntityExplodeEvent(getBukkitEntity(), getBukkitEntity().getLocation(), blocks, 0F);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        for (Block block : event.blockList()) {
            Material type = block.getType();
            if (type == Material.OBSIDIAN || type == Material.ENDER_STONE) {
                block.setType(Material.AIR);
            }
        }
    }

    private List<Block> findProtectedBlocks() {
        AxisAlignedBB head = bn.getBoundingBox();
        int headMinX = MathHelper.floor(head.a);
        int headMinY = MathHelper.floor(head.b);
        int headMinZ = MathHelper.floor(head.c);
        int headMaxX = MathHelper.floor(head.d);
        int headMaxY = MathHelper.floor(head.e);
        int headMaxZ = MathHelper.floor(head.f);
        org.bukkit.World bukkitWorld = world.getWorld();
        List<Block> blocks = null;

        for (int x = headMinX; x <= headMaxX; x++) {
            for (int y = headMinY; y <= headMaxY; y++) {
                for (int z = headMinZ; z <= headMaxZ; z++) {
                    Block block = bukkitWorld.getBlockAt(x, y, z);
                    Material type = block.getType();
                    if (type == Material.OBSIDIAN || type == Material.ENDER_STONE) {
                        if (blocks == null) {
                            blocks = new ArrayList<>();
                        }
                        blocks.add(block);
                    }
                }
            }
        }

        AxisAlignedBB body = bo.getBoundingBox();
        int bodyMinX = MathHelper.floor(body.a);
        int bodyMinY = MathHelper.floor(body.b);
        int bodyMinZ = MathHelper.floor(body.c);
        int bodyMaxX = MathHelper.floor(body.d);
        int bodyMaxY = MathHelper.floor(body.e);
        int bodyMaxZ = MathHelper.floor(body.f);
        for (int x = bodyMinX; x <= bodyMaxX; x++) {
            for (int y = bodyMinY; y <= bodyMaxY; y++) {
                for (int z = bodyMinZ; z <= bodyMaxZ; z++) {
                    if (x >= headMinX && x <= headMaxX && y >= headMinY && y <= headMaxY
                            && z >= headMinZ && z <= headMaxZ) {
                        continue;
                    }
                    Block block = bukkitWorld.getBlockAt(x, y, z);
                    Material type = block.getType();
                    if (type == Material.OBSIDIAN || type == Material.ENDER_STONE) {
                        if (blocks == null) {
                            blocks = new ArrayList<>();
                        }
                        blocks.add(block);
                    }
                }
            }
        }
        return blocks;
    }
}
