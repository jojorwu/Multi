package com.axalotl.async.common.util;

import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class ChunkSaveMetrics {
    private static final AtomicInteger chunksSaved = new AtomicInteger(0);
    private static final AtomicLong totalSaveTime = new AtomicLong(0);
    private static final AtomicInteger errors = new AtomicInteger(0);

    private ChunkSaveMetrics() {
    }

    public static void incrementChunksSaved() {
        chunksSaved.incrementAndGet();
    }

    public static void addSaveTime(long time) {
        totalSaveTime.addAndGet(time);
    }

    public static void incrementErrors() {
        errors.incrementAndGet();
    }

    public static void printMetrics(Logger logger) {
        final int saved = chunksSaved.get();
        final long time = totalSaveTime.get();
        final int err = errors.get();

        logger.info("--- Chunk Save Metrics ---");
        logger.info("Chunks saved: " + saved);
        logger.info("Average save time: " + (saved > 0 ? String.format("%.2f", (double) time / saved / 1_000_000) : "0.00") + "ms");
        logger.info("Errors: " + err);
        logger.info("--------------------------");
    }

    public static void reset() {
        chunksSaved.set(0);
        totalSaveTime.set(0);
        errors.set(0);
    }

    public static void setChunksSaved(int i) {
        chunksSaved.set(i);
    }

    public static void setTotalSaveTime(long i) {
        totalSaveTime.set(i);
    }
}
