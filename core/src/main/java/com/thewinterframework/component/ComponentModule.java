package com.thewinterframework.component;

import com.google.inject.Binder;
import com.google.inject.Scopes;
import com.thewinterframework.component.annotation.Component;
import com.thewinterframework.component.context.ComponentContext;
import com.thewinterframework.component.handler.ComponentHandler;
import com.thewinterframework.component.interceptor.ComponentInterceptor;
import com.thewinterframework.component.lifecycle.ComponentPhase;
import com.thewinterframework.component.meta.ComponentDescriptor;
import com.thewinterframework.component.meta.ComponentOrder;
import com.thewinterframework.plugin.WinterPlugin;
import com.thewinterframework.service.ReloadServiceManager;
import com.thewinterframework.service.ServiceManager;
import com.thewinterframework.wire.condition.context.ConditionContext;
import com.thewinterframework.wire.module.AbstractProcessorModule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Runtime module for every class declared with the unified {@link Component} model. */
public final class ComponentModule extends AbstractProcessorModule {

	private final ServiceManager componentManager = new ServiceManager();
	private final Map<Class<? extends ComponentHandler>, ComponentHandler> handlers = new LinkedHashMap<>();
	private final Map<Class<? extends ComponentInterceptor>, ComponentInterceptor> interceptors = new LinkedHashMap<>();
	private final Set<Class<? extends ComponentInterceptor>> globalInterceptors = new HashSet<>();
	private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
	private List<ComponentDescriptor> components = List.of();

	public ComponentModule() {
		super(Component.class);
	}

	@Override
	public boolean onLoad(final WinterPlugin plugin) throws Exception {
		initWire(plugin);
		discoverExtensions(plugin);

		final var discovered = new ArrayList<ComponentDescriptor>();
		for (final var componentType : wire.getWiredClasses()) {
			final var descriptor = ComponentDescriptor.from(componentType);
			final var context = new ConditionContext(plugin, componentType);
			if (!evaluateConditions(componentType, com.thewinterframework.plugin.module.Stage.LOAD, context, null)) {
				plugin.getSLF4JLogger().debug("Skipped LOAD (condition failed) for {}", componentType.getSimpleName());
				continue;
			}

			registerExplicitInterceptors(descriptor);
			if (!componentManager.registerComponent(
					new ComponentContext(plugin, descriptor),
					selectedInterceptors(descriptor)
			)) {
				plugin.getSLF4JLogger().debug("Discarded component during admission: {}", componentType.getName());
				continue;
			}
			registerExplicitHandlers(descriptor);
			discovered.add(descriptor);
		}

		components = ComponentOrder.resolve(discovered);
		componentManager.loadDecorators(plugin);
		runPhase(plugin, ComponentPhase.LOAD, components);
		plugin.getSLF4JLogger().info("Loaded {} components", components.size());
		return true;
	}

	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void configure(final Binder binder) {
		binder.bind(ComponentManager.class).toInstance(componentManager);
		binder.bind(ServiceManager.class).toInstance(componentManager);
		binder.bind(ReloadServiceManager.class).in(Scopes.SINGLETON);

		for (final var component : components) {
			binder.bind((Class) component.type()).in(Scopes.SINGLETON);
		}

		componentManager.configureDecorators(binder);
		orderedHandlers().forEach(handler -> handler.configure(binder, supportedComponents(handler)));
	}

	@Override
	public boolean onEnable(final WinterPlugin plugin) throws Exception {
		injectExtensions(plugin);
		final var enabled = new ArrayList<ComponentDescriptor>();
		for (final var component : components) {
			final var context = new ConditionContext(plugin, component.type());
			if (evaluateConditions(component.type(), com.thewinterframework.plugin.module.Stage.ENABLE, context, plugin.getInjector())) {
				enabled.add(component);
			} else {
				plugin.getSLF4JLogger().debug("Skipped ENABLE (condition failed) for {}", component.type().getSimpleName());
			}
		}

		runPhase(plugin, ComponentPhase.ENABLE, enabled);
		componentManager.enableDecorators(plugin);
		plugin.getSLF4JLogger().info("Enabled {} components", enabled.size());
		return true;
	}

	@Override
	public boolean onDisable(final WinterPlugin plugin) throws Exception {
		try {
			componentManager.disableDecorators(plugin);
			final var reverse = new ArrayList<>(components);
			java.util.Collections.reverse(reverse);
			runPhase(plugin, ComponentPhase.DISABLE, reverse);
		} finally {
			executor.close();
		}
		plugin.getSLF4JLogger().info("Disabled {} components", components.size());
		return true;
	}

	private void discoverExtensions(final WinterPlugin plugin) {
		ServiceLoader.load(ComponentHandler.class, plugin.getClass().getClassLoader())
				.forEach(handler -> handlers.putIfAbsent(handler.getClass(), handler));
		ServiceLoader.load(ComponentInterceptor.class, plugin.getClass().getClassLoader())
				.forEach(interceptor -> {
					interceptors.putIfAbsent(interceptor.getClass(), interceptor);
					globalInterceptors.add(interceptor.getClass());
				});
	}

