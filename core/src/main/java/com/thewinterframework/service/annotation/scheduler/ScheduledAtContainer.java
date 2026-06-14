package com.thewinterframework.service.annotation.scheduler;

import com.thewinterframework.service.decorator.ServiceDecorator;
import com.thewinterframework.service.decorator.scheduler.ScheduledAtDecoratorHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ServiceDecorator(ScheduledAtDecoratorHandler.class)
public @interface ScheduledAtContainer {
	ScheduledAt[] value();
}
