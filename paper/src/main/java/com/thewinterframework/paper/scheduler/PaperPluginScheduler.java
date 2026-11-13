package com.thewinterframework.paper.scheduler;

import com.thewinterframework.scheduler.PluginScheduler;
import com.thewinterframework.utils.TimeUnit;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/** Uses Paper's shared Folia schedulers; sync tasks run on the global region. */
public class PaperPluginScheduler implements PluginScheduler {

  private final JavaPlugin plugin;
  private final AtomicInteger nextId = new AtomicInteger();
  private final ConcurrentHashMap<Integer, CompletableFuture<ScheduledTask>> tasks = new ConcurrentHashMap<>();

  public PaperPluginScheduler(final JavaPlugin plugin) {
    this.plugin = Objects.requireNonNull(plugin, "plugin");
  }

  @Override
  public Executor getMainThreadExecutor() {
    return this::runSync;
  }

  @Override
  public void cancelAllTasks() {
    Bukkit.getGlobalRegionScheduler().cancelTasks(plugin);
    Bukkit.getAsyncScheduler().cancelTasks(plugin);
    tasks.keySet().forEach(this::cancelTask);
  }

  @Override
  public void cancelTask(final int taskId) {
    final var task = tasks.remove(taskId);
    if (task != null) {
      task.thenAccept(ScheduledTask::cancel);
    }
  }

  @Override
  public int runAsync(final Runnable runnable) {
    return schedule(runnable, false, task -> Bukkit.getAsyncScheduler().runNow(plugin, task));
  }

  @Override
  public int runSync(final Runnable runnable) {
    return schedule(runnable, false, task -> Bukkit.getGlobalRegionScheduler().run(plugin, task));
  }

  @Override
  public int runAtFixedRateAsync(final Runnable runnable, final long initialDelay, final long intervalTicks, final TimeUnit unit) {
    final long delay = millis(initialDelay, unit);
    final long period = Math.max(50, millis(intervalTicks, unit));
    return schedule(runnable, true, task -> Bukkit.getAsyncScheduler().runAtFixedRate(
        plugin, task, delay, period, java.util.concurrent.TimeUnit.MILLISECONDS));
  }

  @Override
  public int runAtFixedRateSync(final Runnable runnable, final long initialDelay, final long intervalTicks, final TimeUnit unit) {
    final long delay = Math.max(1, unit.toTicks(initialDelay));
    final long period = Math.max(1, unit.toTicks(intervalTicks));
    return schedule(runnable, true, task -> Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, task, delay, period));
  }

  @Override
  public int runDelayedAsync(final Runnable runnable, final long delayTicks, final TimeUnit unit) {
    final long delay = millis(delayTicks, unit);
    if (delay == 0) {
      return runAsync(runnable);
    }
    return schedule(runnable, false, task -> Bukkit.getAsyncScheduler().runDelayed(
        plugin, task, delay, java.util.concurrent.TimeUnit.MILLISECONDS));
  }

  @Override
  public int runDelayedSync(final Runnable runnable, final long delayTicks, final TimeUnit unit) {
    final long delay = Math.max(1, unit.toTicks(delayTicks));
    return schedule(runnable, false, task -> Bukkit.getGlobalRegionScheduler().runDelayed(plugin, task, delay));
  }

  @Override
  public void ensureSync(final Runnable runnable) {
    if (Bukkit.isGlobalTickThread()) {
      runnable.run();
    } else {
      runSync(runnable);
    }
  }

  @Override
  public <T> CompletableFuture<T> getSync(final Supplier<T> supplier) {
    final var future = new CompletableFuture<T>();
    final Runnable task = () -> {
      try {
        future.complete(supplier.get());
      } catch (final Throwable failure) {
        future.completeExceptionally(failure);
      }
    };
    try {
      ensureSync(task);
    } catch (final Throwable failure) {
      future.completeExceptionally(failure);
    }
    return future;
  }

  private int schedule(final Runnable runnable, final boolean repeating,
      final Function<Consumer<ScheduledTask>, ScheduledTask> submit) {
    Objects.requireNonNull(runnable, "runnable");
    final int id = nextId.incrementAndGet();
    final var handle = new CompletableFuture<ScheduledTask>();
    tasks.put(id, handle);
    try {
      handle.complete(submit.apply(task -> {
        try {
          runnable.run();
        } catch (final RuntimeException | Error failure) {
          task.cancel();
          tasks.remove(id, handle);
          throw failure;
        } finally {
          if (!repeating) {
            tasks.remove(id, handle);
          }
        }
      }));
    } catch (final RuntimeException | Error failure) {
      tasks.remove(id, handle);
      handle.completeExceptionally(failure);
      throw failure;
    }
    return id;
  }

  private long millis(final long amount, final TimeUnit unit) {
    return Math.multiplyExact(Math.max(0, unit.toTicks(amount)), 50);
  }
}
