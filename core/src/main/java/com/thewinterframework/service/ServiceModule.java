package com.thewinterframework.service;

import com.google.inject.Binder;
import com.google.inject.Scopes;
import com.thewinterframework.module.processor.ModuleComponentAnnotationProcessor;
import com.thewinterframework.plugin.WinterPlugin;
import com.thewinterframework.plugin.module.PluginModule;
import com.thewinterframework.service.annotation.Service;
import com.thewinterframework.service.meta.lifecycle.LifeCycleMethod;

import java.util.stream.Collectors;

/**
 * Module for services.
 * <p> This module scans for classes annotated with {@link Service} and binds them to the injector. </p>
 */
public class ServiceModule implements PluginModule {

	private final ServiceManager serviceManager = new ServiceManager();

	@Override
	public void configure(Binder binder) {
		binder.bindScope(Service.class, Scopes.SINGLETON);
	}

	@Override
	public boolean onLoad(WinterPlugin plugin) {
		final var start = System.currentTimeMillis();

		try {
			final var classListProvider = ModuleComponentAnnotationProcessor.scan(plugin.getClass(), Service.class);
			for (final var service : classListProvider.getClassList()) {
				serviceManager.registerService(service);
			}
		} catch (Exception e) {
			plugin.getSLF4JLogger().error("Failed to scan services", e);
			return false;
		}

		serviceManager.builtGraphs();
		plugin.getSLF4JLogger().info("Loaded {} services in {}ms", serviceManager.services().size(), System.currentTimeMillis() - start);
		return true;
	}

	@Override
	public boolean onEnable(WinterPlugin plugin) {
		final var start = System.currentTimeMillis();
		final var result = serviceManager.enableServices(plugin.getInjector());
		if (!result.result()) {
			if (!result.cycles().isEmpty()) {
				plugin.getSLF4JLogger().error(
						"Failed to enable services due to cyclic dependencies: {}",
						result.cycles()
								.stream()
								.map(LifeCycleMethod::service)
								.map(Class::getCanonicalName)
								.collect(Collectors.joining(", "))
				);
			} else {
				plugin.getSLF4JLogger().error("Failed to enable services", result.throwable());
			}

			return false;
		}

		serviceManager.startSchedulers(plugin);
		plugin.getSLF4JLogger().info("Enabled {} services in {}ms", serviceManager.services().size(), System.currentTimeMillis() - start);
		return true;
	}

	@Override
	public boolean onDisable(WinterPlugin plugin) {
		final var start = System.currentTimeMillis();
		final var result = serviceManager.disableServices(plugin.getInjector());
		if (!result.result()) {
			if (!result.cycles().isEmpty()) {
				plugin.getSLF4JLogger().error(
						"Failed to disable services due to cyclic dependencies: {}",
						result.cycles()
								.stream()
								.map(LifeCycleMethod::service)
								.map(Class::getCanonicalName)
								.collect(Collectors.joining(", "))
				);
			} else {
				plugin.getSLF4JLogger().error("Failed to disable services", result.throwable());
			}
			return false;
		}

		serviceManager.stopTasks(plugin);
		plugin.getSLF4JLogger().info("Disabled {} services in {}ms", serviceManager.services().size(), System.currentTimeMillis() - start);
		return true;
	}
}
