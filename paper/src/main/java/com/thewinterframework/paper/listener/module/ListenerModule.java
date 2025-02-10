package com.thewinterframework.paper.listener.module;

import com.google.inject.Binder;
import com.google.inject.Scopes;
import com.thewinterframework.paper.listener.ListenerComponent;
import com.thewinterframework.paper.listener.processor.ListenerComponentProcessor;
import com.thewinterframework.plugin.WinterPlugin;
import com.thewinterframework.plugin.module.PluginModule;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

/**
 * A module that handles listener components.
 */
public class ListenerModule implements PluginModule {

	private final Set<Class<? extends Listener>> discoveredListeners = new HashSet<>();

	@Override
	public void configure(Binder binder) {
		binder.bindScope(ListenerComponent.class, Scopes.SINGLETON);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean onLoad(WinterPlugin plugin) {
		try {
			final var listeners = ListenerComponentProcessor.scan(plugin.getClass(), ListenerComponent.class).getClassList();
			discoveredListeners.addAll(
					listeners.stream()
							.filter(Listener.class::isAssignableFrom)
							.map(clazz -> (Class<? extends Listener>) clazz)
							.toList()
			);
			return true;
		} catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
			plugin.getSLF4JLogger().error("Failed to scan listeners", e);
			return false;
		}
	}

	@Override
	public boolean onEnable(WinterPlugin plugin) {
		final var javaPlugin = (JavaPlugin) plugin;
		final var pluginManager = javaPlugin.getServer().getPluginManager();
		discoveredListeners.forEach(listener -> pluginManager.registerEvents(plugin.getInjector().getInstance(listener), javaPlugin));
		return true;
	}

	@Override
	public boolean onDisable(WinterPlugin plugin) {
		HandlerList.unregisterAll((JavaPlugin) plugin);
		return true;
	}
}
