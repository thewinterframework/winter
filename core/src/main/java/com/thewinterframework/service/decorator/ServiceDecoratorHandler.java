package com.thewinterframework.service.decorator;

import com.thewinterframework.plugin.WinterPlugin;
import com.thewinterframework.utils.reflect.AnnotatedMethodHandle;

import java.lang.annotation.Annotation;

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
	void onDiscover(final Class<?> service, final AnnotatedMethodHandle<A> method);

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
