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
import com.andrei1058.bedwars.api.events.shop.ShopBuyEvent;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.arena.Arena;
import com.andrei1058.bedwars.configuration.Sounds;
import com.andrei1058.bedwars.shop.ShopCache;
import com.andrei1058.bedwars.shop.ShopClickMode;
import com.andrei1058.bedwars.database.PlayerPreferenceService;
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

    private List<ItemStack> prepareItems(Player player, List<IBuyItem> items) {
        List<ItemStack> prepared = new ArrayList<>(items.size());
        IArena arena = Arena.getArenaByPlayer(player);
        for (IBuyItem item : items) {
            ItemStack stack = item.getItemStack();
            if (stack == null || (item.isAutoEquip() && nms.isArmor(stack))) continue;
            prepared.add(item instanceof BuyItem ? ((BuyItem) item).prepareItem(player, arena) : stack);
        }
        return prepared;
    }

    private ItemStack[] purchaseContents(Player player, List<ItemStack> items) {
        ItemStack[] contents = new ItemStack[36];
        for (int slot = 0; slot < contents.length; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            contents[slot] = stack == null || stack.getType() == Material.AIR ? null : stack;
        }
        for (ItemStack item : items) {
            if (!nms.isSword(item)) continue;
            for (int slot = 0; slot < contents.length; slot++) {
                ItemStack stack = contents[slot];
                if (stack != null && nms.isSword(stack) && nms.isCustomBedWarsItem(stack)
                        && nms.getCustomData(stack).equals("DEFAULT_ITEM") && nms.getDamage(stack) <= nms.getDamage(item)) {
                    contents[slot] = null;
                }
            }
        }
        return contents;
    }

    /** 只计算数量，不修改真实物品；付款腾出的空位也参与整批容量计算。 */
    private static int maxPurchases(ItemStack[] contents, List<ItemStack> items, Material currency, int price, int limit) {
        for (ItemStack item : items) {
            if (item.getAmount() > 0) limit = Math.min(limit, contents.length * item.getMaxStackSize() / item.getAmount());
        }
        ItemStack[] stacks = new ItemStack[contents.length];
        int[] amounts = new int[contents.length];
        // 付款清空货币槽时容量会跃升，不能按单份容量或单调二分推算。
        for (int purchases = limit; purchases > 0; purchases--) {
            System.arraycopy(contents, 0, stacks, 0, contents.length);
            for (int slot = 0; slot < stacks.length; slot++) {
                amounts[slot] = stacks[slot] == null ? 0 : stacks[slot].getAmount();
            }
            int cost = price * purchases;
            for (int slot = 0; slot < stacks.length && cost > 0; slot++) {
                if (stacks[slot] == null || stacks[slot].getType() != currency) continue;
                int paid = Math.min(cost, amounts[slot]);
                cost -= paid;
                amounts[slot] -= paid;
                if (amounts[slot] == 0) stacks[slot] = null;
            }
            if (cost > 0) continue;
            boolean fits = true;
            for (ItemStack item : items) {
                int remaining = item.getAmount() * purchases;
                int maxStack = item.getMaxStackSize();
                for (int slot = 0; slot < stacks.length && remaining > 0; slot++) {
                    if (stacks[slot] == null || !stacks[slot].isSimilar(item)) continue;
                    int added = Math.min(remaining, Math.max(0, maxStack - amounts[slot]));
                    amounts[slot] += added;
                    remaining -= added;
                }
                for (int slot = 0; slot < stacks.length && remaining > 0; slot++) {
                    if (stacks[slot] != null) continue;
                    stacks[slot] = item;
                    amounts[slot] = Math.min(remaining, maxStack);
                    remaining -= amounts[slot];
                }
                if (remaining > 0) {
                    fits = false;
                    break;
                }
            }
            if (fits) return purchases;
        }
        return 0;
    }

    public void execute(Player player, ShopCache shopCache, int slot) {
        execute(player, shopCache, slot, false);
    }

    public void executeBulk(Player player, ShopCache shopCache, int slot) {
        execute(player, shopCache, slot, true);
    }

    /** 单买和整批购买共用校验与结算，只有购买上限和成功提示不同。 */
    private void execute(Player player, ShopCache shopCache, int slot, boolean bulk) {
        IContentTier tier = resolveTier(shopCache);
        if (tier == null || shopCache.getCategoryWeight(father) > weight) return;
        if (isPermanent() && shopCache.hasCachedItem(this)
                && shopCache.getContentTier(getIdentifier()) == contentTiers.size()) {
            player.sendMessage(getMsg(player, Messages.SHOP_ALREADY_BOUGHT));
            Sounds.playSound(ConfigPath.SOUNDS_INSUFF_MONEY, player);
            return;
        }
        int price = tier.getPrice();
        Material currency = tier.getCurrency();
        int money = calculateMoney(player, currency);
        if (money < price) {
            player.sendMessage(getMsg(player, Messages.SHOP_INSUFFICIENT_MONEY)
                    .replace("{currency}", getMsg(player, getCurrencyMsgPath(tier)))
                    .replace("{amount}", String.valueOf(price - money)));
            Sounds.playSound(ConfigPath.SOUNDS_INSUFF_MONEY, player);
            return;
        }
        boolean batchItems = supportsBulkPurchase(tier);
        int limit = bulk && batchItems && price > 0 ? money / price : 1;
        List<ItemStack> items = prepareItems(player, tier.getBuyItemsList());
        int amount = maxPurchases(purchaseContents(player, items), items, currency, price, limit);
        if (amount == 0) {
            player.sendMessage(getMsg(player, Messages.SHOP_INSUFFICIENT_INVENTORY_SPACE));
            Sounds.playSound(ConfigPath.SOUNDS_INSUFF_INVENTORY, player);
            return;
        }
        IArena arena = Arena.getArenaByPlayer(player);
        ShopBuyEvent event = new ShopBuyEvent(player, arena, this);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        takeMoney(player, currency, price * amount);
        shopCache.upgradeCachedItem(this, slot);
        if (batchItems) {
            for (int index = 0; index < tier.getBuyItemsList().size(); index++) {
                ((BuyItem) tier.getBuyItemsList().get(index)).give(player, arena, items.get(index), amount);
            }
            player.updateInventory();
        } else {
            giveItems(player, shopCache, arena);
        }
        shopCache.setCategoryWeight(father, weight);
        sendPurchase(player, tier, bulk ? Messages.SHOP_BULK_PURCHASE : Messages.SHOP_NEW_PURCHASE, amount);
    }

    private IContentTier resolveTier(ShopCache shopCache) {
        int current = shopCache.getContentTier(getIdentifier());
        if (current < 1 || current > contentTiers.size()) {
            Bukkit.getLogger().severe("Wrong tier order at: " + getIdentifier());
            return null;
        }
        if (current == contentTiers.size()) return contentTiers.get(current - 1);
        return contentTiers.get(shopCache.hasCachedItem(this) ? current : 0);
    }

    private boolean supportsBulkPurchase(IContentTier tier) {
        if (contentTiers.size() != 1 || isPermanent() || isDowngradable() || tier.getBuyItemsList().isEmpty()) return false;
        for (IBuyItem item : tier.getBuyItemsList()) {
            ItemStack stack = item.getItemStack();
            if (!(item instanceof BuyItem) || stack == null || stack.getType() == Material.AIR || stack.getAmount() <= 0
                    || item.isPermanent() || (item.isAutoEquip() && nms.isArmor(stack))) return false;
        }
        return true;
    }

    private String getPurchaseName(Player player, IContentTier tier) {
        if (itemNamePath == null || Language.getPlayerLanguage(player).getYml().get(itemNamePath) == null) {
            ItemMeta meta = tier.getItemStack().getItemMeta();
            return meta != null && meta.hasDisplayName() ? meta.getDisplayName() : null;
        }
        return ChatColor.stripColor(replacePotionPlaceholders(getMsg(player, itemNamePath), tier))
                .replace("{color}", "").replace("{tier}", "");
    }

    private void sendPurchase(Player player, IContentTier tier, String message, int amount) {
        Sounds.playSound(ConfigPath.SOUNDS_BOUGHT, player);
        String name = getPurchaseName(player, tier);
        if (name != null) {
            player.sendMessage(getMsg(player, message).replace("{item}", name).replace("{amount}", String.valueOf(amount)));
        }
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

    /** 展示基础档商品，只提供编辑提示，不读取购买状态。 */
    public ItemStack getEditorItemStack(Player player, boolean selected) {
        IContentTier tier = contentTiers.get(0);
        ItemStack item = tier.getItemStack().clone();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(replacePotionPlaceholders(getMsg(player, itemNamePath), tier)
                    .replace("{color}", ChatColor.YELLOW.toString())
                    .replace("{tier}", getRomanNumber(tier.getValue())));
            meta.setLore(java.util.Collections.singletonList(getMsg(player,
                    selected ? Messages.QUICK_BUY_EDITOR_ADDED : Messages.QUICK_BUY_EDITOR_ADD)));
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack getItemStack(Player player, ShopCache shopCache) {
        IContentTier ct = resolveTier(shopCache);
        if (ct == null) return null;

        ItemStack i = ct.getItemStack();
        ItemMeta im = i.getItemMeta();

        if (im != null) {
            im = i.getItemMeta().clone();
            boolean canAfford = calculateMoney(player, ct.getCurrency()) >= ct.getPrice();
            PlayerQuickBuyCache qbc = PlayerQuickBuyCache.getQuickBuyCache(player.getUniqueId());
            boolean hasQuick = qbc != null && hasQuick(qbc);
            ShopClickMode mode = ShopClickMode.from(PlayerPreferenceService.getInstance().isShiftBulkBuy(player.getUniqueId()));

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
                buyStatus = getMsg(player, mode.canBuyMessage());
            }

            im.setDisplayName(replacePotionPlaceholders(getMsg(player, itemNamePath), ct)
                    .replace("{color}", color).replace("{tier}", tier));

            List<String> lore = new ArrayList<>();
            for (String s : Language.getList(player, itemLorePath)) {
                if (s.contains("{quick_buy}")) {
                    if (hasQuick) {
                        if (ShopIndex.getIndexViewers().contains(player.getUniqueId())) {
                            s = getMsg(player, mode.quickRemoveMessage());
                        } else {
                            continue;
                        }
                    } else {
                        s = getMsg(player, mode.quickAddMessage());
                    }
                }
                s = replacePotionPlaceholders(s, ct).replace("{tier}", tier).replace("{color}", color)
                        .replace("{cost}", cColor + String.valueOf(ct.getPrice()))
                        .replace("{currency}", cColor + translatedCurrency).replace("{buy_status}", buyStatus);
                lore.add(s);
            }

            if (mode == ShopClickMode.SHIFT_BULK) {
                lore.add(getMsg(player, supportsBulkPurchase(ct) ? Messages.SHOP_BULK_HINT : Messages.SHOP_BULK_SINGLE_HINT));
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
