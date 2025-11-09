package com.axalotl.async.common;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParallelProcessorTest {

    @Mock
    private Entity mockEntity;

    @Mock
    private EntityType<?> mockEntityType;

    private UUID entityUuid;

    @BeforeEach
    public void setUp() {
        entityUuid = UUID.randomUUID();
        when(mockEntity.getUUID()).thenReturn(entityUuid);
        ParallelProcessor.portalTickSyncMap.clear();
    }

    @Test
    public void testHandlePortalSyncRaceCondition() {
        // Arrange
        ParallelProcessor.portalTickSyncMap.put(entityUuid, 1);
        when(ParallelProcessor.isPortalTickRequired(mockEntity)).thenReturn(true);

        // Act
        boolean result1 = ParallelProcessor.handlePortalSync(mockEntity);
        boolean result2 = ParallelProcessor.handlePortalSync(mockEntity);

        // Assert
        assertTrue(result1, "First call should return true as ticksLeft > 0");
        assertFalse(result2, "Second call should return false as ticksLeft is now 0");
        assertFalse(ParallelProcessor.portalTickSyncMap.containsKey(entityUuid), "Map should not contain entity after ticksLeft is 0");
    }
}
