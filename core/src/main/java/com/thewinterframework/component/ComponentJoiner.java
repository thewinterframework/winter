package com.thewinterframework.component;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Utility class for joining components with a delimiter.
 */
@SuppressWarnings("unused")
public class ComponentJoiner {

	/**
	 * The delimiter to use when joining components.
	 */
	private final Component delimiter;

	/**
	 * The result of the join operation.
	 */
	private Component result = Component.empty();

	/**
	 * Creates a new component joiner with the given delimiter.
	 *
	 * @param delimiter The delimiter to use.
	 */
	public ComponentJoiner(final @NotNull Component delimiter) {
		this.delimiter = delimiter;
	}

	/**
	 * Creates a new component joiner with newlines as the delimiter.
	 *
	 * @param components The components to join.
	 */
	public static Component newLine(final @NotNull List<Component> components) {
		return Component.join(JoinConfiguration.newlines(), components);
	}

	/**
	 * Appends a component to the joiner.
	 *
	 * @param component The component to append.
	 * @return The component joiner.
	 */
	public ComponentJoiner append(final @NotNull Component component) {
		if (isEmpty()) {
			result = component;
		} else {
			result = result.append(delimiter).append(component);
		}

		return this;
	}

	/**
	 * @return The joined component.
	 */
	public Component component() {
		return result;
	}

	/**
	 * @return Whether the joiner is empty.
	 */
	public boolean isEmpty() {
		return result.equals(Component.empty());
	}
}
