package com.andrei1058.bedwars.listeners;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.practice.PracticeManager;
import com.andrei1058.bedwars.practice.PracticeSession;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import com.andrei1058.bedwars.arena.Arena;
import com.andrei1058.bedwars.arena.LastHit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.*;

import static com.andrei1058.bedwars.BedWars.config;
import static com.andrei1058.bedwars.BedWars.getAPI;

public class FireballListener implements Listener, com.andrei1058.bedwars.configuration.ReloadableConfiguration {

    private double fireballExplosionSize;
    private boolean fireballMakeFire;
    private double fireballHorizontal;
    private double fireballVertical;
    private double knockbackTeammates;
    private double knockbackEnemy;

    private double damageSelf;
    private double damageEnemy;
    private double damageTeammates;

    public FireballListener() {
        refreshConfiguration();
    }

    @Override
    public void refreshConfiguration() {
        this.fireballExplosionSize = config.getYml().getDouble(ConfigPath.GENERAL_FIREBALL_EXPLOSION_SIZE);
        this.fireballMakeFire = config.getYml().getBoolean(ConfigPath.GENERAL_FIREBALL_MAKE_FIRE);
        this.fireballHorizontal = config.getYml().getDouble(ConfigPath.GENERAL_FIREBALL_KNOCKBACK_HORIZONTAL);
        this.fireballVertical = config.getYml().getDouble(ConfigPath.GENERAL_FIREBALL_KNOCKBACK_VERTICAL);
        this.knockbackTeammates = config.getYml().getDouble(ConfigPath.GENERAL_FIREBALL_KNOCKBACK_TEAMMATES);
        this.knockbackEnemy = config.getYml().getDouble(ConfigPath.GENERAL_FIREBALL_KNOCKBACK_ENEMY);

        this.damageSelf = config.getYml().getDouble(ConfigPath.GENERAL_FIREBALL_DAMAGE_SELF);
        this.damageEnemy = config.getYml().getDouble(ConfigPath.GENERAL_FIREBALL_DAMAGE_ENEMY);
        this.damageTeammates = config.getYml().getDouble(ConfigPath.GENERAL_FIREBALL_DAMAGE_TEAMMATES);
    }

    public static Fireball launch(Player player, double speedMultiplier, float explosionSize) {
        Fireball fireball = player.launchProjectile(Fireball.class);
        fireball = BedWars.nms.setFireballDirection(fireball, player.getEyeLocation().getDirection());
        fireball.setVelocity(fireball.getDirection().multiply(speedMultiplier));
        fireball.setYield(explosionSize);
        fireball.setMetadata("bw1058", new FixedMetadataValue(BedWars.plugin, "ceva"));
        return fireball;
    }

    private static PracticeSession practiceSession(Fireball fireball) {
        PracticeManager manager = BedWars.getPracticeManager();
        return manager == null ? null : manager.getSession(fireball);
    }

    private Vector calculateKnockback(Location playerLoc, Location explosionLoc) {
        Vector knockbackDir = new Vector(playerLoc.getX() - explosionLoc.getX(),
                playerLoc.getY() + 0.9 - explosionLoc.getY(), playerLoc.getZ() - explosionLoc.getZ());
        double distance = explosionLoc.distance(playerLoc);
        double multiplier = 1.0;
        if (distance < fireballExplosionSize) {
            double x = distance / fireballExplosionSize;
            multiplier = Math.sqrt(1 - x * x);
        }
        if (multiplier < 0.1) multiplier = 0.1;
        knockbackDir.normalize();
        double verticalFactor = knockbackDir.getY();
        verticalFactor = 4 * verticalFactor * verticalFactor * verticalFactor - 6 * verticalFactor * verticalFactor + 3 * verticalFactor;
        if (verticalFactor < 0.25) verticalFactor = 0.25;
        return knockbackDir.multiply(fireballHorizontal * multiplier)
                .setY(fireballVertical * multiplier * verticalFactor);
    }

