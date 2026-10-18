package com.andrei1058.bedwars.practice;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.configuration.ReloadableConfiguration;
import com.andrei1058.bedwars.listeners.FireballListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import java.io.IOException;

/** Practice owns only its worlds, sessions and current-round projectiles. */
public final class PracticeListener implements Listener, ReloadableConfiguration {
    private final PracticeManager manager;
    private final BukkitTask ticker;
    private double speed, cooldown;
    private float explosion;
    private int ticks;

    public PracticeListener(PracticeManager manager) {
        this.manager = manager;
        refreshConfiguration();
        ticker = Bukkit.getScheduler().runTaskTimer(BedWars.plugin, this::tick, 1, 1);
    }
    @Override public void refreshConfiguration() {
        speed = BedWars.config.getYml().getDouble(ConfigPath.GENERAL_FIREBALL_SPEED_MULTIPLIER);
        cooldown = BedWars.config.getYml().getDouble(ConfigPath.GENERAL_FIREBALL_COOLDOWN) * 1000;
        explosion = (float) BedWars.config.getYml().getDouble(ConfigPath.GENERAL_FIREBALL_EXPLOSION_SIZE);
    }
    public void shutdown() { ticker.cancel(); }
    private void later(PracticeSession session, Runnable action) {
        long round = session.getRound();
        Bukkit.getScheduler().runTask(BedWars.plugin, () -> {
            if (manager.getSession(session.getPlayer()) == session && session.getRound() == round && session.getPlayer().isOnline()) action.run();
        });
    }
    private void tick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PracticeSession session = manager.getSession(player);
            if (session == null) continue;
            Location location = session.sampleLocation();
            if (!session.getPoint().containsPlayer(location)) {
                manager.reset(player, location.getY() < session.getPoint().getFailY()
                        ? Messages.PRACTICE_FAIL_FALL : Messages.PRACTICE_FAIL_BOUNDS);
                continue;
            }
            manager.sample(session);
            session.pruneProjectiles();
        }
        if (++ticks % 10 == 0) manager.refreshUi();
    }
    public static boolean control(ItemStack item) {
        if (item == null || !BedWars.nms.isCustomBedWarsItem(item)) return false;
        String value = BedWars.nms.getCustomData(item);
        return "PRACTICE_RETRY".equals(value) || "PRACTICE_LEAVE".equals(value) || "PRACTICE_MENU".equals(value);
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void interact(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PracticeSession session = manager.getSession(player);
        boolean right = event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK;
        if (session == null) {
            if (manager.isPracticeWorld(player.getWorld()) && !manager.canBuild(player, player.getLocation()) && event.getClickedBlock() != null) event.setCancelled(true);
            return;
        }
        ItemStack item = event.getItem();
        if (right && control(item)) {
            event.setCancelled(true);
            String marker = BedWars.nms.getCustomData(item);
            later(session, () -> {
                if ("PRACTICE_RETRY".equals(marker)) manager.reset(player);
                else if ("PRACTICE_MENU".equals(marker)) manager.openMenu(player);
                else manager.leave(player);
            });
            return;
        }
        if (right && item != null && item.getType() == Material.FIREBALL && session.getPoint().getMode() == PracticeMode.FIREBALL) {
            boolean denied = event.useItemInHand() == Event.Result.DENY;
            // Air interactions can start cancelled because vanilla has no fireball use action.
            event.setCancelled(true);
            if (denied) return;
            long now = System.currentTimeMillis();
            if (now - session.getLastFireballTime() <= cooldown) return;
            Fireball fireball = FireballListener.launch(player, speed, explosion);
            if (fireball.isDead()) return;
            session.trackProjectile(fireball); session.setLastFireballTime(now);
            later(session, () -> manager.replenish(player));
            return;
        }
        if (event.getClickedBlock() != null && right) {
            if (item != null && item.getType() == Material.WOOL) {
                Block candidate = event.getClickedBlock().getRelative(event.getBlockFace());
                if (!session.getPoint().contains(candidate.getLocation()) || candidate.getType() != Material.AIR
                        || BedWars.nms.isBlockPlacementIntersectingPlayer(player, candidate)) event.setUseItemInHand(Event.Result.DENY);
            } else if (item == null || item.getType() != Material.ENDER_PEARL) event.setCancelled(true);
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void place(BlockPlaceEvent event) {
        if (!manager.isPracticeWorld(event.getBlock().getWorld())) return;
        PracticeSession session = manager.getSession(event.getPlayer());
        if (session == null) { if (!manager.canBuild(event.getPlayer(), event.getBlock().getLocation())) event.setCancelled(true); return; }
        if (event instanceof BlockMultiPlaceEvent || event.getBlock().getType() != Material.WOOL
                || event.getBlockReplacedState().getType() != Material.AIR || !session.getPoint().contains(event.getBlock().getLocation())) event.setCancelled(true);
        if (!event.isCancelled()) {
            try { session.recordBlock(event.getBlockReplacedState()); }
            catch (IOException ex) {
                event.setCancelled(true);
                PracticeManager.message(event.getPlayer(), Messages.PRACTICE_SAVE_FAILED, null, ex.getMessage());
            }
        }
    }
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void recordPlacement(BlockPlaceEvent event) {
        PracticeSession session = manager.getSession(event.getPlayer());
        if (session == null) return;
        manager.placed(session); later(session, () -> manager.replenish(event.getPlayer()));
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void breakBlock(BlockBreakEvent event) {
        if (!manager.isPracticeWorld(event.getBlock().getWorld())) return;
        PracticeSession session = manager.getSession(event.getPlayer());
        if (session == null && manager.canBuild(event.getPlayer(), event.getBlock().getLocation())) return;
        event.setCancelled(true);
        if (session != null) session.restoreBlock(event.getBlock());
    }
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void launch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof EnderPearl) || !(event.getEntity().getShooter() instanceof Player)) return;
        Player player = (Player) event.getEntity().getShooter();
        PracticeSession session = manager.getSession(player);
        if (session == null) return;
        session.trackProjectile(event.getEntity());
        later(session, () -> manager.replenish(player));
    }
    @EventHandler(priority = EventPriority.MONITOR)
    public void hit(ProjectileHitEvent event) {
        PracticeSession session = manager.getSession(event.getEntity());
        if (session != null) later(session, () -> session.removeProjectile(event.getEntity()));
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void teleport(PlayerTeleportEvent event) {
        if (manager.isInternalTeleport(event.getPlayer())) return;
        PracticeSession session = manager.getSession(event.getPlayer());
        if (session != null && !session.getPoint().containsPlayer(event.getTo())) {
            event.setCancelled(true);
            if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL)
                session.status(PracticeManager.text(session.getPlayer(), Messages.PRACTICE_PEARL_OUT_OF_BOUNDS));
            else PracticeManager.message(event.getPlayer(), Messages.PRACTICE_LEAVE_FIRST);
        } else if (session == null && manager.getOccupiedPoint(event.getTo()) != null) event.setCancelled(true);
    }
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void held(PlayerItemHeldEvent event) {
        PracticeSession session = manager.getSession(event.getPlayer());
        if (session != null) session.rememberHeldSlot(event.getNewSlot());
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void move(PlayerMoveEvent event) {
        if (event instanceof PlayerTeleportEvent) return;
        PracticeSession session = manager.getSession(event.getPlayer());
        if (session == null) {
            if (manager.getOccupiedPoint(event.getTo()) != null) event.setCancelled(true);
        } else if (!session.getPoint().containsPlayer(event.getTo())) {
            String reason = event.getTo().getY() < session.getPoint().getFailY()
                    ? Messages.PRACTICE_FAIL_FALL : Messages.PRACTICE_FAIL_BOUNDS;
            event.setTo(event.getFrom());
            later(session, () -> manager.reset(event.getPlayer(), reason));
        }
    }
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void observeMove(PlayerMoveEvent event) {
        if (event instanceof PlayerTeleportEvent) return;
        PracticeSession session = manager.getSession(event.getPlayer());
        if (session == null || session.isResetting() || !session.getPoint().containsPlayer(event.getTo())) return;
        Location from = event.getFrom(), to = event.getTo();
        if (from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ()) return;
        // 本包移动尚未应用，实际位置可保留上一包的落地瞬间。
        manager.sample(session);
    }
    @EventHandler(priority = EventPriority.LOWEST) public void quit(PlayerQuitEvent event) { manager.disconnect(event.getPlayer()); }
    @EventHandler public void world(PlayerChangedWorldEvent event) {
        if (!manager.isInternalTeleport(event.getPlayer()) && manager.isPracticing(event.getPlayer())) manager.terminateAfterWorldChange(event.getPlayer());
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void damage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        PracticeSession session = manager.getSession((Player) event.getEntity());
        if (session == null) return;
        if (event instanceof EntityDamageByEntityEvent) {
            Entity damager = ((EntityDamageByEntityEvent) event).getDamager();
            if (damager instanceof Player || damager instanceof Projectile && !(damager instanceof EnderPearl
                    && ((EnderPearl) damager).getShooter() == session.getPlayer())) {
                event.setCancelled(true);
                return;
            }
        }
        if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
            event.setCancelled(true); later(session, () -> manager.reset(session.getPlayer(), Messages.PRACTICE_FAIL_FALL));
        } else if (!session.isDamageEnabled()) {
            event.setCancelled(true); session.getPlayer().setFireTicks(0);
        } else if (event.getFinalDamage() >= session.getPlayer().getHealth()) {
            event.setCancelled(true); later(session, () -> manager.reset(session.getPlayer(), Messages.PRACTICE_FAIL_DAMAGE));
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void attack(EntityDamageByEntityEvent event) {
        Entity source = event.getDamager();
        if (source instanceof Projectile && ((Projectile) source).getShooter() instanceof Player) source = (Player) ((Projectile) source).getShooter();
        if (source instanceof Player && manager.isPracticing((Player) source)
                && !(event.getDamager() instanceof EnderPearl && source == event.getEntity())) event.setCancelled(true);
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void food(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player && manager.isPracticing((Player) event.getEntity())) event.setCancelled(true);
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void drop(PlayerDropItemEvent event) { if (manager.isPracticing(event.getPlayer()) || control(event.getItemDrop().getItemStack())) event.setCancelled(true); }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void pickup(PlayerPickupItemEvent event) { if (manager.isPracticing(event.getPlayer())) event.setCancelled(true); }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void open(InventoryOpenEvent event) {
        if (event.getPlayer() instanceof Player && manager.isPracticing((Player) event.getPlayer())
                && !(event.getInventory().getHolder() instanceof PracticeMenu.PracticeMenuHolder)) event.setCancelled(true);
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void click(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        int hotbar = event.getHotbarButton();
        boolean protectedHotbar = hotbar >= 0 && control(player.getInventory().getItem(hotbar));
        if (control(event.getCurrentItem()) || control(event.getCursor()) || protectedHotbar) {
            event.setCancelled(true);
            return;
        }
        if (manager.isPracticing(player) && (event.getClickedInventory() != player.getInventory()
                || event.getSlotType() == InventoryType.SlotType.ARMOR
                || event.isShiftClick() || event.getClick() == ClickType.DOUBLE_CLICK
                || event.getClick() == ClickType.DROP || event.getClick() == ClickType.CONTROL_DROP)) {
            event.setCancelled(true);
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void drag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (!manager.isPracticing(player)) return;
        if (control(event.getOldCursor())) { event.setCancelled(true); return; }
        for (int slot : event.getRawSlots()) {
            if (slot < event.getView().getTopInventory().getSize()
                    || control(event.getView().getItem(slot))) {
                event.setCancelled(true);
                return;
            }
        }
    }
    @EventHandler(ignoreCancelled = true) public void bucket(PlayerBucketEmptyEvent event) { if (manager.isPracticeWorld(event.getBlockClicked().getWorld())) event.setCancelled(true); }
    @EventHandler(ignoreCancelled = true) public void bucketFill(PlayerBucketFillEvent event) { if (manager.isPracticeWorld(event.getBlockClicked().getWorld())) event.setCancelled(true); }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true) public void explode(EntityExplodeEvent event) { if (manager.isPracticeWorld(event.getLocation().getWorld())) event.blockList().clear(); }
    @EventHandler(ignoreCancelled = true) public void blockExplode(BlockExplodeEvent event) { if (manager.isPracticeWorld(event.getBlock().getWorld())) event.blockList().clear(); }
    @EventHandler(ignoreCancelled = true) public void burn(BlockBurnEvent event) { if (manager.isPracticeWorld(event.getBlock().getWorld())) event.setCancelled(true); }
    @EventHandler(ignoreCancelled = true) public void spread(BlockSpreadEvent event) { if (manager.isPracticeWorld(event.getBlock().getWorld())) event.setCancelled(true); }
    @EventHandler(ignoreCancelled = true) public void ignite(BlockIgniteEvent event) { if (manager.isPracticeWorld(event.getBlock().getWorld())) event.setCancelled(true); }
    @EventHandler(ignoreCancelled = true) public void flow(BlockFromToEvent event) { if (manager.isPracticeWorld(event.getBlock().getWorld())) event.setCancelled(true); }
    @EventHandler(ignoreCancelled = true) public void entityBlock(EntityChangeBlockEvent event) { if (manager.isPracticeWorld(event.getBlock().getWorld())) event.setCancelled(true); }
}
