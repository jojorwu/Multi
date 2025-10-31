package com.axalotl.async.common.parallelised.fastutil;

import org.junit.jupiter.api.Test;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ConcurrentLongLinkedOpenHashSetTest {

    @Test
    void removeFirstLong_whenCalledConcurrently_shouldNotThrow() throws InterruptedException {
        // Arrange
        ConcurrentLongLinkedOpenHashSet set = new ConcurrentLongLinkedOpenHashSet();
        for (int i = 0; i < 1000; i++) {
            set.add(i);
        }
        ExecutorService executor = Executors.newFixedThreadPool(10);

        // Act & Assert
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 500; i++) {
                executor.submit(set::removeFirstLong);
            }
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.MINUTES);
        }, "The corrected logic should not throw an exception.");
    }

    @Test
    void removeLastLong_whenCalledConcurrently_shouldNotThrow() throws InterruptedException {
        // Arrange
        ConcurrentLongLinkedOpenHashSet set = new ConcurrentLongLinkedOpenHashSet();
        for (int i = 0; i < 1000; i++) {
            set.add(i);
        }
        ExecutorService executor = Executors.newFixedThreadPool(10);

        // Act & Assert
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 500; i++) {
                executor.submit(set::removeLastLong);
            }
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.MINUTES);
        }, "The corrected logic should not throw an exception.");
    }
}