package com.thewinterframework.velocity.platform;

import com.thewinterframework.plugin.platform.PlatformPluginManager;
import com.velocitypowered.api.proxy.ProxyServer;

/**
 * Velocity identifies plugins by ID and has no separate enabled/disabled state.
 */
public class VelocityPlatform implements PlatformPluginManager {

	private final ProxyServer server;

	public VelocityPlatform(final ProxyServer server) {
		this.server = server;
	}

	@Override
	public boolean isPluginPresent(final String pluginId) {
		return server.getPluginManager().getPlugin(pluginId).isPresent();
	}

	@Override
	public boolean isPluginEnabled(final String pluginId) {
		return server.getPluginManager().getPlugin(pluginId)
				.flatMap(container -> container.getInstance()).isPresent();
	}
}
