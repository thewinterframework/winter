package com.thewinterframework.velocity.listener.module;

import com.google.inject.Binder;
import com.google.inject.Scopes;
import com.thewinterframework.plugin.WinterPlugin;
import com.thewinterframework.velocity.VelocityWinterPlugin;
import com.thewinterframework.velocity.listener.ListenerComponent;
import com.thewinterframework.wire.module.AbstractProcessorModule;

import java.util.ArrayList;
import java.util.List;

public class ListenerModule extends AbstractProcessorModule {

	private final List<Object> listeners = new ArrayList<>();

	public ListenerModule() {
		super(ListenerComponent.class);
	}

	@Override
	public void configure(final Binder binder) {
		binder.bindScope(ListenerComponent.class, Scopes.SINGLETON);
	}

	@Override
	protected void enableComponent(final WinterPlugin plugin, final Class<?> componentClass) {
		final var velocityPlugin = (VelocityWinterPlugin) plugin;
		final var listener = plugin.getInjector().getInstance(componentClass);
		velocityPlugin.getServer().getEventManager().register(plugin, listener);
		listeners.add(listener);
	}

	@Override
	public boolean onDisable(final WinterPlugin plugin) throws Exception {
		try {
			final var eventManager = ((VelocityWinterPlugin) plugin).getServer().getEventManager();
			listeners.forEach(listener -> eventManager.unregisterListener(plugin, listener));
			listeners.clear();
		} finally {
			super.onDisable(plugin);
		}
		return true;
	}
}
