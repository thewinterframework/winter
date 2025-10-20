package com.thewinterframework.service.meta.scheduler;

import com.thewinterframework.plugin.WinterPlugin;
import com.thewinterframework.utils.reflect.Reflections.AnnotatedMethodHandle;
import com.thewinterframework.utils.TimeUnit;

import java.time.Instant;
import java.util.List;

public record ScheduledAtMethod(
		Class<?> service,
		AnnotatedMethodHandle<?> method,
		List<ScheduledAtTime> schedules,
		boolean async
) implements SchedulerMethod {

	public record ScheduledAtTime(
			int hour,
			int minute,
			int second
	) {}

	@Override
	public int schedule(WinterPlugin plugin) {
		return plugin.scheduleRepeatingTask(
				() -> {
					final var pluginTimeZone = plugin.getZoneId();
					final var now = Instant.now().atZone(pluginTimeZone);
					final var isScheduledTime = schedules.stream()
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
					} catch (Throwable e) {
						plugin.getSLF4JLogger().error("Error while executing repeating task", e);
					}
				},
				0,
				1,
				TimeUnit.SECONDS,
				async
		);
	}
}
