package com.axalotl.async.common.mixin.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

class EntityLockingTest {

    private static class OriginalEntity {
        private static final Object async$lock = new Object();
        public Object getLock() {
            return async$lock;
        }
    }

    private static class CorrectedEntity {
        private final Object async$lock = new Object();
        public Object getLock() {
            return async$lock;
        }
    }

    @Test
    void originalLogic_whenCreatingTwoInstances_shouldUseSameLock() {
        // Arrange
        OriginalEntity entity1 = new OriginalEntity();
        OriginalEntity entity2 = new OriginalEntity();

        // Act
        Object lock1 = entity1.getLock();
        Object lock2 = entity2.getLock();

        // Assert
        assertSame(lock1, lock2, "The original logic should use the same lock for both instances.");
    }

    @Test
    void correctedLogic_whenCreatingTwoInstances_shouldUseDifferentLocks() {
        // Arrange
        CorrectedEntity entity1 = new CorrectedEntity();
        CorrectedEntity entity2 = new CorrectedEntity();

        // Act
        Object lock1 = entity1.getLock();
        Object lock2 = entity2.getLock();

        // Assert
        assertNotSame(lock1, lock2, "The corrected logic should use different locks for each instance.");
    }
}