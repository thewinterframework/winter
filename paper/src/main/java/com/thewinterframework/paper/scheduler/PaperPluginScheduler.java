package com.thewinterframework.paper.scheduler;

import com.thewinterframework.scheduler.PluginScheduler;
import com.thewinterframework.utils.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public class PaperPluginScheduler implements PluginScheduler {

	private final JavaPlugin plugin;

	public PaperPluginScheduler(final JavaPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public Executor getMainThreadExecutor() {
		return Bukkit.getScheduler().getMainThreadExecutor(plugin);
	}

	@Override
	public void cancelAllTasks() {
		Bukkit.getScheduler().cancelTasks(plugin);
	}

	@Override
	public void cancelTask(final int taskId) {
		Bukkit.getScheduler().cancelTask(taskId);
	}

	@Override
	public int runAsync(final Runnable runnable) {
		return Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable).getTaskId();
	}

	@Override
	public int runSync(final Runnable runnable) {
		return Bukkit.getScheduler().runTask(plugin, runnable).getTaskId();
	}

	@Override
	public int runAtFixedRateAsync(final Runnable runnable, final long initialDelay, final long intervalTicks, final TimeUnit unit) {
		return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, unit.toTicks(initialDelay), unit.toTicks(intervalTicks)).getTaskId();
	}

	@Override
	public int runAtFixedRateSync(final Runnable runnable, final long initialDelay, final long intervalTicks, final TimeUnit unit) {
		return Bukkit.getScheduler().runTaskTimer(plugin, runnable, unit.toTicks(initialDelay), unit.toTicks(intervalTicks)).getTaskId();
	}

	@Override
	public int runDelayedAsync(final Runnable runnable, final long delayTicks, final TimeUnit unit) {
		return Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, unit.toTicks(delayTicks)).getTaskId();
	}

	@Override
	public int runDelayedSync(final Runnable runnable, final long delayTicks, final TimeUnit unit) {
		return Bukkit.getScheduler().runTaskLater(plugin, runnable, unit.toTicks(delayTicks)).getTaskId();
	}

	@Override
	public void ensureSync(final Runnable runnable) {
		if (Bukkit.isPrimaryThread()) {
			runnable.run();
		} else {
			runSync(runnable);
		}
	}

	@Override
	public <T> CompletableFuture<T> getSync(final Supplier<T> supplier) {
		if (Bukkit.isPrimaryThread()) {
			return CompletableFuture.completedFuture(supplier.get());
		} else {
			final var future = new CompletableFuture<T>();
			runSync(() -> {
				try {
					future.complete(supplier.get());
				} catch (final Throwable e) {
					future.completeExceptionally(e);
				}
			});
			return future;
		}
	}
}
