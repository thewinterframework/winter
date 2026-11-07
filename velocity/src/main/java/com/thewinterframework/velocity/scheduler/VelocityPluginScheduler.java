package com.thewinterframework.velocity.scheduler;

import com.thewinterframework.scheduler.PluginScheduler;
import com.thewinterframework.utils.TimeUnit;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/** Adapts Winter tasks to Velocity's asynchronous scheduler. */
public final class VelocityPluginScheduler implements PluginScheduler {

  private final ProxyServer server;
  private final Object plugin;
  private final AtomicInteger nextId = new AtomicInteger();
  private final AtomicBoolean closed = new AtomicBoolean();
  private final ConcurrentHashMap<Integer, ScheduledTask> tasks = new ConcurrentHashMap<>();

  /**
   * Creates a scheduler for tasks owned by the given Velocity plugin instance.
   *
   * @param server the proxy server
   * @param plugin the owning plugin instance
   */
  public VelocityPluginScheduler(final ProxyServer server, final Object plugin) {
    this.server = Objects.requireNonNull(server, "server");
    this.plugin = Objects.requireNonNull(plugin, "plugin");
  }

  /** Velocity has no main scheduler thread; this executor runs on the calling thread. */
  @Override
  public Executor getMainThreadExecutor() {
    return Runnable::run;
  }

  @Override
  public void cancelAllTasks() {
    closed.set(true);
    tasks.values().forEach(ScheduledTask::cancel);
    server.getScheduler().tasksByPlugin(plugin).forEach(ScheduledTask::cancel);
    tasks.clear();
  }

  @Override
  public void cancelTask(final int taskId) {
    final var task = tasks.remove(taskId);
    if (task != null) {
      task.cancel();
    }
  }

  @Override
  public int runAsync(final Runnable runnable) {
    return schedule(runnable, null, null);
  }

  /** Velocity has no main thread; scheduled sync tasks use its regular scheduler. */
  @Override
  public int runSync(final Runnable runnable) {
    return runAsync(runnable);
  }

  @Override
  public int runAtFixedRateAsync(
      final Runnable runnable,
      final long initialDelay,
      final long interval,
      final TimeUnit unit
  ) {
    return schedule(runnable, duration(initialDelay, unit, false), duration(interval, unit, true));
  }

  @Override
  public int runAtFixedRateSync(
      final Runnable runnable,
      final long initialDelay,
      final long interval,
      final TimeUnit unit
  ) {
    return runAtFixedRateAsync(runnable, initialDelay, interval, unit);
  }

  @Override
  public int runDelayedAsync(final Runnable runnable, final long delay, final TimeUnit unit) {
    return schedule(runnable, duration(delay, unit, false), null);
  }

  @Override
  public int runDelayedSync(final Runnable runnable, final long delay, final TimeUnit unit) {
    return runDelayedAsync(runnable, delay, unit);
  }

  @Override
  public void ensureSync(final Runnable runnable) {
    Objects.requireNonNull(runnable, "runnable").run();
  }

  @Override
  public <T> CompletableFuture<T> getSync(final Supplier<T> supplier) {
    try {
      return CompletableFuture.completedFuture(Objects.requireNonNull(supplier, "supplier").get());
    } catch (final Throwable failure) {
      return CompletableFuture.failedFuture(failure);
    }
  }

  private int schedule(final Runnable runnable, final Duration delay, final Duration repeat) {
    Objects.requireNonNull(runnable, "runnable");
    if (closed.get()) {
      throw new IllegalStateException("Velocity plugin scheduler is closed");
    }
    final int id = nextId.incrementAndGet();
    final var finished = new AtomicBoolean();
    var builder = server.getScheduler().buildTask(plugin, () -> {
      try {
        runnable.run();
      } finally {
        if (repeat == null) {
          finished.set(true);
          tasks.remove(id);
        }
      }
    });
    if (delay != null && !delay.isZero()) {
      builder = builder.delay(delay);
    }
    if (repeat != null) {
      builder = builder.repeat(repeat);
    }
    final var scheduledTask = builder.schedule();
    tasks.put(id, scheduledTask);
    if (finished.get()) {
      tasks.remove(id, scheduledTask);
    }
    if (closed.get()) {
      scheduledTask.cancel();
      tasks.remove(id, scheduledTask);
    }
    return id;
  }

  private Duration duration(final long amount, final TimeUnit unit, final boolean positive) {
    Objects.requireNonNull(unit, "unit");
    if (amount < 0 || (positive && amount == 0)) {
      throw new IllegalArgumentException(positive ? "Interval must be positive" : "Delay cannot be negative");
    }
    return unit.getDuration().multipliedBy(amount);
  }
}
