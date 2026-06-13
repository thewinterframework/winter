package com.thewinterframework.module;

import com.google.inject.Binder;
import com.google.inject.Scopes;
import com.thewinterframework.module.annotation.ModuleComponent;
import com.thewinterframework.plugin.WinterPlugin;
import com.thewinterframework.plugin.module.PluginModule;
import com.thewinterframework.wire.module.AbstractProcessorModule;

import java.util.ArrayList;
import java.util.List;

/**
 * Module for plugin components.
 * <p> This module scans for classes annotated with {@link ModuleComponent} and binds them to the injector. </p>
 */
public class PluginComponentModule extends AbstractProcessorModule {

	public PluginComponentModule() {
		super(ModuleComponent.class);
	}

	@Override
	public List<Class<? extends PluginModule>> depends(final WinterPlugin plugin) {
		try {
			initWire(plugin);

			final var depends = new ArrayList<Class<? extends PluginModule>>();
			for (final var clazz : wire.getWiredClasses()) {
				depends.add((Class<? extends PluginModule>) clazz);
			}
			return depends;
		} catch (final Exception e) {
			plugin.getSLF4JLogger().error("Failed to scan for module components", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean onLoad(final WinterPlugin plugin) {
		return true;
	}

	@Override
	public boolean onEnable(final WinterPlugin plugin) {
		return true;
	}

	@Override
	public boolean onDisable(final WinterPlugin plugin) {
		return true;
	}

	@Override
	public void configure(final Binder binder) {
		binder.bindScope(ModuleComponent.class, Scopes.SINGLETON);
	}
}
