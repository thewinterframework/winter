package com.thewinterframework.service;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.thewinterframework.service.ServiceManager.HandleServicesResult;

/**
 * Manages the reloading of services.
 */
public class ReloadServiceManager {

	private final ServiceManager serviceManager;
	private final Injector injector;

	@Inject
	public ReloadServiceManager(ServiceManager serviceManager, Injector injector) {
		this.serviceManager = serviceManager;
		this.injector = injector;
	}

	/**
	 * Adds a service to be reloaded.
	 * @param service The service
	 * @param reloadService The reload service
	 */
	public void addOnReload(Class<?> service, Runnable reloadService) {
		serviceManager.addReloadMethod(service, reloadService);
	}

	/**
	 * Reloads all services.
	 *
	 * @return The result of the reload
	 */
	public HandleServicesResult reload() {
		return serviceManager.reloadServices(injector);
	}
}
