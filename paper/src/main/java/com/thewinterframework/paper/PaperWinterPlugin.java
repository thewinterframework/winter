package com.thewinterframework.paper;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.thewinterframework.expression.ExpressionResolver;
import com.thewinterframework.expression.JexlExpressionResolver;
import com.thewinterframework.paper.platform.PaperPlatform;
import com.thewinterframework.paper.scheduler.PaperPluginScheduler;
import com.thewinterframework.plugin.DataFolder;
import com.thewinterframework.plugin.WinterPlugin;
import com.thewinterframework.plugin.WinterDiagnostics;
import com.thewinterframework.plugin.lifecycle.PluginLifecycle;
import com.thewinterframework.plugin.lifecycle.PluginPhase;
import com.thewinterframework.plugin.module.PluginModuleManager;
import com.thewinterframework.plugin.platform.PlatformPluginManager;
import com.thewinterframework.scheduler.PluginScheduler;
import net.kyori.adventure.key.KeyPattern;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * A base class for Winter plugins that use Paper.
 */
public abstract class PaperWinterPlugin extends JavaPlugin implements WinterPlugin {

  protected final PluginModuleManager moduleManager = new PluginModuleManager(this);
  private final PluginLifecycle lifecycle = new PluginLifecycle(this);
  protected final PluginScheduler scheduler = new PaperPluginScheduler(this);
  protected final PlatformPluginManager platformPluginManager = new PaperPlatform();

  protected ExpressionResolver expressionResolver = new JexlExpressionResolver();

  protected @Inject Injector injector;
  private boolean modulesLoaded;

  @Override
  public final void onLoad() {
    final var startTime = System.currentTimeMillis();
    try {
      lifecycle.run(PluginPhase.SCAN, () -> requireSuccess(moduleManager.scanModules(), "scan"));
      lifecycle.run(PluginPhase.LOAD, () -> {
        requireSuccess(moduleManager.loadModules(), "load");
        modulesLoaded = true;
        onPluginLoad();
      });
      WinterDiagnostics.log(this, "Plugin loaded in {}ms", System.currentTimeMillis() - startTime);
    } catch (final Exception exception) {
      getSLF4JLogger().error("Plugin load failed", exception);
      modulesLoaded = false;
      moduleManager.disableModules();
      Bukkit.getPluginManager().disablePlugin(this);
    }
  }

  @Override
  public final void onEnable() {
    final var startTime = System.currentTimeMillis();
    if (!modulesLoaded) {
      getSLF4JLogger().error("Cannot enable plugin because its modules did not load");
      Bukkit.getPluginManager().disablePlugin(this);
      return;
    }
    try {
      lifecycle.run(PluginPhase.INJECTION, () -> {
        final var injector = createInjector();
        injector.injectMembers(this);
        requireSuccess(moduleManager.injectModules(), "inject");
      });
      lifecycle.run(PluginPhase.ENABLE, () -> {
        requireSuccess(moduleManager.enableModules(), "enable");
        onPluginEnable();
      });
      WinterDiagnostics.log(this, "Plugin enabled in {}ms", System.currentTimeMillis() - startTime);
    } catch (final Exception exception) {
      getSLF4JLogger().error("Plugin enable failed", exception);
      Bukkit.getPluginManager().disablePlugin(this);
    }
  }

  protected Injector createInjector() {
    return Guice.createInjector(getGuiceStage(), getGuiceModules());
  }

  @Override
  public final void onDisable() {
    final var startTime = System.currentTimeMillis();
    try {
      lifecycle.run(PluginPhase.DISABLE, () -> {
        if (!moduleManager.disableModules()) {
          getSLF4JLogger().error("One or more modules failed to disable");
        }
        if (modulesLoaded && injector != null) {
          onPluginDisable();
        }
      });
    } catch (final Exception exception) {
      getSLF4JLogger().error("Plugin disable failed", exception);
    } finally {
      scheduler.cancelAllTasks();
    }

    WinterDiagnostics.log(this, "Plugin disabled in {}ms", System.currentTimeMillis() - startTime);
  }

  private void requireSuccess(final boolean successful, final String action) {
    if (!successful) {
      throw new IllegalStateException("Failed to " + action + " Winter modules");
    }
  }

  @Override
  public PluginScheduler getScheduler() {
    return scheduler;
  }

  @Override
  public ExpressionResolver getExpressionResolver() {
    return expressionResolver;
  }

  @Override
  public void setExpressionResolver(final ExpressionResolver resolver) {
    this.expressionResolver = Objects.requireNonNull(resolver, "resolver");
  }

  @Override
  public Injector getInjector() {
    return injector;
  }

  @Override
  public @NotNull Logger getSLF4JLogger() {
    return super.getSLF4JLogger();
  }

  @Override
  public final PluginModuleManager getModuleManager() {
    return moduleManager;
  }

  @Override
  public PlatformPluginManager getPlatformManager() {
    return platformPluginManager;
  }

  protected List<Module> getGuiceModules() {
    final var modules = new ArrayList<Module>();
    modules.add(binder -> {
      binder.bind(WinterPlugin.class).toInstance(this);
      binder.bind(Plugin.class).toInstance(this);
      binder.bind(JavaPlugin.class).toInstance(this);
      binder.bind(Logger.class).toInstance(this.getSLF4JLogger());
      binder.bind(Path.class).annotatedWith(DataFolder.class).toInstance(this.getDataFolder().toPath());
    });

    modules.addAll(moduleManager.asGuiceModules());
    modules.add(this);

    return modules;
  }

  protected Stage getGuiceStage() {
    return Stage.PRODUCTION;
  }

  @Override
  @KeyPattern.Namespace
  public @NotNull String namespace() {
    return this.getName().toLowerCase(Locale.ROOT);
  }
}
