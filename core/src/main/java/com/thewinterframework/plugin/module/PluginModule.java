package com.thewinterframework.plugin.module;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.thewinterframework.plugin.WinterPlugin;

import java.util.List;

public interface PluginModule extends Module {

	default List<Class<? extends PluginModule>> depends(WinterPlugin plugin) {
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

	default boolean onEnable(WinterPlugin plugin) {
		// Override this handle if you want to do something on plugin enable
		return true;
	}

	default boolean onDisable(WinterPlugin plugin) {
		// Override this handle if you want to do something on plugin disable
		return true;
	}

	@Override
	default void configure(Binder binder) {
		// Override this handle if you want to bind something
	}

}
