package com.thewinterframework.plugin.module;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.thewinterframework.plugin.WinterPlugin;

import java.util.List;

/**
 * This class should be extended by any module that is used by the WinterBoot plugin.
 */
public interface PluginModule extends Module {

	/**
	 * List of modules that this module depends on
	 * @param plugin the plugin instance which is loaded
	 * @return list of modules that this module depends on, and will be loaded before this module
	 */
	default List<Class<? extends PluginModule>> depends(WinterPlugin plugin) {
		return List.of();
	}

	/**
	 * List of modules that this module should be loaded before them
	 * @param plugin the plugin instance which is loaded
	 * @return list of modules that this module should be loaded before them
	 */
	default List<Class<? extends PluginModule>> before(WinterPlugin plugin) {
		return List.of();
	}

	/**
	 * Called when the plugin is loaded
	 * <br><b>NOTE: Plugin injections is not available at this point</b>
	 * @param plugin the plugin instance which is loaded
	 */
	default boolean onLoad(WinterPlugin plugin) {
		// Override this handle if you want to do something on plugin load
		return true;
	}

	/**
	 * Called when the plugin is enabled
	 * <br><b>NOTE: Plugin injections is available at this point</b>
	 * @param plugin the plugin instance which is enabled
	 */
	default boolean onEnable(WinterPlugin plugin) {
		// Override this handle if you want to do something on plugin enable
		return true;
	}

	/**
	 * Called when the plugin is disabled
	 * <br><b>NOTE: Plugin injections is available at this point</b>
	 * @param plugin the plugin instance which is disabled
	 */
	default boolean onDisable(WinterPlugin plugin) {
		// Override this handle if you want to do something on plugin disable
		return true;
	}

	@Override
	default void configure(Binder binder) {
		// Override this handle if you want to bind something
	}

}