    @EventHandler
    public void fireballHit(ProjectileHitEvent e) {
        // validate important info
        if (!(e.getEntity() instanceof Fireball)) return;
        Fireball fireball = (Fireball) e.getEntity();
        ProjectileSource projectileSource = e.getEntity().getShooter();
        if (!(projectileSource instanceof Player)) return;
        Player source = (Player) projectileSource;

        IArena arena = Arena.getArenaByPlayer(source);
        PracticeSession practice = arena == null ? practiceSession(fireball) : null;
        if (arena == null && practice == null) return;

        // calc explosion loc
        Vector velocity = fireball.getVelocity();
        Location startLoc = fireball.getLocation();
        Location explosionLoc = calculateExactExplosionLoc(startLoc, velocity, 0);

        if (practice != null) {
            if (!practice.getPoint().contains(explosionLoc)) return;
            Location playerLoc = source.getLocation();
            if (playerLoc.getWorld() != explosionLoc.getWorld()
                    || Math.abs(playerLoc.getX() - explosionLoc.getX()) > fireballExplosionSize
                    || Math.abs(playerLoc.getY() - explosionLoc.getY()) > fireballExplosionSize
                    || Math.abs(playerLoc.getZ() - explosionLoc.getZ()) > fireballExplosionSize) return;
            source.setFallDistance(0);
            source.setVelocity(calculateKnockback(playerLoc, explosionLoc));
            // 1.8 GENERIC plugin damage can bypass Bukkit's damage event.
            if (damageSelf >= source.getHealth()) {
                BedWars.getPracticeManager().reset(source);
            } else if (damageSelf > 0) {
                source.damage(damageSelf);
            }
            return;
        }

        // apply player kb
        World world = explosionLoc.getWorld();
        assert world != null;
        Collection<Entity> nearbyEntities = world.getNearbyEntities(explosionLoc, fireballExplosionSize, fireballExplosionSize, fireballExplosionSize);
        for (Entity entity : nearbyEntities) {
            if (!(entity instanceof Player)) continue;
            Player player = (Player) entity;
            if (!getAPI().getArenaUtil().isPlaying(player)) continue;

            // calc & apply player kb
            Vector finalVelocity = calculateKnockback(player.getLocation(), explosionLoc);

            // Reset fall distance
            player.setFallDistance(0);

            // Prevent teammates from overriding last hit
            boolean isTeammate = arena.getTeam(player).equals(arena.getTeam(source));
            boolean isSelf = player.equals(source);

            if (isSelf) {
                player.setVelocity(finalVelocity);
            } else if (isTeammate) {
                if (knockbackTeammates > 0) {
                    player.setVelocity(finalVelocity.multiply(knockbackTeammates));
                }
            } else {
                if (knockbackEnemy > 0) {
                    player.setVelocity(finalVelocity.multiply(knockbackEnemy));
                }
            }

            // debug
            // String debugMsg = String.format("[FireballDebug]: %s | BoomLoc: %.2f, %.2f, %.2f | PlayerLoc: %.2f, %.2f, %.2f | KB_Vel: %.2f, %.2f, %.2f", player.getName(), explosionLoc.getX(), explosionLoc.getY(), explosionLoc.getZ(), player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), finalVelocity.getX(), finalVelocity.getY(), finalVelocity.getZ());
            // System.out.println(debugMsg);

            // Prevent teammates from overriding last hit
            if (!isTeammate || isSelf) {
                LastHit lh = LastHit.getLastHit(player);
                if (lh != null) {
                    lh.setDamager(source);
                    lh.setTime(System.currentTimeMillis());
                } else {
                    new LastHit(player, source, System.currentTimeMillis());
                }
            }

            if (player.equals(source)) {
                if (damageSelf > 0) {
                    player.damage(damageSelf); // damage shooter
                }
            } else if (arena.getTeam(player).equals(arena.getTeam(source))) {
                if (damageTeammates > 0) {
                    player.damage(damageTeammates); // damage teammates
                }
            } else {
                if (damageEnemy > 0) {
                    player.damage(damageEnemy); // damage enemies
                }
            }
        }
    }

    // AI 写的，好复杂的算法我没太认真看，反正测试了能用

