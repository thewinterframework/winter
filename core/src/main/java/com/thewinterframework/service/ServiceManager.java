package com.thewinterframework.service;

import com.thewinterframework.plugin.WinterPlugin;
import com.thewinterframework.service.decorator.ServiceDecorator;
import com.thewinterframework.service.decorator.ServiceDecoratorHandler;
import com.thewinterframework.utils.reflect.Reflections;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * <p> This class is responsible for managing services. </p>
 */
public class ServiceManager {

	private final Map<Class<? extends ServiceDecoratorHandler<?>>, ServiceDecoratorHandler<?>> handlers = new HashMap<>();
	private final Set<Class<?>> registeredServices = new HashSet<>();

	/**
	 * Register a service with the service manager.
	 *
	 * @param service The service to register.
	 */
	@SuppressWarnings("unchecked")
	public void registerService(final @NotNull Class<?> service) throws IllegalAccessException, NoSuchMethodException {
		final var serviceDecorators = Reflections.findMethodsWith(service, ServiceDecorator.class);
		for (final var serviceDecoratorMethod : serviceDecorators) {
			final var decoratorAnnotation = serviceDecoratorMethod.annotation();
			final var handlerClass = decoratorAnnotation.value();

			final var handler = (ServiceDecoratorHandler<Annotation>) handlers.computeIfAbsent(handlerClass, this::createInstance);
			final var annotatedMethod = serviceDecoratorMethod.annotatedWith(handler.getAnnotationType());
			handler.onDiscover(service, annotatedMethod);
		}

		registeredServices.add(service);
	}

	/**
	 * Load all plugin service handlers.
	 * @param plugin The plugin to load handlers for.
	 */
	public void loadHandlers(final WinterPlugin plugin) {
		for (final var handler : handlers.values()) {
			handler.onPluginLoad(plugin);
		}
	}

	/**
	 * Start all plugin service handlers.
	 * @param plugin The plugin to start handlers for.
	 */
	public void startHandlers(final WinterPlugin plugin) {
		for (final var handler : handlers.values()) {
			handler.onPluginEnable(plugin);
		}
	}

	/**
	 * Stop all plugin service handlers.
	 * @param plugin The plugin to stop handlers for.
	 */
	public void stopHandlers(final WinterPlugin plugin) {
		for (final var handler : handlers.values()) {
			handler.onPluginDisable(plugin);
		}
	}

	/**
	 * Get a handler by its class.
	 * @param handlerClass The handler class.
	 * @return The handler instance or null if not found.
	 * @param <T> The type of the handler.
	 */
	@Nullable
	public <T extends ServiceDecoratorHandler<?>> T getHandler(final Class<T> handlerClass) {
		final var handler = handlers.get(handlerClass);
		if (handler == null) {
			return null;
		}

		return handlerClass.cast(handler);
	}

	/**
	 * Get the services.
	 *
	 * @return The services.
	 */
	public Set<Class<?>> services() {
		return registeredServices;
	}

	/**
	 * Creates an instance of the given class.
	 *
	 * @param clazz the class to create an instance of.
	 * @param <T>   the type of the class.
	 * @return an instance of the given class.
	 */
	private <T> T createInstance(final Class<T> clazz) {
		try {
			return clazz.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
