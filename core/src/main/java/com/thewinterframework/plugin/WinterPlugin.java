package com.thewinterframework.plugin;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.thewinterframework.expression.AnnotationExpressionResolver;
import com.thewinterframework.plugin.module.PluginModuleManager;
import com.thewinterframework.scheduler.PluginScheduler;
import net.kyori.adventure.key.Namespaced;
import org.slf4j.Logger;

import java.time.ZoneId;

/**
 * This class should be extended by any plugin that is used by the WinterBoot annotation.
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
	 * Returns the annotation expression resolver
	 * @return the annotation expression resolver
	 */
	AnnotationExpressionResolver getExpressionResolver();

	/**
	 * Sets the annotation expression resolver
	 * @param resolver the annotation expression resolver
	 */
	void setExpressionResolver(final AnnotationExpressionResolver resolver);

	/**
	 * Returns the plugin zone id
	 * @return the plugin zone id
	 */
	default ZoneId getZoneId() {
		return ZoneId.systemDefault();
	}

	/**
	 * Returns the task scheduler
	 * @return the task scheduler
	 */
	PluginScheduler getScheduler();

}
