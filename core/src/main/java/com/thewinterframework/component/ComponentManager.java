package com.thewinterframework.component;

import com.google.inject.Binder;
import com.thewinterframework.component.admission.ComponentDecision;
import com.thewinterframework.component.context.ComponentContext;
import com.thewinterframework.component.decorator.ComponentDecorator;
import com.thewinterframework.component.decorator.ComponentDecoratorHandler;
import com.thewinterframework.component.interceptor.ComponentInterceptor;
import com.thewinterframework.component.meta.ComponentDescriptor;
import com.thewinterframework.plugin.WinterPlugin;
import com.thewinterframework.service.decorator.ServiceDecorator;
import com.thewinterframework.service.decorator.ServiceDecoratorHandler;
import com.thewinterframework.utils.reflect.Reflections;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Comparator;
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

	/**
	 * Runs admission policies and registers the component only when every policy accepts it.
	 * Admission happens before decorator discovery and dependency-injection configuration.
	 *
	 * @param context the candidate component context
	 * @param interceptors interceptors that apply to the candidate
	 * @return {@code true} when the component was registered
	 * @throws Exception when an admission policy fails
	 */
	public boolean registerComponent(
			final @NotNull ComponentContext context,
			final @NotNull Collection<? extends ComponentInterceptor> interceptors
	) throws Exception {
		final var candidates = decoratorCandidates(context.componentType());
		final var orderedInterceptors = interceptors.stream()
				.sorted(Comparator.comparingInt(ComponentInterceptor::order)
						.thenComparing(interceptor -> interceptor.getClass().getName()))
				.toList();
		for (final var interceptor : orderedInterceptors) {
			if (interceptor.decide(context) == ComponentDecision.DISCARD) {
				return false;
			}
		}

		final var orderedDecorators = candidates.values().stream()
				.sorted(Comparator.comparing(handler -> handler.getClass().getName()))
				.toList();
		for (final var decorator : orderedDecorators) {
			if (decorator.decide(context) == ComponentDecision.DISCARD) {
				return false;
			}
		}

		decorators.putAll(candidates);
		discoverDecorators(context.descriptor());
		return true;
	}

	/**
	 * Registers a component without admission policies.
	 *
	 * @deprecated use {@link #registerComponent(ComponentContext, Collection)} so interceptors and decorators can
	 * discard the component before it is bound.
	 */
	@Deprecated(forRemoval = false)
	@SuppressWarnings("unchecked")
	public void registerComponent(final @NotNull ComponentDescriptor component) throws ReflectiveOperationException {
		discoverDecorators(component);
	}

	@SuppressWarnings("unchecked")
	private void discoverDecorators(final ComponentDescriptor component) throws ReflectiveOperationException {
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

	private Map<Class<? extends ComponentDecoratorHandler<?>>, ComponentDecoratorHandler<?>> decoratorCandidates(
			final Class<?> componentType
	) throws ReflectiveOperationException {
		final Map<Class<? extends ComponentDecoratorHandler<?>>, ComponentDecoratorHandler<?>> candidates = new LinkedHashMap<>();
		for (final var decoratorAnnotation : Reflections.findClassAnnotations(componentType, ComponentDecorator.class)) {
			addCandidate(candidates, decoratorAnnotation.annotationType().getAnnotation(ComponentDecorator.class).value());
		}
		for (final var decoratorAnnotation : Reflections.findClassAnnotations(componentType, ServiceDecorator.class)) {
			addCandidate(candidates, decoratorAnnotation.annotationType().getAnnotation(ServiceDecorator.class).value());
		}
		for (final var decoratedMethod : Reflections.findMethodsWith(componentType, ComponentDecorator.class)) {
			addCandidate(candidates, decoratedMethod.annotation().value());
		}
		for (final var decoratedMethod : Reflections.findMethodsWith(componentType, ServiceDecorator.class)) {
			addCandidate(candidates, decoratedMethod.annotation().value());
		}
		return candidates;
	}

	private void addCandidate(
			final Map<Class<? extends ComponentDecoratorHandler<?>>, ComponentDecoratorHandler<?>> candidates,
			final Class<? extends ComponentDecoratorHandler<?>> handlerType
	) {
		candidates.computeIfAbsent(handlerType, type -> {
			final var existing = decorators.get(type);
			return existing == null ? createInstance(type) : existing;
		});
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
