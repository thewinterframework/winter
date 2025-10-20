package com.thewinterframework.service.annotation.lifecycle;

import com.thewinterframework.service.decorator.ServiceDecorator;
import com.thewinterframework.service.decorator.lifecycle.OnEnableDecoratorHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method to be executed when the service is enabled.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ServiceDecorator(OnEnableDecoratorHandler.class)
public @interface OnEnable {

	/**
	 * The priority of this method. The higher the priority, the earlier it will be executed
	 * @return The priority of this method
	 */
	Class<?>[] after() default {};
}