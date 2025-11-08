package com.axalotl.async.common.util;

import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ChunkSaveMetricsTest {

    @RepeatedTest(10)
    void printMetrics_whenCalledConcurrentlyWithReset_doesNotThrowException() {
        Logger logger = Mockito.mock(Logger.class);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Runnable printTask = () -> {
            for (int i = 0; i < 1000; i++) {
                ChunkSaveMetrics.chunksSaved.set(1);
                assertDoesNotThrow(() -> ChunkSaveMetrics.printMetrics(logger));
            }
        };

        Runnable resetTask = () -> {
            for (int i = 0; i < 1000; i++) {
                ChunkSaveMetrics.reset();
            }
        };

        executor.submit(printTask);
        executor.submit(resetTask);

        executor.shutdown();
        try {
            if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}
