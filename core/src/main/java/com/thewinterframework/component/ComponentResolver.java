package com.thewinterframework.component;

import net.kyori.adventure.text.Component;

/**
 * Resolves an object into a component.
 * @param <T> The type of object to resolve.
 */
public interface ComponentResolver<T> {

	/**
	 * The default component resolver, which simply calls {@link Object#toString()} and wraps it in a text component.
	 */
	ComponentResolver<Object> DEFAULT = t -> Component.text(t.toString());

	/**
	 * Resolves the given object into a component.
	 *
	 * @param t The object to resolve.
	 * @return The resolved component.
	 */
	Component resolve(T t);
}
