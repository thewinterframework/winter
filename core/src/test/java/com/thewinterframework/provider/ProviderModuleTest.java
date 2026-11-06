package com.thewinterframework.provider;

import com.google.inject.Guice;
import com.google.inject.Provider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProviderModuleTest {

	@Test
	void resolvesProviderWhenAnotherInterfaceComesFirst() {
		final var module = new TestProviderModule();
		module.add(MultiInterfaceProvider.class);

		final var injector = Guice.createInjector(module);

		assertEquals("provided", injector.getInstance(String.class));
	}

	@Test
	void resolvesProviderThroughGenericSuperclass() {
		final var module = new TestProviderModule();
		module.add(InheritedProvider.class);

		assertEquals("provided", Guice.createInjector(module).getInstance(String.class));
	}

	private static final class TestProviderModule extends ProviderModule {
		private void add(final Class<?> providerType) {
			activeComponents.add(providerType);
		}
	}

	private interface Marker {
	}

	public static final class MultiInterfaceProvider implements Marker, Provider<String> {
		@Override
		public String get() {
			return "provided";
		}
	}

	public abstract static class BaseProvider<T> implements Provider<T> {
	}

	public static final class InheritedProvider extends BaseProvider<String> {
		@Override
		public String get() {
			return "provided";
		}
	}
}
