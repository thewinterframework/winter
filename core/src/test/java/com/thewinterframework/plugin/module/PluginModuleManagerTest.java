package com.thewinterframework.plugin.module;

import com.thewinterframework.component.annotation.Component;
import com.thewinterframework.component.annotation.ComponentPriority;
import com.thewinterframework.component.annotation.SyncLoading;
import com.thewinterframework.plugin.WinterPlugin;
import org.junit.jupiter.api.Test;
import org.slf4j.helpers.NOPLogger;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginModuleManagerTest {

  @Test
  void synchronousModuleWaitsForEarlierAsynchronousLoad() {
    final var manager = new PluginModuleManager(plugin());
    final var lifecycleThread = Thread.currentThread();
    SlowModule.loaded = false;
    assertTrue(manager.registerModule(SlowModule.class));
    assertTrue(manager.registerModule(BarrierModule.class));
    assertEquals(List.of(SlowModule.class, BarrierModule.class), manager.asGuiceModules().stream()
        .map(Object::getClass).toList());

    assertTrue(manager.loadModules());
    assertSame(lifecycleThread, BarrierModule.loadThread);
    assertFalse(manager.loadModules());
    assertTrue(manager.disableModules());
    assertFalse(manager.enableModules());
  }

  @Test
  void modulesWithoutAnnotationsLoadOnAWorkerByDefault() {
    final var manager = new PluginModuleManager(plugin());
    final var lifecycleThread = Thread.currentThread();
    assertTrue(manager.registerModule(DefaultAsyncModule.class));

    assertTrue(manager.loadModules());
    assertFalse(DefaultAsyncModule.loadThread == lifecycleThread);
    assertTrue(manager.disableModules());
  }

  @Test
  void failedLoadCleansUpSuccessfullyLoadedModules() {
    final var manager = new PluginModuleManager(plugin());
    LoadedModule.disables.set(0);
    assertTrue(manager.registerModule(LoadedModule.class));
    assertTrue(manager.registerModule(FailingModule.class));

    assertFalse(manager.loadModules());
    assertEquals(1, LoadedModule.disables.get());
    assertTrue(manager.disableModules());
    assertEquals(1, LoadedModule.disables.get());
  }

  @Test
  void disableContinuesAfterOneModuleFails() {
    final var manager = new PluginModuleManager(plugin());
    TailModule.disables.set(0);
    assertTrue(manager.registerModule(TailModule.class));
    assertTrue(manager.registerModule(FailingDisableModule.class));
    assertTrue(manager.loadModules());

    assertFalse(manager.disableModules());
    assertEquals(1, TailModule.disables.get());
  }

  private static WinterPlugin plugin() {
    return (WinterPlugin) Proxy.newProxyInstance(
        WinterPlugin.class.getClassLoader(),
        new Class<?>[]{WinterPlugin.class},
        (proxy, method, arguments) -> method.getName().equals("getSLF4JLogger") ? NOPLogger.NOP_LOGGER : null
    );
  }

  @Component
  @ComponentPriority(-100)
  public static final class SlowModule implements PluginModule {
    private static volatile boolean loaded;

    @Override
    public boolean onLoad(final WinterPlugin plugin) throws InterruptedException {
      TimeUnit.MILLISECONDS.sleep(100);
      loaded = true;
      return true;
    }
  }

  @Component
  @SyncLoading
  @ComponentPriority(100)
  public static final class BarrierModule implements PluginModule {
    private static volatile Thread loadThread;

    @Override
    public boolean onLoad(final WinterPlugin plugin) {
      loadThread = Thread.currentThread();
      return SlowModule.loaded;
    }
  }

  public static final class DefaultAsyncModule implements PluginModule {
    private static volatile Thread loadThread;

    @Override
    public boolean onLoad(final WinterPlugin plugin) {
      loadThread = Thread.currentThread();
      return true;
    }
  }

  @Component
  @ComponentPriority(-100)
  public static final class LoadedModule implements PluginModule {
    private static final AtomicInteger disables = new AtomicInteger();

    @Override
    public boolean onDisable(final WinterPlugin plugin) {
      disables.incrementAndGet();
      return true;
    }
  }

  @Component
  @ComponentPriority(100)
  public static final class FailingModule implements PluginModule {
    @Override
    public boolean onLoad(final WinterPlugin plugin) {
      return false;
    }
  }

  @Component
  @ComponentPriority(-100)
  public static final class TailModule implements PluginModule {
    private static final AtomicInteger disables = new AtomicInteger();

    @Override
    public boolean onDisable(final WinterPlugin plugin) {
      disables.incrementAndGet();
      return true;
    }
  }

  @Component
  @ComponentPriority(100)
  public static final class FailingDisableModule implements PluginModule {
    @Override
    public boolean onDisable(final WinterPlugin plugin) {
      return false;
    }
  }
}
