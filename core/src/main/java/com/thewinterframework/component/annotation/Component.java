package com.thewinterframework.component.annotation;

import com.thewinterframework.component.handler.ComponentHandler;
import com.thewinterframework.component.interceptor.ComponentInterceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a Winter-managed component.
 *
 * <p>The annotation can be placed directly on a class or on another annotation to create a
 * domain-specific component stereotype. Components are singletons by default.</p>
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Component {

	ComponentScope scope() default ComponentScope.LOCAL;

	/** Lower values are loaded first when no explicit dependency decides the order. */
	int order() default 0;

	/** Components that must be loaded before this component. */
	Class<?>[] after() default {};

	/** Components that must be loaded after this component. */
	Class<?>[] before() default {};

	/**
	 * Allows lifecycle work for this component to run on a worker thread.
	 * This is opt-in because most platform APIs are not thread-safe.
	 */
	boolean async() default false;

	/** Additional handlers applied to this component. */
	Class<? extends ComponentHandler>[] handlers() default {};

	/** Lifecycle interceptors applied around this component. */
	Class<? extends ComponentInterceptor>[] interceptors() default {};
}
