package com.axalotl.async.common.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AsyncThreadTracker {
    private static final Map<String, Set<WeakReference<Thread>>> mcThreadTracker = new ConcurrentHashMap<>();

    public static void registerThread(String poolName, Thread thread) {
        mcThreadTracker
                .computeIfAbsent(poolName, key -> ConcurrentHashMap.newKeySet())
                .add(new WeakReference<>(thread));
    }

    private static boolean isThreadInPool(Thread thread) {
        Set<WeakReference<Thread>> threadRefs = mcThreadTracker.get("Async-Tick");
        if (threadRefs == null) {
            return false;
        }
        boolean found = false;
        List<WeakReference<Thread>> toRemove = new ArrayList<>();
        for (WeakReference<Thread> ref : threadRefs) {
            Thread t = ref.get();
            if (t == null) {
                toRemove.add(ref);
            } else if (t.getId() == thread.getId()) {
                found = true;
            }
        }
        if (!toRemove.isEmpty()) {
            threadRefs.removeAll(toRemove);
        }
        return found;
    }

    public static boolean isServerExecutionThread() {
        return isThreadInPool(Thread.currentThread());
    }
}
