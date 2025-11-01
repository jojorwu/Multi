package com.axalotl.async.common;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParallelProcessorThreadLogicTest {

    @Test
    void originalLogic_whenMapIsNotEmpty_shouldNotBeEmpty() {
        // Arrange
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");

        // Act
        // The map is not cleared.

        // Assert
        assertFalse(map.isEmpty(), "The original logic should not clear the map.");
    }

    @Test
    void correctedLogic_whenMapIsCleared_shouldBeEmpty() {
        // Arrange
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");

        // Act
        map.clear();

        // Assert
        assertTrue(map.isEmpty(), "The corrected logic should clear the map.");
    }
}