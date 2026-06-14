package com.thewinterframework.service.decorator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an annotation as a service decorator.
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceDecorator {
	/**
	 * The handler class that will process the decorator.
	 *
	 * @return The handler class.
	 */
	Class<? extends ServiceDecoratorHandler<?>> value();
}
