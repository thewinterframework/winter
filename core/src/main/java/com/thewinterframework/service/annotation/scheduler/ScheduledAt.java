package com.thewinterframework.service.annotation.scheduler;

import java.lang.annotation.*;

/**
 * Annotates a method to be executed at a fixed time of the day
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ScheduledAtContainer.class)
public @interface ScheduledAt {

	/**
	 * The hour of the day to run the task
	 * @return the hour of the day to run the task
	 */
	int hour();

	/**
	 * The minute of the hour to run the task
	 * @return the minute of the hour to run the task
	 */
	int minute() default 0;

	/**
	 * The second of the minute to run the task
	 * @return the second of the minute to run the task
	 */
	int second() default 0;

	/**
	 * Whether the task should be executed asynchronously...
	 * <p>
	 * If the method has more than one ScheduledAt annotation, if this is set to true, all annotations will be executed asynchronously.
	 * @return whether the task should be executed asynchronously
	 */
	boolean async() default false;

}
