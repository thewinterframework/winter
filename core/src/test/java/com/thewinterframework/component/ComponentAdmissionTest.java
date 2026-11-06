package com.thewinterframework.component;

import com.thewinterframework.component.admission.ComponentDecision;
import com.thewinterframework.component.annotation.Component;
import com.thewinterframework.component.annotation.InterceptedBy;
import com.thewinterframework.component.context.ComponentContext;
import com.thewinterframework.component.decorator.ComponentDecorator;
import com.thewinterframework.component.decorator.ComponentDecoratorHandler;
import com.thewinterframework.component.interceptor.ComponentInterceptor;
import com.thewinterframework.component.lifecycle.ComponentPhase;
import com.thewinterframework.component.meta.ComponentDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class ComponentAdmissionTest {

  @BeforeEach
  void resetDiscoveries() {
    RejectingDecorator.discoveries = 0;
    RejectingDecorator.constructions = 0;
  }

  @Test
  void interceptorCanDiscardComponentBeforeRegistration() throws Exception {
    final var manager = new ComponentManager();
    final var descriptor = ComponentDescriptor.from(InterceptedComponent.class);

    final var registered = manager.registerComponent(
        new ComponentContext(null, descriptor),
        List.of(new RejectingInterceptor())
    );

    assertFalse(registered);
    assertEquals(0, manager.components().size());
    assertEquals(0, RejectingDecorator.constructions);
  }

  @Test
  void decoratorCanDiscardComponentBeforeDiscovery() throws Exception {
    final var manager = new ComponentManager();
    final var descriptor = ComponentDescriptor.from(DecoratedComponent.class);

    final var registered = manager.registerComponent(
        new ComponentContext(null, descriptor),
        List.of()
    );

    assertFalse(registered);
    assertEquals(0, RejectingDecorator.discoveries);
    assertEquals(0, manager.components().size());
    assertNull(manager.getDecorator(RejectingDecorator.class));
  }

  @Test
  void interceptorCanDiscardBeforeInjectionOrEnable() throws Exception {
    final var descriptor = ComponentDescriptor.from(InterceptedComponent.class);
    final var context = new ComponentContext(null, descriptor);
    final var manager = new ComponentManager();
    final ComponentInterceptor rejectInjection = new ComponentInterceptor() {
      @Override
      public ComponentDecision decide(final ComponentPhase phase, final ComponentContext ignored) {
        return phase == ComponentPhase.INJECTION ? ComponentDecision.DISCARD : ComponentDecision.CONTINUE;
      }
    };
    assertFalse(manager.registerComponent(context, List.of(rejectInjection),
        ComponentPhase.LOAD, ComponentPhase.INJECTION));
    assertNull(manager.getDecorator(RejectingDecorator.class));
    assertEquals(0, RejectingDecorator.constructions);

    final ComponentInterceptor rejectEnable = new ComponentInterceptor() {
      @Override
      public ComponentDecision decide(final ComponentPhase phase, final ComponentContext ignored) {
        return phase == ComponentPhase.ENABLE ? ComponentDecision.DISCARD : ComponentDecision.CONTINUE;
      }
    };
    assertFalse(manager.admits(context, List.of(rejectEnable), ComponentPhase.ENABLE));
  }

  private static final class RejectingInterceptor implements ComponentInterceptor {

    @Override
    public ComponentDecision decide(final ComponentContext context) {
      return ComponentDecision.DISCARD;
    }
  }

  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @ComponentDecorator(RejectingDecorator.class)
  private @interface Rejected {
  }

  static final class RejectingDecorator implements ComponentDecoratorHandler<Rejected> {

    private static int discoveries;
    private static int constructions;

    RejectingDecorator() {
      constructions++;
    }

    @Override
    public Class<Rejected> getAnnotationType() {
      return Rejected.class;
    }

    @Override
    public ComponentDecision decide(final ComponentContext context) {
      return ComponentDecision.DISCARD;
    }

    @Override
    public void onDiscoverOnType(final Class<?> component, final Rejected annotation) {
      discoveries++;
    }
  }

  @Component
  @InterceptedBy(RejectingInterceptor.class)
  @Rejected
  private static final class InterceptedComponent {
  }

  @Component
  @Rejected
  private static final class DecoratedComponent {
  }
}
