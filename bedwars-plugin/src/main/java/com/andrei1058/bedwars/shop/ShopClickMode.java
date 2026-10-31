package com.andrei1058.bedwars.shop;

import com.andrei1058.bedwars.api.language.Messages;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;

/** 对局商店的商品操作与对应提示。 */
public enum ShopClickMode {
    DEFAULT, SHIFT_BULK;

    public enum Action { BUY_ONE, BUY_BULK, EDIT_QUICK }

    public static ShopClickMode from(boolean enabled) {
        return enabled ? SHIFT_BULK : DEFAULT;
    }

    public Action resolve(ClickType click, InventoryAction action) {
        if (this == DEFAULT) {
            return action == InventoryAction.MOVE_TO_OTHER_INVENTORY ? Action.EDIT_QUICK : Action.BUY_ONE;
        }
        if (click == ClickType.SHIFT_LEFT || click == ClickType.SHIFT_RIGHT) return Action.BUY_BULK;
        return click == ClickType.RIGHT ? Action.EDIT_QUICK : Action.BUY_ONE;
    }

    public String quickAddMessage() {
        return this == DEFAULT ? Messages.SHOP_LORE_QUICK_ADD : Messages.SHOP_BULK_QUICK_ADD;
    }

    public String quickRemoveMessage() {
        return this == DEFAULT ? Messages.SHOP_LORE_QUICK_REMOVE : Messages.SHOP_BULK_QUICK_REMOVE;
    }

    public String emptyLoreMessage() {
        return this == DEFAULT ? Messages.SHOP_QUICK_EMPTY_LORE : Messages.SHOP_BULK_EMPTY_LORE;
    }

    public String canBuyMessage() {
        return this == DEFAULT ? Messages.SHOP_LORE_STATUS_CAN_BUY : Messages.SHOP_BULK_CAN_BUY;
    }
}
