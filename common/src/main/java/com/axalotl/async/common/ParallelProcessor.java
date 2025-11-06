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
import net.minecraft.world.level.*;
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
import java.util.concurrent.locks.LockSupport;

public class ParallelProcessor {
    public static final Logger LOGGER = LogManager.getLogger(ParallelProcessor.class);

    @Getter
    @Setter
    private static MinecraftServer server;

    public static final AtomicInteger currentEntities = new AtomicInteger();
    private static final AtomicInteger threadPoolID = new AtomicInteger();
    public static ExecutorService workPool;
    public static ExecutorService chunkIOPool;
    private static final BlockingQueue<CompletableFuture<?>> taskQueue = new LinkedBlockingQueue<>();
    private static final Set<UUID> blacklistedEntity = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Integer> portalTickSyncMap = new ConcurrentHashMap<>();
    private static final Map<String, Set<WeakReference<Thread>>> mcThreadTracker = new ConcurrentHashMap<>();
    public static final Set<Class<?>> BLOCKED_ENTITIES = Set.of(
            FallingBlockEntity.class,
            Shulker.class,
            Boat.class
    );

    public static void setupWorkPool(int parallelism, Class<?> asyncClass) {
        workPool = createNamedForkJoinPool("Work", parallelism, Thread.NORM_PRIORITY, asyncClass);
        LOGGER.info("Initialized Work Pool with {} threads", parallelism);
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
                .anyMatch(thread::equals);
    }

    public static boolean isServerExecutionThread() {
        return isThreadInPool(Thread.currentThread());
    }

    public static void callEntityTick(ServerLevel world, Entity entity) {
        if (shouldTickSynchronously(entity)) {
            tickSynchronously(world, entity);
        } else {
            if (!workPool.isShutdown() && !workPool.isTerminated()) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() ->
                        performAsyncEntityTick(world, entity), workPool
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
        return AsyncConfig.disabled ||
                entity instanceof Projectile ||
                entity instanceof AbstractMinecart ||
                entity instanceof ServerPlayer ||
                BLOCKED_ENTITIES.contains(entity.getClass()) ||
                blacklistedEntity.contains(entity.getUUID()) ||
                AsyncConfig.synchronizedEntities.contains(EntityType.getKey(entity.getType()));
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
            portalTickSyncMap.put(entityId, 39);
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
        if (!AsyncConfig.disabled && AsyncConfig.enableAsyncSpawn) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> NaturalSpawner.spawnForChunk(level, chunk, spawnState, categories), ParallelProcessor.workPool).exceptionally(e -> {
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
        if (!AsyncConfig.disabled && AsyncConfig.enableAsyncSpawn) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(entity::checkDespawn, workPool
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
        if (AsyncConfig.disabled) return;
        List<CompletableFuture<?>> futuresList = new ArrayList<>();
        CompletableFuture<?> future;
        while ((future = taskQueue.poll()) != null) {
            futuresList.add(future);
        }

        CompletableFuture<?> allTasks = CompletableFuture.allOf(
                futuresList.toArray(new CompletableFuture[0])
        );

        allTasks.exceptionally(ex -> {
            Throwable cause = ex instanceof java.util.concurrent.CompletionException
                    ? ex.getCause() : ex;
            LOGGER.error("Error during entity tick processing: ", cause);
            return null;
        });

        server.managedBlock(allTasks::isDone);
    }

    public static void setupChunkIOPool(int paraMax, Class<?> asyncClass) {
        chunkIOPool = createNamedForkJoinPool("Chunk-IO", paraMax, Thread.NORM_PRIORITY - 1, asyncClass);
        LOGGER.info("Initialized Chunk IO Pool with {} threads", paraMax);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void stop() {
        if (workPool != null) {
            LOGGER.info("Shutting down Async workPool...");
            workPool.shutdown();
        }
        if (chunkIOPool != null) {
            LOGGER.info("Shutting down Async chunkIOPool...");
            chunkIOPool.shutdown();
        }

        try {
            if (workPool != null && !workPool.awaitTermination(60L, TimeUnit.SECONDS)) {
                LOGGER.warn("Async workPool did not terminate in 60 seconds.");
            }
            if (chunkIOPool != null && !chunkIOPool.awaitTermination(60L, TimeUnit.SECONDS)) {
                LOGGER.warn("Async chunkIOPool did not terminate in 60 seconds.");
            }
        } catch (InterruptedException ignored) {
            LOGGER.error("Thread pool shutdown interrupted.");
        }

        mcThreadTracker.clear();
    }

    private static void logEntityError(String message, Entity entity, Throwable e) {
        LOGGER.error("{} Entity Type: {}, UUID: {}", message, entity.getType().toString(), entity.getUUID(), e);
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