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

package com.andrei1058.bedwars.shop.main;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.shop.IBuyItem;
import com.andrei1058.bedwars.api.arena.shop.ICategoryContent;
import com.andrei1058.bedwars.api.arena.shop.IContentTier;
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import com.andrei1058.bedwars.api.arena.team.TeamEnchant;
import com.andrei1058.bedwars.api.events.shop.ShopBuyEvent;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.arena.Arena;
import com.andrei1058.bedwars.configuration.Sounds;
import com.andrei1058.bedwars.shop.ShopCache;
import com.andrei1058.bedwars.shop.quickbuy.PlayerQuickBuyCache;
import com.andrei1058.bedwars.shop.quickbuy.QuickBuyElement;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.andrei1058.bedwars.BedWars.nms;
import static com.andrei1058.bedwars.api.language.Language.getMsg;

@SuppressWarnings("WeakerAccess")
public class CategoryContent implements ICategoryContent {

    private int slot;
    private boolean loaded = false;
    private List<IContentTier> contentTiers = new ArrayList<>();
    private String contentName;
    private String itemNamePath, itemLorePath;
    private String identifier;
    private boolean permanent = false, downgradable = false, unbreakable = false;
    private byte weight = 0;
    private ShopCategory father;

    /**
     * Load a new category
     */
    public CategoryContent(String path, String name, String categoryName, YamlConfiguration yml, ShopCategory father) {
        BedWars.debug("Loading CategoryContent " + path);
        this.contentName = name;
        this.father = father;

        if (path == null || name == null || categoryName == null || yml == null) return;

        if (yml.get(path + "." + ConfigPath.SHOP_CATEGORY_CONTENT_CONTENT_SLOT) == null) {
            BedWars.plugin.getLogger().severe("Content slot not set at " + path);
            return;
        }

        if (yml.get(path + "." + ConfigPath.SHOP_CATEGORY_CONTENT_CONTENT_TIERS) == null) {
            BedWars.plugin.getLogger().severe("No tiers set for " + path);
            return;
        }

        if (yml.getConfigurationSection(path + "." + ConfigPath.SHOP_CATEGORY_CONTENT_CONTENT_TIERS).getKeys(false).isEmpty()) {
            BedWars.plugin.getLogger().severe("No tiers set for " + path);
            return;
        }

        if (yml.get(path + "." + ConfigPath.SHOP_CATEGORY_CONTENT_CONTENT_TIERS + ".tier1") == null) {
            BedWars.plugin.getLogger().severe("tier1 not found for " + path);
            return;
        }

        if (yml.get(path + "." + ConfigPath.SHOP_CATEGORY_CONTENT_IS_PERMANENT) != null) {
            permanent = yml.getBoolean(path + "." + ConfigPath.SHOP_CATEGORY_CONTENT_IS_PERMANENT);
        }

        if (yml.get(path + "." + ConfigPath.SHOP_CATEGORY_CONTENT_IS_DOWNGRADABLE) != null) {
            downgradable = yml.getBoolean(path + "." + ConfigPath.SHOP_CATEGORY_CONTENT_IS_DOWNGRADABLE);
        }

        if (yml.get(path + "." + ConfigPath.SHOP_CATEGORY_CONTENT_IS_UNBREAKABLE) != null) {
            unbreakable = yml.getBoolean(path + "." + ConfigPath.SHOP_CATEGORY_CONTENT_IS_UNBREAKABLE);
        }

        if (yml.get(path + "." + ConfigPath.SHOP_CATEGORY_CONTENT_WEIGHT) != null) {
            weight = (byte) yml.getInt(path + "." + ConfigPath.SHOP_CATEGORY_CONTENT_WEIGHT);
        }

        this.slot = yml.getInt(path + "." + ConfigPath.SHOP_CATEGORY_CONTENT_CONTENT_SLOT);

        ContentTier ctt;
        for (String s : yml.getConfigurationSection(path + "." + ConfigPath.SHOP_CATEGORY_CONTENT_CONTENT_TIERS).getKeys(false)) {
            ctt = new ContentTier(path + "." + ConfigPath.SHOP_CATEGORY_CONTENT_CONTENT_TIERS + "." + s, s, path, yml);
            if (ctt.isLoaded()) contentTiers.add(ctt);
        }
        if (contentTiers.isEmpty()) return;

        itemNamePath = Messages.SHOP_CONTENT_TIER_ITEM_NAME.replace("%category%", categoryName).replace("%content%", contentName);
        for (Language language : Language.getLanguages()) {
            language.ensureLocalizedName(itemNamePath);
        }
        itemLorePath = Messages.SHOP_CONTENT_TIER_ITEM_LORE.replace("%category%", categoryName).replace("%content%", contentName);
        for (Language language : Language.getLanguages()) {
            language.ensureLocalizedLore(itemLorePath);
        }

        identifier = path;

        loaded = true;

    }

