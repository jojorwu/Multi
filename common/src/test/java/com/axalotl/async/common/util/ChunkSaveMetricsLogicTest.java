package com.axalotl.async.common.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ChunkSaveMetricsLogicTest {

    @Test
    void originalLogic_whenDividingIntegers_shouldReturnTruncatedInteger() {
        long totalSaveTime = 10;
        int chunksSaved = 3;
        long averageSaveTime = totalSaveTime / chunksSaved;
        assertEquals(3, averageSaveTime, "The original logic should truncate the result.");
    }

    @Test
    void correctedLogic_whenDividingIntegers_shouldReturnFloat() {
        long totalSaveTime = 10;
        int chunksSaved = 3;
        double averageSaveTime = (double) totalSaveTime / chunksSaved;
        assertEquals(3.3333333333333335, averageSaveTime, "The corrected logic should produce a float.");
    }
}