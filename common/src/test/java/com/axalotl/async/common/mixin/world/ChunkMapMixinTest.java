package com.axalotl.async.common.mixin.world;

import org.junit.jupiter.api.Test;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ChunkMapMixinTest {

    private static class CompoundTag {}
    private static class ChunkPos {}
    private static class ChunkStorage {
        public void write(ChunkPos pos, Supplier<CompoundTag> tag) {}
    }

    @Test
    void originalLogic_whenSavingChunk_shouldSerializeOnMainThread() {
        // Arrange
        ExecutorService chunkIOPool = Executors.newSingleThreadExecutor();
        final long mainThreadId = Thread.currentThread().getId();
        final long[] serializationThreadId = new long[1];
        Supplier<CompoundTag> tag = () -> {
            serializationThreadId[0] = Thread.currentThread().getId();
            return new CompoundTag();
        };

        // Act
        final CompoundTag compoundTag = tag.get();
        CompletableFuture.runAsync(() -> {
            new ChunkStorage().write(new ChunkPos(), () -> compoundTag);
        }, chunkIOPool).join();

        // Assert
        assertEquals(mainThreadId, serializationThreadId[0], "The original logic should serialize on the main thread.");
        chunkIOPool.shutdown();
    }

    @Test
    void correctedLogic_whenSavingChunk_shouldSerializeOnIOThread() {
        // Arrange
        ExecutorService chunkIOPool = Executors.newSingleThreadExecutor();
        final long mainThreadId = Thread.currentThread().getId();
        final long[] serializationThreadId = new long[1];
        Supplier<CompoundTag> tag = () -> {
            serializationThreadId[0] = Thread.currentThread().getId();
            return new CompoundTag();
        };

        // Act
        CompletableFuture.runAsync(() -> {
            final CompoundTag compoundTag = tag.get();
            new ChunkStorage().write(new ChunkPos(), () -> compoundTag);
        }, chunkIOPool).join();

        // Assert
        assertNotEquals(mainThreadId, serializationThreadId[0], "The corrected logic should serialize on the IO thread.");
        chunkIOPool.shutdown();
    }
}