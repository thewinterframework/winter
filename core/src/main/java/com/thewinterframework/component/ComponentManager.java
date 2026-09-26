package com.thewinterframework.component;

import com.google.inject.Binder;
import com.thewinterframework.component.decorator.ComponentDecorator;
import com.thewinterframework.component.decorator.ComponentDecoratorHandler;
import com.thewinterframework.component.meta.ComponentDescriptor;
import com.thewinterframework.plugin.WinterPlugin;
import com.thewinterframework.service.decorator.ServiceDecorator;
import com.thewinterframework.service.decorator.ServiceDecoratorHandler;
import com.thewinterframework.utils.reflect.Reflections;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Registry for unified components and their decorators.
 */
public class ComponentManager {

	private final Map<Class<? extends ComponentDecoratorHandler<?>>, ComponentDecoratorHandler<?>> decorators = new LinkedHashMap<>();
	private final Map<Class<?>, ComponentDescriptor> components = new LinkedHashMap<>();

	@SuppressWarnings("unchecked")
	public void registerComponent(final @NotNull ComponentDescriptor component) throws ReflectiveOperationException {
		final var componentType = component.type();
		for (final var decoratorAnnotation : Reflections.findClassAnnotations(componentType, ComponentDecorator.class)) {
			final var decorator = decoratorAnnotation.annotationType().getAnnotation(ComponentDecorator.class);
			final var handler = (ComponentDecoratorHandler<Annotation>) decorators.computeIfAbsent(
					decorator.value(),
					this::createInstance
			);
			handler.onDiscoverOnType(componentType, decoratorAnnotation);
		}

		for (final var decoratorAnnotation : Reflections.findClassAnnotations(componentType, ServiceDecorator.class)) {
			final var decorator = decoratorAnnotation.annotationType().getAnnotation(ServiceDecorator.class);
			final var handler = (ServiceDecoratorHandler<Annotation>) decorators.computeIfAbsent(
					decorator.value(),
					this::createInstance
			);
			handler.onDiscoverOnType(componentType, decoratorAnnotation);
		}

		for (final var decoratedMethod : Reflections.findMethodsWith(componentType, ComponentDecorator.class)) {
			final var decoratorAnnotation = decoratedMethod.annotation();
			final var handler = (ComponentDecoratorHandler<Annotation>) decorators.computeIfAbsent(
					decoratorAnnotation.value(),
					this::createInstance
			);
			handler.onDiscover(componentType, decoratedMethod.annotatedWith(handler.getAnnotationType()));
		}

		for (final var decoratedMethod : Reflections.findMethodsWith(componentType, ServiceDecorator.class)) {
			final var decoratorAnnotation = decoratedMethod.annotation();
			final var handler = (ServiceDecoratorHandler<Annotation>) decorators.computeIfAbsent(
					decoratorAnnotation.value(),
					this::createInstance
			);
			handler.onDiscover(componentType, decoratedMethod.annotatedWith(handler.getAnnotationType()));
		}

		components.put(componentType, component);
	}

	public void loadDecorators(final WinterPlugin plugin) {
		decorators.values().forEach(handler -> handler.onPluginLoad(plugin));
	}

	public void configureDecorators(final Binder binder) {
		decorators.values().forEach(handler -> handler.onConfigure(binder));
	}

	public void enableDecorators(final WinterPlugin plugin) {
		decorators.values().forEach(handler -> handler.onPluginEnable(plugin));
	}

	public void disableDecorators(final WinterPlugin plugin) {
		decorators.values().forEach(handler -> handler.onPluginDisable(plugin));
	}

	@Nullable
	public <T extends ComponentDecoratorHandler<?>> T getDecorator(final Class<T> handlerType) {
		final var handler = decorators.get(handlerType);
		return handler == null ? null : handlerType.cast(handler);
	}

	public Set<ComponentDescriptor> components() {
		return new LinkedHashSet<>(components.values());
	}

	private <T> T createInstance(final Class<T> type) {
		try {
			return type.getDeclaredConstructor().newInstance();
		} catch (final ReflectiveOperationException exception) {
			throw new IllegalStateException("Cannot create component extension " + type.getName(), exception);
		}
	}
}
