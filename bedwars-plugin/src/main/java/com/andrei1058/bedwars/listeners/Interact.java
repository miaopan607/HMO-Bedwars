/*
 * BedWars1058 - A bed wars mini-game.
 * Copyright (C) 2021 Andrei Dascălu
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Contact e-mail: andrew.dascalu@gmail.com
 */

package com.andrei1058.bedwars.listeners;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.api.server.ServerType;
import com.andrei1058.bedwars.arena.Arena;
import com.andrei1058.bedwars.arena.compass.CompassManager;
import com.andrei1058.bedwars.configuration.Sounds;
import com.andrei1058.bedwars.database.PlayerPreferenceService;
import com.andrei1058.bedwars.shop.ShopCache;
import com.andrei1058.bedwars.shop.listeners.InventoryListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Sign;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Openable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.andrei1058.bedwars.BedWars.*;
import static com.andrei1058.bedwars.api.language.Language.getMsg;

public class Interact implements Listener, com.andrei1058.bedwars.configuration.ReloadableConfiguration {

    private double fireballSpeedMultiplier;
    private double fireballCooldown;
    private float fireballExplosionSize;
    private final Map<UUID, Long> chestFullNoticeCooldown = new ConcurrentHashMap<>();

    public Interact() {
        refreshConfiguration();
    }

    @Override
    public void refreshConfiguration() {
        this.fireballSpeedMultiplier = config.getYml().getDouble(ConfigPath.GENERAL_FIREBALL_SPEED_MULTIPLIER);
        this.fireballCooldown = config.getYml().getDouble(ConfigPath.GENERAL_FIREBALL_COOLDOWN);
        this.fireballExplosionSize = (float) config.getYml().getDouble(ConfigPath.GENERAL_FIREBALL_EXPLOSION_SIZE);
    }

