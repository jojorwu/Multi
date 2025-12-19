package com.axalotl.async.common.mixin.world;

import com.axalotl.async.common.ParallelProcessor;
import com.axalotl.async.common.util.ChunkSaveMetrics;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.CompletableFuture;

@Mixin(ChunkMap.class)
public class ChunkMapMixin {
    @Redirect(method = "save", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/storage/ChunkStorage;write(Lnet/minecraft/world/level/ChunkPos;Ljava/util/function/Supplier;)V"))
    private void onSave(ChunkStorage storage, ChunkPos pos, java.util.function.Supplier<net.minecraft.nbt.CompoundTag> tag) {
        final long startTime = System.nanoTime();
        CompletableFuture.runAsync(() -> storage.write(pos, tag::get), ParallelProcessor.chunkIOPool).whenComplete((v, e) -> {
            if (e != null) {
                ParallelProcessor.LOGGER.error("Failed to save chunk async", e);
                ChunkSaveMetrics.incrementErrors();
            } else {
                ChunkSaveMetrics.incrementChunksSaved();
                ChunkSaveMetrics.addSaveTime(System.nanoTime() - startTime);
            }
        });
    }
}
