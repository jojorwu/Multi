package com.axalotl.async.neoforge.mixin.world;

import com.axalotl.async.common.ParallelProcessor;
import com.axalotl.async.common.config.AsyncConfig;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.CompletableFuture;

@Mixin(ChunkMap.class)
public class ChunkMapMixin {
    @Redirect(method = "scheduleChunkGenerationTask", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkHolder;scheduleChunkGenerationTask(Lnet/minecraft/world/level/chunk/status/ChunkStatus;Lnet/minecraft/server/level/ChunkMap;)Ljava/util/concurrent/CompletableFuture;"))
    private CompletableFuture<ChunkResult<ChunkAccess>> onScheduleChunkGenerationTask(ChunkHolder holder, ChunkStatus status, ChunkMap map) {
        if (AsyncConfig.disabled) {
            return holder.scheduleChunkGenerationTask(status, map);
        }
        return CompletableFuture.supplyAsync(() -> holder.scheduleChunkGenerationTask(status, map), ParallelProcessor.chunkGenPool)
                .thenCompose(future -> future)
                .exceptionally(e -> {
                    ParallelProcessor.LOGGER.error("Error during async chunk generation for chunk " + holder.getPos(), e);
                    return ChunkResult.error("Async chunk generation failed for chunk " + holder.getPos() + ": " + e.getMessage());
                });
    }
}
