package com.thewinterframework.service.decorator.scheduler;

import com.thewinterframework.service.annotation.scheduler.ScheduledAtContainer;
import com.thewinterframework.service.meta.scheduler.ScheduledAtMethod;
import com.thewinterframework.service.meta.scheduler.SchedulerMethod;
import com.thewinterframework.utils.reflect.AnnotatedMethodHandle;

import java.util.Arrays;

public class ScheduledAtDecoratorHandler extends SchedulerDecoratorHandler<ScheduledAtContainer> {
	@Override
	protected SchedulerMethod map(Class<?> service, ScheduledAtContainer annotation, AnnotatedMethodHandle<ScheduledAtContainer> method) {
		final var schedules = Arrays.stream(annotation.value())
				.map(scheduledAt -> new ScheduledAtMethod.ScheduledAtTime(scheduledAt.hour(), scheduledAt.minute(), scheduledAt.second(), scheduledAt.async()))
				.toList();

		return new ScheduledAtMethod(service, method, schedules);
	}

	@Override
	public Class<ScheduledAtContainer> getAnnotationType() {
		return ScheduledAtContainer.class;
	}
}
