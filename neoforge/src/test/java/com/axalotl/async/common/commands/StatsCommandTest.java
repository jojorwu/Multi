package com.axalotl.async.common.commands;

import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StatsCommandTest {

    @Test
    public void testBuildGeneralStatsMessageWithNonIntegerAverage() {
        // Arrange
        double avgThreads = 2.5;
        String expectedValue = "2.5";

        // Act
        Component message = StatsCommand.buildGeneralStatsMessage(avgThreads);
        String messageString = message.getString();

        // Assert
        Pattern pattern = Pattern.compile("Average Active Processing Threads: ([\\d.,]+)");
        Matcher matcher = pattern.matcher(messageString);
        assertTrue(matcher.find(), "Could not find average thread count value in message: " + messageString);
        String actualValue = matcher.group(1);
        assertEquals(expectedValue, actualValue, "Average should not be rounded up");
    }
}
