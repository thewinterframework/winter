package com.thewinterframework.component.context;

import com.thewinterframework.component.meta.ComponentDescriptor;
import com.thewinterframework.plugin.WinterPlugin;

/** Runtime context shared with component extensions. */
public record ComponentContext(WinterPlugin plugin, ComponentDescriptor descriptor) {

	public Class<?> componentType() {
		return descriptor.type();
	}

	public Object instance() {
		if (plugin.getInjector() == null) {
			throw new IllegalStateException("Component instances are unavailable before injector creation");
		}
		return plugin.getInjector().getInstance(componentType());
	}
}
