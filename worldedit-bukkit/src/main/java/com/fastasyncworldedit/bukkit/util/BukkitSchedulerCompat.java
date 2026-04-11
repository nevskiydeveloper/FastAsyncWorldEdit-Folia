package com.fastasyncworldedit.bukkit.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class BukkitSchedulerCompat {

    @FunctionalInterface
    public interface CancellableTask {
        void cancel();
    }

    private static final boolean REGION_SCHEDULERS_AVAILABLE;
    private static final Method SERVER_GET_REGION_SCHEDULER;
    private static final Method SERVER_GET_ASYNC_SCHEDULER;
    private static final Method SERVER_GET_GLOBAL_REGION_SCHEDULER;
    private static final Method SERVER_IS_OWNED_BY_CURRENT_REGION_LOCATION;
    private static final Method SERVER_IS_OWNED_BY_CURRENT_REGION_WORLD_CHUNK;
    private static final Method SERVER_IS_OWNED_BY_CURRENT_REGION_ENTITY;
    private static final Method SERVER_IS_GLOBAL_TICK_THREAD;
    private static final Method REGION_RUN;
    private static final Method GLOBAL_EXECUTE;
    private static final Method GLOBAL_RUN;
    private static final Method GLOBAL_RUN_DELAYED;
    private static final Method GLOBAL_RUN_AT_FIXED_RATE;
    private static final Method GLOBAL_CANCEL_TASKS;
    private static final Method ASYNC_RUN_NOW;
    private static final Method ASYNC_RUN_DELAYED;
    private static final Method ASYNC_RUN_AT_FIXED_RATE;
    private static final Method ASYNC_CANCEL_TASKS;
    private static final Method ENTITY_GET_SCHEDULER;
    private static final Method ENTITY_RUN;
    private static final Method SCHEDULED_TASK_CANCEL;

    static {
        boolean available = false;
        Method serverGetRegionScheduler = null;
        Method serverGetAsyncScheduler = null;
        Method serverGetGlobalRegionScheduler = null;
        Method serverIsOwnedByCurrentRegionLocation = null;
        Method serverIsOwnedByCurrentRegionWorldChunk = null;
        Method serverIsOwnedByCurrentRegionEntity = null;
        Method serverIsGlobalTickThread = null;
        Method regionRun = null;
        Method globalExecute = null;
        Method globalRun = null;
        Method globalRunDelayed = null;
        Method globalRunAtFixedRate = null;
        Method globalCancelTasks = null;
        Method asyncRunNow = null;
        Method asyncRunDelayed = null;
        Method asyncRunAtFixedRate = null;
        Method asyncCancelTasks = null;
        Method entityGetScheduler = null;
        Method entityRun = null;
        Method scheduledTaskCancel = null;
        try {
            serverGetRegionScheduler = org.bukkit.Server.class.getMethod("getRegionScheduler");
            serverGetAsyncScheduler = org.bukkit.Server.class.getMethod("getAsyncScheduler");
            serverGetGlobalRegionScheduler = org.bukkit.Server.class.getMethod("getGlobalRegionScheduler");
            serverIsOwnedByCurrentRegionLocation = org.bukkit.Server.class.getMethod("isOwnedByCurrentRegion", Location.class);
            serverIsOwnedByCurrentRegionWorldChunk = org.bukkit.Server.class.getMethod(
                    "isOwnedByCurrentRegion",
                    World.class,
                    int.class,
                    int.class
            );
            serverIsOwnedByCurrentRegionEntity = org.bukkit.Server.class.getMethod("isOwnedByCurrentRegion", Entity.class);
            serverIsGlobalTickThread = org.bukkit.Server.class.getMethod("isGlobalTickThread");

            Class<?> regionSchedulerClass = Class.forName("io.papermc.paper.threadedregions.scheduler.RegionScheduler");
            Class<?> globalRegionSchedulerClass = Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
            Class<?> asyncSchedulerClass = Class.forName("io.papermc.paper.threadedregions.scheduler.AsyncScheduler");
            Class<?> entitySchedulerClass = Class.forName("io.papermc.paper.threadedregions.scheduler.EntityScheduler");
            Class<?> scheduledTaskClass = Class.forName("io.papermc.paper.threadedregions.scheduler.ScheduledTask");

            regionRun = regionSchedulerClass.getMethod(
                    "run",
                    Plugin.class,
                    World.class,
                    int.class,
                    int.class,
                    Consumer.class
            );
            globalExecute = globalRegionSchedulerClass.getMethod("execute", Plugin.class, Runnable.class);
            globalRun = globalRegionSchedulerClass.getMethod("run", Plugin.class, Consumer.class);
            globalRunDelayed = globalRegionSchedulerClass.getMethod("runDelayed", Plugin.class, Consumer.class, long.class);
            globalRunAtFixedRate = globalRegionSchedulerClass.getMethod(
                    "runAtFixedRate",
                    Plugin.class,
                    Consumer.class,
                    long.class,
                    long.class
            );
            globalCancelTasks = globalRegionSchedulerClass.getMethod("cancelTasks", Plugin.class);
            asyncRunNow = asyncSchedulerClass.getMethod("runNow", Plugin.class, Consumer.class);
            asyncRunDelayed = asyncSchedulerClass.getMethod(
                    "runDelayed",
                    Plugin.class,
                    Consumer.class,
                    long.class,
                    TimeUnit.class
            );
            asyncRunAtFixedRate = asyncSchedulerClass.getMethod(
                    "runAtFixedRate",
                    Plugin.class,
                    Consumer.class,
                    long.class,
                    long.class,
                    TimeUnit.class
            );
            asyncCancelTasks = asyncSchedulerClass.getMethod("cancelTasks", Plugin.class);
            entityGetScheduler = Entity.class.getMethod("getScheduler");
            entityRun = entitySchedulerClass.getMethod("run", Plugin.class, Consumer.class, Runnable.class);
            scheduledTaskCancel = scheduledTaskClass.getMethod("cancel");
            available = true;
        } catch (ReflectiveOperationException ignored) {
            available = false;
        }
        REGION_SCHEDULERS_AVAILABLE = available;
        SERVER_GET_REGION_SCHEDULER = serverGetRegionScheduler;
        SERVER_GET_ASYNC_SCHEDULER = serverGetAsyncScheduler;
        SERVER_GET_GLOBAL_REGION_SCHEDULER = serverGetGlobalRegionScheduler;
        SERVER_IS_OWNED_BY_CURRENT_REGION_LOCATION = serverIsOwnedByCurrentRegionLocation;
        SERVER_IS_OWNED_BY_CURRENT_REGION_WORLD_CHUNK = serverIsOwnedByCurrentRegionWorldChunk;
        SERVER_IS_OWNED_BY_CURRENT_REGION_ENTITY = serverIsOwnedByCurrentRegionEntity;
        SERVER_IS_GLOBAL_TICK_THREAD = serverIsGlobalTickThread;
        REGION_RUN = regionRun;
        GLOBAL_EXECUTE = globalExecute;
        GLOBAL_RUN = globalRun;
        GLOBAL_RUN_DELAYED = globalRunDelayed;
        GLOBAL_RUN_AT_FIXED_RATE = globalRunAtFixedRate;
        GLOBAL_CANCEL_TASKS = globalCancelTasks;
        ASYNC_RUN_NOW = asyncRunNow;
        ASYNC_RUN_DELAYED = asyncRunDelayed;
        ASYNC_RUN_AT_FIXED_RATE = asyncRunAtFixedRate;
        ASYNC_CANCEL_TASKS = asyncCancelTasks;
        ENTITY_GET_SCHEDULER = entityGetScheduler;
        ENTITY_RUN = entityRun;
        SCHEDULED_TASK_CANCEL = scheduledTaskCancel;
    }

    private BukkitSchedulerCompat() {
    }

    public static boolean hasRegionSchedulers() {
        return REGION_SCHEDULERS_AVAILABLE;
    }

    public static boolean isGlobalTickThread() {
        if (!REGION_SCHEDULERS_AVAILABLE) {
            return Bukkit.isPrimaryThread();
        }
        try {
            return (boolean) SERVER_IS_GLOBAL_TICK_THREAD.invoke(Bukkit.getServer());
        } catch (IllegalAccessException | InvocationTargetException e) {
            return Bukkit.isPrimaryThread();
        }
    }

    public static boolean isOwnedByCurrentRegion(Location location) {
        if (location == null || location.getWorld() == null) {
            return isGlobalTickThread();
        }
        if (!REGION_SCHEDULERS_AVAILABLE) {
            return Bukkit.isPrimaryThread();
        }
        try {
            return (boolean) SERVER_IS_OWNED_BY_CURRENT_REGION_LOCATION.invoke(Bukkit.getServer(), location);
        } catch (IllegalAccessException | InvocationTargetException e) {
            return Bukkit.isPrimaryThread();
        }
    }

    public static boolean isOwnedByCurrentRegion(World world, int chunkX, int chunkZ) {
        if (world == null) {
            return isGlobalTickThread();
        }
        if (!REGION_SCHEDULERS_AVAILABLE) {
            return Bukkit.isPrimaryThread();
        }
        try {
            return (boolean) SERVER_IS_OWNED_BY_CURRENT_REGION_WORLD_CHUNK.invoke(Bukkit.getServer(), world, chunkX, chunkZ);
        } catch (IllegalAccessException | InvocationTargetException e) {
            return Bukkit.isPrimaryThread();
        }
    }

    public static boolean isOwnedByCurrentRegion(Entity entity) {
        if (entity == null) {
            return isGlobalTickThread();
        }
        if (!REGION_SCHEDULERS_AVAILABLE) {
            return Bukkit.isPrimaryThread();
        }
        try {
            return (boolean) SERVER_IS_OWNED_BY_CURRENT_REGION_ENTITY.invoke(Bukkit.getServer(), entity);
        } catch (IllegalAccessException | InvocationTargetException e) {
            return Bukkit.isPrimaryThread();
        }
    }

    public static void executeGlobal(Plugin plugin, Runnable runnable) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(runnable, "runnable");
        if (REGION_SCHEDULERS_AVAILABLE) {
            Object scheduler = invoke(SERVER_GET_GLOBAL_REGION_SCHEDULER, plugin.getServer());
            invoke(GLOBAL_EXECUTE, scheduler, plugin, runnable);
            return;
        }
        plugin.getServer().getScheduler().runTask(plugin, runnable);
    }

    public static <T> T callGlobal(Plugin plugin, Supplier<T> supplier) {
        Objects.requireNonNull(supplier, "supplier");
        if (isGlobalTickThread()) {
            return supplier.get();
        }
        CompletableFuture<T> future = new CompletableFuture<>();
        executeGlobal(plugin, () -> complete(future, supplier));
        return join(future);
    }

    public static CancellableTask runGlobalLater(Plugin plugin, Runnable runnable, long delayTicks) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(runnable, "runnable");
        if (REGION_SCHEDULERS_AVAILABLE) {
            Object scheduler = invoke(SERVER_GET_GLOBAL_REGION_SCHEDULER, plugin.getServer());
            if (delayTicks <= 0L) {
                Object task = invoke(
                        GLOBAL_RUN,
                        scheduler,
                        plugin,
                        consumer(runnable)
                );
                return () -> cancelScheduledTask(task);
            }
            Object task = invoke(
                    GLOBAL_RUN_DELAYED,
                    scheduler,
                    plugin,
                    consumer(runnable),
                    delayTicks
            );
            return () -> cancelScheduledTask(task);
        }
        int taskId = plugin.getServer().getScheduler().runTaskLater(plugin, runnable, delayTicks).getTaskId();
        return () -> plugin.getServer().getScheduler().cancelTask(taskId);
    }

    public static CancellableTask runGlobalRepeating(Plugin plugin, Runnable runnable, long delayTicks, long periodTicks) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(runnable, "runnable");
        if (REGION_SCHEDULERS_AVAILABLE) {
            Object scheduler = invoke(SERVER_GET_GLOBAL_REGION_SCHEDULER, plugin.getServer());
            Object task = invoke(
                    GLOBAL_RUN_AT_FIXED_RATE,
                    scheduler,
                    plugin,
                    consumer(runnable),
                    Math.max(1L, delayTicks),
                    Math.max(1L, periodTicks)
            );
            return () -> cancelScheduledTask(task);
        }
        int taskId = plugin.getServer().getScheduler()
                .runTaskTimer(plugin, runnable, delayTicks, Math.max(1L, periodTicks))
                .getTaskId();
        return () -> plugin.getServer().getScheduler().cancelTask(taskId);
    }

    public static void executeAsync(Plugin plugin, Runnable runnable) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(runnable, "runnable");
        if (REGION_SCHEDULERS_AVAILABLE) {
            Object scheduler = invoke(SERVER_GET_ASYNC_SCHEDULER, plugin.getServer());
            invoke(ASYNC_RUN_NOW, scheduler, plugin, consumer(runnable));
            return;
        }
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    public static CancellableTask runAsyncLater(Plugin plugin, Runnable runnable, long delayTicks) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(runnable, "runnable");
        if (REGION_SCHEDULERS_AVAILABLE) {
            Object scheduler = invoke(SERVER_GET_ASYNC_SCHEDULER, plugin.getServer());
            if (delayTicks <= 0L) {
                Object task = invoke(
                        ASYNC_RUN_NOW,
                        scheduler,
                        plugin,
                        consumer(runnable)
                );
                return () -> cancelScheduledTask(task);
            }
            Object task = invoke(
                    ASYNC_RUN_DELAYED,
                    scheduler,
                    plugin,
                    consumer(runnable),
                    ticksToMillis(delayTicks),
                    TimeUnit.MILLISECONDS
            );
            return () -> cancelScheduledTask(task);
        }
        int taskId = plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, runnable, delayTicks).getTaskId();
        return () -> plugin.getServer().getScheduler().cancelTask(taskId);
    }

    public static CancellableTask runAsyncRepeating(Plugin plugin, Runnable runnable, long delayTicks, long periodTicks) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(runnable, "runnable");
        if (REGION_SCHEDULERS_AVAILABLE) {
            Object scheduler = invoke(SERVER_GET_ASYNC_SCHEDULER, plugin.getServer());
            Object task = invoke(
                    ASYNC_RUN_AT_FIXED_RATE,
                    scheduler,
                    plugin,
                    consumer(runnable),
                    ticksToMillis(Math.max(1L, delayTicks)),
                    ticksToMillis(Math.max(1L, periodTicks)),
                    TimeUnit.MILLISECONDS
            );
            return () -> cancelScheduledTask(task);
        }
        int taskId = plugin.getServer().getScheduler()
                .runTaskTimerAsynchronously(plugin, runnable, delayTicks, Math.max(1L, periodTicks))
                .getTaskId();
        return () -> plugin.getServer().getScheduler().cancelTask(taskId);
    }

    public static void executeAtLocation(Plugin plugin, Location location, Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable");
        if (location == null || location.getWorld() == null) {
            executeGlobal(plugin, runnable);
            return;
        }
        if (isOwnedByCurrentRegion(location)) {
            runnable.run();
            return;
        }
        if (REGION_SCHEDULERS_AVAILABLE) {
            Object scheduler = invoke(SERVER_GET_REGION_SCHEDULER, plugin.getServer());
            invoke(
                    REGION_RUN,
                    scheduler,
                    plugin,
                    location.getWorld(),
                    location.getBlockX() >> 4,
                    location.getBlockZ() >> 4,
                    consumer(runnable)
            );
            return;
        }
        executeGlobal(plugin, runnable);
    }

    public static <T> T callAtLocation(Plugin plugin, Location location, Supplier<T> supplier) {
        Objects.requireNonNull(supplier, "supplier");
        if (location == null || location.getWorld() == null) {
            return callGlobal(plugin, supplier);
        }
        if (isOwnedByCurrentRegion(location)) {
            return supplier.get();
        }
        CompletableFuture<T> future = new CompletableFuture<>();
        executeAtLocation(plugin, location, () -> complete(future, supplier));
        return join(future);
    }

    public static CompletableFuture<Void> runAtChunk(Plugin plugin, World world, int chunkX, int chunkZ, Runnable runnable) {
        return supplyAtChunk(plugin, world, chunkX, chunkZ, () -> {
            runnable.run();
            return null;
        });
    }

    public static void executeAtChunk(Plugin plugin, World world, int chunkX, int chunkZ, Runnable runnable) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(runnable, "runnable");
        if (world == null) {
            executeGlobal(plugin, runnable);
            return;
        }
        if (isOwnedByCurrentRegion(world, chunkX, chunkZ)) {
            runnable.run();
            return;
        }
        if (!REGION_SCHEDULERS_AVAILABLE) {
            executeGlobal(plugin, runnable);
            return;
        }
        Object scheduler = invoke(SERVER_GET_REGION_SCHEDULER, plugin.getServer());
        invoke(
                REGION_RUN,
                scheduler,
                plugin,
                world,
                chunkX,
                chunkZ,
                consumer(runnable)
        );
    }

    public static <T> CompletableFuture<T> supplyAtChunk(
            Plugin plugin,
            World world,
            int chunkX,
            int chunkZ,
            Supplier<T> supplier
    ) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(supplier, "supplier");
        if (world == null) {
            return CompletableFuture.completedFuture(callGlobal(plugin, supplier));
        }
        if (isOwnedByCurrentRegion(world, chunkX, chunkZ)) {
            try {
                return CompletableFuture.completedFuture(supplier.get());
            } catch (Throwable t) {
                return CompletableFuture.failedFuture(t);
            }
        }
        if (!REGION_SCHEDULERS_AVAILABLE) {
            return CompletableFuture.completedFuture(callGlobal(plugin, supplier));
        }
        CompletableFuture<T> future = new CompletableFuture<>();
        Object scheduler = invoke(SERVER_GET_REGION_SCHEDULER, plugin.getServer());
        invoke(
                REGION_RUN,
                scheduler,
                plugin,
                world,
                chunkX,
                chunkZ,
                consumer(future, supplier)
        );
        return future;
    }

    public static <T> T callAtChunk(Plugin plugin, World world, int chunkX, int chunkZ, Supplier<T> supplier) {
        return join(supplyAtChunk(plugin, world, chunkX, chunkZ, supplier));
    }

    public static void executeEntity(Plugin plugin, Entity entity, Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable");
        if (entity == null) {
            executeGlobal(plugin, runnable);
            return;
        }
        if (isOwnedByCurrentRegion(entity)) {
            runnable.run();
            return;
        }
        if (REGION_SCHEDULERS_AVAILABLE) {
            Object scheduler = invoke(ENTITY_GET_SCHEDULER, entity);
            invoke(ENTITY_RUN, scheduler, plugin, consumer(runnable), (Runnable) () -> {
            });
            return;
        }
        executeGlobal(plugin, runnable);
    }

    public static <T> T callEntity(Plugin plugin, Entity entity, Supplier<T> supplier) {
        Objects.requireNonNull(supplier, "supplier");
        if (entity == null) {
            return callGlobal(plugin, supplier);
        }
        if (isOwnedByCurrentRegion(entity)) {
            return supplier.get();
        }
        if (!REGION_SCHEDULERS_AVAILABLE) {
            return callGlobal(plugin, supplier);
        }
        CompletableFuture<T> future = new CompletableFuture<>();
        Object scheduler = invoke(ENTITY_GET_SCHEDULER, entity);
        invoke(
                ENTITY_RUN,
                scheduler,
                plugin,
                consumer(future, supplier),
                (Runnable) () -> future.completeExceptionally(
                        new IllegalStateException("Entity scheduler retired before task execution.")
                )
        );
        return join(future);
    }

    public static void cancelTasks(Plugin plugin) {
        Objects.requireNonNull(plugin, "plugin");
        if (REGION_SCHEDULERS_AVAILABLE) {
            Object globalScheduler = invoke(SERVER_GET_GLOBAL_REGION_SCHEDULER, plugin.getServer());
            Object asyncScheduler = invoke(SERVER_GET_ASYNC_SCHEDULER, plugin.getServer());
            invoke(GLOBAL_CANCEL_TASKS, globalScheduler, plugin);
            invoke(ASYNC_CANCEL_TASKS, asyncScheduler, plugin);
            return;
        }
        try {
            plugin.getServer().getScheduler().cancelTasks(plugin);
        } catch (UnsupportedOperationException ignored) {
        }
    }

    private static Consumer<Object> consumer(Runnable runnable) {
        return ignored -> runnable.run();
    }

    private static <T> Consumer<Object> consumer(CompletableFuture<T> future, Supplier<T> supplier) {
        return ignored -> complete(future, supplier);
    }

    private static <T> void complete(CompletableFuture<T> future, Supplier<T> supplier) {
        try {
            future.complete(supplier.get());
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }
    }

    private static <T> T join(CompletableFuture<T> future) {
        try {
            return future.join();
        } catch (CompletionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new RuntimeException(cause);
        }
    }

    private static long ticksToMillis(long ticks) {
        return ticks * 50L;
    }

    private static Object invoke(Method method, Object instance, Object... args) {
        try {
            return method.invoke(instance, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static void cancelScheduledTask(Object task) {
        if (task == null) {
            return;
        }
        invoke(SCHEDULED_TASK_CANCEL, task);
    }

}
