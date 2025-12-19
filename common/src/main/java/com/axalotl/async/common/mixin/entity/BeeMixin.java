package com.axalotl.async.common.mixin.entity;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.animal.Bee;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Bee.class)
public class BeeMixin {

    @WrapMethod(method = "wantsToEnterHive")
    private synchronized boolean loot(Operation<Boolean> original) {
        return original.call();
    }
}
