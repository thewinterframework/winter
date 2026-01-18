package com.thewinterframework.component;

import com.thewinterframework.number.NumberUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.UnaryOperator;

/**
 * Utility class for working with Adventure components.
 */
@SuppressWarnings("unused")
public class ComponentUtils {

	/**
	 * The MiniMessage instance used for parsing and serializing components.
	 */
	private final static MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

	/**
	 * The legacy component serializer for section components (§).
	 */
	private final static LegacyComponentSerializer LEGACY_SECTION_COMPONENT_SERIALIZER = LegacyComponentSerializer.legacySection();

	/**
	 * The legacy component serializer for ampersand components (&).
	 */
	private final static LegacyComponentSerializer LEGACY_AMPERSAND_COMPONENT_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

	/**
	 * The plain text component serializer.
	 */
	private final static PlainTextComponentSerializer PLAIN_TEXT_COMPONENT_SERIALIZER = PlainTextComponentSerializer.plainText();

	/**
	 * The global tag resolvers, which are used when parsing MiniMessage components.
	 */
	private final static List<TagResolver> GLOBAL_TAG_RESOLVERS = new ArrayList<>();

	/**
	 * The component resolvers, which are used to resolve objects into components.
	 */
	private final static Map<Class<?>, ComponentResolver<?>> COMPONENT_RESOLVERS = new HashMap<>();

	/**
	 * The component modifiers, which are used to modify components after they are resolved.
	 */
	private final static Set<UnaryOperator<Component>> COMPONENT_MODIFIERS = new HashSet<>();

	/**
	 * Adds a global tag resolver.
	 *
	 * @param tagResolver The tag resolver to add.
	 */
	public static void addGlobalTagResolver(final @NotNull TagResolver tagResolver) {
		GLOBAL_TAG_RESOLVERS.add(tagResolver);
	}

	/**
	 * Adds a component resolver.
	 *
	 * @param clazz     The class to resolve.
	 * @param resolver  The resolver to add.
	 * @param <T>       The type of the class to resolve.
	 */
	public static <T> void addComponentResolver(final @NotNull Class<T> clazz, final @NotNull ComponentResolver<T> resolver) {
		COMPONENT_RESOLVERS.put(clazz, resolver);
	}

	/**
	 * Adds a component modifier.
	 *
	 * @param modifier The modifier to add.
	 */
	public static void addComponentModifier(final @NotNull UnaryOperator<Component> modifier) {
		COMPONENT_MODIFIERS.add(modifier);
	}

	/**
	 * Serializes a component into a MiniMessage string.
	 *
	 * @param component The component to serialize.
	 * @return The serialized MiniMessage string.
	 */
	public static String miniMessage(final @NotNull Component component) {
		return MINI_MESSAGE.serialize(component);
	}

	/**
	 * Serializes multiple components into MiniMessage strings.
	 *
	 * @param components The components to serialize.
	 * @return The serialized MiniMessage strings.
	 */
	public static List<String> miniMessage(final @NotNull List<Component> components) {
		return components.stream()
				.map(ComponentUtils::miniMessage)
				.toList();
	}

	/**
	 * Parses a MiniMessage string into a component.
	 *
	 * @param message      The message to parse.
	 * @param applyModifiers Whether to apply component modifiers.
	 * @param tagResolvers The tag resolvers to use.
	 * @return The parsed component.
	 */
	public static Component miniMessage(final @NotNull String message, final boolean applyModifiers, final @NotNull TagResolver... tagResolvers) {
		final var component = MINI_MESSAGE.deserialize(
				message,
				TagResolver.builder()
						.resolvers(GLOBAL_TAG_RESOLVERS)
						.resolvers(tagResolvers)
						.build()
		);
		return applyModifiers ? applyModifiers(component) : component;
	}

	/**
	 * Parses a MiniMessage string into a component.
	 *
	 * @param message    The message to parse.
	 * @param tagResolvers The tag resolvers to use.
	 * @return The parsed component.
	 */
	public static Component miniMessage(final @NotNull String message, final @NotNull TagResolver... tagResolvers) {
		return miniMessage(message, false, tagResolvers);
	}

