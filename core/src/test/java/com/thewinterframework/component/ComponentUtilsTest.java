package com.thewinterframework.component;

import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ComponentUtilsTest {

  @Test
  void resolverRegistrationRestoresPreviousResolver() {
    final var value = new Value("example");
    try (final var first = ComponentUtils.registerComponentResolver(Value.class,
        item -> Component.text("first:" + item.text()))) {
      assertEquals(Component.text("first:example"), ComponentUtils.resolve(value));
      try (final var second = ComponentUtils.registerComponentResolver(Value.class,
          item -> Component.text("second:" + item.text()))) {
        assertEquals(Component.text("second:example"), ComponentUtils.resolve(value));
      }
      assertEquals(Component.text("first:example"), ComponentUtils.resolve(value));
    }
    assertEquals(Component.text("Value[text=example]"), ComponentUtils.resolve(value));
  }

  private record Value(String text) {
  }
}
