package com.thewinterframework.velocity;

import com.google.inject.Inject;
import com.thewinterframework.plugin.DataFolder;
import com.thewinterframework.plugin.WinterBootPlugin;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Plugin(id = "winter-test", name = "Winter Test", version = "1.0.0")
@WinterBootPlugin
public final class FixtureVelocityPlugin extends VelocityWinterPlugin {

  private final List<String> callbacks = new ArrayList<>();

  @Inject
  @DataFolder
  private Path injectedDataDirectory;

  @Inject
  public FixtureVelocityPlugin(
      final ProxyServer server,
      final Logger logger,
      final @DataDirectory Path dataDirectory
  ) {
    super(server, logger, dataDirectory);
  }

  @Override
  public void onPluginLoad() {
    callbacks.add("load");
  }

  @Override
  public void onPluginEnable() {
    callbacks.add("enable");
  }

  @Override
  public void onPluginDisable() {
    callbacks.add("disable");
  }

  public List<String> callbacks() {
    return List.copyOf(callbacks);
  }

  public Path injectedDataDirectory() {
    return injectedDataDirectory;
  }
}