	/**
	 * Parses multiple MiniMessage strings into components.
	 *
	 * @param messages     The messages to parse.
	 * @param applyModifiers Whether to applycomponent modifiers.
	 * @param tagResolvers The tag resolvers to use.
	 * @return The parsed components.
	 */
	public static List<Component> miniMessage(final @NotNull List<String> messages, final boolean applyModifiers, final @NotNull TagResolver... tagResolvers) {
		return messages.stream()
				.map(line -> miniMessage(line, applyModifiers, tagResolvers))
				.toList();
	}

	/**
	 * Parses multiple MiniMessage strings into components.
	 *
	 * @param messages     The messages to parse.
	 * @param tagResolvers The tag resolvers to use.
	 * @return The parsed components.
	 */
	public static List<Component> miniMessage(final @NotNull List<String> messages, final @NotNull TagResolver... tagResolvers) {
		return miniMessage(messages, false,  tagResolvers);
	}

	/**
	 * Serializes a string into a legacy section string.
	 * This is a string where color codes are prefixed with §.
	 * For example, §cHello §fworld.
	 *
	 * @param message The message to serialize.
	 * @return The serialized legacy section component.
	 */
	public static Component legacySection(final @NotNull String message, final boolean applyModifiers) {
		final var component = LEGACY_SECTION_COMPONENT_SERIALIZER.deserialize(message);
		return applyModifiers ? applyModifiers(component) : component;
	}

	/**
	 * Serializes a string into a legacy section string.
	 * This is a string where color codes are prefixed with §.
	 * For example, §cHello §fworld.
	 *
	 * @param message The message to serialize.
	 * @return The serialized legacy section component.
	 */
	public static Component legacySection(final @NotNull String message) {
		return legacySection(message, false);
	}

	/**
	 * Serializes multiple strings into legacy section components.
	 *
	 * @param messages The messages to serialize.
	 * @param applyModifiers Whether to apply component modifiers.
	 * @return The serialized legacy section components.
	 */
	public static List<Component> legacySection(final @NotNull List<String> messages, final boolean applyModifiers) {
		return messages.stream()
				.map(line -> legacySection(line, applyModifiers))
				.toList();
	}

	/**
	 * Serializes multiple strings into legacy section components.
	 *
	 * @param messages The messages to serialize.
	 * @return The serialized legacy section components.
	 */
	public static List<Component> legacySection(final @NotNull List<String> messages) {
		return legacySection(messages, false);
	}

	/**
	 * Serializes a component into a legacy section string.
	 * This is a string where color codes are prefixed with §.
	 * For example, §cHello §fworld.
	 *
	 * @param component The component to serialize.
	 * @return The serialized legacy section component.
	 */
	public static String legacySection(final @NotNull Component component) {
		return LEGACY_SECTION_COMPONENT_SERIALIZER.serialize(component);
	}

	/**
	 * Serializes a string into a legacy ampersand string.
	 * This is a string where color codes are prefixed with ampersand.
	 *
	 * @param message The message to serialize.
	 * @param applyModifiers Whether to apply component modifiers.
	 * @return The serialized legacy ampersand component.
	 */
	public static Component legacyAmpersand(final @NotNull String message, final boolean applyModifiers) {
		final var component = LEGACY_AMPERSAND_COMPONENT_SERIALIZER.deserialize(message);
		return applyModifiers ? applyModifiers(component) : component;
	}

	/**
	 * Serializes a string into a legacy ampersand string.
	 * This is a string where color codes are prefixed with ampersand.
	 *
	 * @param message The message to serialize.
	 * @return The serialized legacy ampersand component.
	 */
	public static Component legacyAmpersand(final @NotNull String message) {
		return legacyAmpersand(message, false);
	}

