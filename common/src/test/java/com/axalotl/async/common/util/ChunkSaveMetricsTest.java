package com.axalotl.async.common.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ChunkSaveMetricsTest {

    @BeforeEach
    void setUp() {
        ChunkSaveMetrics.reset();
    }

    @Test
    void testMetrics() {
        ChunkSaveMetrics.incrementChunksSaved();
        ChunkSaveMetrics.addSaveTime(1_000_000);
        ChunkSaveMetrics.incrementErrors();

        assertEquals(1, ChunkSaveMetrics.getChunksSaved());
        assertEquals(1_000_000, ChunkSaveMetrics.getTotalSaveTime());
        assertEquals(1, ChunkSaveMetrics.getErrors());
    }

    @Test
    void testReset() {
        ChunkSaveMetrics.incrementChunksSaved();
        ChunkSaveMetrics.addSaveTime(1_000_000);
        ChunkSaveMetrics.incrementErrors();

        ChunkSaveMetrics.reset();

        assertEquals(0, ChunkSaveMetrics.getChunksSaved());
        assertEquals(0, ChunkSaveMetrics.getTotalSaveTime());
        assertEquals(0, ChunkSaveMetrics.getErrors());
    }

    @Test
    void printMetrics_whenCalledConcurrentlyWithReset_doesNotThrowException() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        AtomicBoolean running = new AtomicBoolean(true);

        Runnable printTask = () -> {
            while (running.get()) {
                assertDoesNotThrow(ChunkSaveMetrics::printMetrics);
            }
        };

        Runnable resetTask = () -> {
            while (running.get()) {
                ChunkSaveMetrics.incrementChunksSaved();
                ChunkSaveMetrics.addSaveTime(1_000_000);
                ChunkSaveMetrics.reset();
            }
        };

        executor.submit(printTask);
        executor.submit(resetTask);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        running.set(false);
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}
