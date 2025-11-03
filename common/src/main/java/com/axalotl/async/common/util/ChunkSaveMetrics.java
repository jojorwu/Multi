package com.axalotl.async.common.util;

import com.axalotl.async.common.ParallelProcessor;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ChunkSaveMetrics {
    public static final AtomicInteger chunksSaved = new AtomicInteger(0);
    public static final AtomicLong totalSaveTime = new AtomicLong(0);
    public static final AtomicInteger errors = new AtomicInteger(0);

    public static void printMetrics() {
        final int saved = chunksSaved.get();
        final long time = totalSaveTime.get();
        final int err = errors.get();
        ParallelProcessor.LOGGER.info("--- Chunk Save Metrics ---");
        ParallelProcessor.LOGGER.info("Chunks saved: " + saved);
        if (saved > 0) {
            ParallelProcessor.LOGGER.info("Average save time: " + String.format("%.2f", (double) time / saved) + "ms");
        }
        ParallelProcessor.LOGGER.info("Errors: " + err);
        ParallelProcessor.LOGGER.info("--------------------------");
    }

    public static void reset() {
        chunksSaved.set(0);
        totalSaveTime.set(0);
        errors.set(0);
    }
}
