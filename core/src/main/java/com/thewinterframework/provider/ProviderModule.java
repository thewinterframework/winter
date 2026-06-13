package com.thewinterframework.provider;

import com.google.inject.Binder;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.thewinterframework.provider.annotation.ProviderComponent;
import com.thewinterframework.wire.module.AbstractProcessorModule;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.ParameterizedType;

public class ProviderModule extends AbstractProcessorModule {
	public ProviderModule() {
		super(ProviderComponent.class);
	}

	@Override
	public void configure(final Binder binder) {
		binder.bindScope(ProviderComponent.class, Scopes.SINGLETON);
		for (final var providerClass : this.activeComponents) {
			bindProvider(binder, (Class<? extends Provider<Object>>) providerClass);
		}
		this.activeComponents.clear();
	}

	private static <O, T extends Provider<O>> void bindProvider(final @NotNull Binder binder, final Class<T> providerClass) {
		final var typeLiteral = resolveTypeLiteral(providerClass);
		binder.bind(typeLiteral).toProvider(providerClass);
	}

	@SuppressWarnings("unchecked")
	private static <T> TypeLiteral<T> resolveTypeLiteral(final @NotNull Class<? extends Provider<T>> providerClass) {
		final var generic = providerClass.getGenericInterfaces()[0];
		if (!(generic instanceof final ParameterizedType parameterizedType)) {
			throw new IllegalStateException("Provider is not parameterized");
		}
		final var actualType = parameterizedType.getActualTypeArguments()[0];
		return (TypeLiteral<T>) TypeLiteral.get(actualType);
	}
}
