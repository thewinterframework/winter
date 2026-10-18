package com.thewinterframework.service.decorator.scheduler;

import com.thewinterframework.service.annotation.scheduler.ScheduledAt;
import com.thewinterframework.service.meta.scheduler.ScheduledAtMethod;
import com.thewinterframework.service.meta.scheduler.SchedulerMethod;
import com.thewinterframework.utils.reflect.AnnotatedMethodHandle;

import java.util.List;

/** Handles a single schedule, which Java does not wrap in the repeatable container. */
public class SingleScheduledAtDecoratorHandler extends SchedulerDecoratorHandler<ScheduledAt> {
	@Override
	protected SchedulerMethod map(final Class<?> service, final ScheduledAt annotation, final AnnotatedMethodHandle<ScheduledAt> method) {
		return new ScheduledAtMethod(service, method, List.of(new ScheduledAtMethod.ScheduledAtTime(
				annotation.hour(), annotation.minute(), annotation.second(), annotation.async())));
	}

	@Override
	public Class<ScheduledAt> getAnnotationType() {
		return ScheduledAt.class;
	}
}
