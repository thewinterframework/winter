package com.thewinterframework.plugin.lifecycle;

import com.thewinterframework.plugin.WinterPlugin;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PluginLifecycleTest {

  @Test
  void runsInPriorityOrderAndReportsFailure() {
    final var events = new ArrayList<String>();
    final var plugin = (WinterPlugin) Proxy.newProxyInstance(
        getClass().getClassLoader(), new Class<?>[]{WinterPlugin.class},
        (proxy, method, arguments) -> null);
    final var early = new RecordingInterceptor(0, "early", events);
    final var late = new RecordingInterceptor(10, "late", events);
    final var lifecycle = new PluginLifecycle(plugin, List.of(late, early));

    assertThrows(IllegalStateException.class,
        () -> lifecycle.run(PluginPhase.LOAD, () -> {
          events.add("action");
          throw new IllegalStateException("failed");
        }));
    assertEquals(List.of("early-before", "late-before", "action", "early-failure", "late-failure"), events);

    events.clear();
    org.junit.jupiter.api.Assertions.assertDoesNotThrow(
        () -> lifecycle.run(PluginPhase.ENABLE, () -> events.add("action")));
    assertEquals(List.of("early-before", "late-before", "action", "late-after", "early-after"), events);
  }

  private record RecordingInterceptor(int order, String name, List<String> events)
      implements PluginLifecycleInterceptor {

    @Override
    public void before(final PluginPhase phase, final WinterPlugin plugin) {
      events.add(name + "-before");
    }

    @Override
    public void after(final PluginPhase phase, final WinterPlugin plugin) {
      events.add(name + "-after");
    }

    @Override
    public void onFailure(final PluginPhase phase, final WinterPlugin plugin, final Throwable failure) {
      events.add(name + "-failure");
    }
  }
}
