package com.thewinterframework.service.annotation.lifecycle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method to be executed when the service is reloaded.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OnReload {
	/**
	 * The priority of this method. The higher the priority, the earlier it will be executed
	 * @return The priority of this method
	 */
	Class<?>[] after() default {};

	/**
	 * The priority of this method. The higher the priority, the earlier it will be executed
	 * @return The priority of this method
	 */
	Class<?>[] before() default {};
}
