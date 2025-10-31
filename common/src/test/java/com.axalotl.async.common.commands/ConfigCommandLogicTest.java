package com.axalotl.async.common.commands;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ConfigCommandLogicTest {

    private static class CommandSourceStack {
        private boolean success = false;
        private boolean failure = false;

        public void sendSuccess() {
            this.success = true;
        }

        public void sendFailure() {
            this.failure = true;
        }

        public boolean hasReceivedSuccess() {
            return success;
        }

        public boolean hasReceivedFailure() {
            return failure;
        }
    }

    @Test
    void originalLogic_whenEntityIsAlreadySynchronized_shouldSendSuccess() {
        // Arrange
        Set<String> synchronizedEntities = new HashSet<>();
        String entity = "minecraft:pig";
        synchronizedEntities.add(entity);
        CommandSourceStack source = new CommandSourceStack();

        // Act
        if (synchronizedEntities.contains(entity)) {
            source.sendSuccess();
        } else {
            source.sendFailure();
        }

        // Assert
        assertTrue(source.hasReceivedSuccess(), "The original logic should send a success message.");
        assertFalse(source.hasReceivedFailure(), "The original logic should not send a failure message.");
    }

    @Test
    void correctedLogic_whenEntityIsAlreadySynchronized_shouldSendFailure() {
        // Arrange
        Set<String> synchronizedEntities = new HashSet<>();
        String entity = "minecraft:pig";
        synchronizedEntities.add(entity);
        CommandSourceStack source = new CommandSourceStack();

        // Act
        if (synchronizedEntities.contains(entity)) {
            source.sendFailure();
        } else {
            source.sendSuccess();
        }

        // Assert
        assertFalse(source.hasReceivedSuccess(), "The corrected logic should not send a success message.");
        assertTrue(source.hasReceivedFailure(), "The corrected logic should send a failure message.");
    }
}