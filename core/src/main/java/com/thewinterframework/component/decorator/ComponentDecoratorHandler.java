package com.thewinterframework.component.decorator;

import com.google.inject.Binder;
import com.thewinterframework.component.admission.ComponentDecision;
import com.thewinterframework.component.context.ComponentContext;
import com.thewinterframework.plugin.WinterPlugin;
import com.thewinterframework.utils.reflect.AnnotatedMethodHandle;

import java.lang.annotation.Annotation;

/** Handles a reusable type- or method-level component decorator. */
public interface ComponentDecoratorHandler<A extends Annotation> {

	Class<A> getAnnotationType();

	/**
	 * Evaluates a component that declares this decorator before discovery callbacks and binding.
	 *
	 * @param context the candidate component
	 * @return {@link ComponentDecision#DISCARD} to remove the component from the pipeline
	 * @throws Exception when the admission policy cannot be evaluated
	 */
	default ComponentDecision decide(final ComponentContext context) throws Exception {
		return ComponentDecision.CONTINUE;
	}

	default void onDiscover(final Class<?> component, final AnnotatedMethodHandle<A> method) {
	}

	default void onDiscoverOnType(final Class<?> component, final A annotation) {
	}

	default void onConfigure(final Binder binder) {
	}

	default void onPluginLoad(final WinterPlugin plugin) {
	}

	default void onPluginEnable(final WinterPlugin plugin) {
	}

	default void onPluginDisable(final WinterPlugin plugin) {
	}
}
