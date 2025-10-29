package com.axalotl.async.common;

import com.axalotl.async.common.util.AsyncThreadTracker;
import org.junit.jupiter.api.Test;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AsyncThreadTrackerTest {

    @Test
    void testThreadCleanup() throws Exception {
        Field field = AsyncThreadTracker.class.getDeclaredField("mcThreadTracker");
        field.setAccessible(true);
        Map<String, Set<WeakReference<Thread>>> mcThreadTracker = (Map<String, Set<WeakReference<Thread>>>) field.get(null);
        mcThreadTracker.clear();

        Thread thread = new Thread();
        AsyncThreadTracker.registerThread("Async-Tick", thread);

        assertEquals(1, mcThreadTracker.get("Async-Tick").size());

        thread = null;

        boolean cleaned = false;
        for (int i = 0; i < 10; i++) {
            System.gc();
            System.runFinalization();
            Method method = AsyncThreadTracker.class.getDeclaredMethod("isThreadInPool", Thread.class);
            method.setAccessible(true);
            method.invoke(null, new Thread());
            if (mcThreadTracker.get("Async-Tick").isEmpty()) {
                cleaned = true;
                break;
            }
            Thread.sleep(500);
        }

        assertTrue(cleaned, "The thread tracker should have been cleaned up");
    }
}
