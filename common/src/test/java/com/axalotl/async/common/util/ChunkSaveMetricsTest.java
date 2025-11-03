package com.axalotl.async.common.util;

import org.junit.jupiter.api.RepeatedTest;
import org.mockito.Mockito;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class ChunkSaveMetricsTest {

    @RepeatedTest(10)
    void printMetrics_shouldNotThrowException_whenCalledConcurrentlyWithReset() {
        Logger mockLogger = Mockito.mock(Logger.class);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Runnable printMetricsTask = () -> {
            ChunkSaveMetrics.chunksSaved.incrementAndGet();
            ChunkSaveMetrics.totalSaveTime.addAndGet(100);
            ChunkSaveMetrics.printMetrics(mockLogger);
        };

        Runnable resetTask = ChunkSaveMetrics::reset;

        for (int i = 0; i < 1000; i++) {
            executor.submit(printMetricsTask);
            executor.submit(resetTask);
        }

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
