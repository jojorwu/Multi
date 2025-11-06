package com.axalotl.async.common.mixin.world;

import com.axalotl.async.common.ParallelProcessor;
import com.axalotl.async.common.config.AsyncConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.concurrent.CompletableFuture;

@Mixin(ChunkMap.class)
public abstract class ChunkMapRandomTickMixin {

    @Shadow
    private ServerLevel level;

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;tickChunk(Lnet/minecraft/world/level/chunk/LevelChunk;I)V"))
    private void onTickChunk(ServerLevel instance, LevelChunk chunk, int randomTickSpeed, Operation<Void> original) {
        if (AsyncConfig.disabled || !AsyncConfig.enableAsyncRandomTicks) {
            original.call(instance, chunk, randomTickSpeed);
            return;
        }
        CompletableFuture.runAsync(() -> original.call(instance, chunk, randomTickSpeed), ParallelProcessor.workPool)
                .exceptionally(e -> {
                    ParallelProcessor.LOGGER.error("Error during async random tick for chunk " + chunk.getPos(), e);
                    return null;
                });
    }
}
