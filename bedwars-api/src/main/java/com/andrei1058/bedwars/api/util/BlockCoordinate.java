package com.andrei1058.bedwars.api.util;

/** Immutable integer coordinates within an arena world. */
public final class BlockCoordinate {
    private final int x;
    private final int y;
    private final int z;

    public BlockCoordinate(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof BlockCoordinate)) return false;
        BlockCoordinate coordinate = (BlockCoordinate) other;
        return x == coordinate.x && y == coordinate.y && z == coordinate.z;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return 31 * result + z;
    }
}
