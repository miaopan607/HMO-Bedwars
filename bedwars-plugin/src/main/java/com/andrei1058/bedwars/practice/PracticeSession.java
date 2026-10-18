package com.andrei1058.bedwars.practice;

import com.andrei1058.bedwars.arena.PlayerGoods;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;
import java.io.IOException;
import java.util.*;

public final class PracticeSession {
    private final Player player;
    private PracticePoint point;
    private PracticeTarget target;
    private final PlayerGoods goods;
    private final Location returnLocation;
    private final PracticeRecovery recovery;
    private final Map<BlockKey, BlockState> blocks = new LinkedHashMap<>();
    private final Map<UUID, Projectile> projectiles = new HashMap<>();
    private final EnumMap<PracticeMode, int[]> layouts = new EnumMap<>(PracticeMode.class);
    private PracticeAttempt attempt;
    private final Location sample;
    private PracticeRecovery.Journal journal;
    private long round, lastFireballTime, statusUntil;
    private int heldSlot;
    private boolean resetting, damageEnabled;
    private String status = "";

    PracticeSession(Player player, PracticePoint point, PracticeTarget target, PracticeRecovery recovery) {
        this.player = player; this.point = point; this.target = target; this.recovery = recovery;
        goods = new PlayerGoods(player, false);
        returnLocation = player.getLocation();
        sample = point.getSpawn();
    }
    public Player getPlayer() { return player; }
    public PracticePoint getPoint() { return point; }
    public PracticeTarget getTarget() { return target; }
    public Location getSpawn() { return target == null ? point.getSpawn() : target.getSpawn(); }
    public Location getReturnLocation() { return returnLocation.clone(); }
    public PracticeAttempt getAttempt() {
        if (attempt == null) attempt = new PracticeAttempt();
        return attempt;
    }
    public boolean hasActiveAttempt() { return attempt != null && attempt.isActive(); }
    public Location sampleLocation() { return player.getLocation(sample); }
    public long getRound() { return round; }
    public boolean isResetting() { return resetting; }
    public void setResetting(boolean value) { resetting = value; }
    public boolean isDamageEnabled() { return damageEnabled; }
    public void toggleDamage() { damageEnabled = !damageEnabled; }
    public long getLastFireballTime() { return lastFireballTime; }
    public void setLastFireballTime(long value) { lastFireballTime = value; }
    public void status(String value) { status = value; statusUntil = System.nanoTime() + 3_000_000_000L; }
    public String getStatus(long now) { return now < statusUntil ? status : ""; }
    public void select(PracticePoint point, PracticeTarget target) {
        this.point = point; this.target = target;
    }
    public void nextRound() {
        round++; lastFireballTime = 0;
        if (attempt != null) attempt.clear();
        status = ""; statusUntil = 0;
        clearProjectiles();
    }
    public void trackProjectile(Projectile projectile) {
        projectiles.put(projectile.getUniqueId(), projectile);
    }
    public boolean ownsProjectile(Projectile projectile) { return projectiles.get(projectile.getUniqueId()) == projectile; }
    void pruneProjectiles() {
        Iterator<Projectile> iterator = projectiles.values().iterator();
        while (iterator.hasNext()) {
            Projectile projectile = iterator.next();
            if (projectile.isDead() || !projectile.isValid() || !point.contains(projectile.getLocation())) {
                projectile.remove(); iterator.remove();
            }
        }
    }
    public void removeProjectile(Projectile projectile) {
        projectiles.remove(projectile.getUniqueId());
    }
    private void clearProjectiles() {
        for (Projectile projectile : projectiles.values()) projectile.remove();
        projectiles.clear();
    }
    public void recordBlock(BlockState state) throws IOException {
        BlockKey key = new BlockKey(state.getBlock());
        if (blocks.containsKey(key)) return;
        if (journal == null) journal = recovery.open(player, state.getWorld());
        journal.record(state.getBlock());
        blocks.put(key, state);
    }
    public void restoreBlock(Block block) {
        BlockState state = blocks.remove(new BlockKey(block));
        if (state != null) state.update(true, false);
    }
    public void clearBlocks() throws IOException {
        for (BlockState state : blocks.values()) state.update(true, false);
        blocks.clear();
        if (journal != null) { journal.clear(); journal = null; }
    }
    public void rememberHeldSlot(int slot) {
        ItemStack item = player.getInventory().getItem(slot);
        if (item != null && !PracticeListener.control(item)) heldSlot = slot;
    }
    public void rememberLayout() {
        int[] slots = layouts.computeIfAbsent(point.getMode(), mode -> new int[]{0, 1});
        for (int slot = 0; slot < 36; slot++) {
            ItemStack item = player.getInventory().getItem(slot);
            if (item == null || PracticeListener.control(item)) continue;
            if (item.getType() == point.getMode().getMaterial()) slots[0] = slot;
            if (point.getMode() == PracticeMode.FIREBALL && item.getType() == Material.WOOL) slots[1] = slot;
        }
    }
    public int itemSlot(boolean wool) {
        int[] slots = layouts.get(point.getMode());
        return slots == null ? (wool ? 1 : 0) : slots[wool ? 1 : 0];
    }
    public int heldSlot() {
        int slot = heldSlot % 9;
        ItemStack item = player.getInventory().getItem(slot);
        if (item != null && !PracticeListener.control(item)) return slot;
        // 主物品在普通背包中时，保持选中训练槽而非固定控制槽。
        int primary = itemSlot(false);
        return primary < 6 ? primary : 0;
    }
    public void prepare() {
        player.setItemOnCursor(null); player.closeInventory();
        player.getInventory().clear(); player.getInventory().setArmorContents(null);
        for (PotionEffect effect : player.getActivePotionEffects()) player.removePotionEffect(effect.getType());
        player.setFlying(false); player.setAllowFlight(false); player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(player.getMaxHealth()); player.setFoodLevel(20); player.setSaturation(5); player.setExhaustion(0);
        player.setFireTicks(0); player.setFallDistance(0); player.setVelocity(new Vector());
    }
    public void restore() { goods.restore(); }

    private static final class BlockKey {
        private final int x, y, z;
        BlockKey(Block block) { x = block.getX(); y = block.getY(); z = block.getZ(); }
        @Override public boolean equals(Object value) {
            if (!(value instanceof BlockKey)) return false;
            BlockKey other = (BlockKey) value;
            return x == other.x && y == other.y && z == other.z;
        }
        @Override public int hashCode() { return (x * 31 + y) * 31 + z; }
    }
}