    /**
     * Check if player has enough inventory space
     * 检查玩家是否有足够的背包空间
     *
     * @param player   The player buying the item / 购买物品的玩家
     * @param items    List of items to be bought / 要购买的物品列表
     * @param currency The currency used for payment / 支付使用的货币类型
     * @param price    The total price / 总价格
     * @return true if there is enough space, false otherwise / 如果空间足够返回 true，否则返回 false
     */
    private boolean hasInventorySpace(Player player, List<IBuyItem> items, Material currency, int price) {
        // Use an array to simulate inventory instead of creating a full Inventory object to save memory
        // 使用数组来模拟背包，代替创建完整的 Inventory 对象以节省内存
        ItemStack[] simulatedInv = new ItemStack[36];
        
        // Copy player storage contents to the simulated array
        // 将玩家当前的背包内容复制到模拟数组中
        for (int i = 0; i < 36; i++) {
            ItemStack item = player.getInventory().getItem(i);
            // Clone item to avoid modifying original inventory during simulation
            // 克隆物品以避免在模拟过程中修改原始背包
            simulatedInv[i] = (item != null && item.getType() != Material.AIR) ? item.clone() : null;
        }

        // Simulate taking money
        // 模拟扣除货币的过程
        int cost = price;
        for (int i = 0; i < 36; i++) {
            ItemStack item = simulatedInv[i];
            if (item == null || item.getType() != currency) continue;

            if (item.getAmount() <= cost) {
                cost -= item.getAmount();
                simulatedInv[i] = null;
            } else {
                item.setAmount(item.getAmount() - cost);
                cost = 0;
            }
            if (cost <= 0) break;
        }

        // Check space for each item to be bought
        // 检查每一个要购买的物品是否有空间放下
        IArena arena = Arena.getArenaByPlayer(player);
        for (IBuyItem buyItem : items) {
            ItemStack itemToAdd = buyItem.getItemStack().clone();

            // Skip auto-equip armor
            // 跳过自动装备的护甲
            if (buyItem.isAutoEquip() && BedWars.nms.isArmor(itemToAdd)) {
                continue;
            }

            // Simulate item modifications (Team color, enchants, etc.) to match actual given item
            // 模拟物品修改（队伍颜色、附魔等），以匹配实际给予的物品
            if (arena != null && arena.getTeam(player) != null) {
                itemToAdd = BedWars.nms.colourItem(itemToAdd, arena.getTeam(player));
                ItemMeta im = itemToAdd.getItemMeta();
                if (im != null) {
                    if (buyItem.isPermanent()) BedWars.nms.setUnbreakable(im);
                    if (buyItem.isUnbreakable()) BedWars.nms.setUnbreakable(im);

                    if (itemToAdd.getType() == Material.BOW) {
                        for (TeamEnchant e : arena.getTeam(player).getBowsEnchantments()) {
                            im.addEnchant(e.getEnchantment(), e.getAmplifier(), true);
                        }
                    } else if (BedWars.nms.isSword(itemToAdd) || BedWars.nms.isAxe(itemToAdd)) {
                        for (TeamEnchant e : arena.getTeam(player).getSwordsEnchantments()) {
                            im.addEnchant(e.getEnchantment(), e.getAmplifier(), true);
                        }
                    }
                    itemToAdd.setItemMeta(im);
                }

                if (buyItem.isPermanent()) {
                    itemToAdd = BedWars.nms.setShopUpgradeIdentifier(itemToAdd, buyItem.getUpgradeIdentifier());
                }
            }

            // Handle sword upgrades
            // 处理剑类升级逻辑
            if (BedWars.nms.isSword(itemToAdd)) {
                for (int i = 0; i < 36; i++) {
                    ItemStack itm = simulatedInv[i];
                    if (itm == null || !BedWars.nms.isSword(itm)) continue;

                    if (BedWars.nms.isCustomBedWarsItem(itm) && BedWars.nms.getCustomData(itm).equals("DEFAULT_ITEM")) {
                        if (BedWars.nms.getDamage(itm) <= BedWars.nms.getDamage(itemToAdd)) {
                            simulatedInv[i] = null; // Remove old sword / 移除旧剑
                        }
                    }
                }
            }

            int amountNeeded = itemToAdd.getAmount();
            int maxStack = itemToAdd.getMaxStackSize();

            // Pass 1: Try to stack into existing items
            // 第一步：尝试堆叠到现有的同类物品中
            for (int i = 0; i < 36; i++) {
                if (amountNeeded <= 0) break;
                ItemStack current = simulatedInv[i];
                
                // Check if item is similar and has space in stack
                // 检查物品是否相似且堆叠未满
                if (current != null && current.isSimilar(itemToAdd) && current.getAmount() < maxStack) {
                    int space = maxStack - current.getAmount();
                    int toAdd = Math.min(space, amountNeeded);
                    
                    current.setAmount(current.getAmount() + toAdd);
                    amountNeeded -= toAdd;
                }
            }

            // Pass 2: Place remaining items into empty slots
            // 第二步：将剩余物品放入空位
            if (amountNeeded > 0) {
                for (int i = 0; i < 36; i++) {
                    if (amountNeeded <= 0) break;
                    
                    if (simulatedInv[i] == null) {
                        ItemStack newItem = itemToAdd.clone();
                        newItem.setAmount(Math.min(amountNeeded, maxStack));
                        simulatedInv[i] = newItem;
                        amountNeeded -= newItem.getAmount();
                    }
                }
            }

            // If there are still items left, it means no space
            // 如果仍有剩余物品，说明空间不足
            if (amountNeeded > 0) {
                return false;
            }
        }
        
        return true;
    }

