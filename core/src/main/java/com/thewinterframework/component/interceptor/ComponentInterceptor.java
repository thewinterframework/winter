package com.thewinterframework.component.interceptor;

import com.thewinterframework.component.context.ComponentContext;
import com.thewinterframework.component.lifecycle.ComponentPhase;

/** Intercepts a component lifecycle without coupling the component to its infrastructure. */
public interface ComponentInterceptor {

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
