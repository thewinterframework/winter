package com.thewinterframework.service;

import com.google.inject.Inject;
import com.thewinterframework.component.ComponentManager;
import com.thewinterframework.plugin.WinterPlugin;
import com.thewinterframework.service.decorator.lifecycle.LifeCycleDecoratorHandler.LifeCycleResult;
import com.thewinterframework.service.decorator.lifecycle.OnReloadDecoratorHandler;

/**
 * Manages the reloading of services.
 */
public class ReloadServiceManager {

	private final ComponentManager componentManager;
	private final WinterPlugin plugin;

	@Inject
	public ReloadServiceManager(final ComponentManager componentManager, final WinterPlugin plugin) {
		this.componentManager = componentManager;
		this.plugin = plugin;
	}

	/**
	 * Adds a service to be reloaded.
	 *
	 * @param service       The service
	 * @param reloadService The reload service
	 */
	public void addOnReload(final Class<?> service, final Runnable reloadService) {
		final var handler = componentManager.getDecorator(OnReloadDecoratorHandler.class);
		if (handler != null) {
			handler.addReloadMethod(service, reloadService);
		}
	}

	/**
	 * Reloads all services.
	 *
	 * @return The result of the reload
	 */
	public LifeCycleResult reload() {
		final var handler = componentManager.getDecorator(OnReloadDecoratorHandler.class);
		if (handler == null) {
			return new LifeCycleResult(false, null);
		}

		return handler.execute(plugin);
	}
}
