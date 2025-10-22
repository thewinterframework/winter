package com.thewinterframework.service.decorator.scheduler;

import com.thewinterframework.plugin.WinterPlugin;
import com.thewinterframework.service.decorator.ServiceDecoratorHandler;
import com.thewinterframework.service.meta.scheduler.SchedulerMethod;
import com.thewinterframework.utils.reflect.AnnotatedMethodHandle;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public abstract class SchedulerDecoratorHandler<A extends Annotation> implements ServiceDecoratorHandler<A> {

	private final List<SchedulerMethod> registeredMethods = new ArrayList<>();

	@Override
	public void onDiscover(Class<?> service, AnnotatedMethodHandle<A> method) {
		final var annotation = method.annotation();
		registeredMethods.add(map(service, annotation, method));
	}

	protected abstract SchedulerMethod map(Class<?> service, A annotation, AnnotatedMethodHandle<A> method);

	@Override
	public void onPluginEnable(WinterPlugin plugin) {
		for (final var method : registeredMethods) {
			method.schedule(plugin);
		}
	}
}
