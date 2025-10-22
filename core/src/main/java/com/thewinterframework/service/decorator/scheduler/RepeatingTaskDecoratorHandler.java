package com.thewinterframework.service.decorator.scheduler;

import com.thewinterframework.service.annotation.scheduler.RepeatingTask;
import com.thewinterframework.service.meta.scheduler.RepeatingTaskMethod;
import com.thewinterframework.service.meta.scheduler.SchedulerMethod;
import com.thewinterframework.utils.reflect.AnnotatedMethodHandle;

public class RepeatingTaskDecoratorHandler extends SchedulerDecoratorHandler<RepeatingTask> {
	@Override
	protected SchedulerMethod map(Class<?> service, RepeatingTask annotation, AnnotatedMethodHandle<RepeatingTask> method) {
		return new RepeatingTaskMethod(
				service,
				method,
				annotation.delay(),
				annotation.every(),
				annotation.unit(),
				annotation.async()
		);
	}

	@Override
	public Class<RepeatingTask> getAnnotationType() {
		return RepeatingTask.class;
	}
}
