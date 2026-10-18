package com.thewinterframework.paper.platform;

import com.thewinterframework.plugin.platform.PlatformPluginManager;
import org.bukkit.Bukkit;

public class PaperPlatform implements PlatformPluginManager {
	@Override
	public boolean isPluginPresent(final String pluginName) {
		return Bukkit.getPluginManager().getPlugin(pluginName) != null;
	}

	@Override
	public boolean isPluginEnabled(final String pluginName) {
		return Bukkit.getPluginManager().isPluginEnabled(pluginName);
	}
}
