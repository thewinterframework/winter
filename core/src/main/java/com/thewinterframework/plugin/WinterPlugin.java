package com.thewinterframework.plugin;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.thewinterframework.plugin.module.PluginModuleManager;
import com.thewinterframework.utils.TimeUnit;
import org.slf4j.Logger;

public interface WinterPlugin extends Module {

	@Override
	default void configure(Binder binder) {
		// Override this handle to bind your classes
	}

	default void onPluginLoad() {
		// Override this handle to perform actions when the plugin is loaded
	}

	default void onPluginEnable() {
		// Override this handle to perform actions when the plugin is enabled
	}

	default void onPluginDisable() {
		// Override this handle to perform actions when the plugin is disabled
	}

	Injector getInjector();

	Logger getSLF4JLogger();

	PluginModuleManager getModuleManager();

	//TODO: Move to SchedulerManager or something like that
	int scheduleRepeatingTask(Runnable task, long delay, long period, TimeUnit unit, boolean async);

	//TODO: Move to SchedulerManager or something like that
	void cancelTask(int taskId);

}
