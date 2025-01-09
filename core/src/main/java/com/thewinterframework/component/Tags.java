package com.thewinterframework.component;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for creating tag resolvers.
 */
@SuppressWarnings("all")
public class Tags {

	/**
	 * The list of tag resolvers.
	 */
	private final List<TagResolver> resolvers = new ArrayList<>();

	/**
	 * Creates a new tags instance.
	 *
	 * @return The new tags instance.
	 */
	public static Tags create() {
		return new Tags();
	}

	/**
	 * Creates a new tag resolver from the given objects. <br>
	 * The objects must be in the format key, value, key, value, ...
	 *
	 * @param objects The objects to create the tag resolver from.
	 * @return The new tag resolver.
	 */
	public static TagResolver from(Object... objects) {
		if (objects.length % 2 != 0) {
			throw new IllegalArgumentException("Objects length must be even");
		}

		final var tags = Tags.create();
		for (int i = 0; i < objects.length; i += 2) {
			final var rawKey = objects[i];
			if (!(rawKey instanceof String key)) {
				throw new IllegalArgumentException("Key '" + rawKey + "' must be a string");
			}

			final var rawValue = objects[i + 1];
			if (rawValue instanceof Tag tag) {
				tags.add(key, tag);
				continue;
			}

			tags.add(key, rawValue);
		}

		return tags.build();
	}

	/**
	 * Adds a tag resolver to the list of resolvers.
	 *
	 * @param resolver The resolver to add.
	 * @return The tags instance.
	 */
	public Tags add(TagResolver resolver) {
		resolvers.add(resolver);
		return this;
	}

	/**
	 * Adds a tag resolver to the list of resolvers.
	 *
	 * @param placeholder The placeholder to add.
	 * @param tag         The tag to add.
	 * @return The tags instance.
	 */
	public Tags add(String placeholder, Tag tag) {
		resolvers.add(TagResolver.resolver(placeholder, tag));
		return this;
	}

	/**
	 * Adds a tag resolver to the list of resolvers.
	 *
	 * @param placeholder The placeholder to add.
	 * @param component   The component to add.
	 * @return The tags instance.
	 */
	public Tags add(String placeholder, Component component) {
		resolvers.add(TagResolver.resolver(placeholder, Tag.selfClosingInserting(component)));
		return this;
	}

	/**
	 * Adds a tag resolver to the list of resolvers.
	 *
	 * @param placeholder The placeholder to add.
	 * @param unknown     The unknown object to add.
	 * @return The tags instance.
	 */
	public Tags add(String placeholder, Object unknown) {
		resolvers.add(TagResolver.resolver(placeholder, Tag.selfClosingInserting(ComponentUtils.resolve(unknown))));
		return this;
	}

	/**
	 * Adds a tag resolver to the list of resolvers.
	 *
	 * @param placeholder The placeholder to add.
	 * @param text        The text to add.
	 * @return The tags instance.
	 */
	public Tags add(String placeholder, String text) {
		resolvers.add(TagResolver.resolver(placeholder, Tag.selfClosingInserting(Component.text(text))));
		return this;
	}

	/**
	 * Adds a tag resolver to the list of resolvers.
	 *
	 * @param placeholder The placeholder to add.
	 * @param number      The number to add.
	 * @return The tags instance.
	 */
	public Tags add(String placeholder, int number) {
		resolvers.add(TagResolver.resolver(placeholder, Tag.selfClosingInserting(Component.text(number))));
		return this;
	}

	/**
	 * Adds a tag resolver to the list of resolvers.
	 *
	 * @param placeholder The placeholder to add.
	 * @param number      The number to add.
	 * @return The tags instance.
	 */
	public Tags add(String placeholder, long number) {
		resolvers.add(TagResolver.resolver(placeholder, Tag.selfClosingInserting(Component.text(number))));
		return this;
	}

	/**
	 * Adds a tag resolver to the list of resolvers.
	 *
	 * @param placeholder The placeholder to add.
	 * @param number      The number to add.
	 * @return The tags instance.
	 */
	public Tags add(String placeholder, double number) {
		resolvers.add(TagResolver.resolver(placeholder, Tag.selfClosingInserting(Component.text(number))));
		return this;
	}

	/**
	 * Builds the tag resolver.
	 */
	public TagResolver build() {
		return TagResolver.resolver(resolvers);
	}
}
