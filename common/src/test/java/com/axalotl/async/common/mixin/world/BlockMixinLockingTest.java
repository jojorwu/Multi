package com.axalotl.async.common.mixin.world;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

class BlockMixinLockingTest {

    private static class OriginalBlock {
        private static final Object async$lock = new Object();
        public Object getLock() {
            return async$lock;
        }
    }

    private static class CorrectedBlock {
        private final Object async$lock = new Object();
        public Object getLock() {
            return async$lock;
        }
    }

    @Test
    void originalLogic_whenCreatingTwoInstances_shouldUseSameLock() {
        // Arrange
        OriginalBlock block1 = new OriginalBlock();
        OriginalBlock block2 = new OriginalBlock();

        // Act
        Object lock1 = block1.getLock();
        Object lock2 = block2.getLock();

        // Assert
        assertSame(lock1, lock2, "The original logic should use the same lock for both instances.");
    }

    @Test
    void correctedLogic_whenCreatingTwoInstances_shouldUseDifferentLocks() {
        // Arrange
        CorrectedBlock block1 = new CorrectedBlock();
        CorrectedBlock block2 = new CorrectedBlock();

        // Act
        Object lock1 = block1.getLock();
        Object lock2 = block2.getLock();

        // Assert
        assertNotSame(lock1, lock2, "The corrected logic should use different locks for each instance.");
    }
}