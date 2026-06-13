package com.thewinterframework.service;

import com.google.inject.Binder;
import com.google.inject.Scopes;
import com.thewinterframework.plugin.WinterPlugin;
import com.thewinterframework.service.annotation.Service;
import com.thewinterframework.wire.module.AbstractProcessorModule;

/**
 * Module for services.
 * <p> This module scans for classes annotated with {@link Service} and binds them to the injector. </p>
 */
public class ServiceModule extends AbstractProcessorModule {

	private final ServiceManager serviceManager = new ServiceManager();

	public ServiceModule() {
		super(Service.class);
	}

	@Override
	public void configure(final Binder binder) {
		binder.bindScope(Service.class, Scopes.SINGLETON);
		binder.bind(ServiceManager.class).toInstance(serviceManager);
		binder.bind(ReloadServiceManager.class).in(Scopes.SINGLETON);

		serviceManager.configureHandlers(binder);
	}

	@Override
	protected void registerComponent(final WinterPlugin unused, final Class<?> componentClass) throws Exception {
		serviceManager.registerService(componentClass);
	}

	@Override
	public boolean onLoad(final WinterPlugin plugin) throws Exception {
		final var start = System.currentTimeMillis();
		if (!super.onLoad(plugin)) {
			return false;
		}
		serviceManager.loadHandlers(plugin);
		plugin.getSLF4JLogger().debug("Loaded {} services in {}ms", serviceManager.services().size(), System.currentTimeMillis() - start);
		return true;
	}

	@Override
	public boolean onEnable(final WinterPlugin plugin) {
		try {
			final var start = System.currentTimeMillis();
			if (!super.onEnable(plugin)) {
				return false;
			}
			serviceManager.startHandlers(plugin);
			plugin.getSLF4JLogger().info("Enabled {} services in {}ms", serviceManager.services().size(), System.currentTimeMillis() - start);
			return true;
		} catch (final Exception e) {
			plugin.getSLF4JLogger().error("Failed to enable services", e);
			return false;
		}
	}

	@Override
	public boolean onDisable(final WinterPlugin plugin) {
		try {
			final var start = System.currentTimeMillis();
			if (!super.onDisable(plugin)) {
				return false;
			}
			serviceManager.stopHandlers(plugin);
			plugin.getSLF4JLogger().info("Disabled {} services in {}ms", serviceManager.services().size(), System.currentTimeMillis() - start);
			return true;
		} catch (final Exception e) {
			plugin.getSLF4JLogger().error("Failed to disable services", e);
			return false;
		}
	}
}
