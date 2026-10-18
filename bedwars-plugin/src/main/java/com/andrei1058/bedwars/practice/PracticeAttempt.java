package com.andrei1058.bedwars.practice;

/** 使用单调时钟计时；距离为起点到当前位置的水平直线距离，不累计绕行路程。 */
public final class PracticeAttempt {
    private double x, y, z, peak, distance;
    private long started, ended;
    private int blocks;
    private boolean active, finished, airborne;

    public void begin(double x, double y, double z, long now) {
        this.x = x; this.y = y; this.z = z;
        peak = y; distance = 0; started = now; ended = 0; blocks = 0;
        active = true; finished = false; airborne = false;
    }
    public void sample(double x, double y, double z, boolean onGround) {
        if (!active || finished) return;
        peak = Math.max(peak, y);
        distance = Math.hypot(x - this.x, z - this.z);
        if (!onGround) airborne = true;
    }
    public void placed() { blocks++; }
    public boolean isActive() { return active && !finished; }
    public boolean wasAirborne() { return airborne; }
    public void finish(long now) { if (isActive()) { ended = now; finished = true; } }
    public void clear() { active = false; finished = false; }
    public double seconds(long now) { return active ? Math.max(0.001, ((finished ? ended : now) - started) / 1_000_000_000.0) : 0; }
    public double getHeight() { return Math.max(0, peak - y); }
    public double getDistance() { return distance; }
    public int getBlocks() { return blocks; }
    public double speed(long now) { return active ? distance / seconds(now) : 0; }
}