    private Location calculateExactExplosionLoc(Location startLoc, Vector velocity, double offset) {
        if (velocity.length() < 0.0001) return startLoc; // 速度为0防报错

        // 1. 先找到撞到了哪个方块 (使用之前的粗测法，只需确定 Block 坐标)
        // 这一步只是为了拿到 targetBlock，不需要精确坐标
        org.bukkit.block.Block targetBlock = null;
        Vector dirNorm = velocity.clone().normalize();
        Location trace = startLoc.clone();
        double checkDist = velocity.length() * 1.5;

        // 如果起点就在方块里，直接向上推一点返回
        if (trace.getBlock().getType().isSolid()) return trace.add(0, 0.5, 0);

        for (double d = 0; d <= checkDist; d += 0.1) {
            trace.add(dirNorm.clone().multiply(0.1));
            if (trace.getBlock().getType().isSolid()) {
                targetBlock = trace.getBlock();
                break;
            }
        }

        // 如果没撞到方块，就返回理论飞行终点
        if (targetBlock == null) return startLoc.clone().add(velocity);

        // 2. 核心算法：计算进入 X, Y, Z 三个平面的时间 t
        // 射线公式: P = Origin + Direction * t
        // 我们要求 t = (PlanePosition - Origin) / Direction

        double blockMinX = targetBlock.getX();
        double blockMaxX = blockMinX + 1.0;
        double blockMinY = targetBlock.getY();
        double blockMaxY = blockMinY + 1.0;
        double blockMinZ = targetBlock.getZ();
        double blockMaxZ = blockMinZ + 1.0;

        double dirX = velocity.getX();
        double dirY = velocity.getY();
        double dirZ = velocity.getZ();
        double startX = startLoc.getX();
        double startY = startLoc.getY();
        double startZ = startLoc.getZ();

        // 计算 X 轴碰撞时间 (tx)
        // 如果速度 X > 0，说明它是从左往右撞，只会撞到 MinX 面
        // 如果速度 X < 0，说明它是从右往左撞，只会撞到 MaxX 面
        double tx = -1;
        boolean hitXFaceIsMax = false; // 标记是撞到 x=1 还是 x=0
        if (dirX > 0) {
            tx = (blockMinX - startX) / dirX;
            hitXFaceIsMax = false;
        } else if (dirX < 0) {
            tx = (blockMaxX - startX) / dirX;
            hitXFaceIsMax = true;
        }

        // 计算 Y 轴碰撞时间 (ty)
        double ty = -1;
        boolean hitYFaceIsMax = false;
        if (dirY > 0) {
            ty = (blockMinY - startY) / dirY;
            hitYFaceIsMax = false;
        } else if (dirY < 0) {
            ty = (blockMaxY - startY) / dirY;
            hitYFaceIsMax = true;
        }

        // 计算 Z 轴碰撞时间 (tz)
        double tz = -1;
        boolean hitZFaceIsMax = false;
        if (dirZ > 0) {
            tz = (blockMinZ - startZ) / dirZ;
            hitZFaceIsMax = false;
        } else if (dirZ < 0) {
            tz = (blockMaxZ - startZ) / dirZ;
            hitZFaceIsMax = true;
        }

        // 3. 比较 tx, ty, tz，最大的那个就是“最后进入方块”的轴
        // 也就是说，射线在进入另外两个轴的范围时，还没碰到这个轴的范围。
        // 当三个轴都进入范围时，才是真正的碰撞。所以最大值即为碰撞点。
        // (注意：这里我们假设 t 必须 >= 0，因为是向前飞)

        double maxT = tx;
        int hitAxis = 0; // 0=X, 1=Y, 2=Z

        if (ty > maxT) {
            maxT = ty;
            hitAxis = 1;
        }
        if (tz > maxT) {
            maxT = tz;
            hitAxis = 2;
        }

        // 4. 根据结果构建坐标
        // 算出精确撞击坐标：P = Start + Velocity * t
        Location hitLoc = startLoc.clone().add(velocity.clone().multiply(maxT));

        // 强制修正：根据撞击的轴，把该轴的坐标强制锁定到 方块表面 + offset
        // 其他两个轴的坐标保持算出来的精确值
        if (hitAxis == 0) { // Hit X
            if (hitXFaceIsMax) hitLoc.setX(blockMaxX + offset);
            else hitLoc.setX(blockMinX - offset);
        } else if (hitAxis == 1) { // Hit Y
            if (hitYFaceIsMax) hitLoc.setY(blockMaxY + offset);
            else hitLoc.setY(blockMinY - offset);
        } else { // Hit Z
            if (hitZFaceIsMax) hitLoc.setZ(blockMaxZ + offset);
            else hitLoc.setZ(blockMinZ - offset);
        }

        return hitLoc;
    }

    /**
     * Remove the damage effect caused by the explosion created by the fireball
     *
     */
    @EventHandler
    public void fireballExplosionHit(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Fireball)) return;
        if (!(e.getEntity() instanceof Player)) return;

        if (practiceSession((Fireball) e.getDamager()) != null) {
            e.setCancelled(true);
            return;
        }

        if (Arena.getArenaByPlayer((Player) e.getEntity()) == null) return;

        e.setCancelled(true);
    }

    @EventHandler
    public void fireballPrime(ExplosionPrimeEvent e) {
        if (!(e.getEntity() instanceof Fireball)) return;
        if (practiceSession((Fireball) e.getEntity()) != null) {
            e.setFire(false);
            return;
        }
        ProjectileSource shooter = ((Fireball) e.getEntity()).getShooter();
        if (!(shooter instanceof Player)) return;
        Player player = (Player) shooter;

        if (Arena.getArenaByPlayer(player) == null) return;

        e.setFire(fireballMakeFire);
    }

}