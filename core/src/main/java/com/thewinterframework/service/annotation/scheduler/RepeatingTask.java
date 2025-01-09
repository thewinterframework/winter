package com.thewinterframework.service.annotation.scheduler;

import com.thewinterframework.utils.TimeUnit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a handle to be executed repeatedly at a fixed interval
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RepeatingTask {

	/**
	 * The interval between each execution of the handle
	 * @return the interval between each execution of the handle
	 */
	long every();

	/**
	 * The delay before the first execution of the handle
	 * @return the delay before the first execution of the handle
	 */
	long delay() default 0;

	/**
	 * The time unit of the interval
	 * @return the time unit of the interval
	 */
	TimeUnit unit();

	/**
	 * Whether the task should be executed asynchronously
	 * @return whether the task should be executed asynchronously
	 */
	boolean async() default false;

}