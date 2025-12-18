package com.axalotl.async.common.mixin.entity.breed;

import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.animal.Fox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(Fox.FoxBreedGoal.class)
public abstract class FoxBreedGoalMixin extends BreedGoal {

    public FoxBreedGoalMixin(Fox fox, double speedModifier) {
        super(fox, speedModifier);
    }

    @Unique
    private static final ConcurrentHashMap<String, Boolean> async$breedingPairs = new ConcurrentHashMap<>();

    @Inject(method = "start", at = @At("HEAD"))
    private void resetBreedingFlag(CallbackInfo ci) {
        async$breedingPairs.remove(async$getPairKey());
    }

    @Inject(method = "breed", at = @At("HEAD"), cancellable = true)
    private synchronized void preventDoubleBreed(CallbackInfo ci) {
        String pairKey = async$getPairKey();
        if (async$breedingPairs.putIfAbsent(pairKey, Boolean.TRUE) != null) {
            ci.cancel();
        }
    }

    @Unique
    private String async$getPairKey() {
        UUID id1 = this.animal.getUUID();
        if (this.partner == null) {
            return id1.toString();
        }
        UUID id2 = this.partner.getUUID();
        return id1.compareTo(id2) <= 0 ? id1 + "|" + id2 : id2 + "|" + id1;
    }
}
