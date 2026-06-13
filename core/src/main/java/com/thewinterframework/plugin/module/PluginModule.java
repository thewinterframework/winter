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
	default List<Class<? extends PluginModule>> depends(final WinterPlugin plugin) {
		return List.of();
	}

	/**
	 * List of modules that this module should be loaded before them
	 * @param plugin the plugin instance which is loaded
	 * @return list of modules that this module should be loaded before them
	 */
	default List<Class<? extends PluginModule>> before(final WinterPlugin plugin) {
		return List.of();
	}

	/**
	 * Called when the plugin is loaded
	 * <br><b>NOTE: Plugin injections are not available at this point</b>
	 * @param plugin the plugin instance which is loaded
	 */
	default boolean onLoad(final WinterPlugin plugin) throws Exception {
		// Override this handle if you want to do something on a plugin load
		return true;
	}

	/**
	 * Called when the plugin is enabled
	 * <br><b>NOTE: Plugin injections are available at this point</b>
	 * @param plugin the plugin instance which is enabled
	 */
	default boolean onEnable(final WinterPlugin plugin) throws Exception {
		// Override this handle if you want to do something on plugin enabled
		return true;
	}

	/**
	 * Called when the plugin is disabled
	 * <br><b>NOTE: Plugin injections are available at this point</b>
	 * @param plugin the plugin instance which is disabled
	 */
	default boolean onDisable(final WinterPlugin plugin) throws Exception {
		// Override this handle if you want to do something on plugin disabled
		return true;
	}

	@Override
	default void configure(final Binder binder) {
		// Override this handle if you want to bind something
	}

}
