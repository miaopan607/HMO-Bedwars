package com.andrei1058.bedwars.api.util;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

import java.util.Iterator;
import java.util.NoSuchElementException;

/** Traces each sampled block coordinate once between two vectors. */
public class BlockRay implements Iterator<Block> {
    private final World world;

    private final Vector delta;

    private final double multiple;
    private final int parts;
    private int consumed;

    private final double xOffset;
    private final double yOffset;
    private final double zOffset;

    private double lcx;
    private double lcy;
    private double lcz;

    private int currentBlock;
    private int queueSize;

    private final Block[] blockQueue;

    /**
     * Constructs a BlockRay instance.
     *
     * @param world bukkit world to get the blocks from.
     * @param src   the source vector (where we shoot the ray).
     * @param dst   the destination vector.
     * @param step  how frequent to check the ray (0.25 - 0.5 recommended).
     */
    public BlockRay(World world, Vector src, Vector dst, double step) {

        // How much `pov` needs to change in order to reach the target
        this.delta = new Vector(
                dst.getBlockX() - src.getBlockX(),
                dst.getBlockY() - src.getBlockY(),
                dst.getBlockZ() - src.getBlockZ()
        );

        if (delta.lengthSquared() == 0) {
            throw new IllegalArgumentException("The source vector is the same as the destination vector");
        }

        this.world = world;

        // Coordinates for where we should shoot our ray
        // Exactly from the center.
        this.xOffset = src.getBlockX() + 0.5;
        this.yOffset = src.getBlockY() + 0.5;
        this.zOffset = src.getBlockZ() + 0.5;

        double sections = delta.length() / step;
        this.multiple = 1 / sections;
        this.parts = NumberConversions.ceil(sections);

        // Buffer distinct components in their original visitation order.
        this.blockQueue = new Block[7];

        scan();
    }

    @Override
    public boolean hasNext() {
        while (currentBlock >= queueSize && consumed <= parts) {
            scan();
        }
        return currentBlock < queueSize;
    }

    @Override
    public Block next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more blocks");
        }

        return blockQueue[currentBlock++];
    }

    private void scan() {
        double cx = (multiple * delta.getX() * consumed);
        double cy = (multiple * delta.getY() * consumed);
        double cz = (multiple * delta.getZ() * consumed);

        int lastXFloor = NumberConversions.floor(xOffset + lcx);
        int lastYFloor = NumberConversions.floor(yOffset + lcy);
        int lastZFloor = NumberConversions.floor(zOffset + lcz);

        int currentXFloor = NumberConversions.floor(xOffset + cx);
        int currentYFloor = NumberConversions.floor(yOffset + cy);
        int currentZFloor = NumberConversions.floor(zOffset + cz);

        queueSize = 0;
        currentBlock = 0;
        queueBlock(currentXFloor, currentYFloor, currentZFloor, lastXFloor, lastYFloor, lastZFloor);
        queueBlock(lastXFloor, currentYFloor, currentZFloor, lastXFloor, lastYFloor, lastZFloor);
        queueBlock(currentXFloor, lastYFloor, currentZFloor, lastXFloor, lastYFloor, lastZFloor);
        queueBlock(currentXFloor, currentYFloor, lastZFloor, lastXFloor, lastYFloor, lastZFloor);
        queueBlock(lastXFloor, lastYFloor, currentZFloor, lastXFloor, lastYFloor, lastZFloor);
        queueBlock(lastXFloor, currentYFloor, lastZFloor, lastXFloor, lastYFloor, lastZFloor);
        queueBlock(currentXFloor, lastYFloor, lastZFloor, lastXFloor, lastYFloor, lastZFloor);

        lcx = cx;
        lcy = cy;
        lcz = cz;

        consumed += 1;
    }

    private void queueBlock(int x, int y, int z, int previousX, int previousY, int previousZ) {
        // Adjacent monotone sampling steps only share their common endpoint.
        if (consumed > 0 && x == previousX && y == previousY && z == previousZ) return;
        for (int i = 0; i < queueSize; i++) {
            Block queued = blockQueue[i];
            if (queued.getX() == x && queued.getY() == y && queued.getZ() == z) return;
        }
        blockQueue[queueSize++] = world.getBlockAt(x, y, z);
    }

}
