package com.andrei1058.bedwars.practice;

import com.andrei1058.bedwars.api.language.Messages;
import org.bukkit.Material;
import java.util.Locale;

public enum PracticeMode {
    FIREBALL(Material.FIREBALL, 64, Messages.PRACTICE_MODE_FIREBALL),
    BRIDGE(Material.WOOL, 64, Messages.PRACTICE_MODE_BRIDGE),
    PEARL(Material.ENDER_PEARL, 16, Messages.PRACTICE_MODE_PEARL);

    private final Material material;
    private final int amount;
    private final String id, messagePath;

    PracticeMode(Material material, int amount, String messagePath) {
        this.material = material;
        this.amount = amount;
        this.id = name().toLowerCase(Locale.ROOT);
        this.messagePath = messagePath;
    }

    public Material getMaterial() { return material; }
    public int getAmount() { return amount; }
    public String getId() { return id; }
    public String getMessagePath() { return messagePath; }

    public static PracticeMode parse(String value) {
        for (PracticeMode mode : values()) {
            if (mode.getId().equals(value)) return mode;
        }
        throw new IllegalArgumentException("Unknown practice mode: " + value);
    }
}
