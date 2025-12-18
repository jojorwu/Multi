package com.axalotl.async.common;

import com.axalotl.async.common.platform.PlatformEvents;

public class AsyncCommon {
    private static final boolean LITHIUM = PlatformEvents.getInstance().isModLoaded("lithium");

    public static boolean isLithiumLoaded() {
        return LITHIUM;
    }
}