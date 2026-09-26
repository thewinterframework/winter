package com.thewinterframework.provider.handler;

import com.google.auto.service.AutoService;
import com.google.inject.Binder;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.thewinterframework.component.handler.ComponentHandler;
import com.thewinterframework.component.meta.ComponentDescriptor;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

/** Adds Guice provider bindings for provider components. */
@AutoService(ComponentHandler.class)
public final class ProviderComponentHandler implements ComponentHandler {

	@Override
	public boolean supports(final Class<?> componentType) {
		return Provider.class.isAssignableFrom(componentType);
	}

	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void configure(final Binder binder, final Collection<ComponentDescriptor> components) {
		for (final var component : components) {
			final var providerType = (Class<? extends Provider<?>>) component.type();
			final var providedType = resolveProvidedType(providerType);
			binder.bind((TypeLiteral) TypeLiteral.get(providedType)).toProvider((Class) providerType);
		}
	}

	private Type resolveProvidedType(final Class<? extends Provider<?>> providerType) {
		for (final var candidate : providerType.getGenericInterfaces()) {
			if (candidate instanceof final ParameterizedType parameterized
					&& parameterized.getRawType().equals(Provider.class)) {
				return parameterized.getActualTypeArguments()[0];
			}
		}
		throw new IllegalStateException(providerType.getName() + " must declare Provider<T> directly");
	}
}
