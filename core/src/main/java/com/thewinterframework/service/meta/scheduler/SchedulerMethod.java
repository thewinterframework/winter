package com.thewinterframework.service.meta.scheduler;

import com.thewinterframework.plugin.WinterPlugin;

/**
 * Represents a method that can be scheduled.
 */
public interface SchedulerMethod {

	/**
	 * Schedules this method to be executed by the plugin.
	 *
	 * @param plugin Plugin to schedule this method for
	 * @return The created schedule
	 */
	int schedule(WinterPlugin plugin);

}
