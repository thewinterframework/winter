package com.thewinterframework.component.meta;

import com.thewinterframework.component.annotation.Component;
import com.thewinterframework.component.annotation.ComponentPriority;
import com.thewinterframework.component.annotation.LoadAfter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ComponentOrderTest {

  @Test
  void explicitDependenciesTakePrecedenceOverNumericOrder() {
    final var result = ComponentOrder.resolve(List.of(
        ComponentDescriptor.from(Last.class),
        ComponentDescriptor.from(Middle.class),
        ComponentDescriptor.from(First.class)
    ));

    assertEquals(List.of(First.class, Middle.class, Last.class), result.stream().map(ComponentDescriptor::type).toList());
  }

  @Test
  void rejectsDependencyCycles() {
    assertThrows(IllegalStateException.class, () -> ComponentOrder.resolve(List.of(
        ComponentDescriptor.from(CycleOne.class),
        ComponentDescriptor.from(CycleTwo.class)
    )));
  }

  @Test
  void rejectsDuplicateComponents() {
    final var descriptor = ComponentDescriptor.from(First.class);
    assertThrows(IllegalArgumentException.class, () -> ComponentOrder.resolve(List.of(descriptor, descriptor)));
  }

  @Test
  void rejectsSelfDependencies() {
    assertThrows(IllegalStateException.class, () -> ComponentOrder.resolve(List.of(
        ComponentDescriptor.from(SelfDependent.class)
    )));
  }

  @Component
  @ComponentPriority(100)
  private static final class First {
  }

  @Component
  @ComponentPriority(-100)
  @LoadAfter(First.class)
  private static final class Middle {
  }

  @Component
  @ComponentPriority(-200)
  @LoadAfter(Middle.class)
  private static final class Last {
  }

  @Component
  @LoadAfter(CycleTwo.class)
  private static final class CycleOne {
  }

  @Component
  @LoadAfter(CycleOne.class)
  private static final class CycleTwo {
  }

  @Component
  @LoadAfter(SelfDependent.class)
  private static final class SelfDependent {
  }
}
