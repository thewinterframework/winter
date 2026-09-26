package com.thewinterframework.paper.listener.handler;

import com.google.auto.service.AutoService;
import com.thewinterframework.component.context.ComponentContext;
import com.thewinterframework.component.handler.ComponentHandler;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/** Registers every listener declared with the unified component annotation. */
@AutoService(ComponentHandler.class)
public final class ListenerComponentHandler implements ComponentHandler {

	@Override
	public boolean supports(final Class<?> componentType) {
		return Listener.class.isAssignableFrom(componentType);
	}

	@Override
	public void onEnable(final ComponentContext context) {
		final var listener = (Listener) context.instance();
		Bukkit.getPluginManager().registerEvents(listener, (JavaPlugin) context.plugin());
	}

	@Override
	public void onDisable(final ComponentContext context) {
		HandlerList.unregisterAll((Listener) context.instance());
	}
}
