package com.thewinterframework.component.meta;

import com.thewinterframework.component.annotation.Component;
import com.thewinterframework.component.annotation.ComponentScope;
import com.thewinterframework.component.handler.ComponentHandler;
import com.thewinterframework.component.interceptor.ComponentInterceptor;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComponentDescriptorTest {

	@Test
	void resolvesAComposedComponentAnnotation() {
		final var descriptor = ComponentDescriptor.from(ExternalAddon.class);

		assertEquals(ComponentScope.EXTERNAL, descriptor.scope());
		assertEquals(42, descriptor.order());
		assertTrue(descriptor.async());
		assertEquals(TestHandler.class, descriptor.handlers().getFirst());
		assertEquals(TestInterceptor.class, descriptor.interceptors().getFirst());
	}

	@Component(
			scope = ComponentScope.EXTERNAL,
			order = 42,
			async = true,
			handlers = TestHandler.class,
			interceptors = TestInterceptor.class
	)
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	private @interface AddonComponent {
	}

	@AddonComponent
	private static final class ExternalAddon {
	}

	public static final class TestHandler implements ComponentHandler {
	}

	public static final class TestInterceptor implements ComponentInterceptor {
	}
}