	/**
	 * Serializes multiple strings into legacy ampersand components.
	 *
	 * @param messages The messages to serialize.
	 * @param applyModifiers Whether to applycomponent modifiers.
	 * @return The serialized legacy ampersand components.
	 */
	public static List<Component> legacyAmpersand(final @NotNull List<String> messages, final boolean applyModifiers) {
		return messages.stream()
				.map(line -> legacyAmpersand(line, applyModifiers))
				.toList();
	}

	/**
	 * Serializes multiple strings into legacy ampersand components.
	 *
	 * @param messages The messages to serialize.
	 * @return The serialized legacy ampersand components.
	 */
	public static List<Component> legacyAmpersand(final @NotNull List<String> messages) {
		return legacyAmpersand(messages, false);
	}

	/**
	 * Serializes a component into a legacy ampersand string.
	 * This is a string where color codes are prefixed with ampersand.
	 *
	 * @param component The component to serialize.
	 * @return The serialized legacy ampersand component.
	 */
	public static String legacyAmpersand(final @NotNull Component component) {
		return LEGACY_AMPERSAND_COMPONENT_SERIALIZER.serialize(component);
	}

	/**
	 * Serializes a component into a plain text string.
	 * This is a string where all formatting is removed.
	 *
	 * @param component The component to serialize.
	 * @return The serialized plain text component.
	 */
	public static String plainText(final @NotNull Component component) {
		return PLAIN_TEXT_COMPONENT_SERIALIZER.serialize(component);
	}

	/**
	 * Serializes multiple components into plain text strings.
	 *
	 * @param components The components to serialize.
	 * @return The serialized plain text components.
	 */
	public static List<String> plainText(final @NotNull List<Component> components) {
		return components.stream()
				.map(ComponentUtils::plainText)
				.toList();
	}

	/**
	 * Serializes a string into a plain text component.
	 * This is a component where all formatting is removed.
	 *
	 * @param message The message to serialize.
	 * @return The serialized plain text component.
	 */
	public static Component plainText(final @NotNull String message, final boolean applyModifiers) {
		final var component = PLAIN_TEXT_COMPONENT_SERIALIZER.deserialize(message);
		return applyModifiers ? applyModifiers(component) : component;
	}

	/**
	 * Serializes a string into a plain text component.
	 * This is a component where all formatting is removed.
	 *
	 * @param message The message to serialize.
	 * @return The serialized plain text component.
	 */
	public static Component plainText(final @NotNull String message) {
		return plainText(message, false);
	}

	/**
	 * Resolves an object into a component.
	 * @param object The object to resolve.
	 * @return The resolved component.
	 */
	@SuppressWarnings("unchecked")
	public static @NotNull Component resolve(final @NotNull Object object) {
		switch (object) {
			case Component component -> {
				return component;
			}
			case String s -> {
				return Component.text(s);
			}
			case Number number -> {
				if (number instanceof Double decimal) {
					return Component.text(NumberUtils.formatNumber(decimal, 2));
				} else {
					return Component.text(number.toString());
				}
			}
			case Iterable<?> objects -> {
				final var builder = new ComponentJoiner(Component.text(", "));
				for (final var o : objects) {
					builder.append(resolve(o));
				}
				return builder.component();
			}
			default -> {
				final var resolver = (ComponentResolver<Object>) COMPONENT_RESOLVERS.getOrDefault(object.getClass(), ComponentResolver.DEFAULT);
				return resolver.resolve(object);
			}
		}
	}

	/**
	 * Resolves multiple objects into components.
	 *
	 * @param objects The objects to resolve.
	 * @return The resolved components.
	 */
	public static @NotNull Component[] resolveMany(final @NotNull Object... objects) {
		final var components = new Component[objects.length];
		for (var i = 0; i < objects.length; i++) {
			components[i] = resolve(objects[i]);
		}
		return components;
	}

	private static @NotNull Component applyModifiers(final @NotNull Component component) {
		var modifiedComponent = component;
		for (final var modifier : COMPONENT_MODIFIERS) {
			modifiedComponent = modifier.apply(modifiedComponent);
		}
		return modifiedComponent;
	}
}
