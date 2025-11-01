package com.axalotl.async.common.parallelised.fastutil;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Int2ObjectConcurrentHashMapTest {

    @Test
    void remove_whenValueMatches_shouldRemoveEntry() {
        // Arrange
        Int2ObjectConcurrentHashMap<String> map = new Int2ObjectConcurrentHashMap<>();
        map.put(1, "one");

        // Act
        boolean removed = map.remove(1, "one");

        // Assert
        assertTrue(removed, "The entry should be removed when the value matches.");
        assertFalse(map.containsKey(1), "The map should not contain the key after removal.");
    }

    @Test
    void remove_whenValueDoesNotMatch_shouldNotRemoveEntry() {
        // Arrange
        Int2ObjectConcurrentHashMap<String> map = new Int2ObjectConcurrentHashMap<>();
        map.put(1, "one");

        // Act
        boolean removed = map.remove(1, "two");

        // Assert
        assertFalse(removed, "The entry should not be removed when the value does not match.");
        assertTrue(map.containsKey(1), "The map should still contain the key.");
    }
}