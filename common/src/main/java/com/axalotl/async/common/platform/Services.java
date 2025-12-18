package com.axalotl.async.common.platform;

import java.util.ServiceLoader;

public class Services {
    public static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz, clazz.getClassLoader()).findFirst().orElseThrow(() -> new IllegalStateException("Failed to load service for " + clazz.getName()));
    }
}
