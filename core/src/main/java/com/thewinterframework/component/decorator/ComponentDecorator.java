package com.thewinterframework.component.decorator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Marks an annotation as a decorator for any unified component. */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ComponentDecorator {
	Class<? extends ComponentDecoratorHandler<?>> value();
}
