package com.thewinterframework.service.meta.lifecycle;

import com.thewinterframework.utils.Reflections.AnnotatedMethodHandle;

/**
 * Represents a lifecycle method.
 */
public record LifeCycleMethod(
		Class<?> service,
		AnnotatedMethodHandle<?> method,
		Class<?>[] before,
		Class<?>[] after
) {}
