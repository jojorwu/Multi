package com.axalotl.async.common.commands;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.lang.reflect.Field;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

class StatsCommandTest {

    @Test
    void testCalculateAverageThreadsRaceCondition() throws NoSuchFieldException, IllegalAccessException {
        Field field = StatsCommand.class.getDeclaredField("threadSamples");
        field.setAccessible(true);

        Queue<Integer> originalQueue = (Queue<Integer>) field.get(null);

        Queue<Integer> mockQueue = new ConcurrentLinkedQueue<>() {
            @Override
            public int size() {
                return 1;
            }

            @Override
            public Stream<Integer> stream() {
                return Stream.empty();
            }
        };

        field.set(null, mockQueue);

        try {
            Assertions.assertThrows(ArithmeticException.class, () -> {
                StatsCommand.class.getDeclaredMethod("calculateAverageThreads").invoke(null);
            });
        } finally {
            field.set(null, originalQueue);
        }
    }
}
