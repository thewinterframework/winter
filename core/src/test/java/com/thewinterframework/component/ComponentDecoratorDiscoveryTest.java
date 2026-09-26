package com.thewinterframework.component;

import com.thewinterframework.component.annotation.Component;
import com.thewinterframework.component.meta.ComponentDescriptor;
import com.thewinterframework.service.annotation.lifecycle.OnEnable;
import com.thewinterframework.service.annotation.scheduler.ScheduledAt;
import com.thewinterframework.service.decorator.lifecycle.OnEnableDecoratorHandler;
import com.thewinterframework.service.decorator.scheduler.SingleScheduledAtDecoratorHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ComponentDecoratorDiscoveryTest {

	@Test
	void discoversLifecycleAndSingleScheduleDecoratorsOnAnyComponent() throws Exception {
		final var manager = new ComponentManager();
		manager.registerComponent(ComponentDescriptor.from(DecoratedComponent.class));

		assertNotNull(manager.getDecorator(OnEnableDecoratorHandler.class));
		assertNotNull(manager.getDecorator(SingleScheduledAtDecoratorHandler.class));
	}

	@Component
	private static final class DecoratedComponent {

		@OnEnable
		void enable() {
		}

		@ScheduledAt(hour = "12")
		void schedule() {
		}
	}
}
