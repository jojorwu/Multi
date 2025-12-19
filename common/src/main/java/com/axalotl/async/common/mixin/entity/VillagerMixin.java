package com.axalotl.async.common.mixin.entity;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Villager.class)
public class VillagerMixin {

    @WrapMethod(method = "pickUpItem")
    private synchronized void pickUpItem(ServerLevel level, ItemEntity entity, Operation<Void> original) {
        if (!entity.isRemoved()) {
            original.call(level, entity);
        }
    }

    @WrapMethod(method = "spawnGolemIfNeeded")
    private synchronized void spawnGolemIfNeeded(ServerLevel world, long time, int requiredCount, Operation<Void> original) {
        original.call(world, time, requiredCount);
    }
}
