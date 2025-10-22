package com.thewinterframework.scheduler;

import com.thewinterframework.utils.TimeUnit;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * Interface for scheduling tasks within a plugin environment.
 */
public interface PluginScheduler {

	/**
	 * Gets the executor for running tasks on the main thread.
	 *
	 * @return the main thread executor
	 */
	Executor getMainThreadExecutor();

	/**
	 * Cancels all scheduled tasks.
	 */
	void cancelAllTasks();

	/**
	 * Cancels a specific task by its ID.
	 *
	 * @param taskId the ID of the task to cancel
	 */
	void cancelTask(int taskId);

	/**
	 * Runs a task asynchronously.
	 *
	 * @param runnable the task to run
	 * @return the ID of the scheduled task
	 */
	int runAsync(Runnable runnable);

	/**
	 * Runs a task synchronously.
	 *
	 * @param runnable the task to run
	 * @return the ID of the scheduled task
	 */
	int runSync(Runnable runnable);

	/**
	 * Runs a task at a fixed rate asynchronously.
	 *
	 * @param runnable      the task to run
	 * @param intervalTicks the interval between executions
	 * @param initialDelay the delay before the first execution
	 * @param unit          the time unit of the interval
	 * @return the ID of the scheduled task
	 */
	int runAtFixedRateAsync(Runnable runnable, long initialDelay, long intervalTicks, TimeUnit unit);

	/**
	 * Runs a task at a fixed rate synchronously.
	 *
	 * @param runnable      the task to run
	 * @param intervalTicks the interval between executions
	 * @param initialDelay the delay before the first execution
	 * @param unit          the time unit of the interval
	 * @return the ID of the scheduled task
	 */
	int runAtFixedRateSync(Runnable runnable, long initialDelay, long intervalTicks, TimeUnit unit);

	/**
	 * Runs a task after a delay asynchronously.
	 *
	 * @param runnable   the task to run
	 * @param delayTicks the delay before execution in ticks
	 * @param unit       the time unit of the delay
	 * @return the ID of the scheduled task
	 */
	int runDelayedAsync(Runnable runnable, long delayTicks, TimeUnit unit);

	/**
	 * Runs a task after a delay synchronously.
	 *
	 * @param runnable   the task to run
	 * @param delayTicks the delay before execution in ticks
	 * @param unit       the time unit of the delay
	 * @return the ID of the scheduled task
	 */
	int runDelayedSync(Runnable runnable, long delayTicks, TimeUnit unit);

	/**
	 * Ensures a task runs synchronously
	 *
	 * @param runnable the task to run
	 */
	void ensureSync(Runnable runnable);

	/**
	 * Gets a value ensuring the supplier is executed on the main thread
	 *
	 * @param supplier the supplier to get the value from
	 * @param <T>      the type of the value
	 * @return a CompletableFuture that will complete with the value
	 */
	<T> CompletableFuture<T> getSync(final Supplier<T> supplier);

}
