package com.thewinterframework.service.annotation.lifecycle;

import com.thewinterframework.service.annotation.Service;
import com.thewinterframework.service.decorator.ServiceDecorator;
import com.thewinterframework.service.decorator.lifecycle.OnDisableDecoratorHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a method should be called when the plugin is disabled.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ServiceDecorator(OnDisableDecoratorHandler.class)
public @interface OnDisable {

	/**
	 * This indicates the priority of the method.
	 * @return The classes to call before this method
	 */
	Class<?>[] before() default {};
}