    public void execute(Player player, ShopCache shopCache, int slot) {

        IContentTier ct;

        //check weight
        if (shopCache.getCategoryWeight(father) > weight) return;

        if (shopCache.getContentTier(getIdentifier()) > contentTiers.size()) {
            Bukkit.getLogger().severe("Wrong tier order at: " + getIdentifier());
            return;
        }

        //check if can re-buy
        if (shopCache.getContentTier(getIdentifier()) == contentTiers.size()) {
            if (isPermanent() && shopCache.hasCachedItem(this)) {
                player.sendMessage(getMsg(player, Messages.SHOP_ALREADY_BOUGHT));
                Sounds.playSound(ConfigPath.SOUNDS_INSUFF_MONEY, player);
                return;
            }
            //current tier
            ct = contentTiers.get(shopCache.getContentTier(getIdentifier()) - 1);
        } else {
            if (!shopCache.hasCachedItem(this)) {
                ct = contentTiers.get(0);
            } else {
                ct = contentTiers.get(shopCache.getContentTier(getIdentifier()));
            }
        }

        //check money
        int money = calculateMoney(player, ct.getCurrency());
        if (money < ct.getPrice()) {
            player.sendMessage(getMsg(player, Messages.SHOP_INSUFFICIENT_MONEY).replace("{currency}", getMsg(player, getCurrencyMsgPath(ct))).
                    replace("{amount}", String.valueOf(ct.getPrice() - money)));
            Sounds.playSound(ConfigPath.SOUNDS_INSUFF_MONEY, player);
            return;
        }

        if (!hasInventorySpace(player, ct.getBuyItemsList(), ct.getCurrency(), ct.getPrice())) {
            player.sendMessage(getMsg(player, Messages.SHOP_INSUFFICIENT_INVENTORY_SPACE));
            Sounds.playSound(ConfigPath.SOUNDS_INSUFF_INVENTORY, player);
            return;
        }

        ShopBuyEvent event;
        //call shop buy event
        Bukkit.getPluginManager().callEvent(event = new ShopBuyEvent(player, Arena.getArenaByPlayer(player), this));

        if (event.isCancelled()){
            return;
        }

        //take money
        takeMoney(player, ct.getCurrency(), ct.getPrice());

        //upgrade if possible
        shopCache.upgradeCachedItem(this, slot);


        //give items
        giveItems(player, shopCache, Arena.getArenaByPlayer(player));

        //play sound
        Sounds.playSound(ConfigPath.SOUNDS_BOUGHT, player);

        //send purchase msg
        if (itemNamePath == null || Language.getPlayerLanguage(player).getYml().get(itemNamePath) == null) {
            ItemStack displayItem = ct.getItemStack();
            if (displayItem.getItemMeta() != null && displayItem.getItemMeta().hasDisplayName()) {
                player.sendMessage(getMsg(player, Messages.SHOP_NEW_PURCHASE).replace("{item}", displayItem.getItemMeta().getDisplayName()));
            }
        } else {
            player.sendMessage(getMsg(player, Messages.SHOP_NEW_PURCHASE).replace("{item}", ChatColor.stripColor(getMsg(player, itemNamePath))).replace("{color}", "").replace("{tier}", ""));
        }


        shopCache.setCategoryWeight(father, weight);
    }

