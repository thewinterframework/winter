package com.thewinterframework.service.meta.scheduler;

import com.thewinterframework.plugin.WinterPlugin;
import com.thewinterframework.utils.TimeUnit;
import com.thewinterframework.utils.reflect.AnnotatedMethodHandle;

import java.time.Instant;
import java.util.List;

import static java.util.Objects.requireNonNull;

public record ScheduledAtMethod(
		Class<?> service,
		AnnotatedMethodHandle<?> method,
		List<ScheduledAtTime> schedules
) implements SchedulerMethod {

	public record ScheduledAtTime(
			String hour,
			String minute,
			String second,
			String async
	) {
	}

	public record ParsedScheduledAtTime(
			int hour,
			int minute,
			int second
	) {
	}

	@Override
	public int schedule(final WinterPlugin plugin) {
		final var scheduler = plugin.getScheduler();
		final var expressionResolver = plugin.getExpressionResolver();

		var shouldBeAsync = false;
		for (final var schedule : schedules) {
			final var asyncParsed = expressionResolver.resolve(schedule.async, Boolean.class);
			if (asyncParsed != null && asyncParsed) {
				shouldBeAsync = true;
				break;
			}
		}

		final var parsedSchedules = schedules.stream()
				.map(schedule -> {
					final var hourParsed = requireNonNull(expressionResolver.resolve(schedule.hour, Integer.class));
					final var minuteParsed = expressionResolver.resolve(schedule.minute, Integer.class);
					final var secondParsed = expressionResolver.resolve(schedule.second, Integer.class);

					return new ParsedScheduledAtTime(
							hourParsed,
							minuteParsed != null ? minuteParsed : 0,
							secondParsed != null ? secondParsed : 0
					);
				})
				.toList();

		final Runnable task = () -> {
			final var pluginTimeZone = plugin.getZoneId();
			final var now = Instant.now().atZone(pluginTimeZone);
			final var isScheduledTime = parsedSchedules.stream()
					.anyMatch(time ->
							time.hour() == now.getHour() &&
									time.minute() == now.getMinute() &&
									time.second() == now.getSecond()
					);

			if (!isScheduledTime) {
				return;
			}

			try {
				method.invoke(service, plugin.getInjector());
			} catch (final Throwable e) {
				plugin.getSLF4JLogger().error("Error while executing repeating task", e);
			}
		};

		if (shouldBeAsync) {
			return scheduler.runAtFixedRateAsync(task, 0, 1, TimeUnit.SECONDS);
		} else {
			return scheduler.runAtFixedRateSync(task, 0, 1, TimeUnit.SECONDS);
		}
	}
}
