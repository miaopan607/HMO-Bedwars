package com.andrei1058.bedwars.shop.hotbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 玩家 9 格快捷栏的类别映射快照（不可变对象，修改返回新实例）。
 */
public final class HotbarLayout {
    public static final int HOTBAR_SIZE = 9;
    public static final HotbarLayout EMPTY = new HotbarLayout(new HotbarCategory[HOTBAR_SIZE]);

    private final HotbarCategory[] slots;

    private HotbarLayout(HotbarCategory[] slots) {
        this.slots = slots;
    }

    public HotbarCategory get(int slot) {
        if (slot < 0 || slot >= HOTBAR_SIZE) return null;
        return slots[slot];
    }

    /** 按从左到右顺序返回某个类别配置的所有快捷栏槽位（0-8）。 */
    public List<Integer> getSlotsFor(HotbarCategory category) {
        List<Integer> result = new ArrayList<>();
        if (category == null) return result;
        for (int i = 0; i < HOTBAR_SIZE; i++) {
            if (slots[i] == category) result.add(i);
        }
        return result;
    }

    public boolean isEmpty() {
        for (HotbarCategory category : slots) {
            if (category != null) return false;
        }
        return true;
    }

    public HotbarLayout withSlot(int slot, HotbarCategory category) {
        if (slot < 0 || slot >= HOTBAR_SIZE) return this;
        if (slots[slot] == category) return this;
        HotbarCategory[] copy = Arrays.copyOf(slots, HOTBAR_SIZE);
        copy[slot] = category;
        return of(copy);
    }

    public HotbarLayout withoutCategory(HotbarCategory category) {
        if (category == null) return this;
        boolean changed = false;
        HotbarCategory[] copy = Arrays.copyOf(slots, HOTBAR_SIZE);
        for (int i = 0; i < HOTBAR_SIZE; i++) {
            if (copy[i] == category) {
                copy[i] = null;
                changed = true;
            }
        }
        return changed ? of(copy) : this;
    }

    private static HotbarLayout of(HotbarCategory[] copy) {
        for (HotbarCategory category : copy) {
            if (category != null) return new HotbarLayout(copy);
        }
        return EMPTY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HotbarLayout)) return false;
        return Arrays.equals(slots, ((HotbarLayout) o).slots);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(slots);
    }

    public String serialize() {
        if (isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < HOTBAR_SIZE; i++) {
            if (i > 0) sb.append(',');
            if (slots[i] != null) sb.append(slots[i].getKey());
        }
        return sb.toString();
    }

    public static HotbarLayout deserialize(String raw) {
        if (raw == null || raw.trim().isEmpty()) return EMPTY;
        String[] parts = raw.split(",", -1);
        HotbarCategory[] parsed = new HotbarCategory[HOTBAR_SIZE];
        boolean any = false;
        for (int i = 0; i < HOTBAR_SIZE && i < parts.length; i++) {
            HotbarCategory category = HotbarCategory.fromKey(parts[i]);
            if (category != null) {
                parsed[i] = category;
                any = true;
            }
        }
        return any ? new HotbarLayout(parsed) : EMPTY;
    }
}