    /**
     * Add tier items to player inventory
     */
    public void giveItems(Player player, ShopCache shopCache, IArena arena) {
        for (IBuyItem bi : contentTiers.get(shopCache.getContentTier(getIdentifier()) - 1).getBuyItemsList()) {
            bi.give(player, arena);
        }
    }

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public ItemStack getItemStack(Player player) {
        ShopCache sc = ShopCache.getShopCache(player.getUniqueId());
        return sc == null ? null : getItemStack(player, sc);
    }
    private String replacePotionPlaceholders(String text, IContentTier tier) {
        if (!text.contains("{duration}") && !text.contains("{level}")) return text;
        if (tier.getBuyItemsList().isEmpty()) return text;
        ItemStack potion = tier.getBuyItemsList().get(0).getItemStack();
        if (potion == null || !(potion.getItemMeta() instanceof PotionMeta)) return text;
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        if (meta.getCustomEffects().isEmpty()) return text;
        org.bukkit.potion.PotionEffect effect = meta.getCustomEffects().get(0);
        return text.replace("{duration}", String.valueOf(effect.getDuration() / 20))
                .replace("{level}", getRomanNumber(effect.getAmplifier() + 1));
    }


    @Override
    public boolean hasQuick(Player player) {
        PlayerQuickBuyCache pqbc = PlayerQuickBuyCache.getQuickBuyCache(player.getUniqueId());
        return pqbc != null && hasQuick(pqbc);
    }

    public ItemStack getItemStack(Player player, ShopCache shopCache) {
        IContentTier ct;
        if (shopCache.getContentTier(identifier) == contentTiers.size()) {
            ct = contentTiers.get(contentTiers.size() - 1);
        } else {
            if (shopCache.hasCachedItem(this)) {
                ct = contentTiers.get(shopCache.getContentTier(identifier));
            } else {
                ct = contentTiers.get(shopCache.getContentTier(identifier) - 1);
            }
        }

        ItemStack i = ct.getItemStack();
        ItemMeta im = i.getItemMeta();

        if (im != null) {
            im = i.getItemMeta().clone();
            boolean canAfford = calculateMoney(player, ct.getCurrency()) >= ct.getPrice();
            PlayerQuickBuyCache qbc = PlayerQuickBuyCache.getQuickBuyCache(player.getUniqueId());
            boolean hasQuick = qbc != null && hasQuick(qbc);

            String color = getMsg(player, canAfford ? Messages.SHOP_CAN_BUY_COLOR : Messages.SHOP_CANT_BUY_COLOR);
            String translatedCurrency = getMsg(player, getCurrencyMsgPath(ct));
            ChatColor cColor = getCurrencyColor(ct.getCurrency());

            int tierI = ct.getValue();
            String tier = getRomanNumber(tierI);
            String buyStatus;

            if (isPermanent() && shopCache.hasCachedItem(this) && shopCache.getCachedItem(this).getTier() == getContentTiers().size()) {
                if (!(nms.isArmor(i))) {
                    buyStatus = getMsg(player, Messages.SHOP_LORE_STATUS_MAXED);
                } else {
                    buyStatus = getMsg(player, Messages.SHOP_LORE_STATUS_ARMOR);
                }
            } else if (!canAfford) {
                buyStatus = getMsg(player, Messages.SHOP_LORE_STATUS_CANT_AFFORD).replace("{currency}", translatedCurrency);
            } else {
                buyStatus = getMsg(player, Messages.SHOP_LORE_STATUS_CAN_BUY);
            }

            im.setDisplayName(replacePotionPlaceholders(getMsg(player, itemNamePath), ct)
                    .replace("{color}", color).replace("{tier}", tier));

            List<String> lore = new ArrayList<>();
            for (String s : Language.getList(player, itemLorePath)) {
                if (s.contains("{quick_buy}")) {
                    if (hasQuick) {
                        if (ShopIndex.getIndexViewers().contains(player.getUniqueId())) {
                            s = getMsg(player, Messages.SHOP_LORE_QUICK_REMOVE);
                        } else {
                            continue;
                        }
                    } else {
                        s = getMsg(player, Messages.SHOP_LORE_QUICK_ADD);
                    }
                }
                s = replacePotionPlaceholders(s, ct).replace("{tier}", tier).replace("{color}", color)
                        .replace("{cost}", cColor + String.valueOf(ct.getPrice()))
                        .replace("{currency}", cColor + translatedCurrency).replace("{buy_status}", buyStatus);
                lore.add(s);
            }

            im.setLore(lore);
            i.setItemMeta(im);
        }
        return i;
    }

