package com.thewinterframework.service;

import com.google.inject.Inject;
import com.thewinterframework.plugin.WinterPlugin;
import com.thewinterframework.service.decorator.lifecycle.LifeCycleDecoratorHandler.LifeCycleResult;
import com.thewinterframework.service.decorator.lifecycle.OnReloadDecoratorHandler;

/**
 * Manages the reloading of services.
 */
public class ReloadServiceManager {

	private final ServiceManager serviceManager;
	private final WinterPlugin plugin;

	@Inject
	public ReloadServiceManager(ServiceManager serviceManager, WinterPlugin plugin) {
		this.serviceManager = serviceManager;
		this.plugin = plugin;
	}

	/**
	 * Adds a service to be reloaded.
	 * @param service The service
	 * @param reloadService The reload service
	 */
	public void addOnReload(Class<?> service, Runnable reloadService) {
		final var handler = serviceManager.getHandler(OnReloadDecoratorHandler.class);
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
		final var handler = serviceManager.getHandler(OnReloadDecoratorHandler.class);
		if (handler == null) {
			return new LifeCycleResult(false, null);
		}

		return handler.execute(plugin);
	}
}
