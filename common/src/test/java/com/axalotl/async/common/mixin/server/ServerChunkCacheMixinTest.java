package com.axalotl.async.common.mixin.server;

import org.junit.jupiter.api.Test;
import java.util.concurrent.CompletableFuture;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ServerChunkCacheMixinTest {

    private static class ChunkAccess {}
    private static class ChunkResult<T> {
        private final T result;
        public ChunkResult(T result) {
            this.result = result;
        }
        public T orElse(T other) {
            return result != null ? result : other;
        }
    }

    @Test
    void originalLogic_whenFutureIsNotDone_shouldReturnNull() {
        // Arrange
        CompletableFuture<ChunkResult<ChunkAccess>> future = new CompletableFuture<>();

        // Act
        ChunkAccess chunk = future.getNow(new ChunkResult<>(null)).orElse(null);

        // Assert
        assertNull(chunk, "The original logic should return null when the future is not done.");
    }

    @Test
    void correctedLogic_whenFutureIsNotDone_shouldBlockAndReturnChunk() {
        // Arrange
        CompletableFuture<ChunkResult<ChunkAccess>> future = new CompletableFuture<>();
        ChunkAccess expectedChunk = new ChunkAccess();
        new Thread(() -> {
            try {
                Thread.sleep(100);
                future.complete(new ChunkResult<>(expectedChunk));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        // Act
        ChunkAccess chunk = future.join().orElse(null);

        // Assert
        assertNotNull(chunk, "The corrected logic should block and return the chunk.");
    }
}