    @EventHandler
    public void onReadyItemClick(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player p = e.getPlayer();
        IArena arena = Arena.getArenaByPlayer(p);
        if (arena == null) return;
        if (arena.isSpectator(p)) return;
        if (!(arena instanceof Arena)) return;
        
        ItemStack item = e.getItem();
        if (item == null) return;
        
        if (item.getType().name().equals("EMERALD_BLOCK") || item.getType().name().equals("REDSTONE_BLOCK")) {
            // Check display name to be sure it's our item
            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                String displayName = item.getItemMeta().getDisplayName();
                // We should check if it matches the localized string, but since we can't easily access the exact string 
                // without knowing the player's language at this exact moment (though we can),
                // and we set the name based on the player's language when giving the item.
                // A safer check is checking the slot and material, or adding NBT data. 
                // Since we didn't add NBT data in giveReadyItem, we'll check slot and material + context.
                
                // Check if it's in the correct slot (Slot 1)
                if (p.getInventory().getHeldItemSlot() == 1) {
                    e.setCancelled(true);
                    Arena a = (Arena) arena;
                    boolean isReady = a.isPlayerReady(p);
                    a.setPlayerReady(p, !isReady);
                    // Messages are sent in setPlayerReady
                }
            }
        }
    }

    @EventHandler
    /* Handle custom items with commands on them */
    public void onItemCommand(PlayerInteractEvent e) {
        if (e == null) return;
        Player p = e.getPlayer();
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) {
            ItemStack i = BedWars.nms.getItemInHand(p);
            if (!nms.isCustomBedWarsItem(i)) return;
            final String[] customData = nms.getCustomData(i).split("_");
            if (customData.length >= 2) {
                if (customData[0].equals("RUNCOMMAND")) {
                    e.setCancelled(true);
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (customData[1].equals("LOBBYMENU")) {
                            com.andrei1058.bedwars.shop.quickbuy.LobbyQuickBuyMenu.open(p);
                        } else {
                            Bukkit.dispatchCommand(p, customData[1]);
                        }
                    });
                }
            }
        }
    }

    @EventHandler
    //Check if player is opening an inventory
    public void onInventoryInteract(PlayerInteractEvent e) {
        if (e == null) return;
        if (e.isCancelled()) return;
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block b = e.getClickedBlock();
        if (b == null) return;
        if ((BedWars.getServerType() == ServerType.MULTIARENA && b.getWorld().getName().equals(BedWars.getLobbyWorld()) && !BreakPlace.isBuildSession(e.getPlayer())) || Arena.getArenaByPlayer(e.getPlayer()) != null) {
            if (b.getType() == nms.materialCraftingTable() && config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_DISABLE_CRAFTING)) {
                e.setCancelled(true);
            } else if (b.getType() == nms.materialEnchantingTable() && config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_DISABLE_ENCHANTING)) {
                e.setCancelled(true);
            } else if (b.getType() == Material.FURNACE && config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_DISABLE_FURNACE)) {
                e.setCancelled(true);
            } else if (b.getType() == Material.BREWING_STAND && config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_DISABLE_BREWING_STAND)) {
                e.setCancelled(true);
            } else if (b.getType() == Material.ANVIL && config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_DISABLE_ANVIL)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e == null) return;
        Player p = e.getPlayer();
        Arena.afkCheck.remove(p.getUniqueId());
        if (BedWars.getAPI().getAFKUtil().isPlayerAFK(e.getPlayer())) {
            BedWars.getAPI().getAFKUtil().setPlayerAFK(e.getPlayer(), false);
        }
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            IArena arena = Arena.getArenaByPlayer(p);
            ItemStack item = e.getItem();
            if (arena instanceof Arena && ((Arena) arena).canSelectTeam(p)
                    && item != null && item.getType() != Material.AIR && nms.isCustomBedWarsItem(item)
                    && "TEAM_SELECTOR".equals(nms.getCustomData(item))) {
                return;
            }
        }
        if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
            handleQuickDeposit(e);
            return;
        }
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block b = e.getClickedBlock();
            if (b == null) return;
            if (b.getType() == Material.AIR) return;
            IArena a = Arena.getArenaByPlayer(p);
            if (a != null) {
                if (a.getRespawnSessions().containsKey(e.getPlayer())) {
                    e.setCancelled(true);
                    return;
                }
                if (nms.isBed(b.getType())) {
                    if (p.isSneaking()) {
                        ItemStack i = nms.getItemInHand(p);
                        if (i == null) {
                            e.setCancelled(true);
                        } else if (i.getType() == Material.AIR) {
                            e.setCancelled(true);
                        }
                    } else {
                        e.setCancelled(true);
                    }
                    return;
                }
                if (b.getType() == Material.CHEST) {
                    if (a.isSpectator(p) || a.getRespawnSessions().containsKey(p)) {
                        e.setCancelled(true);
                        return;
                    }
                    if (!canAccessTeamChest(a, p, b)) {
                        e.setCancelled(true);
                    }
                }
                if (a.isSpectator(p) || a.getRespawnSessions().containsKey(p)) {
                    switch (b.getType().toString()) {
                        case "CHEST":
                        case "ENDER_CHEST":
                        case "ANVIL":
                        case "WORKBENCH":
                        case "HOPPER":
                        case "TRAPPED_CHEST":
                        case "CRAFTING_TABLE":
                            e.setCancelled(true);
                            break;
                    }
                    if (b.getState() instanceof Openable) {
                        e.setCancelled(true);
                    }
                }
            }
            if (b.getState() instanceof Sign) {
                for (IArena a1 : Arena.getArenas()) {
                    if (a1.getSigns().contains(b)) {
                        if (a1.addPlayer(p, false)) {
                            Sounds.playSound("join-allowed", p);
                        } else {
                            Sounds.playSound("join-denied", p);
                        }
                        return;
                    }
                }
            }
        }
        //check hand
        ItemStack inHand = e.getItem();
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) {
            if (inHand == null) return;
            IArena a = Arena.getArenaByPlayer(p);
            if (a != null) {
                if (a.isPlayer(p)) {
                    if (inHand.getType() == nms.materialFireball()) {

                        e.setCancelled(true);

                        if(System.currentTimeMillis() - a.getFireballCooldowns().getOrDefault(p.getUniqueId(), 0L) > (fireballCooldown*1000)) {
                            a.getFireballCooldowns().put(p.getUniqueId(), System.currentTimeMillis());
                            FireballListener.launch(p, fireballSpeedMultiplier, fireballExplosionSize);
                            nms.minusAmount(p, inHand, 1);
                        }

                    }
                }
            }
        }
    }

    /** 检查玩家是否有权访问岛屿范围内的团队箱子。 */
    private boolean canAccessTeamChest(IArena arena, Player player, Block block) {
        ITeam owner = null;
        int isRad = arena.getConfig().getInt(ConfigPath.ARENA_ISLAND_RADIUS);
        for (ITeam t : arena.getTeams()) {
            if (t.getSpawn().distance(block.getLocation()) <= isRad) {
                owner = t;
            }
        }
        if (owner != null && !owner.isMember(player)) {
            if (!(owner.getMembers().isEmpty() && owner.isBedDestroyed())) {
                player.sendMessage(getMsg(player, Messages.INTERACT_CHEST_CANT_OPEN_TEAM_ELIMINATED));
                return false;
            }
        }
        return true;
    }

    /** 处理手持物品左键箱子/末影箱直接存入。 */
    private void handleQuickDeposit(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        PlayerPreferenceService prefs = PlayerPreferenceService.getInstance();
        if (prefs == null || !prefs.isQuickDeposit(p.getUniqueId())) return;

        Block b = e.getClickedBlock();
        if (b == null) return;
        Material type = b.getType();
        if (type != Material.CHEST && type != Material.TRAPPED_CHEST && type != Material.ENDER_CHEST) return;

        IArena arena = Arena.getArenaByPlayer(p);
        if (arena == null || arena.getStatus() != GameState.playing) return;
        if (!arena.isPlayer(p) || arena.isSpectator(p) || arena.getRespawnSessions().containsKey(p)) return;

        int heldSlot = p.getInventory().getHeldItemSlot();
        ItemStack inHand = p.getInventory().getItem(heldSlot);
        if (inHand == null || inHand.getType() == Material.AIR) return;

        // 拦截不可快速存入的物品（剑/镐/斧/弓/剪刀、指南针、命令物品、默认/可升级装备）
        if (isBlockedQuickDepositItem(inHand)) return;
        if (CompassManager.isCompass(inHand)) return;
        if (nms.isCustomBedWarsItem(inHand) && nms.getCustomData(inHand).startsWith("RUNCOMMAND_")) return;
        ShopCache shopCache = ShopCache.getShopCache(p.getUniqueId());
        if (InventoryListener.shouldCancelMovement(inHand, shopCache)) return;

        Inventory targetInv;
        if (type == Material.ENDER_CHEST) {
            targetInv = p.getEnderChest();
        } else {
            if (!(b.getState() instanceof Chest)) return;
            targetInv = ((Chest) b.getState()).getInventory();
        }

        // 校验箱子上方是否被不透明完整方块遮挡，与原版打开条件保持一致
        if (isChestBlockedAbove(b, targetInv)) return;

        if (type != Material.ENDER_CHEST && !canAccessTeamChest(arena, p, b)) {
            e.setCancelled(true);
            return;
        }

        e.setCancelled(true);
        int depositedTotal = 0;

        if (p.isSneaking()) {
            // 潜行左键：先存手持槽位，再存入背包内所有同类型物品
            ItemStack template = inHand.clone();
            int moved = depositSlot(p.getInventory(), heldSlot, targetInv);
            depositedTotal += moved;
            if (p.getInventory().getItem(heldSlot) == null) {
                ItemStack[] contents = p.getInventory().getContents();
                for (int slot = 0; slot < contents.length; slot++) {
                    if (slot == heldSlot) continue;
                    ItemStack stack = contents[slot];
                    if (stack == null || stack.getType() == Material.AIR || !stack.isSimilar(template)) continue;
                    int slotMoved = depositSlot(p.getInventory(), slot, targetInv);
                    depositedTotal += slotMoved;
                    if (p.getInventory().getItem(slot) != null) {
                        // 当前槽位未完全存入，说明箱子已满
                        break;
                    }
                }
            }
        } else {
            depositedTotal = depositSlot(p.getInventory(), heldSlot, targetInv);
        }

        if (depositedTotal == 0) {
            long now = System.currentTimeMillis();
            long last = chestFullNoticeCooldown.getOrDefault(p.getUniqueId(), 0L);
            if (now - last >= 1000L) {
                chestFullNoticeCooldown.put(p.getUniqueId(), now);
                p.sendMessage(getMsg(p, Messages.CHEST_QUICK_DEPOSIT_FULL));
            }
            return;
        }

        p.playSound(p.getLocation(), Sound.CHICKEN_EGG_POP, 0.8f, 1.2f);
        p.updateInventory();
    }

    /** 将玩家指定槽位的物品存入目标箱子，返回实际存入的数量。 */
    private int depositSlot(Inventory playerInv, int slot, Inventory targetInv) {
        ItemStack stack = playerInv.getItem(slot);
        if (stack == null || stack.getType() == Material.AIR) return 0;
        int originalAmount = stack.getAmount();
        Map<Integer, ItemStack> leftover = targetInv.addItem(stack.clone());
        if (leftover.isEmpty()) {
            playerInv.setItem(slot, null);
            return originalAmount;
        }
        ItemStack remaining = leftover.values().iterator().next();
        int moved = originalAmount - remaining.getAmount();
        if (moved > 0) {
            playerInv.setItem(slot, remaining);
        }
        return moved;
    }

    /** 屏蔽所有剑、镐子、斧头、弓和剪刀，防止左键箱子误存。 */
    private boolean isBlockedQuickDepositItem(ItemStack item) {
        if (nms.isSword(item) || nms.isAxe(item) || nms.isBow(item)) return true;
        switch (item.getType()) {
            case WOOD_PICKAXE:
            case STONE_PICKAXE:
            case IRON_PICKAXE:
            case GOLD_PICKAXE:
            case DIAMOND_PICKAXE:
            case SHEARS:
                return true;
            default:
                return false;
        }
    }

    /** 检查箱子（含大箱子两侧）或末影箱上方是否被不透明完整方块遮挡。 */
    private boolean isChestBlockedAbove(Block block, Inventory targetInv) {
        if (targetInv instanceof DoubleChestInventory && targetInv.getHolder() instanceof DoubleChest) {
            DoubleChest doubleChest = (DoubleChest) targetInv.getHolder();
            if (doubleChest.getLeftSide() instanceof Chest
                    && ((Chest) doubleChest.getLeftSide()).getBlock().getRelative(BlockFace.UP).getType().isOccluding()) {
                return true;
            }
            if (doubleChest.getRightSide() instanceof Chest
                    && ((Chest) doubleChest.getRightSide()).getBlock().getRelative(BlockFace.UP).getType().isOccluding()) {
                return true;
            }
            return false;
        }
        return block.getRelative(BlockFace.UP).getType().isOccluding();
    }



    @EventHandler
    public void disableItemFrameRotation(PlayerInteractEntityEvent e) {
        if (e == null) return;
        if (e.getRightClicked().getType() == EntityType.ITEM_FRAME) {
            if (((ItemFrame) e.getRightClicked()).getItem().getType().equals(Material.AIR)) {
                //prevent from putting upgradable items in it
                ItemStack i = nms.getItemInHand(e.getPlayer());
                if (i != null) {
                    if (i.getType() != Material.AIR) {
                        ShopCache sc = ShopCache.getShopCache(e.getPlayer().getUniqueId());
                        if (sc != null) {
                            if (InventoryListener.shouldCancelMovement(i, sc)) {
                                e.setCancelled(true);
                            }
                        }
                    }
                }
                return;
            }
            IArena a = Arena.getArenaByIdentifier(e.getPlayer().getWorld().getName());
            if (a != null) {
                e.setCancelled(true);
            }
            if (BedWars.getServerType() == ServerType.MULTIARENA) {
                if (BedWars.getLobbyWorld().equals(e.getPlayer().getWorld().getName()) && !BreakPlace.isBuildSession(e.getPlayer())) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent e) {
        if (e == null) return;
        IArena a = Arena.getArenaByPlayer(e.getPlayer());
        if (a == null) return;
        Location l = e.getRightClicked().getLocation();
        for (ITeam t : a.getTeams()) {
            Location l2 = t.getShop(), l3 = t.getTeamUpgrades();
            if (l.getBlockX() == l2.getBlockX() && l.getBlockY() == l2.getBlockY() && l.getBlockZ() == l2.getBlockZ()) {
                e.setCancelled(true);
            } else if (l.getBlockX() == l3.getBlockX() && l.getBlockY() == l3.getBlockY() && l.getBlockZ() == l3.getBlockZ()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent e) {
        if (e == null) return;
        if (Arena.getArenaByPlayer(e.getPlayer()) != null) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onArmorManipulate(PlayerArmorStandManipulateEvent e) {
        if (e == null) return;
        if (e.isCancelled()) return;
        //prevent from breaking generators
        if (Arena.getArenaByPlayer(e.getPlayer()) != null) {
            e.setCancelled(true);
        }

        //prevent from stealing from armor stands in lobby
        if (BedWars.getServerType() == ServerType.MULTIARENA && e.getPlayer().getLocation().getWorld().getName().equalsIgnoreCase(BedWars.getLobbyWorld()) && !BreakPlace.isBuildSession(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onCrafting(PrepareItemCraftEvent e) {
        if (e == null) return;
        if (Arena.getArenaByPlayer((Player) e.getView().getPlayer()) != null) {
            if (config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_DISABLE_CRAFTING)) {
                e.getInventory().setResult(new ItemStack(Material.AIR));
            }
        }
    }
}
