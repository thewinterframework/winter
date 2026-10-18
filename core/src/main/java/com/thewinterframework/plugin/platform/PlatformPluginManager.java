package com.thewinterframework.plugin.platform;

public interface PlatformPluginManager {

	boolean isPluginEnabled(final String pluginName);

	/**
	 * Checks whether a plugin is available during Winter's load phase, before
	 * platforms such as Paper enable their plugins.
	 */
	default boolean isPluginPresent(final String pluginName) {
		return isPluginEnabled(pluginName);
	}

}