	private void registerExplicitHandlers(final ComponentDescriptor component) {
		component.handlers().forEach(handler -> handlers.computeIfAbsent(handler, this::createInstance));
	}

	private void registerExplicitInterceptors(final ComponentDescriptor component) {
		component.interceptors().forEach(interceptor -> interceptors.computeIfAbsent(interceptor, this::createInstance));
	}

	private void injectExtensions(final WinterPlugin plugin) {
		handlers.values().forEach(plugin.getInjector()::injectMembers);
		interceptors.values().forEach(plugin.getInjector()::injectMembers);
	}

	private List<ComponentHandler> orderedHandlers() {
		return handlers.values().stream()
				.sorted(Comparator.comparingInt(ComponentHandler::order).thenComparing(handler -> handler.getClass().getName()))
				.toList();
	}

	private Collection<ComponentDescriptor> supportedComponents(final ComponentHandler handler) {
		return components.stream().filter(component -> supports(handler, component)).toList();
	}

	private boolean supports(final ComponentHandler handler, final ComponentDescriptor component) {
		return component.handlers().contains(handler.getClass()) || handler.supports(component.type());
	}

	private List<ComponentInterceptor> selectedInterceptors(final ComponentDescriptor component) {
		final var selectedInterceptorTypes = new HashSet<>(globalInterceptors);
		selectedInterceptorTypes.addAll(component.interceptors());
		return selectedInterceptorTypes.stream()
				.map(interceptors::get)
				.filter(java.util.Objects::nonNull)
				.sorted(Comparator.comparingInt(ComponentInterceptor::order)
						.thenComparing(interceptor -> interceptor.getClass().getName()))
				.toList();
	}

	private void runPhase(
			final WinterPlugin plugin,
			final ComponentPhase phase,
			final List<ComponentDescriptor> ordered
	) throws Exception {
		if (phase == ComponentPhase.DISABLE) {
			for (final var component : ordered) {
				executeComponent(plugin, phase, component);
			}
			return;
		}

		final var completion = new LinkedHashMap<Class<?>, CompletableFuture<Void>>();
		for (final var component : ordered) {
			final Runnable lifecycle = () -> executeComponent(plugin, phase, component);
			if (component.async()) {
				final var dependencies = componentDependencies(component, ordered).stream()
						.map(completion::get)
						.filter(java.util.Objects::nonNull)
						.toArray(CompletableFuture[]::new);
				completion.put(
						component.type(),
						CompletableFuture.allOf(dependencies).thenRunAsync(lifecycle, executor)
				);
			} else {
				await(completion.values());
				lifecycle.run();
				completion.put(component.type(), CompletableFuture.completedFuture(null));
			}
		}
		await(completion.values());
	}

	private Set<Class<?>> componentDependencies(
			final ComponentDescriptor component,
			final Collection<ComponentDescriptor> allComponents
	) {
		final var dependencies = new HashSet<>(component.after());
		for (final var candidate : allComponents) {
			if (candidate.before().contains(component.type())) {
				dependencies.add(candidate.type());
			}
		}
		return dependencies;
	}

	private void executeComponent(
			final WinterPlugin plugin,
			final ComponentPhase phase,
			final ComponentDescriptor component
	) {
		final var context = new ComponentContext(plugin, component);
		final var selectedInterceptors = selectedInterceptors(component);

		try {
			for (final var interceptor : selectedInterceptors) {
				interceptor.before(phase, context);
			}
			for (final var handler : orderedHandlers()) {
				if (supports(handler, component)) {
					invoke(handler, phase, context);
				}
			}
			for (var index = selectedInterceptors.size() - 1; index >= 0; index--) {
				selectedInterceptors.get(index).after(phase, context);
			}
		} catch (final Throwable failure) {
			for (final var interceptor : selectedInterceptors) {
				try {
					interceptor.onFailure(phase, context, failure);
				} catch (final Throwable callbackFailure) {
					failure.addSuppressed(callbackFailure);
				}
			}
			throw new CompletionException(failure);
		}
	}

	private void invoke(
			final ComponentHandler handler,
			final ComponentPhase phase,
			final ComponentContext context
	) throws Exception {
		switch (phase) {
			case LOAD -> handler.onLoad(context);
			case ENABLE -> handler.onEnable(context);
			case DISABLE -> handler.onDisable(context);
		}
	}

	private void await(final Collection<CompletableFuture<Void>> futures) throws Exception {
		try {
			CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
		} catch (final CompletionException exception) {
			final var cause = exception.getCause();
			if (cause instanceof final Exception checked) {
				throw checked;
			}
			throw exception;
		}
	}

	private <T> T createInstance(final Class<T> type) {
		try {
			return type.getDeclaredConstructor().newInstance();
		} catch (final ReflectiveOperationException exception) {
			throw new IllegalStateException("Cannot create component extension " + type.getName(), exception);
		}
	}
}
