package com.thewinterframework.component;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AdventureComponentsTest {

	@Test
	void parsesMiniMessageWithPlaceholdersAndClickEvents() {
		final var component = ComponentUtils.miniMessage(
				"<green><click:run_command:'/spawn'><player></click></green>",
				Tags.from("player", "Alex"));
		final var expected = Component.text("Alex", NamedTextColor.GREEN)
				.clickEvent(ClickEvent.runCommand("/spawn"));

		assertEquals(expected, component.compact());
	}

	@Test
	void roundTripsMiniMessageWithFormattingAndEvents() {
		final var component = Component.text("Open", NamedTextColor.AQUA)
				.decorate(TextDecoration.BOLD)
				.clickEvent(ClickEvent.openUrl("https://example.com"))
				.hoverEvent(Component.text("Visit", NamedTextColor.YELLOW));

		final var serialized = ComponentUtils.miniMessage(component);

		assertEquals(component, ComponentUtils.miniMessage(serialized).compact());
	}

	@Test
	void roundTripsLegacyColorsAndDecorations() {
		final var ampersand = ComponentUtils.legacyAmpersand("&aHello &lworld");
		final var section = ComponentUtils.legacySection("\u00a7aHello \u00a7lworld");

		assertEquals(ampersand, section);
		assertEquals("Hello world", ComponentUtils.plainText(ampersand));
		assertEquals("&aHello &lworld", ComponentUtils.legacyAmpersand(ampersand));
		assertEquals("\u00a7aHello \u00a7lworld", ComponentUtils.legacySection(section));
	}

	@Test
	void preservesLiteralPlaceholdersAndSupportsParsedTags() {
		final var tags = Tags.create()
				.add("literal", "<red>literal</red>")
				.add("count", 3)
				.add("formatted", Component.text("OK", NamedTextColor.GREEN))
				.add("parsed", Tag.preProcessParsed("<blue>parsed</blue>"))
				.build();
		final var component = ComponentUtils.miniMessage("<literal> <count> <formatted> <parsed>", tags);

		assertEquals("<red>literal</red> 3 OK parsed", ComponentUtils.plainText(component));
		assertEquals(Component.text("<red>&aLiteral"), ComponentUtils.plainText("<red>&aLiteral"));
	}

	@Test
	void joinsAndResolvesComponentCollections() {
		final var first = Component.text("First", NamedTextColor.RED);
		final var second = Component.text("Second", NamedTextColor.GREEN);

		assertEquals("First\nSecond", ComponentUtils.plainText(ComponentJoiner.newLine(List.of(first, second))));
		assertEquals("First, Second", ComponentUtils.plainText(
				new ComponentJoiner(Component.text(", ")).append(first).append(second).component()));
		assertEquals("First, 5, Second", ComponentUtils.plainText(ComponentUtils.resolve(List.of(first, 5, second))));
	}
}
