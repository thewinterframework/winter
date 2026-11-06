package com.thewinterframework.processor.template;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TemplateBuilderTest {

	@Test
	void rendersLiteralLoopIdentifiersThroughFluentBuilder() {
		final var result = TemplateBuilder.fromString("<EACH_list.items><VALUE></EACH_list.items>")
				.withEach("list.items", List.of("a", "b"), (each, value) -> each.add("VALUE", value))
				.render();

		assertEquals("ab", result);
	}
}
