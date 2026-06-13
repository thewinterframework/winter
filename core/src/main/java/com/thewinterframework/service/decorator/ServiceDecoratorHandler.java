package com.thewinterframework.service.decorator;

import com.google.inject.Binder;
import com.thewinterframework.plugin.WinterPlugin;
import com.thewinterframework.utils.reflect.AnnotatedMethodHandle;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Handler for service decorators (annotations).
 *
 * @param <A> The type of annotation this handler processes.
 */
public interface ServiceDecoratorHandler<A extends Annotation> {

	/**
	 * Gets the type of annotation this handler processes.
	 *
	 * @return The annotation type.
	 */
	Class<A> getAnnotationType();

	/**
	 * Called when a method with the decorator annotation is discovered.
	 *
	 * @param service The class containing the annotated method.
	 * @param method  The annotated method handle.
	 */
	default void onDiscover(final Class<?> service, final AnnotatedMethodHandle<A> method) {}

	/**
	 * Called when a method with the decorator annotation is discovered.
	 * @param service The class containing the annotated method.
	 * @param annotation The annotation.
	 */
	default void onDiscoverOnType(final Class<?> service, final A annotation) {}

	/**
	 * Called when the plugin is configured.
	 * @param binder The Guice binder.
	 */
	default void onConfigure(final Binder binder) {}

	/**
	 * Called when a plugin is loaded.
	 *
	 * @param plugin The plugin being loaded.
	 */
	default void onPluginLoad(final WinterPlugin plugin) {}

	/**
	 * Called when a plugin is enabled.
	 *
	 * @param plugin The plugin being enabled.
	 */
	default void onPluginEnable(final WinterPlugin plugin) {}

	/**
	 * Called when a plugin is disabled.
	 *
	 * @param plugin The plugin being disabled.
	 */
	default void onPluginDisable(final WinterPlugin plugin) {}

}
