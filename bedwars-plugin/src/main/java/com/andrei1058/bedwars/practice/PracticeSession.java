package com.andrei1058.bedwars.practice;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;
import java.util.*;

public final class PracticeSession {
    private final Player player;
    private final PracticePoint point;
    private final Location returnLocation;
    private final ItemStack[] contents, armor;
    private final ItemStack cursor;
    private final int heldSlot, food, fireTicks;
    private final float saturation, exhaustion, fallDistance;
    private final double health;
    private final GameMode gameMode;
    private final boolean allowFlight, flying;
    private final Vector velocity;
    private final Collection<PotionEffect> effects;
    private final Map<BlockKey, BlockState> blocks = new LinkedHashMap<>();
    private final Map<UUID, Projectile> projectiles = new HashMap<>();
    private long round, lastFireballTime;
    private boolean resetting;

    public PracticeSession(Player player, PracticePoint point) {
        this.player = player;
        this.point = point;
        returnLocation = player.getLocation();
        contents = copy(player.getInventory().getContents());
        armor = copy(player.getInventory().getArmorContents());
        cursor = cloneItem(player.getItemOnCursor());
        heldSlot = player.getInventory().getHeldItemSlot();
        food = player.getFoodLevel(); saturation = player.getSaturation(); exhaustion = player.getExhaustion();
        health = player.getHealth(); gameMode = player.getGameMode();
        allowFlight = player.getAllowFlight(); flying = player.isFlying();
        fireTicks = player.getFireTicks(); fallDistance = player.getFallDistance(); velocity = player.getVelocity();
        effects = new ArrayList<>(player.getActivePotionEffects());
    }
    public Player getPlayer() { return player; }
    public PracticePoint getPoint() { return point; }
    public Location getReturnLocation() { return returnLocation.clone(); }
    public long getRound() { return round; }
    public boolean isResetting() { return resetting; }
    public void setResetting(boolean value) { resetting = value; }
    public long getLastFireballTime() { return lastFireballTime; }
    public void setLastFireballTime(long value) { lastFireballTime = value; }
    public void nextRound() { round++; lastFireballTime = 0; clearProjectiles(); }
    public void trackProjectile(Projectile projectile) { projectiles.put(projectile.getUniqueId(), projectile); }
    public boolean ownsProjectile(Projectile projectile) { return projectiles.get(projectile.getUniqueId()) == projectile; }
    boolean pruneProjectiles() {
        if (projectiles.isEmpty()) return false;
        Iterator<Projectile> iterator = projectiles.values().iterator();
        while (iterator.hasNext()) {
            Projectile projectile = iterator.next();
            if (projectile.isDead() || !projectile.isValid()) iterator.remove();
            else if (!point.contains(projectile.getLocation())) {
                iterator.remove();
                projectile.remove();
                if (projectile instanceof EnderPearl) return true;
            }
        }
        return false;
    }
    public void removeProjectile(Projectile projectile) { projectiles.remove(projectile.getUniqueId()); }
    private void clearProjectiles() {
        for (Projectile projectile : projectiles.values()) projectile.remove();
        projectiles.clear();
    }
    public void recordBlock(BlockState state) { blocks.putIfAbsent(new BlockKey(state.getBlock()), state); }
    public void restoreBlock(Block block) {
        BlockState state = blocks.remove(new BlockKey(block));
        if (state != null) state.update(true, false);
    }
    public void clearBlocks() {
        for (BlockState state : blocks.values()) state.update(true, false);
        blocks.clear();
    }
    public void prepare() {
        player.setItemOnCursor(null);
        player.closeInventory();
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        for (PotionEffect effect : player.getActivePotionEffects()) player.removePotionEffect(effect.getType());
        player.setFlying(false); player.setAllowFlight(false); player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(player.getMaxHealth()); player.setFoodLevel(20); player.setSaturation(5); player.setExhaustion(0);
        player.setFireTicks(0); player.setFallDistance(0); player.setVelocity(new Vector());
    }
    public void restore() {
        player.setItemOnCursor(null);
        player.closeInventory();
        for (PotionEffect effect : player.getActivePotionEffects()) player.removePotionEffect(effect.getType());
        for (PotionEffect effect : effects) player.addPotionEffect(effect, true);
        player.getInventory().setContents(copy(contents)); player.getInventory().setArmorContents(copy(armor));
        player.getInventory().setHeldItemSlot(heldSlot);
        player.setGameMode(gameMode); player.setAllowFlight(allowFlight); player.setFlying(flying);
        player.setHealth(Math.min(health, player.getMaxHealth())); player.setFoodLevel(food);
        player.setSaturation(saturation); player.setExhaustion(exhaustion);
        player.setFireTicks(fireTicks); player.setFallDistance(fallDistance); player.setVelocity(velocity);
        player.setItemOnCursor(cloneItem(cursor)); player.updateInventory();
    }
    private static ItemStack cloneItem(ItemStack item) { return item == null ? null : item.clone(); }
    private static ItemStack[] copy(ItemStack[] source) {
        ItemStack[] result = new ItemStack[source.length];
        for (int i = 0; i < source.length; i++) result[i] = cloneItem(source[i]);
        return result;
    }
    private static final class BlockKey {
        private final UUID world;
        private final int x, y, z;
        BlockKey(Block block) { world = block.getWorld().getUID(); x = block.getX(); y = block.getY(); z = block.getZ(); }
        @Override public boolean equals(Object value) {
            if (!(value instanceof BlockKey)) return false;
            BlockKey other = (BlockKey) value;
            return x == other.x && y == other.y && z == other.z && world.equals(other.world);
        }
        @Override public int hashCode() {
            int hash = 31 + world.hashCode();
            hash = 31 * hash + x;
            hash = 31 * hash + y;
            return 31 * hash + z;
        }
    }
}
