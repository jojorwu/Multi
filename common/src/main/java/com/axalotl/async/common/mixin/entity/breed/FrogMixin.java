package com.axalotl.async.common.mixin.entity.breed;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(Frog.class)
public abstract class FrogMixin extends Animal {

    @Unique
    private static final ConcurrentHashMap<String, Boolean> async$breedingPairs = new ConcurrentHashMap<>();

    protected FrogMixin(EntityType<? extends Animal> entityType, Level world) {
        super(entityType, world);
    }

    @WrapMethod(method = "spawnChildFromBreeding")
    private void breed(ServerLevel world, Animal other, Operation<Void> original) {
        String pairKey = getPairKey(this, other);
        if (async$breedingPairs.putIfAbsent(pairKey, true) == null) {
            try {
                original.call(world, other);
            } finally {
                async$breedingPairs.remove(pairKey);
            }
        }
    }

    @Unique
    private static String getPairKey(Animal first, Animal second) {
        UUID id1 = first.getUUID();
        UUID id2 = second.getUUID();
        return id1.compareTo(id2) < 0 ? id1 + "|" + id2 : id2 + "|" + id1;
    }
}
