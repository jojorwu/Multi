package com.axalotl.async.common.util;

import com.axalotl.async.common.ParallelProcessor;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ChunkSaveMetrics {
    public static final AtomicInteger chunksSaved = new AtomicInteger(0);
    public static final AtomicLong totalSaveTime = new AtomicLong(0);
    public static final AtomicInteger errors = new AtomicInteger(0);

    public static void printMetrics() {
        ParallelProcessor.LOGGER.info("--- Chunk Save Metrics ---");
        ParallelProcessor.LOGGER.info("Chunks saved: " + chunksSaved.get());
        if (chunksSaved.get() > 0) {
            double averageTime = (double) totalSaveTime.get() / chunksSaved.get();
            ParallelProcessor.LOGGER.info("Average save time: " + String.format("%.2f", averageTime) + "ms");
        }
        ParallelProcessor.LOGGER.info("Errors: " + errors.get());
        ParallelProcessor.LOGGER.info("--------------------------");
    }
}
