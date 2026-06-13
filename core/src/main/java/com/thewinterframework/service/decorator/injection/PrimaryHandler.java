package com.thewinterframework.service.decorator.injection;

import com.google.inject.Binder;
import com.google.inject.Singleton;
import com.thewinterframework.service.annotation.injection.Primary;
import com.thewinterframework.service.decorator.ServiceDecoratorHandler;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"unchecked", "rawtypes"})
public class PrimaryHandler implements ServiceDecoratorHandler<Primary> {

	private final Map<Class<?>, Class<?>> bindAsMap = new HashMap<>();

	@Override
	public Class<Primary> getAnnotationType() {
		return Primary.class;
	}

	@Override
	public void onDiscoverOnType(final Class<?> service, final Primary annotation) {
		final var superType = annotation.value();
		if (superType != Void.class) {
			bindAsMap.put(annotation.value(), superType);
			return;
		}

		final var interfaces = service.getInterfaces();
		if (interfaces.length == 0) {
			throw new RuntimeException("Service " + service.getName() + " has no interfaces! and is annotated with @Primary");
		}

		final var firstInterface = interfaces[0];
		bindAsMap.put(firstInterface, service);
	}

	@Override
	public void onConfigure(final Binder binder) {
		for (final var entry : bindAsMap.entrySet()) {
			final var superType = (Class) entry.getKey();
			final var impl = (Class) entry.getValue();

			binder.bind(superType).to(impl).in(Singleton.class);
		}
	}
}
