package com.andrei1058.bedwars.support.version.v1_8_R3;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;

final class DragonTargetListener implements Listener {

    // 原版龙的随机重选也必须遵守竞技场阵营。
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTarget(EntityTargetEvent event) {
        if (!(event.getEntity() instanceof CraftEnderDragon)) return;
        CraftEnderDragon entity = (CraftEnderDragon) event.getEntity();
        if (!(entity.getHandle() instanceof BedWarsDragon)) return;
        BedWarsDragon dragon = (BedWarsDragon) entity.getHandle();
        if (event.getTarget() instanceof Player && dragon.isValidTarget((Player) event.getTarget())) return;
        event.setTarget(dragon.findTarget());
    }
}
