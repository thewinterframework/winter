package com.thewinterframework.plugin;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.thewinterframework.plugin.module.PluginModuleManager;
import com.thewinterframework.utils.TimeUnit;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.key.Namespaced;
import org.slf4j.Logger;

/**
 * This class should be extended by any plugin that is used by the WinterBoot plugin.
 */
public interface WinterPlugin extends Module, Namespaced {

	@Override
	default void configure(Binder binder) {
		// Override this handle to bind your classes
	}

	/**
	 * Called when the plugin is loaded
	 * <br><b>NOTE: Plugin injections is not available at this point</b>
	 */
	default void onPluginLoad() {
		// Override this handle to perform actions when the plugin is loaded
	}

	/**
	 * Called when the plugin is enabled
	 * <br><b>NOTE: Plugin injections is available at this point</b>
	 */
	default void onPluginEnable() {
		// Override this handle to perform actions when the plugin is enabled
	}

	/**
	 * Called when the plugin is disabled
	 * <br><b>NOTE: Plugin injections is available at this point</b>
	 */
	default void onPluginDisable() {
		// Override this handle to perform actions when the plugin is disabled
	}

	/**
	 * Returns the plugin injector
	 * @return the plugin injector
	 */
	Injector getInjector();

	/**
	 * Returns the plugin logger
	 * @return the plugin logger
	 */
	Logger getSLF4JLogger();

	/**
	 * Returns the plugin module manager
	 * @return the plugin module manager
	 */
	PluginModuleManager getModuleManager();

	/**
	 * Schedules a task to run a/synchronously
	 * @param task the task to run
	 * @param delay the delay before the task starts
	 * @param unit the time unit of the delay
	 * @param async whether the task should run asynchronously
	 * @return the task id
	 */
	//TODO: Move to SchedulerManager or something like that
	int scheduleRepeatingTask(Runnable task, long delay, long period, TimeUnit unit, boolean async);

	/**
	 * Cancels a task
	 * @param taskId the task id
	 */
	//TODO: Move to SchedulerManager or something like that
	void cancelTask(int taskId);

}