    public boolean hasQuick(PlayerQuickBuyCache c) {
        for (QuickBuyElement q : c.getElements()) {
            if (q.getCategoryContent() == this) return true;
        }
        return false;
    }

    /**
     * Get player's money amount
     */
    public static int calculateMoney(Player player, Material currency) {
        int amount = 0;
        for (ItemStack is : player.getInventory().getContents()) {
            if (is == null) continue;
            if (is.getType() == currency) amount += is.getAmount();
        }
        return amount;
    }

    /**
     * Get currency as material
     */
    public static Material getCurrency(String currency) {
        Material material;
        switch (currency) {
            case "iron":
                material = Material.IRON_INGOT;
                break;
            case "gold":
                material = Material.GOLD_INGOT;
                break;
            case "diamond":
                material = Material.DIAMOND;
                break;
            case "emerald":
                material = Material.EMERALD;
                break;
            default:
                throw new IllegalArgumentException("Unsupported currency: " + currency);
        }
        return material;
    }

    public static ChatColor getCurrencyColor(Material currency) {
        ChatColor c = ChatColor.DARK_GREEN;
        if (currency.toString().toLowerCase().contains("diamond")) {
            c = ChatColor.AQUA;
        } else if (currency.toString().toLowerCase().contains("gold")) {
            c = ChatColor.GOLD;
        } else if (currency.toString().toLowerCase().contains("iron")) {
            c = ChatColor.WHITE;
        }
        return c;
    }

    /**
     * Cet currency path
     */
    public static String getCurrencyMsgPath(IContentTier contentTier) {
        String c;

        if (contentTier.getCurrency().toString().toLowerCase().contains("iron")) {
            c = contentTier.getPrice() == 1 ? Messages.MEANING_IRON_SINGULAR : Messages.MEANING_IRON_PLURAL;
        } else if (contentTier.getCurrency().toString().toLowerCase().contains("gold")) {
            c = contentTier.getPrice() == 1 ? Messages.MEANING_GOLD_SINGULAR : Messages.MEANING_GOLD_PLURAL;
        } else if (contentTier.getCurrency().toString().toLowerCase().contains("emerald")) {
            c = contentTier.getPrice() == 1 ? Messages.MEANING_EMERALD_SINGULAR : Messages.MEANING_EMERALD_PLURAL;
        } else if (contentTier.getCurrency().toString().toLowerCase().contains("diamond")) {
            c = contentTier.getPrice() == 1 ? Messages.MEANING_DIAMOND_SINGULAR : Messages.MEANING_DIAMOND_PLURAL;
        } else {
            throw new IllegalArgumentException("Unsupported currency: " + contentTier.getCurrency());
        }
        return c;
    }

    /**
     * Get the roman number for an integer
     */
    public static String getRomanNumber(int n) {
        String s;
        switch (n) {
            default:
                s = String.valueOf(n);
                break;
            case 1:
                s = "I";
                break;
            case 2:
                s = "II";
                break;
            case 3:
                s = "III";
                break;
            case 4:
                s = "IV";
                break;
            case 5:
                s = "V";
                break;
            case 6:
                s = "VI";
                break;
            case 7:
                s = "VII";
                break;
            case 8:
                s = "VIII";
                break;
            case 9:
                s = "IX";
                break;
            case 10:
                s = "X";
                break;
        }
        return s;
    }


    /**
     * Take money from player on buy
     */
    public static void takeMoney(Player player, Material currency, int amount) {
        int cost = amount;
        for (ItemStack i : player.getInventory().getContents()) {
            if (i == null) continue;
            if (i.getType() == currency) {
                if (i.getAmount() < cost) {
                    cost -= i.getAmount();
                    nms.minusAmount(player, i, i.getAmount());
                    player.updateInventory();
                } else {
                    nms.minusAmount(player, i, cost);
                    player.updateInventory();
                    break;
                }
            }
        }

    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    /**
     * Check if category content was loaded
     */
    public boolean isLoaded() {
        return loaded;
    }

    public boolean isPermanent() {
        return permanent;
    }

    public boolean isDowngradable() {
        return downgradable;
    }

    public String getIdentifier() {
        return identifier;
    }

    public List<IContentTier> getContentTiers() {
        return contentTiers;
    }
}
