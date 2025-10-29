package com.axalotl.async.common;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.concurrent.ExecutionException;
import static org.junit.jupiter.api.Assertions.*;

class ParallelProcessorTest {

    @BeforeEach
    void setUp() {
        ParallelProcessor.setupThreadPool(1, ParallelProcessor.class);
    }

    @AfterEach
    void tearDown() {
        ParallelProcessor.stop();
    }

    @Test
    void testIsServerExecutionThread() throws ExecutionException, InterruptedException {
        assertFalse(ParallelProcessor.isServerExecutionThread(), "Main thread should not be in the pool");
        ParallelProcessor.tickPool.submit(() -> {
            assertTrue(Parallel.isServerExecutionThread(), "Pool thread should be in the pool");
        }).get();
    }
}
