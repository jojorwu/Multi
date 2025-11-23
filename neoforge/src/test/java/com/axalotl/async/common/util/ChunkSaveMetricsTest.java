package com.axalotl.async.common.util;

import org.junit.jupiter.api.Test;
import org.apache.logging.log4j.Logger;
import static org.mockito.Mockito.*;

public class ChunkSaveMetricsTest {

    @Test
    public void testPrintMetrics() {
        Logger logger = mock(Logger.class);

        ChunkSaveMetrics.reset();
        ChunkSaveMetrics.setChunksSaved(10);
        ChunkSaveMetrics.setTotalSaveTime(123490000);

        ChunkSaveMetrics.printMetrics(logger);

        verify(logger).info("--- Chunk Save Metrics ---");
        verify(logger).info("Chunks saved: 10");
        verify(logger).info("Average save time: " + String.format("%.2f", 12.35) + "ms");
        verify(logger).info("Errors: 0");
        verify(logger).info("--------------------------");
    }
}
