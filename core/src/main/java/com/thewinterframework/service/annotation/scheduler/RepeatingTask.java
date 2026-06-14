package com.thewinterframework.service.annotation.scheduler;

import com.thewinterframework.service.decorator.ServiceDecorator;
import com.thewinterframework.service.decorator.scheduler.RepeatingTaskDecoratorHandler;
import com.thewinterframework.service.decorator.scheduler.SchedulerDecoratorHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a handle to be executed repeatedly at a fixed interval
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ServiceDecorator(RepeatingTaskDecoratorHandler.class)
public @interface RepeatingTask {

	/**
	 * The interval between each execution of the handle
	 * @return the interval between each execution of the handle
	 */
	String every();

	/**
	 * The delay before the first execution of the handle
	 * @return the delay before the first execution of the handle
	 */
	String delay() default "0";

	/**
	 * The time unit of the interval
	 * @return the time unit of the interval
	 */
	String unit() default "MILLISECONDS";

	/**
	 * Whether the task should be executed asynchronously
	 * @return whether the task should be executed asynchronously
	 */
	String async() default "false";

}