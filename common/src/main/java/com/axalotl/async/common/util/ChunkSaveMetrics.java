package com.axalotl.async.common.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ChunkSaveMetrics {
    private static final AtomicInteger chunksSaved = new AtomicInteger(0);
    private static final AtomicLong totalSaveTime = new AtomicLong(0);
    private static final AtomicInteger errors = new AtomicInteger(0);
    private static final Logger LOGGER = LogManager.getLogger("Async | ChunkSaveMetrics");

    public static void printMetrics() {
        final int saved = chunksSaved.get();
        final long time = totalSaveTime.get();
        final int err = errors.get();

        LOGGER.info("--- Chunk Save Metrics ---");
        LOGGER.info("Chunks saved: " + saved);
        if (saved > 0) {
            LOGGER.info("Average save time: " + String.format("%.2f", (double) time / saved / 1_000_000) + "ms");
        }
        LOGGER.info("Errors: " + err);
        LOGGER.info("--------------------------");
    }

    public static void reset() {
        chunksSaved.set(0);
        totalSaveTime.set(0);
        errors.set(0);
    }

    public static void incrementChunksSaved() {
        chunksSaved.incrementAndGet();
    }

    public static void incrementErrors() {
        errors.incrementAndGet();
    }

    public static void addSaveTime(long nanoTime) {
        totalSaveTime.addAndGet(nanoTime);
    }

    public static int getChunksSaved() {
        return chunksSaved.get();
    }

    public static long getTotalSaveTime() {
        return totalSaveTime.get();
    }

    public static int getErrors() {
        return errors.get();
    }
}
