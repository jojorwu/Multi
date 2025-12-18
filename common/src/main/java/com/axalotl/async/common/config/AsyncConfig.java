package com.axalotl.async.common.config;

import com.axalotl.async.common.platform.PlatformEvents;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class AsyncConfig {
    public static final Logger LOGGER = LoggerFactory.getLogger("Async Config");

    private static boolean disabled = false;
    private static int paraMax = -1;
    private static int chunkIOParaMax = -1;
    private static int chunkGenParaMax = -1;
    private static boolean enableAsyncSpawn = true;
    private static boolean enableAsyncRandomTicks = false;

    public static Set<ResourceLocation> synchronizedEntities = new HashSet<>(getDefaultSynchronizedEntities());

    public static boolean isDisabled() {
        return disabled;
    }

    public static void setDisabled(boolean disabled) {
        AsyncConfig.disabled = disabled;
    }

    public static int getParaMax() {
        if (paraMax <= 0) return Runtime.getRuntime().availableProcessors();
        return Math.max(1, Math.min(Runtime.getRuntime().availableProcessors(), paraMax));
    }

    public static void setParaMax(int paraMax) {
        AsyncConfig.paraMax = paraMax;
    }

    public static int getChunkIOParaMax() {
        if (chunkIOParaMax <= 0) return 4;
        return Math.max(1, chunkIOParaMax);
    }

    public static void setChunkIOParaMax(int chunkIOParaMax) {
        AsyncConfig.chunkIOParaMax = chunkIOParaMax;
    }

    public static int getChunkGenParaMax() {
        if (chunkGenParaMax <= 0) return Runtime.getRuntime().availableProcessors();
        return Math.max(1, Math.min(Runtime.getRuntime().availableProcessors(), chunkGenParaMax));
    }

    public static void setChunkGenParaMax(int chunkGenParaMax) {
        AsyncConfig.chunkGenParaMax = chunkGenParaMax;
    }

    public static boolean isEnableAsyncSpawn() {
        return enableAsyncSpawn;
    }

    public static void setEnableAsyncSpawn(boolean enableAsyncSpawn) {
        AsyncConfig.enableAsyncSpawn = enableAsyncSpawn;
    }


    public static boolean isEnableAsyncRandomTicks() {
        return enableAsyncRandomTicks;
    }

    public static void setEnableAsyncRandomTicks(boolean enableAsyncRandomTicks) {
        AsyncConfig.enableAsyncRandomTicks = enableAsyncRandomTicks;
    }

    public static Set<ResourceLocation> getDefaultSynchronizedEntities() {
        return Set.of(
                Objects.requireNonNull(ResourceLocation.tryBuild("minecraft", "tnt")),
                Objects.requireNonNull(ResourceLocation.tryBuild("minecraft", "item")),
                Objects.requireNonNull(ResourceLocation.tryBuild("minecraft", "experience_orb"))
        );
    }

    public static void syncEntity(ResourceLocation entityId) {
        if (synchronizedEntities.add(entityId)) {
            PlatformEvents.getInstance().saveConfig();
            LOGGER.info("Sync entity class: {}", entityId);
        } else {
            LOGGER.warn("Entity class already synchronized: {}", entityId);
        }
    }

    public static void asyncEntity(ResourceLocation entityId) {
        if (synchronizedEntities.remove(entityId)) {
            PlatformEvents.getInstance().saveConfig();
            LOGGER.info("Enable async process entity class: {}", entityId);
        } else {
            LOGGER.warn("Entity class not found: {}", entityId);
        }
    }
}
