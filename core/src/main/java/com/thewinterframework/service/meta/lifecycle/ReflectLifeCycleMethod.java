package com.thewinterframework.service.meta.lifecycle;

import com.thewinterframework.utils.reflect.AnnotatedMethodHandle;

/**
 * Represents a lifecycle method that is reflected.
 */
public record ReflectLifeCycleMethod(
		Class<?> service,
		AnnotatedMethodHandle<?> method,
		Class<?>[] before,
		Class<?>[] after
) implements LifeCycleMethod {}
