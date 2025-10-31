package com.axalotl.async.common;

import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParallelProcessorLogicTest {

    private static class BaseClass {}
    private static class SubClass extends BaseClass {}

    @Test
    void originalLogic_whenGivenSubclass_shouldReturnFalse() {
        Set<Class<?>> blockedClasses = Set.of(BaseClass.class);
        SubClass instance = new SubClass();
        boolean isBlocked = blockedClasses.contains(instance.getClass());
        assertFalse(isBlocked, "The original logic should fail to identify subclasses.");
    }

    @Test
    void correctedLogic_whenGivenSubclass_shouldReturnTrue() {
        Set<Class<?>> blockedClasses = Set.of(BaseClass.class);
        SubClass instance = new SubClass();
        boolean isBlocked = blockedClasses.stream().anyMatch(c -> c.isAssignableFrom(instance.getClass()));
        assertTrue(isBlocked, "The corrected logic should correctly identify subclasses.");
    }
}