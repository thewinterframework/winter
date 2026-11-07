package com.thewinterframework.component;

import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComponentJoinerTest {

  @Test
  void emptyComponentStillCountsAsFirstEntry() {
    final var joiner = new ComponentJoiner(Component.text(", "));
    assertTrue(joiner.isEmpty());

    joiner.append(Component.empty()).append(Component.text("second"));

    assertFalse(joiner.isEmpty());
    assertEquals(Component.empty().append(Component.text(", ")).append(Component.text("second")), joiner.component());
  }
}
