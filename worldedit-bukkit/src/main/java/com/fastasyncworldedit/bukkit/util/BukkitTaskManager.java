package com.fastasyncworldedit.bukkit.util;

import com.fastasyncworldedit.core.util.TaskManager;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class BukkitTaskManager extends TaskManager {

    private final Plugin plugin;
    private final AtomicInteger taskIds = new AtomicInteger();
    private final Map<Integer, BukkitSchedulerCompat.CancellableTask> tasks = new ConcurrentHashMap<>();

    public BukkitTaskManager(final Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public int repeat(@Nonnull final Runnable runnable, final int interval) {
        return register(BukkitSchedulerCompat.runGlobalRepeating(this.plugin, runnable, interval, interval));
    }

    @Override
    public int repeatAsync(@Nonnull final Runnable runnable, final int interval) {
        return register(BukkitSchedulerCompat.runAsyncRepeating(this.plugin, runnable, interval, interval));
    }

    @Override
    public void async(@Nonnull final Runnable runnable) {
        BukkitSchedulerCompat.executeAsync(this.plugin, runnable);
    }

    @Override
    public void task(@Nonnull final Runnable runnable) {
        BukkitSchedulerCompat.executeGlobal(this.plugin, runnable);
    }

    @Override
    public void later(@Nonnull final Runnable runnable, final int delay) {
        BukkitSchedulerCompat.runGlobalLater(this.plugin, runnable, delay);
    }

    @Override
    public void laterAsync(@Nonnull final Runnable runnable, final int delay) {
        BukkitSchedulerCompat.runAsyncLater(this.plugin, runnable, delay);
    }

    @Override
    public void cancel(final int task) {
        if (task != -1) {
            BukkitSchedulerCompat.CancellableTask cancellableTask = tasks.remove(task);
            if (cancellableTask != null) {
                cancellableTask.cancel();
            }
        }
    }

    private int register(BukkitSchedulerCompat.CancellableTask task) {
        int id = taskIds.incrementAndGet();
        tasks.put(id, task);
        return id;
    }

}
