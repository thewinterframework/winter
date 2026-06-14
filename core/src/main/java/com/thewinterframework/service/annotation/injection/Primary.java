package com.thewinterframework.service.annotation.injection;

import com.thewinterframework.service.decorator.ServiceDecorator;
import com.thewinterframework.service.decorator.injection.PrimaryHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ServiceDecorator(PrimaryHandler.class)
public @interface Primary {
	Class<?> value() default Void.class;
}