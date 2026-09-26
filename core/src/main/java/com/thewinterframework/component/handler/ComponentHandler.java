package com.thewinterframework.component.handler;

import com.google.inject.Binder;
import com.thewinterframework.component.context.ComponentContext;
import com.thewinterframework.component.meta.ComponentDescriptor;

import java.util.Collection;

/**
 * Extension point for component capabilities such as listeners, commands, providers, or addons.
 * Implementations may be registered with {@link java.util.ServiceLoader} or explicitly on
 * {@code @Component}.
 */
public interface ComponentHandler {

	default int order() {
		return 0;
	}

	default boolean supports(final Class<?> componentType) {
		return false;
	}

	default void configure(final Binder binder, final Collection<ComponentDescriptor> components) {
	}

	default void onLoad(final ComponentContext context) throws Exception {
	}

	default void onEnable(final ComponentContext context) throws Exception {
	}

	default void onDisable(final ComponentContext context) throws Exception {
	}
}
