package com.andrei1058.bedwars.shop.hotbar;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.arena.compass.CompassManager;
import com.andrei1058.bedwars.shop.ShopManager;
import com.andrei1058.bedwars.shop.main.CategoryContent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * 快捷栏自动分配的物品类别定义，将工具独立拆分为镐、斧与剪刀。
 */
public enum HotbarCategory {
    BLOCKS("BLOCKS", 10, Material.STAINED_CLAY, (short) 1, Messages.HOTBAR_CATEGORY_BLOCKS),
    SWORD("SWORD", 11, Material.GOLD_SWORD, (short) 0, Messages.HOTBAR_CATEGORY_SWORD),
    PICKAXE("PICKAXE", 12, Material.IRON_PICKAXE, (short) 0, Messages.HOTBAR_CATEGORY_PICKAXE),
    AXE("AXE", 13, Material.IRON_AXE, (short) 0, Messages.HOTBAR_CATEGORY_AXE),
    SHEARS("SHEARS", 14, Material.SHEARS, (short) 0, Messages.HOTBAR_CATEGORY_SHEARS),
    BOW("BOW", 15, Material.BOW, (short) 0, Messages.HOTBAR_CATEGORY_BOW),
    UTILITY("UTILITY", 16, Material.TNT, (short) 0, Messages.HOTBAR_CATEGORY_UTILITY);

    private final String key;
    private final int menuSlot;
    private final Material iconMaterial;
    private final short iconData;
    private final String namePath;

    HotbarCategory(String key, int menuSlot, Material iconMaterial, short iconData, String namePath) {
        this.key = key;
        this.menuSlot = menuSlot;
        this.iconMaterial = iconMaterial;
        this.iconData = iconData;
        this.namePath = namePath;
    }

    public String getKey() {
        return key;
    }

    public int getMenuSlot() {
        return menuSlot;
    }

    public String getDisplayName(Player player) {
        return Language.getMsg(player, namePath);
    }

    public ItemStack createIcon() {
        ItemStack stack = new ItemStack(iconMaterial, 1, iconData);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            stack.setItemMeta(ShopManager.hideItemStuff(meta));
        }
        return stack;
    }

    public static HotbarCategory fromKey(String key) {
        if (key == null || key.trim().isEmpty()) return null;
        for (HotbarCategory category : values()) {
            if (category.key.equalsIgnoreCase(key.trim())) return category;
        }
        return null;
    }

    public static HotbarCategory fromMenuSlot(int slot) {
        for (HotbarCategory category : values()) {
            if (category.menuSlot == slot) return category;
        }
        return null;
    }

    /** 识别任意物品对应的快捷栏类别；资源、护甲、箭、指南针等返回 null。 */
    public static HotbarCategory resolve(ItemStack item) {
        return resolve(item, null);
    }

    /** 结合商品分类与物品材质识别快捷栏类别。 */
    public static HotbarCategory resolve(ItemStack item, CategoryContent content) {
        if (item == null || item.getType() == Material.AIR) return null;
        Material type = item.getType();
        if (isResource(type) || type == Material.ARROW || CompassManager.isCompass(item)) return null;
        if (BedWars.nms.isArmor(item)) return null;

        if (BedWars.nms.isSword(item)) return SWORD;
        if (isPickaxe(type)) return PICKAXE;
        if (BedWars.nms.isAxe(item) || isAxe(type)) return AXE;
        if (type == Material.SHEARS) return SHEARS;
        if (BedWars.nms.isBow(item)) return BOW;
        if (isUtilityMaterial(type)) return UTILITY;

        if (content != null && content.getIdentifier() != null) {
            String id = content.getIdentifier();
            if (id.startsWith(ConfigPath.SHOP_PATH_CATEGORY_BLOCKS + ".")) return BLOCKS;
            if (id.startsWith(ConfigPath.SHOP_PATH_CATEGORY_UTILITY + ".")
                    || id.startsWith(ConfigPath.SHOP_PATH_CATEGORY_POTIONS + ".")) {
                return UTILITY;
            }
        }

        if (type.isBlock()) return BLOCKS;
        return null;
    }

    public static boolean isResource(Material type) {
        return type == Material.IRON_INGOT
                || type == Material.GOLD_INGOT
                || type == Material.DIAMOND
                || type == Material.EMERALD;
    }

    private static boolean isPickaxe(Material type) {
        switch (type) {
            case WOOD_PICKAXE:
            case STONE_PICKAXE:
            case IRON_PICKAXE:
            case GOLD_PICKAXE:
            case DIAMOND_PICKAXE:
                return true;
            default:
                return false;
        }
    }

    private static boolean isAxe(Material type) {
        switch (type) {
            case WOOD_AXE:
            case STONE_AXE:
            case IRON_AXE:
            case GOLD_AXE:
            case DIAMOND_AXE:
                return true;
            default:
                return false;
        }
    }

    private static boolean isUtilityMaterial(Material type) {
        switch (type) {
            case GOLDEN_APPLE:
            case SNOW_BALL:
            case MONSTER_EGG:
            case FIREBALL:
            case TNT:
            case ENDER_PEARL:
            case WATER_BUCKET:
            case EGG:
            case MILK_BUCKET:
            case SPONGE:
            case CHEST:
            case POTION:
            case STICK:
                return true;
            default:
                return false;
        }
    }
}
