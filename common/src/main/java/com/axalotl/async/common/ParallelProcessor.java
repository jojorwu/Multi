package com.axalotl.async.common;

import com.axalotl.async.common.config.AsyncConfig;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.LevelChunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ParallelProcessor {
    public static final Logger LOGGER = LogManager.getLogger(ParallelProcessor.class);

    @Getter
    @Setter
    private static MinecraftServer server;

    public static final AtomicInteger currentEntities = new AtomicInteger();
    private static final AtomicInteger threadPoolID = new AtomicInteger();
    public static ExecutorService tickPool;
    public static ExecutorService chunkIOPool;
    public static ExecutorService chunkGenPool;
    private static final BlockingQueue<CompletableFuture<?>> taskQueue = new LinkedBlockingQueue<>();
    private static final Set<UUID> blacklistedEntity = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Integer> portalTickSyncMap = new ConcurrentHashMap<>();
    private static final Map<String, ExecutorService> managedPools = new ConcurrentHashMap<>();
    private static final Map<String, Set<WeakReference<Thread>>> mcThreadTracker = new ConcurrentHashMap<>();
    private static final int PORTAL_SYNC_TICKS = 39;
    public static final Set<Class<?>> BLOCKED_ENTITIES = Set.of(
            FallingBlockEntity.class,
            Shulker.class,
            Boat.class
    );

    public static void setupThreadPool(int parallelism, Class<?> asyncClass) {
        tickPool = createThreadPool("Tick", parallelism, Thread.NORM_PRIORITY, asyncClass);
    }

    private static ForkJoinPool createThreadPool(String name, int parallelism, int priority, Class<?> asyncClass) {
        ForkJoinPool pool = createNamedForkJoinPool(name, parallelism, priority, asyncClass);
        managedPools.put(name, pool);
        LOGGER.info("Initialized {} Pool with {} threads", name, parallelism);
        return pool;
    }

    public static void registerThread(String poolName, Thread thread) {
        mcThreadTracker
                .computeIfAbsent(poolName, key -> ConcurrentHashMap.newKeySet())
                .add(new WeakReference<>(thread));
    }

    private static boolean isThreadInPool(Thread thread) {
        return mcThreadTracker.values().stream()
                .flatMap(Set::stream)
                .map(WeakReference::get)
                .filter(java.util.Objects::nonNull)
                .anyMatch(thread::equals);
    }

    public static boolean isServerExecutionThread() {
        return isThreadInPool(Thread.currentThread());
    }

    public static void callEntityTick(ServerLevel world, Entity entity) {
        if (shouldTickSynchronously(entity)) {
            tickSynchronously(world, entity);
        } else {
            if (!tickPool.isShutdown() && !tickPool.isTerminated()) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() ->
                        performAsyncEntityTick(world, entity), tickPool
                ).exceptionally(e -> {
                    logEntityError("Error in async tick, switching to synchronous", entity, e);
                    tickSynchronously(world, entity);
                    blacklistedEntity.add(entity.getUUID());
                    return null;
                });
                taskQueue.add(future);
            } else {
                logEntityError("Rejected task due to ExecutorService shutdown", entity, null);
                tickSynchronously(world, entity);
            }
        }
    }

    public static boolean shouldTickSynchronously(Entity entity) {
        if (entity.level().isClientSide() || isSyncTickRequired(entity)) {
            return true;
        }
        return handlePortalSync(entity);
    }

    private static boolean isSyncTickRequired(Entity entity) {
        if (AsyncConfig.isDisabled()) {
            return true;
        }
        if (entity instanceof Projectile || entity instanceof AbstractMinecart || entity instanceof ServerPlayer) {
            return true;
        }
        if (BLOCKED_ENTITIES.stream().anyMatch(blockedClass -> blockedClass.isAssignableFrom(entity.getClass()))) {
            return true;
        }
        return blacklistedEntity.contains(entity.getUUID())
                || AsyncConfig.synchronizedEntities.contains(EntityType.getKey(entity.getType()));
    }

    private static boolean handlePortalSync(Entity entity) {
        UUID entityId = entity.getUUID();
        if (portalTickSyncMap.containsKey(entityId)) {
            int ticksLeft = portalTickSyncMap.get(entityId);
            if (ticksLeft > 0) {
                portalTickSyncMap.put(entityId, ticksLeft - 1);
                return true;
            } else {
                portalTickSyncMap.remove(entityId);
            }
        }
        if (isPortalTickRequired(entity)) {
            portalTickSyncMap.put(entityId, PORTAL_SYNC_TICKS);
            return true;
        }
        return false;
    }

    private static boolean isPortalTickRequired(Entity entity) {
        return entity.portalProcess != null && entity.portalProcess.isInsidePortalThisTick();
    }

    private static void tickSynchronously(ServerLevel world, Entity entity) {
        try {
            world.tickNonPassenger(entity);
        } catch (Exception e) {
            logEntityError("Error during synchronous tick", entity, e);
        }
    }

    private static void performAsyncEntityTick(ServerLevel world, Entity entity) {
        currentEntities.incrementAndGet();
        try {
            world.tickNonPassenger(entity);
        } finally {
            currentEntities.decrementAndGet();
        }
    }

    public static void asyncSpawnForChunk(ServerLevel level, LevelChunk chunk, NaturalSpawner.SpawnState spawnState, List<MobCategory> categories) {
        if (!AsyncConfig.isDisabled() && AsyncConfig.isEnableAsyncSpawn()) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> NaturalSpawner.spawnForChunk(level, chunk, spawnState, categories), ParallelProcessor.tickPool).exceptionally(e -> {
                ParallelProcessor.LOGGER.error("Error in async spawn, switching to synchronous", e);
                NaturalSpawner.spawnForChunk(level, chunk, spawnState, categories);
                return null;
            });
            taskQueue.add(future);
        } else {
            NaturalSpawner.spawnForChunk(level, chunk, spawnState, categories);
        }
    }

    public static void asyncDespawn(Entity entity) {
        if (!AsyncConfig.isDisabled() && AsyncConfig.isEnableAsyncSpawn()) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(entity::checkDespawn, tickPool
            ).exceptionally(e -> {
                LOGGER.error("Error in async spawn tick, switching to synchronous", e);
                entity.checkDespawn();
                return null;
            });
            taskQueue.add(future);
        } else {
            entity.checkDespawn();
        }
    }

    public static void postEntityTick() {
        if (AsyncConfig.isDisabled()) return;
        List<CompletableFuture<?>> futuresList = new ArrayList<>();
        taskQueue.drainTo(futuresList);

        CompletableFuture<?> allTasks = CompletableFuture.allOf(
                futuresList.toArray(new CompletableFuture[0])
        );

        allTasks.exceptionally(ex -> {
            Throwable cause = ex instanceof java.util.concurrent.CompletionException
                    ? ex.getCause() : ex;
            LOGGER.error("Error during entity tick processing: ", cause);
            return null;
        });

        server.managedBlock(() -> {
            allTasks.join();
            return true;
        });
    }

    public static void setupChunkIOPool(int paraMax, Class<?> asyncClass) {
        chunkIOPool = createThreadPool("Chunk-IO", paraMax, Thread.NORM_PRIORITY - 1, asyncClass);
    }

    public static void setupChunkGenPool(int paraMax, Class<?> asyncClass) {
        chunkGenPool = createThreadPool("Chunk-Gen", paraMax, Thread.NORM_PRIORITY - 1, asyncClass);
    }

    public static void stop() {
        managedPools.forEach((name, pool) -> {
            LOGGER.info("Shutting down Async {} Pool...", name);
            pool.shutdown();
        });

        managedPools.forEach((name, pool) -> {
            try {
                if (!pool.awaitTermination(60L, TimeUnit.SECONDS)) {
                    LOGGER.warn("Async {} Pool did not terminate in 60 seconds.", name);
                }
            } catch (InterruptedException ignored) {
                LOGGER.error("Thread pool shutdown interrupted for {} Pool.", name);
            }
        });

        managedPools.clear();
        mcThreadTracker.clear();
    }

    private static void logEntityError(String message, Entity entity, Throwable e) {
        String entityType = entity.getType() != null ? entity.getType().toString() : "null";
        LOGGER.error("{} Entity Type: {}, UUID: {}", message, entityType, entity.getUUID(), e);
    }

    private static ForkJoinPool createNamedForkJoinPool(String poolName, int parallelism, int priority, Class<?> asyncClass) {
        ForkJoinPool.ForkJoinWorkerThreadFactory threadFactory = pool -> {
            ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
            worker.setName("Async-" + poolName + "-Pool-Thread-" + threadPoolID.getAndIncrement());
            registerThread("Async-" + poolName, worker);
            worker.setDaemon(true);
            worker.setPriority(priority);
            worker.setContextClassLoader(asyncClass.getClassLoader());
            return worker;
        };
        return new ForkJoinPool(parallelism, threadFactory, (t, e) ->
                LOGGER.error("Uncaught exception in thread {}: {}", t.getName(), e), true);
    }
}
