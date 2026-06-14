package com.thewinterframework.wire.condition.context;

import com.thewinterframework.plugin.WinterPlugin;

public class ConditionContext {

	private final WinterPlugin plugin;
	private final Class<?> component;

	public ConditionContext(final WinterPlugin plugin, final Class<?> component) {
		this.plugin = plugin;
		this.component = component;
	}

	public WinterPlugin getPlugin() {
		return plugin;
	}

	public Class<?> getComponent() {
		return component;
	}
}
