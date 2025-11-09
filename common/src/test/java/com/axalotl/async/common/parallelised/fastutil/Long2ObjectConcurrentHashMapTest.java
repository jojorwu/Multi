package com.axalotl.async.common.parallelised.fastutil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Long2ObjectConcurrentHashMapTest {

    private Long2ObjectConcurrentHashMap<String> map;

    @BeforeEach
    void setUp() {
        map = new Long2ObjectConcurrentHashMap<>();
    }

    @Test
    void remove_whenKeyValueMatch_removesEntryAndReturnsTrue() {
        // Arrange
        long key = 123L;
        String value = "test-value";
        map.put(key, value);

        // Act
        boolean result = map.remove(key, value);

        // Assert
        assertTrue(result, "remove(k, v) should return true when the entry is removed.");
        assertTrue(map.isEmpty(), "The map should be empty after removing the only entry.");
    }

    @Test
    void remove_whenValueDoesNotMatch_doesNotRemoveEntryAndReturnsFalse() {
        // Arrange
        long key = 123L;
        String value = "test-value";
        String wrongValue = "wrong-value";
        map.put(key, value);

        // Act
        boolean result = map.remove(key, wrongValue);

        // Assert
        assertFalse(result, "remove(k, v) should return false when the value does not match.");
        assertEquals(1, map.size(), "The map size should not change when removal fails.");
        assertEquals(value, map.get(key), "The entry should remain in the map when removal fails.");
    }

    @Test
    void remove_whenKeyDoesNotExist_returnsFalse() {
        // Arrange
        long key = 123L;
        String value = "test-value";

        // Act
        boolean result = map.remove(key, value);

        // Assert
        assertFalse(result, "remove(k, v) should return false when the key does not exist.");
        assertTrue(map.isEmpty(), "The map should remain empty.");
    }
}
