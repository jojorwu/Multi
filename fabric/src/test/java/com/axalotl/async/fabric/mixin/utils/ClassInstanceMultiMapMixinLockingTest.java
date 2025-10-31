package com.axalotl.async.fabric.mixin.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

class ClassInstanceMultiMapMixinLockingTest {

    private static class OriginalClassInstanceMultiMap {
        private static final Object async$lock = new Object();
        public Object getLock() {
            return async$lock;
        }
    }

    private static class CorrectedClassInstanceMultiMap {
        private final Object async$lock = new Object();
        public Object getLock() {
            return async$lock;
        }
    }

    @Test
    void originalLogic_whenCreatingTwoInstances_shouldUseSameLock() {
        // Arrange
        OriginalClassInstanceMultiMap map1 = new OriginalClassInstanceMultiMap();
        OriginalClassInstanceMultiMap map2 = new OriginalClassInstanceMultiMap();

        // Act
        Object lock1 = map1.getLock();
        Object lock2 = map2.getLock();

        // Assert
        assertSame(lock1, lock2, "The original logic should use the same lock for both instances.");
    }

    @Test
    void correctedLogic_whenCreatingTwoInstances_shouldUseDifferentLocks() {
        // Arrange
        CorrectedClassInstanceMultiMap map1 = new CorrectedClassInstanceMultiMap();
        CorrectedClassInstanceMultiMap map2 = new CorrectedClassInstanceMultiMap();

        // Act
        Object lock1 = map1.getLock();
        Object lock2 = map2.getLock();

        // Assert
        assertNotSame(lock1, lock2, "The corrected logic should use different locks for each instance.");
    }
}