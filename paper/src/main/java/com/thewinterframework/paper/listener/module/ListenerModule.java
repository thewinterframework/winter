package com.thewinterframework.paper.listener.module;

import com.google.inject.Binder;
import com.google.inject.Scopes;
import com.thewinterframework.paper.listener.ListenerComponent;
import com.thewinterframework.plugin.WinterPlugin;
import com.thewinterframework.wire.module.AbstractProcessorModule;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * A module that handles listener components.
 */
public class ListenerModule extends AbstractProcessorModule {

	public ListenerModule() {
		super(ListenerComponent.class);
	}

	@Override
	public void configure(final Binder binder) {
		binder.bindScope(ListenerComponent.class, Scopes.SINGLETON);
	}

	@Override
	protected void enableComponent(final WinterPlugin plugin, final Class<?> componentClass) {
		final var javaPlugin = (JavaPlugin) plugin;
		final var listener = (Listener) plugin.getInjector().getInstance(componentClass);
		Bukkit.getServer().getPluginManager().registerEvents(listener, javaPlugin);
	}

	@Override
	public boolean onDisable(final WinterPlugin plugin) throws Exception {
		super.onDisable(plugin);
		HandlerList.unregisterAll((JavaPlugin) plugin);
		return true;
	}
}
