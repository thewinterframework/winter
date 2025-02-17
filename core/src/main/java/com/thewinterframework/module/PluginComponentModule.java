package com.thewinterframework.module;

import com.google.inject.Binder;
import com.google.inject.Scopes;
import com.thewinterframework.module.annotation.ModuleComponent;
import com.thewinterframework.plugin.WinterPlugin;
import com.thewinterframework.plugin.module.PluginModule;
import com.thewinterframework.processor.provider.AutoGeneratedClassListProvider;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Module for plugin components.
 * <p> This module scans for classes annotated with {@link ModuleComponent} and binds them to the injector. </p>
 */
public class PluginComponentModule implements PluginModule {
	@Override
	public List<Class<? extends PluginModule>> depends(WinterPlugin plugin) {
		try {
			final var classListProvider = AutoGeneratedClassListProvider.scan(plugin.getClass(), ModuleComponent.class);
			final var depends = new ArrayList<Class<? extends PluginModule>>();
			for (final var clazz : classListProvider.getClassList()) {
				depends.add((Class<? extends PluginModule>) clazz);
			}

			return depends;
		} catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
			plugin.getSLF4JLogger().error("Failed to scan for module components", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void configure(Binder binder) {
		binder.bindScope(ModuleComponent.class, Scopes.SINGLETON);
	}
}
