package com.thewinterframework.service.meta.lifecycle;

import com.thewinterframework.utils.Reflections.AnnotatedMethodHandle;

/**
 * Represents a lifecycle method.
 */
public interface LifeCycleMethod {

	/**
	 * The service that this method is associated with.
	 * @return The service
	 */
	Class<?> service();

	/**
	 * The services that should be called before this method.
	 * @return The method
	 */
	Class<?>[] before();

	/**
	 * The services that should be called after this method.
	 * @return The method
	 */
	Class<?>[] after();
}
