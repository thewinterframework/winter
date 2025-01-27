package com.thewinterframework.service.meta.lifecycle;

/**
 * Represents a lifecycle method that is a runnable.
 */
public record RunnableLifeCycleMethod(
		Class<?> service,
		Runnable method,
		Class<?>[] before,
		Class<?>[] after
) implements LifeCycleMethod {}
