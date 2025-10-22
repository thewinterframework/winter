package com.thewinterframework.service.meta.scheduler;

import com.thewinterframework.plugin.WinterPlugin;
import com.thewinterframework.utils.reflect.AnnotatedMethodHandle;
import com.thewinterframework.utils.TimeUnit;

import static java.util.Objects.requireNonNull;

/**
 * Represents a repeating task method
 * @param service The service class
 * @param method The method to execute
 * @param delay The delay before the task starts
 * @param every The delay between each execution
 * @param unit The time unit of the delay
 * @param async Whether the task should be executed
 */
public record RepeatingTaskMethod(
		Class<?> service,
		AnnotatedMethodHandle<?> method,
		String delay,
		String every,
		String unit,
		String async
) implements SchedulerMethod {
	@Override
	public int schedule(WinterPlugin plugin) {
		final var scheduler = plugin.getScheduler();
		final var expressionResolver = plugin.getExpressionResolver();

		final long delayParsed = requireNonNull(expressionResolver.resolve(method, delay, Long.class));
		final long everyParsed = requireNonNull(expressionResolver.resolve(method, every, Long.class));
		final var unitParsed = requireNonNull(expressionResolver.resolve(method, unit, TimeUnit.class));
		final var asyncParsed = requireNonNull(expressionResolver.resolve(method, async, Boolean.class));

		if (asyncParsed) {
			return scheduler.runAtFixedRateAsync(
					() -> {
						try {
							method.invoke(service, plugin.getInjector());
						} catch (Throwable e) {
							plugin.getSLF4JLogger().error("Error while executing repeating task", e);
						}
					},
					delayParsed,
					everyParsed,
					unitParsed
			);
		}

		return scheduler.runAtFixedRateSync(
				() -> {
					try {
						method.invoke(service, plugin.getInjector());
					} catch (Throwable e) {
						plugin.getSLF4JLogger().error("Error while executing repeating task", e);
					}
				},
				delayParsed,
				everyParsed,
				unitParsed
		);
	}
}
