package com.thewinterframework.wire.module;

import com.google.inject.Injector;
import com.thewinterframework.component.annotation.Component;
import com.thewinterframework.plugin.WinterPlugin;
import com.thewinterframework.plugin.module.Stage;
import com.thewinterframework.wire.condition.context.ConditionContext;
import org.junit.jupiter.api.Test;
import org.slf4j.helpers.NOPLogger;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbstractProcessorModuleTest {

  @Test
  void disablesOnlyEnabledComponentsInReverseOrder() throws Exception {
    final var module = new RecordingModule(false);
    final var plugin = plugin();
    assertTrue(module.onLoad(plugin));
    assertTrue(module.onEnable(plugin));
    assertTrue(module.onDisable(plugin));
    assertEquals(List.of(Second.class, First.class), module.disabled);
  }

  @Test
  void continuesCleanupAfterAComponentFails() throws Exception {
    final var module = new RecordingModule(true);
    final var plugin = plugin();
    module.onLoad(plugin);
    module.onEnable(plugin);

    assertThrows(IllegalStateException.class, () -> module.onDisable(plugin));
    assertEquals(List.of(Second.class, First.class), module.disabled);
  }

  private static WinterPlugin plugin() {
    return (WinterPlugin) Proxy.newProxyInstance(
        WinterPlugin.class.getClassLoader(),
        new Class<?>[]{WinterPlugin.class},
        (proxy, method, arguments) -> method.getName().equals("getSLF4JLogger") ? NOPLogger.NOP_LOGGER : null
    );
  }

  private static final class RecordingModule extends AbstractProcessorModule {
    private final List<Class<?>> disabled = new ArrayList<>();
    private final boolean failOnSecond;

    private RecordingModule(final boolean failOnSecond) {
      super(Component.class);
      this.failOnSecond = failOnSecond;
      this.wire = () -> List.of(First.class, Second.class, Skipped.class);
    }

    @Override
    protected boolean evaluateConditions(
        final AnnotatedElement componentClass,
        final Stage targetStage,
        final ConditionContext context,
        final Injector injector
    ) {
      return targetStage != Stage.ENABLE || componentClass != Skipped.class;
    }

    @Override
    protected void disableComponent(final WinterPlugin plugin, final Class<?> componentClass) {
      disabled.add(componentClass);
      if (failOnSecond && componentClass == Second.class) {
        throw new IllegalStateException("Second failed to disable");
      }
    }
  }

  private static final class First {
  }

  private static final class Second {
  }

  private static final class Skipped {
  }
}
