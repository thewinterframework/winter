package com.thewinterframework.service.decorator.lifecycle;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.thewinterframework.plugin.WinterPlugin;
import com.thewinterframework.service.decorator.ServiceDecoratorHandler;
import com.thewinterframework.service.meta.lifecycle.LifeCycleMethod;
import com.thewinterframework.service.meta.lifecycle.ReflectLifeCycleMethod;
import com.thewinterframework.service.meta.lifecycle.RunnableLifeCycleMethod;
import com.thewinterframework.utils.graph.DfsGraph;
import com.thewinterframework.utils.reflect.AnnotatedMethodHandle;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;

public abstract class LifeCycleDecoratorHandler<A extends Annotation> implements ServiceDecoratorHandler<A> {

	protected final Multimap<Class<?>, LifeCycleMethod> lifeCycleMethodsByService = HashMultimap.create();
	protected final DfsGraph<LifeCycleMethod> graph = new DfsGraph<>();

	@Override
	public final void onDiscover(Class<?> service, AnnotatedMethodHandle<A> method) {
		final var reflect = new ReflectLifeCycleMethod(service, method, extractBeforeAnnotations(method.annotation()), extractAfterAnnotations(method.annotation()));
		lifeCycleMethodsByService.put(service, reflect);
	}

	@Override
	public final void onPluginLoad(WinterPlugin plugin) {
		for (final var entry : lifeCycleMethodsByService.entries()) {
			final var lifeCycleMethod = entry.getValue();

			for (final var beforeService : lifeCycleMethod.before()) {
				for (final var beforeMethod : lifeCycleMethodsByService.get(beforeService)) {
					graph.addBefore(lifeCycleMethod, beforeMethod);
				}
			}

			for (final var afterService : lifeCycleMethod.after()) {
				for (final var afterMethod : lifeCycleMethodsByService.get(afterService)) {
					graph.addAfter(lifeCycleMethod, afterMethod);
				}
			}
		}
	}

	protected void executeInternally(final WinterPlugin plugin) {
		final var result = execute(plugin);
		if (!result.result()) {
			plugin.getSLF4JLogger().error("An error occurred while executing lifecycle methods", result.throwable());
		}
	}

	/**
	 * Executes the lifecycle methods in the correct order.
	 *
	 * @param plugin The plugin
	 * @return The result of the execution
	 */
	public LifeCycleResult execute(final WinterPlugin plugin) {
		final var injector = plugin.getInjector();
		final var iterator = graph.iterator();
		try {
			while (iterator.hasNext()) {
				final var meta = iterator.next();
				if (meta instanceof ReflectLifeCycleMethod reflectLifeCycleMethod) {
					final var method = reflectLifeCycleMethod.method();
					method.invoke(reflectLifeCycleMethod.service(), injector);
				} else if (meta instanceof RunnableLifeCycleMethod runnableLifeCycleMethod) {
					runnableLifeCycleMethod.method().run();
				}
			}
		} catch (Throwable throwable) {
			return new LifeCycleResult(false, throwable);
		}

		return new LifeCycleResult(true, null);
	}

	public record LifeCycleResult(boolean result, @Nullable Throwable throwable) {}

	protected abstract Class<?>[] extractAfterAnnotations(A annotation);

	protected abstract Class<?>[] extractBeforeAnnotations(A annotation);
}
