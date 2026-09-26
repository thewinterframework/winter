package com.thewinterframework.component;

import com.thewinterframework.component.admission.ComponentDecision;
import com.thewinterframework.component.annotation.Component;
import com.thewinterframework.component.context.ComponentContext;
import com.thewinterframework.component.decorator.ComponentDecorator;
import com.thewinterframework.component.decorator.ComponentDecoratorHandler;
import com.thewinterframework.component.interceptor.ComponentInterceptor;
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

	@Component(interceptors = RejectingInterceptor.class)
	private static final class InterceptedComponent {
	}

	@Component
	@Rejected
	private static final class DecoratedComponent {
	}
}
