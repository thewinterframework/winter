package com.thewinterframework.velocity;

import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.Scheduler;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class VelocityWinterPluginTest {

  @Test
  void initializesAndShutsDownWithGeneratedModuleProvider() {
    final var scheduler = proxy(Scheduler.class, (method, args) -> {
      if (method.equals("tasksByPlugin")) {
        return List.of();
      }
      throw new UnsupportedOperationException(method);
    });
    final var server = proxy(ProxyServer.class, (method, args) -> {
      if (method.equals("getScheduler")) {
        return scheduler;
      }
      throw new UnsupportedOperationException(method);
    });
    final var directory = Path.of("build", "velocity-test-data");
    final var plugin = new FixtureVelocityPlugin(server, LoggerFactory.getLogger("winter-velocity-test"), directory);

    assertEquals("winter-test", plugin.namespace());
    plugin.onProxyInitialize(new ProxyInitializeEvent());

    assertEquals(List.of("load", "enable"), plugin.callbacks());
    assertNotNull(plugin.getInjector());
    assertSame(server, plugin.getInjector().getInstance(ProxyServer.class));
    assertEquals(directory, plugin.injectedDataDirectory());

    plugin.onProxyShutdown(new ProxyShutdownEvent());
    assertEquals(List.of("load", "enable", "disable"), plugin.callbacks());
  }

  private static <T> T proxy(final Class<T> type, final Invocation invocation) {
    return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
        (instance, method, args) -> invocation.call(method.getName(), args)));
  }

  @FunctionalInterface
  private interface Invocation {
    Object call(String method, Object[] args);
  }
}
