package com.thewinterframework.service.decorator.lifecycle;

import com.thewinterframework.service.annotation.lifecycle.OnReload;
import com.thewinterframework.service.meta.lifecycle.RunnableLifeCycleMethod;
import org.jetbrains.annotations.NotNull;

public class OnReloadDecoratorHandler extends LifeCycleDecoratorHandler<OnReload> {
	@Override
	public Class<OnReload> getAnnotationType() {
		return OnReload.class;
	}

	@Override
	protected Class<?>[] extractAfterAnnotations(OnReload annotation) {
		return annotation.after();
	}

	@Override
	protected Class<?>[] extractBeforeAnnotations(OnReload annotation) {
		return annotation.before();
	}

	/**
	 * Add a runnable to the on reload graph.
	 *
	 * @param service The service to add the runnable to.
	 * @param method  The method to add.
	 */
	public void addReloadMethod(final @NotNull Class<?> service, final @NotNull Runnable method) {
		graph.addNode(new RunnableLifeCycleMethod(service, method, new Class[]{}, new Class[]{}));
	}
}
