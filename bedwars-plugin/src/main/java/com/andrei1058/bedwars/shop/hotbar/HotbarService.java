package com.andrei1058.bedwars.shop.hotbar;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.arena.team.TeamEnchant;
import com.andrei1058.bedwars.database.PlayerPreferenceService;
import com.andrei1058.bedwars.shop.main.CategoryContent;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.andrei1058.bedwars.BedWars.nms;

/**
 * 快捷栏自动分配引擎：统一处理购买自动分配、数字键指定槽位、重生分配与高级剑替换。
 */
public final class HotbarService {
    private static final int INVENTORY_SIZE = 36;
    private static final int HOTBAR_SIZE = HotbarLayout.HOTBAR_SIZE;

    private HotbarService() {
    }

    public static HotbarLayout getLayout(Player player) {
        if (player == null || PlayerPreferenceService.getInstance() == null) return HotbarLayout.EMPTY;
        return PlayerPreferenceService.getInstance().getHotbarLayout(player.getUniqueId());
    }

    /** 计算剑的等级分值（材质等级为主，锋利附魔作为同材质细粒度比较）。 */
    public static int getSwordRank(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !nms.isSword(item)) return 0;
        int base;
        switch (item.getType()) {
            case DIAMOND_SWORD:
                base = 400;
                break;
            case IRON_SWORD:
                base = 300;
                break;
            case STONE_SWORD:
                base = 200;
                break;
            case WOOD_SWORD:
            case GOLD_SWORD:
                base = 100;
                break;
            default:
                base = 50;
                break;
        }
        return base + item.getEnchantmentLevel(Enchantment.DAMAGE_ALL);
    }

    /** 计算弓的等级分值（冲击 > 力量 > 基础弓，确保绿宝石弓 > 力量弓 > 普通弓）。 */
    public static int getBowRank(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !nms.isBow(item)) return 0;
        return 100
                + item.getEnchantmentLevel(Enchantment.ARROW_KNOCKBACK) * 100
                + item.getEnchantmentLevel(Enchantment.ARROW_DAMAGE) * 10
                + item.getEnchantmentLevel(Enchantment.ARROW_FIRE) * 5
                + item.getEnchantmentLevel(Enchantment.ARROW_INFINITE) * 5;
    }

    public static boolean isDefaultBedWarsItem(ItemStack item) {
        return item != null && item.getType() != Material.AIR
                && nms.isCustomBedWarsItem(item)
                && "DEFAULT_ITEM".equalsIgnoreCase(nms.getCustomData(item));
    }

    /**
     * 按快捷栏配置或数字键指定槽位发放物品（适用于购买与重生发放）。
     *
     * @param forcedSlot   玩家按数字键指定的 0-8 槽位；-1 表示走 Hotbar Manager 自动分配
     * @param fallbackSlot 未配置对应类别时的优先回退槽位（例如工具升级前的旧槽位），-1 表示无
     */
    public static void giveItem(Player player, ItemStack template, int purchases, CategoryContent content,
                                int forcedSlot, int fallbackSlot) {
        if (player == null || template == null || template.getType() == Material.AIR || purchases <= 0) return;

        int totalAmount = template.getAmount() * purchases;
        if (totalAmount <= 0) return;

        PlayerInventory inv = player.getInventory();
        HotbarCategory category = HotbarCategory.resolve(template, content);
        HotbarLayout layout = getLayout(player);

        if (category == HotbarCategory.SWORD) {
            giveSword(inv, template, totalAmount, layout, forcedSlot);
            return;
        }
        if (category == HotbarCategory.BOW) {
            giveBow(inv, template, totalAmount, layout, forcedSlot);
            return;
        }

        int remaining = totalAmount;
        if (forcedSlot >= 0 && forcedSlot < HOTBAR_SIZE) {
            remaining = placeIntoSlot(inv, forcedSlot, template, remaining, Collections.singleton(forcedSlot));
        } else if (category != null) {
            List<Integer> targetSlots = layout.getSlotsFor(category);
            if (!targetSlots.isEmpty()) {
                Set<Integer> protectedSlots = new HashSet<>(targetSlots);
                for (int targetSlot : targetSlots) {
                    if (remaining <= 0) break;
                    ItemStack existing = inv.getItem(targetSlot);
                    if (isEmpty(existing)) {
                        remaining = putNewStack(inv, targetSlot, template, remaining);
                    } else if (existing.isSimilar(template)) {
                        remaining = mergeIntoSlot(inv, targetSlot, existing, remaining);
                    } else {
                        HotbarCategory existingCategory = HotbarCategory.resolve(existing);
                        // 目标槽存在其他类别物品或资源物品时，将其移入背包并让新物品占据该位置。
                        if (existingCategory != category && relocateToBackpack(inv, targetSlot, protectedSlots)) {
                            remaining = putNewStack(inv, targetSlot, template, remaining);
                        }
                    }
                }
            } else if (fallbackSlot >= 0 && fallbackSlot < INVENTORY_SIZE && isEmpty(inv.getItem(fallbackSlot))) {
                remaining = putNewStack(inv, fallbackSlot, template, remaining);
            }
        } else if (fallbackSlot >= 0 && fallbackSlot < INVENTORY_SIZE && isEmpty(inv.getItem(fallbackSlot))) {
            remaining = putNewStack(inv, fallbackSlot, template, remaining);
        }

        if (remaining > 0) {
            addRemaining(inv, template, remaining);
        }
    }

    /** 专门处理剑的发放：移除默认低级木剑，并在配置的剑槽或旧剑槽替换为更高级的剑。 */
    private static void giveSword(PlayerInventory inv, ItemStack swordTemplate, int count,
                                  HotbarLayout layout, int forcedSlot) {
        int newRank = getSwordRank(swordTemplate);
        int removedDefaultSlot = removeLowerOrEqualDefaultSwords(inv, newRank);

        int remaining = count;
        if (forcedSlot >= 0 && forcedSlot < HOTBAR_SIZE) {
            ItemStack single = singleCopy(swordTemplate);
            if (placeSwordAt(inv, forcedSlot, single, Collections.singleton(forcedSlot))) {
                remaining--;
            }
        } else {
            List<Integer> targetSlots = layout.getSlotsFor(HotbarCategory.SWORD);
            if (!targetSlots.isEmpty()) {
                Set<Integer> protectedSlots = new HashSet<>(targetSlots);
                int placed = placeSwordInConfiguredSlots(inv, swordTemplate, newRank, targetSlots, protectedSlots);
                if (placed >= 0) {
                    remaining--;
                }
            } else {
                // 未配置剑槽时，优先回到默认木剑原槽位或替换快捷栏中更低级的剑。
                int chosenSlot = -1;
                if (removedDefaultSlot >= 0 && removedDefaultSlot < HOTBAR_SIZE && isEmpty(inv.getItem(removedDefaultSlot))) {
                    chosenSlot = removedDefaultSlot;
                } else {
                    chosenSlot = findLowestSwordSlot(inv, 0, HOTBAR_SIZE, newRank);
                }
                if (chosenSlot >= 0 && placeSwordAt(inv, chosenSlot, singleCopy(swordTemplate), Collections.singleton(chosenSlot))) {
                    remaining--;
                }
            }
        }

        if (remaining > 0) {
            addRemaining(inv, swordTemplate, remaining);
        }
    }

    /** 处理弓的发放：在配置的弓槽或旧弓槽用更高级的弓替换低级弓，旧弓移入背包。 */
    private static void giveBow(PlayerInventory inv, ItemStack bowTemplate, int count,
                                HotbarLayout layout, int forcedSlot) {
        int newRank = getBowRank(bowTemplate);
        int remaining = count;

        if (forcedSlot >= 0 && forcedSlot < HOTBAR_SIZE) {
            if (placeBowAt(inv, forcedSlot, singleCopy(bowTemplate), Collections.singleton(forcedSlot))) {
                remaining--;
            }
        } else {
            List<Integer> targetSlots = layout.getSlotsFor(HotbarCategory.BOW);
            if (!targetSlots.isEmpty()) {
                Set<Integer> protectedSlots = new HashSet<>(targetSlots);
                if (placeBowInConfiguredSlots(inv, bowTemplate, newRank, targetSlots, protectedSlots) >= 0) {
                    remaining--;
                }
            } else {
                int lowerBowSlot = findLowestBowSlot(inv, 0, HOTBAR_SIZE, newRank);
                if (lowerBowSlot >= 0 && placeBowAt(inv, lowerBowSlot, singleCopy(bowTemplate), Collections.singleton(lowerBowSlot))) {
                    remaining--;
                }
            }
        }

        if (remaining > 0) {
            addRemaining(inv, bowTemplate, remaining);
        }
    }

    /**
     * 拾取更高级弓时将其替换到配置的弓位置（或原低级弓位置），旧弓移入背包。
     *
     * @return true 表示已接管本次拾取并完成替换
     */
    public static boolean handleBowPickup(Player player, ItemStack picked, ITeam team) {
        if (player == null || isEmpty(picked) || !nms.isBow(picked)) return false;

        ItemStack candidate = singleCopy(picked);
        applyTeamBowEnchants(candidate, team);
        int newRank = getBowRank(candidate);
        PlayerInventory inv = player.getInventory();

        boolean hasLowerBow = false;
        for (int slot = 0; slot < INVENTORY_SIZE; slot++) {
            ItemStack existing = inv.getItem(slot);
            if (!isEmpty(existing) && nms.isBow(existing) && getBowRank(existing) < newRank) {
                hasLowerBow = true;
                break;
            }
        }
        if (!hasLowerBow) return false;

        HotbarLayout layout = getLayout(player);
        List<Integer> targetSlots = layout.getSlotsFor(HotbarCategory.BOW);

        ItemStack[] backup = snapshot(inv);
        boolean placed;
        if (!targetSlots.isEmpty()) {
            Set<Integer> protectedSlots = new HashSet<>(targetSlots);
            placed = placeBowInConfiguredSlots(inv, candidate, newRank, targetSlots, protectedSlots) >= 0;
        } else {
            int chosenSlot = findLowestBowSlot(inv, 0, HOTBAR_SIZE, newRank);
            if (chosenSlot < 0) {
                chosenSlot = findLowestBowSlot(inv, HOTBAR_SIZE, INVENTORY_SIZE, newRank);
            }
            placed = chosenSlot >= 0 && placeBowAt(inv, chosenSlot, candidate, Collections.singleton(chosenSlot));
        }

        if (!placed) {
            restore(inv, backup);
            return false;
        }
        return true;
    }

    private static int placeBowInConfiguredSlots(PlayerInventory inv, ItemStack bowTemplate, int newRank,
                                                 List<Integer> targetSlots, Set<Integer> protectedSlots) {
        // 第一优先级：空槽或存放更低级弓的槽位。
        for (int slot : targetSlots) {
            ItemStack existing = inv.getItem(slot);
            if (isEmpty(existing)) {
                inv.setItem(slot, singleCopy(bowTemplate));
                return slot;
            }
            if (nms.isBow(existing) && getBowRank(existing) < newRank) {
                if (placeBowAt(inv, slot, singleCopy(bowTemplate), protectedSlots)) {
                    return slot;
                }
            }
        }
        // 第二优先级：存放非弓类别物品或资源的槽位（将其移到背包）。
        for (int slot : targetSlots) {
            ItemStack existing = inv.getItem(slot);
            if (!isEmpty(existing) && !nms.isBow(existing)) {
                if (placeBowAt(inv, slot, singleCopy(bowTemplate), protectedSlots)) {
                    return slot;
                }
            }
        }
        return -1;
    }

    private static boolean placeBowAt(PlayerInventory inv, int targetSlot, ItemStack bow, Set<Integer> protectedSlots) {
        ItemStack existing = inv.getItem(targetSlot);
        if (isEmpty(existing)) {
            inv.setItem(targetSlot, bow);
            return true;
        }
        if (!relocateToBackpack(inv, targetSlot, protectedSlots)) {
            return false;
        }
        inv.setItem(targetSlot, bow);
        return true;
    }

    private static int findLowestBowSlot(PlayerInventory inv, int startInclusive, int endExclusive, int newRank) {
        int bestSlot = -1;
        int bestRank = Integer.MAX_VALUE;
        for (int slot = startInclusive; slot < endExclusive; slot++) {
            ItemStack stack = inv.getItem(slot);
            if (isEmpty(stack) || !nms.isBow(stack)) continue;
            int rank = getBowRank(stack);
            if (rank < newRank && rank < bestRank) {
                bestRank = rank;
                bestSlot = slot;
            }
        }
        return bestSlot;
    }

    /**
     * 拾取更高级剑时将其替换到配置的剑位置（或原低级剑位置）。
     *
     * @return true 表示已接管本次拾取并完成替换
     */
    public static boolean handleSwordPickup(Player player, ItemStack picked, ITeam team) {
        if (player == null || isEmpty(picked) || !nms.isSword(picked)) return false;

        ItemStack candidate = singleCopy(picked);
        applyTeamSwordEnchants(candidate, team);
        int newRank = getSwordRank(candidate);
        PlayerInventory inv = player.getInventory();

        // 只有背包中存在更低级的剑时才触发高级剑替换，避免拾取低级/同级剑覆盖当前武器。
        boolean hasLowerSword = false;
        for (int slot = 0; slot < INVENTORY_SIZE; slot++) {
            ItemStack existing = inv.getItem(slot);
            if (!isEmpty(existing) && nms.isSword(existing) && getSwordRank(existing) < newRank) {
                hasLowerSword = true;
                break;
            }
        }
        if (!hasLowerSword) return false;

        HotbarLayout layout = getLayout(player);
        List<Integer> targetSlots = layout.getSlotsFor(HotbarCategory.SWORD);

        // 先尝试定位需要被替换的槽位，并移除默认低级木剑。
        ItemStack[] backup = snapshot(inv);
        int removedDefaultSlot = removeLowerOrEqualDefaultSwords(inv, newRank);

        boolean placed;
        if (!targetSlots.isEmpty()) {
            Set<Integer> protectedSlots = new HashSet<>(targetSlots);
            placed = placeSwordInConfiguredSlots(inv, candidate, newRank, targetSlots, protectedSlots) >= 0;
        } else {
            int chosenSlot = -1;
            if (removedDefaultSlot >= 0 && removedDefaultSlot < HOTBAR_SIZE && isEmpty(inv.getItem(removedDefaultSlot))) {
                chosenSlot = removedDefaultSlot;
            } else {
                chosenSlot = findLowestSwordSlot(inv, 0, HOTBAR_SIZE, newRank);
            }
            if (chosenSlot < 0) {
                chosenSlot = findLowestSwordSlot(inv, HOTBAR_SIZE, INVENTORY_SIZE, newRank);
            }
            placed = chosenSlot >= 0 && placeSwordAt(inv, chosenSlot, candidate, Collections.singleton(chosenSlot));
        }

        if (!placed) {
            restore(inv, backup);
            return false;
        }
        return true;
    }

    private static int placeSwordInConfiguredSlots(PlayerInventory inv, ItemStack swordTemplate, int newRank,
                                                   List<Integer> targetSlots, Set<Integer> protectedSlots) {
        // 第一优先级：空槽或存放更低级剑的槽位。
        for (int slot : targetSlots) {
            ItemStack existing = inv.getItem(slot);
            if (isEmpty(existing)) {
                inv.setItem(slot, singleCopy(swordTemplate));
                return slot;
            }
            if (nms.isSword(existing) && getSwordRank(existing) < newRank) {
                if (placeSwordAt(inv, slot, singleCopy(swordTemplate), protectedSlots)) {
                    return slot;
                }
            }
        }
        // 第二优先级：存放非剑类物品或资源的槽位（将其移到背包）。
        for (int slot : targetSlots) {
            ItemStack existing = inv.getItem(slot);
            if (!isEmpty(existing) && !nms.isSword(existing)) {
                if (placeSwordAt(inv, slot, singleCopy(swordTemplate), protectedSlots)) {
                    return slot;
                }
            }
        }
        return -1;
    }

    private static boolean placeSwordAt(PlayerInventory inv, int targetSlot, ItemStack sword, Set<Integer> protectedSlots) {
        ItemStack existing = inv.getItem(targetSlot);
        if (isEmpty(existing)) {
            inv.setItem(targetSlot, sword);
            return true;
        }
        if (isDefaultBedWarsItem(existing) && nms.isSword(existing) && getSwordRank(existing) <= getSwordRank(sword)) {
            inv.setItem(targetSlot, sword);
            return true;
        }
        if (!relocateToBackpack(inv, targetSlot, protectedSlots)) {
            return false;
        }
        inv.setItem(targetSlot, sword);
        return true;
    }

    private static int removeLowerOrEqualDefaultSwords(PlayerInventory inv, int newRank) {
        int firstHotbarSlot = -1;
        for (int slot = 0; slot < INVENTORY_SIZE; slot++) {
            ItemStack stack = inv.getItem(slot);
            if (isEmpty(stack) || !nms.isSword(stack) || !isDefaultBedWarsItem(stack)) continue;
            if (getSwordRank(stack) > newRank) continue;
            inv.setItem(slot, null);
            if (slot < HOTBAR_SIZE && firstHotbarSlot == -1) {
                firstHotbarSlot = slot;
            }
        }
        return firstHotbarSlot;
    }

    private static int findLowestSwordSlot(PlayerInventory inv, int startInclusive, int endExclusive, int newRank) {
        int bestSlot = -1;
        int bestRank = Integer.MAX_VALUE;
        for (int slot = startInclusive; slot < endExclusive; slot++) {
            ItemStack stack = inv.getItem(slot);
            if (isEmpty(stack) || !nms.isSword(stack)) continue;
            int rank = getSwordRank(stack);
            if (rank < newRank && rank < bestRank) {
                bestRank = rank;
                bestSlot = slot;
            }
        }
        return bestSlot;
    }

    /** 将物品尽可能放入指定槽位（用于数字键购买），必要时将原物品安全移至背包。 */
    private static int placeIntoSlot(PlayerInventory inv, int targetSlot, ItemStack template, int remaining,
                                     Set<Integer> protectedSlots) {
        ItemStack existing = inv.getItem(targetSlot);
        if (isEmpty(existing)) {
            return putNewStack(inv, targetSlot, template, remaining);
        }
        // 同种物品保留原槽位并尝试补满，已满组时把溢出部分交由背包承接。
        if (existing.isSimilar(template)) {
            return mergeIntoSlot(inv, targetSlot, existing, remaining);
        }
        if (relocateToBackpack(inv, targetSlot, protectedSlots)) {
            return putNewStack(inv, targetSlot, template, remaining);
        }
        return remaining;
    }

    private static int putNewStack(PlayerInventory inv, int slot, ItemStack template, int remaining) {
        int maxStack = Math.max(1, template.getMaxStackSize());
        int amount = Math.min(remaining, maxStack);
        ItemStack stack = template.clone();
        stack.setAmount(amount);
        inv.setItem(slot, stack);
        return remaining - amount;
    }

    private static int mergeIntoSlot(PlayerInventory inv, int slot, ItemStack existing, int remaining) {
        int maxStack = Math.max(1, existing.getMaxStackSize());
        int space = Math.max(0, maxStack - existing.getAmount());
        if (space <= 0) return remaining;
        int added = Math.min(remaining, space);
        ItemStack updated = existing.clone();
        updated.setAmount(existing.getAmount() + added);
        inv.setItem(slot, updated);
        return remaining - added;
    }

    /**
     * 将 sourceSlot 的原有物品移动到玩家背包其他位置；若完全放不下则回滚并返回 false。
     */
    private static boolean relocateToBackpack(PlayerInventory inv, int sourceSlot, Set<Integer> protectedSlots) {
        ItemStack original = inv.getItem(sourceSlot);
        if (isEmpty(original)) return true;

        ItemStack[] backup = snapshot(inv);
        ItemStack moving = original.clone();
        inv.setItem(sourceSlot, null);

        int remaining = moving.getAmount();
        int maxStack = Math.max(1, moving.getMaxStackSize());

        // 1. 优先合并到主背包 (9-35) 已有未满堆叠
        if (maxStack > 1) {
            remaining = mergeRange(inv, moving, remaining, HOTBAR_SIZE, INVENTORY_SIZE, sourceSlot, null, false);
        }
        // 2. 放入主背包 (9-35) 空槽
        if (remaining > 0) {
            remaining = fillEmptyRange(inv, moving, remaining, HOTBAR_SIZE, INVENTORY_SIZE, sourceSlot, null, false);
        }
        // 3. 主背包满时，尝试合并到非受保护的快捷栏槽位，再尝试任意其他快捷栏槽位
        if (remaining > 0 && maxStack > 1) {
            remaining = mergeRange(inv, moving, remaining, 0, HOTBAR_SIZE, sourceSlot, protectedSlots, true);
            if (remaining > 0) {
                remaining = mergeRange(inv, moving, remaining, 0, HOTBAR_SIZE, sourceSlot, null, false);
            }
        }
        // 4. 放入快捷栏空槽（先避开受保护槽位，最后兜底任意其他空槽）
        if (remaining > 0) {
            remaining = fillEmptyRange(inv, moving, remaining, 0, HOTBAR_SIZE, sourceSlot, protectedSlots, true);
        }
        if (remaining > 0) {
            remaining = fillEmptyRange(inv, moving, remaining, 0, HOTBAR_SIZE, sourceSlot, null, false);
        }

        if (remaining > 0) {
            restore(inv, backup);
            return false;
        }
        return true;
    }

    private static int mergeRange(PlayerInventory inv, ItemStack template, int remaining,
                                  int start, int end, int excludeSlot, Set<Integer> skipSet, boolean useSkipSet) {
        int maxStack = Math.max(1, template.getMaxStackSize());
        for (int slot = start; slot < end && remaining > 0; slot++) {
            if (slot == excludeSlot) continue;
            if (useSkipSet && skipSet != null && skipSet.contains(slot)) continue;
            ItemStack existing = inv.getItem(slot);
            if (isEmpty(existing) || !existing.isSimilar(template)) continue;
            int space = Math.max(0, maxStack - existing.getAmount());
            if (space <= 0) continue;
            int added = Math.min(remaining, space);
            ItemStack updated = existing.clone();
            updated.setAmount(existing.getAmount() + added);
            inv.setItem(slot, updated);
            remaining -= added;
        }
        return remaining;
    }

    private static int fillEmptyRange(PlayerInventory inv, ItemStack template, int remaining,
                                      int start, int end, int excludeSlot, Set<Integer> skipSet, boolean useSkipSet) {
        for (int slot = start; slot < end && remaining > 0; slot++) {
            if (slot == excludeSlot) continue;
            if (useSkipSet && skipSet != null && skipSet.contains(slot)) continue;
            if (!isEmpty(inv.getItem(slot))) continue;
            remaining = putNewStack(inv, slot, template, remaining);
        }
        return remaining;
    }

    private static void addRemaining(PlayerInventory inv, ItemStack template, int remaining) {
        int maxStack = Math.max(1, template.getMaxStackSize());
        while (remaining > 0) {
            int amount = Math.min(remaining, maxStack);
            ItemStack stack = template.clone();
            stack.setAmount(amount);
            inv.addItem(stack);
            remaining -= amount;
        }
    }

    private static void applyTeamSwordEnchants(ItemStack sword, ITeam team) {
        if (team == null || team.getSwordsEnchantments().isEmpty()) return;
        ItemMeta meta = sword.getItemMeta();
        if (meta == null) return;
        boolean changed = false;
        for (TeamEnchant enchant : team.getSwordsEnchantments()) {
            if (meta.getEnchantLevel(enchant.getEnchantment()) < enchant.getAmplifier()) {
                meta.addEnchant(enchant.getEnchantment(), enchant.getAmplifier(), true);
                changed = true;
            }
        }
        if (changed) sword.setItemMeta(meta);
    }

    private static void applyTeamBowEnchants(ItemStack bow, ITeam team) {
        if (team == null || team.getBowsEnchantments().isEmpty()) return;
        ItemMeta meta = bow.getItemMeta();
        if (meta == null) return;
        boolean changed = false;
        for (TeamEnchant enchant : team.getBowsEnchantments()) {
            if (meta.getEnchantLevel(enchant.getEnchantment()) < enchant.getAmplifier()) {
                meta.addEnchant(enchant.getEnchantment(), enchant.getAmplifier(), true);
                changed = true;
            }
        }
        if (changed) bow.setItemMeta(meta);
    }

    private static ItemStack singleCopy(ItemStack item) {
        ItemStack copy = item.clone();
        copy.setAmount(1);
        return copy;
    }

    private static boolean isEmpty(ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }

    private static ItemStack[] snapshot(PlayerInventory inv) {
        ItemStack[] copy = new ItemStack[INVENTORY_SIZE];
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            ItemStack stack = inv.getItem(i);
            copy[i] = stack == null ? null : stack.clone();
        }
        return copy;
    }

    private static void restore(PlayerInventory inv, ItemStack[] backup) {
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            inv.setItem(i, backup[i]);
        }
    }
}
