package com.axalotl.async.common.mixin.entity;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Mob.class)
public class MobMixin {

    @WrapMethod(method = "equipItemIfPossible")
    private synchronized ItemStack wrapEquipItemIfPossible(ServerLevel level, ItemStack stack, Operation<ItemStack> original) {
        return original.call(level, stack);
    }

    @WrapMethod(method = "pickUpItem")
    private synchronized void wrapPickUpItem(ServerLevel level, ItemEntity entity, Operation<Void> original) {
        original.call(level, entity);
    }

    @WrapMethod(method = "setItemSlotAndDropWhenKilled")
    private synchronized void wrapSetItemSlotAndDropWhenKilled(EquipmentSlot slot, ItemStack stack, Operation<Void> original) {
        original.call(slot, stack);
    }

    @WrapMethod(method = "setBodyArmorItem")
    private synchronized void wrapSetBodyArmorItem(ItemStack stack, Operation<Void> original) {
        original.call(stack);
    }
}
