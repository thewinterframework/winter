package com.thewinterframework.velocity;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.thewinterframework.expression.ExpressionResolver;
import com.thewinterframework.expression.JexlExpressionResolver;
import com.thewinterframework.plugin.DataFolder;
import com.thewinterframework.plugin.WinterDiagnostics;
import com.thewinterframework.plugin.WinterPlugin;
import com.thewinterframework.plugin.lifecycle.PluginLifecycle;
import com.thewinterframework.plugin.lifecycle.PluginPhase;
import com.thewinterframework.plugin.module.PluginModuleManager;
import com.thewinterframework.plugin.platform.PlatformPluginManager;
import com.thewinterframework.scheduler.PluginScheduler;
import com.thewinterframework.velocity.platform.VelocityPlatform;
import com.thewinterframework.velocity.scheduler.VelocityPluginScheduler;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.key.KeyPattern;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Base class for Winter plugins running on Velocity. */
public abstract class VelocityWinterPlugin implements WinterPlugin {

  private final ProxyServer server;
  private final Logger logger;
  private final Path dataDirectory;
  private final PluginModuleManager moduleManager;
  private final PluginLifecycle lifecycle;
  private final PluginScheduler scheduler;
  private final PlatformPluginManager platformManager;

  private ExpressionResolver expressionResolver = new JexlExpressionResolver();
  private Injector injector;
  private boolean initialized;

  /**
   * Called by a subclass constructor that receives Velocity's injected services.
   * Keep platform registration and other proxy operations in the initialization event.
   *
   * @param server the running proxy
   * @param logger this plugin's Velocity logger
   * @param dataDirectory this plugin's data directory
   */
  protected VelocityWinterPlugin(
      final ProxyServer server,
      final Logger logger,
      final @DataDirectory Path dataDirectory
  ) {
    this.server = Objects.requireNonNull(server, "server");
    this.logger = Objects.requireNonNull(logger, "logger");
    this.dataDirectory = Objects.requireNonNull(dataDirectory, "dataDirectory");
    this.moduleManager = new PluginModuleManager(this);
    this.lifecycle = new PluginLifecycle(this);
    this.scheduler = new VelocityPluginScheduler(server, this);
    this.platformManager = new VelocityPlatform(server);
  }

  /**
   * Runs Winter's scan, load, injection, and enable phases after Velocity registers the plugin.
   *
   * @param event the proxy initialization event
   */
  @Subscribe
  public final void onProxyInitialize(final ProxyInitializeEvent event) {
    if (initialized) {
      throw new IllegalStateException("Winter plugin is already initialized");
    }
    final var startTime = System.currentTimeMillis();
    try {
      lifecycle.run(PluginPhase.SCAN, () -> requireSuccess(moduleManager.scanModules(), "scan"));
      lifecycle.run(PluginPhase.LOAD, () -> {
        requireSuccess(moduleManager.loadModules(), "load");
        onPluginLoad();
      });
      lifecycle.run(PluginPhase.INJECTION, () -> {
        injector = createInjector();
        injector.injectMembers(this);
        requireSuccess(moduleManager.injectModules(), "inject");
      });
      lifecycle.run(PluginPhase.ENABLE, () -> {
        requireSuccess(moduleManager.enableModules(), "enable");
        onPluginEnable();
      });
      initialized = true;
      WinterDiagnostics.log(this, "Plugin enabled in {}ms", System.currentTimeMillis() - startTime);
    } catch (final Exception | Error failure) {
      try {
        if (!moduleManager.disableModules()) {
          logger.error("One or more Winter modules failed to disable after initialization failure");
        }
      } catch (final RuntimeException cleanupFailure) {
        failure.addSuppressed(cleanupFailure);
      } finally {
        scheduler.cancelAllTasks();
      }
      logger.error("Winter plugin initialization failed", failure);
      throw new IllegalStateException("Winter plugin initialization failed", failure);
    }
  }

  /**
   * Disables loaded modules and cancels scheduled tasks during proxy shutdown.
   *
   * @param event the proxy shutdown event
   */
  @Subscribe
  public final void onProxyShutdown(final ProxyShutdownEvent event) {
    final var startTime = System.currentTimeMillis();
    try {
      lifecycle.run(PluginPhase.DISABLE, () -> {
        if (!moduleManager.disableModules()) {
          logger.error("One or more Winter modules failed to disable");
        }
        if (initialized) {
          onPluginDisable();
        }
      });
    } catch (final Exception | Error failure) {
      logger.error("Winter plugin shutdown failed", failure);
    } finally {
      scheduler.cancelAllTasks();
      initialized = false;
    }
    WinterDiagnostics.log(this, "Plugin disabled in {}ms", System.currentTimeMillis() - startTime);
  }

  /**
   * Creates the plugin injector after modules have loaded.
   *
   * @return the new injector
   */
  protected Injector createInjector() {
    return Guice.createInjector(getGuiceStage(), getGuiceModules());
  }

  /**
   * Returns platform bindings, loaded Winter modules, and the plugin's own bindings.
   *
   * @return Guice modules in installation order
   */
  protected List<Module> getGuiceModules() {
    final var modules = new ArrayList<Module>();
    modules.add(binder -> {
      binder.bind(WinterPlugin.class).toInstance(this);
      binder.bind(VelocityWinterPlugin.class).toInstance(this);
      binder.bind(ProxyServer.class).toInstance(server);
      binder.bind(Logger.class).toInstance(logger);
      binder.bind(Path.class).annotatedWith(DataFolder.class).toInstance(dataDirectory);
      binder.bind(Path.class).annotatedWith(DataDirectory.class).toInstance(dataDirectory);
    });
    modules.addAll(moduleManager.asGuiceModules());
    modules.add(this);
    return modules;
  }

  /**
   * Returns the Guice stage used to construct the plugin injector.
   *
   * @return the injector stage
   */
  protected Stage getGuiceStage() {
    return Stage.PRODUCTION;
  }

  private void requireSuccess(final boolean successful, final String action) {
    if (!successful) {
      throw new IllegalStateException("Failed to " + action + " Winter modules");
    }
  }

  /**
   * Returns the Velocity proxy injected into this plugin.
   *
   * @return the proxy server
   */
  public final ProxyServer getProxyServer() {
    return server;
  }

  /**
   * Returns the plugin data directory injected by Velocity.
   *
   * @return the data directory
   */
  public final Path getDataDirectory() {
    return dataDirectory;
  }

  @Override
  public final Injector getInjector() {
    return injector;
  }

  @Override
  public final @NotNull Logger getSLF4JLogger() {
    return logger;
  }

  @Override
  public final PluginModuleManager getModuleManager() {
    return moduleManager;
  }

  @Override
  public final PluginScheduler getScheduler() {
    return scheduler;
  }

  @Override
  public final PlatformPluginManager getPlatformManager() {
    return platformManager;
  }

  @Override
  public final ExpressionResolver getExpressionResolver() {
    return expressionResolver;
  }

  @Override
  public final void setExpressionResolver(final ExpressionResolver resolver) {
    expressionResolver = Objects.requireNonNull(resolver, "resolver");
  }

  @Override
  @KeyPattern.Namespace
  public final @NotNull String namespace() {
    final var metadata = getClass().getAnnotation(Plugin.class);
    if (metadata == null) {
      throw new IllegalStateException("VelocityWinterPlugin subclasses must declare @Plugin");
    }
    return metadata.id();
  }
}
