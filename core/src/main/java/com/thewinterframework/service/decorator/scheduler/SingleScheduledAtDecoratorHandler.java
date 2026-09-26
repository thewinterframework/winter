package com.thewinterframework.service.decorator.scheduler;

import com.thewinterframework.service.annotation.scheduler.ScheduledAt;
import com.thewinterframework.service.meta.scheduler.ScheduledAtMethod;
import com.thewinterframework.service.meta.scheduler.SchedulerMethod;
import com.thewinterframework.utils.reflect.AnnotatedMethodHandle;

import java.util.List;

/** Handles the non-container form of a repeatable {@link ScheduledAt} annotation. */
public final class SingleScheduledAtDecoratorHandler extends SchedulerDecoratorHandler<ScheduledAt> {

	@Override
	protected SchedulerMethod map(
			final Class<?> component,
			final ScheduledAt annotation,
			final AnnotatedMethodHandle<ScheduledAt> method
	) {
		final var schedule = new ScheduledAtMethod.ScheduledAtTime(
				annotation.hour(),
				annotation.minute(),
				annotation.second(),
				annotation.async()
		);
		return new ScheduledAtMethod(component, method, List.of(schedule));
	}

	@Override
	public Class<ScheduledAt> getAnnotationType() {
		return ScheduledAt.class;
	}
}
