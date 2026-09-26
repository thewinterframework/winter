package com.thewinterframework.component.meta;

import com.thewinterframework.component.annotation.Component;
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

	@Component(order = 100)
	private static final class First {
	}

	@Component(order = -100, after = First.class)
	private static final class Middle {
	}

	@Component(order = -200, after = Middle.class)
	private static final class Last {
	}

	@Component(after = CycleTwo.class)
	private static final class CycleOne {
	}

	@Component(after = CycleOne.class)
	private static final class CycleTwo {
	}
}
