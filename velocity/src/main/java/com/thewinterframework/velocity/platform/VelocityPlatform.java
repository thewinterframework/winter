package com.thewinterframework.velocity.platform;

import com.thewinterframework.plugin.platform.PlatformPluginManager;
import com.velocitypowered.api.proxy.ProxyServer;

import java.util.Objects;

/** Resolves loaded Velocity plugins by their plugin ID. */
public final class VelocityPlatform implements PlatformPluginManager {

  private final ProxyServer server;

  /**
   * Creates a plugin lookup backed by the given proxy.
   *
   * @param server the proxy server
   */
  public VelocityPlatform(final ProxyServer server) {
    this.server = Objects.requireNonNull(server, "server");
  }

  @Override
  public boolean isPluginEnabled(final String pluginId) {
    return server.getPluginManager().getPlugin(pluginId).isPresent();
  }
}
