package com.thewinterframework.provider;

import com.google.inject.Binder;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.thewinterframework.plugin.WinterPlugin;
import com.thewinterframework.plugin.module.PluginModule;
import com.thewinterframework.provider.annotation.ProviderComponent;
import com.thewinterframework.provider.processor.ProviderAnnotationProcessor;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.ParameterizedType;
import java.util.HashSet;
import java.util.Set;

public final class ProviderModule implements PluginModule {

	private final Set<Class<? extends Provider<?>>> providers = new HashSet<>();

	@Override
	@SuppressWarnings("unchecked")
	public boolean onLoad(WinterPlugin plugin) {
		final var start = System.currentTimeMillis();
		try {
			this.providers.addAll(
					ProviderAnnotationProcessor.scan(plugin.getClass(), ProviderComponent.class)
							.getClassList()
							.stream()
							.filter(Provider.class::isAssignableFrom)
							.map(clazz -> (Class<? extends Provider<?>>) clazz)
							.toList()
			);
		} catch (Exception e) {
			plugin.getSLF4JLogger().error("Failed to scan providers", e);
			return false;
		}
		plugin.getSLF4JLogger().info("Loaded {} services in {}ms", this.providers.size(), System.currentTimeMillis() - start);
		return true;
	}

	@Override
	public void configure(Binder binder) {
		binder.bindScope(ProviderComponent.class, Scopes.SINGLETON);
		for (final var providerClass : this.providers) {
			bindProvider(binder, providerClass);
		}
		this.providers.clear();
	}

	@SuppressWarnings("unchecked")
	private <O> void bindProvider(final @NotNull Binder binder, final Class<?> providerClass) {
		final var casted = (Class<? extends Provider<O>>) providerClass;
		final var typeLiteral = resolveTypeLiteral(casted);
		binder.bind(typeLiteral).toProvider(casted);
	}

	@SuppressWarnings("unchecked")
	private <T> TypeLiteral<T> resolveTypeLiteral(final @NotNull Class<? extends Provider<T>> providerClass) {
		final var generic = providerClass.getGenericInterfaces()[0];
		if (!(generic instanceof ParameterizedType parameterizedType)) {
			throw new IllegalStateException("Provider is not parameterized");
		}
		final var actualType = parameterizedType.getActualTypeArguments()[0];
		return (TypeLiteral<T>) TypeLiteral.get(actualType);
	}
}
