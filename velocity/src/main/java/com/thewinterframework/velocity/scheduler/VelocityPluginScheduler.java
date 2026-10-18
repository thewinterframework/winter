package com.thewinterframework.velocity.scheduler;

import com.thewinterframework.scheduler.PluginScheduler;
import com.thewinterframework.utils.TimeUnit;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import com.velocitypowered.api.scheduler.Scheduler;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Adapts Winter task IDs to Velocity task handles. Velocity has no main thread:
 * both sync and async methods execute on its scheduler's thread pool.
 */
public class VelocityPluginScheduler implements PluginScheduler {

	private final Object plugin;
	private final Scheduler scheduler;
	private final AtomicInteger nextId = new AtomicInteger();
	private final Map<Integer, ScheduledTask> tasks = new ConcurrentHashMap<>();

	public VelocityPluginScheduler(final Object plugin, final ProxyServer server) {
		this.plugin = plugin;
		this.scheduler = server.getScheduler();
	}

	@Override
	public Executor getMainThreadExecutor() {
		return this::runSync;
	}

	@Override
	public void cancelAllTasks() {
		scheduler.tasksByPlugin(plugin).forEach(ScheduledTask::cancel);
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
		return schedule(runnable, 0, 0, TimeUnit.MILLIS);
	}

	@Override
	public int runSync(final Runnable runnable) {
		return runAsync(runnable);
	}

	@Override
	public int runAtFixedRateAsync(final Runnable runnable, final long initialDelay, final long interval, final TimeUnit unit) {
		if (interval <= 0) {
			throw new IllegalArgumentException("The repeat interval must be positive");
		}
		return schedule(runnable, initialDelay, interval, unit);
	}

	@Override
	public int runAtFixedRateSync(final Runnable runnable, final long initialDelay, final long interval, final TimeUnit unit) {
		return runAtFixedRateAsync(runnable, initialDelay, interval, unit);
	}

	@Override
	public int runDelayedAsync(final Runnable runnable, final long delay, final TimeUnit unit) {
		return schedule(runnable, delay, 0, unit);
	}

	@Override
	public int runDelayedSync(final Runnable runnable, final long delay, final TimeUnit unit) {
		return runDelayedAsync(runnable, delay, unit);
	}

	@Override
	public void ensureSync(final Runnable runnable) {
		runSync(runnable);
	}

	@Override
	public <T> CompletableFuture<T> getSync(final Supplier<T> supplier) {
		return CompletableFuture.supplyAsync(supplier, getMainThreadExecutor());
	}

	private int schedule(final Runnable runnable, final long delay, final long interval, final TimeUnit unit) {
		if (delay < 0) {
			throw new IllegalArgumentException("The delay cannot be negative");
		}
		final var id = nextId.incrementAndGet();
		final var completed = new AtomicBoolean();
		final var builder = scheduler.buildTask(plugin, () -> {
			try {
				runnable.run();
			} finally {
				if (interval == 0) {
					completed.set(true);
					tasks.remove(id);
				}
			}
		}).delay(unit.getDuration().multipliedBy(delay));
		if (interval > 0) {
			builder.repeat(unit.getDuration().multipliedBy(interval));
		}
		tasks.put(id, builder.schedule());
		// An immediate task can finish before schedule() returns its handle.
		if (completed.get()) {
			tasks.remove(id);
		}
		return id;
	}
}
