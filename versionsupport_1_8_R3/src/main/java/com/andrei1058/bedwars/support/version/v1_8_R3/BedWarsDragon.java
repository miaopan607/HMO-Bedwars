package com.andrei1058.bedwars.support.version.v1_8_R3;

import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import net.minecraft.server.v1_8_R3.EntityEnderDragon;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.MathHelper;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.ArrayList;
import java.util.List;

public class BedWarsDragon extends EntityEnderDragon {

    private final ITeam ownerTeam;
    private final IArena arena;

    public BedWarsDragon(World world, ITeam ownerTeam) {
        super(world);
        this.ownerTeam = ownerTeam;
        this.arena = ownerTeam.getArena();
    }

    public ITeam getOwnerTeam() {
        return ownerTeam;
    }

    boolean isValidTarget(Player player) {
        if (player == null || !player.isOnline() || player.isDead() || player.getHealth() <= 0
                || !player.getWorld().equals(arena.getWorld()) || !arena.isPlayer(player)
                || arena.isSpectator(player) || player.getGameMode() == GameMode.SPECTATOR
                || arena.isReSpawning(player)) return false;
        ITeam team = arena.getTeam(player);
        return team != null && !team.equals(ownerTeam);
    }

    Player findTarget() {
        Player nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Player player : arena.getPlayers()) {
            if (!isValidTarget(player)) continue;
            EntityPlayer handle = ((CraftPlayer) player).getHandle();
            double dx = handle.locX - locX, dy = handle.locY - locY, dz = handle.locZ - locZ;
            double distance = dx * dx + dy * dy + dz * dz;
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = player;
            }
        }
        return nearest;
    }

    private boolean hasValidTarget() {
        return target instanceof EntityPlayer && isValidTarget(((EntityPlayer) target).getBukkitEntity());
    }

    @Override
    public void m() {
        if (arena.getStatus() != GameState.playing) {
            getBukkitEntity().remove();
            return;
        }
        if (!hasValidTarget()) {
            target = null;
            Player candidate = findTarget();
            if (candidate != null) {
                EntityTargetEvent event = new EntityTargetEvent(getBukkitEntity(), candidate, EntityTargetEvent.TargetReason.RANDOM_TARGET);
                Bukkit.getPluginManager().callEvent(event);
                if (!event.isCancelled() && event.getTarget() instanceof Player && isValidTarget((Player) event.getTarget())) {
                    target = ((CraftPlayer) event.getTarget()).getHandle();
                }
            }
        }
        if (target != null) {
            a = target.locX;
            b = target.locY;
            c = target.locZ;
        }
        super.m();
        if (!hasValidTarget()) target = null;
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
            if (type != Material.AIR && type != Material.BEDROCK && type != Material.BARRIER && type != Material.COMMAND) {
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
