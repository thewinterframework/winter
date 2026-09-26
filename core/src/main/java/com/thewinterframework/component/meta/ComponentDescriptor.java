package com.thewinterframework.component.meta;

import com.thewinterframework.component.annotation.Component;
import com.thewinterframework.component.annotation.ComponentScope;
import com.thewinterframework.component.handler.ComponentHandler;
import com.thewinterframework.component.interceptor.ComponentInterceptor;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

/** Immutable runtime metadata for a component. */
public record ComponentDescriptor(
		Class<?> type,
		ComponentScope scope,
		int order,
		List<Class<?>> after,
		List<Class<?>> before,
		boolean async,
		List<Class<? extends ComponentHandler>> handlers,
		List<Class<? extends ComponentInterceptor>> interceptors
) {

	public static ComponentDescriptor from(final Class<?> type) {
		final var component = findComponent(type);
		if (component == null) {
			throw new IllegalArgumentException(type.getName() + " is not annotated with @Component");
		}

		return new ComponentDescriptor(
				type,
				component.scope(),
				component.order(),
				List.of(component.after()),
				List.of(component.before()),
				component.async(),
				List.of(component.handlers()),
				List.of(component.interceptors())
		);
	}

	public static boolean isComponent(final Class<?> type) {
		return findComponent(type) != null;
	}

	private static Component findComponent(final Class<?> type) {
		final var direct = type.getDeclaredAnnotation(Component.class);
		if (direct != null) {
			return direct;
		}

		return Arrays.stream(type.getDeclaredAnnotations())
				.map(Annotation::annotationType)
				.map(annotationType -> annotationType.getDeclaredAnnotation(Component.class))
				.filter(component -> component != null)
				.findFirst()
				.orElse(null);
	}
}
