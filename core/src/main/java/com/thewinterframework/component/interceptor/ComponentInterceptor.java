package com.thewinterframework.component.interceptor;

import com.thewinterframework.component.admission.ComponentDecision;
import com.thewinterframework.component.context.ComponentContext;
import com.thewinterframework.component.lifecycle.ComponentPhase;

/** Intercepts a component lifecycle without coupling the component to its infrastructure. */
public interface ComponentInterceptor {

	/**
	 * Evaluates the component before decorator discovery and dependency-injection binding.
	 *
	 * @param context the candidate component
	 * @return {@link ComponentDecision#DISCARD} to remove the component from the pipeline
	 * @throws Exception when the admission policy cannot be evaluated
	 */
	default ComponentDecision decide(final ComponentContext context) throws Exception {
		return ComponentDecision.CONTINUE;
	}

	default int order() {
		return 0;
	}

	default void before(final ComponentPhase phase, final ComponentContext context) throws Exception {
	}

	default void after(final ComponentPhase phase, final ComponentContext context) throws Exception {
	}

	default void onFailure(
			final ComponentPhase phase,
			final ComponentContext context,
			final Throwable failure
	) {
	}
}
