package com.thewinterframework.utils.reflect;

import com.google.inject.Injector;
import com.google.inject.Key;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Represents a method handle annotated with a specific annotation.
 *
 * @param <T> The annotation type.
 */
public record AnnotatedMethodHandle<T>(Method reflect, MethodHandle handle, T annotation, List<Key<?>> parameters) {
	/**
	 * Invokes the method handle with the given parameters.
	 *
	 * @param clazz The class to invoke the method on.
	 * @param injector The injector to get the instances from.
	 * @throws Throwable If an error occurs while invoking the method.
	 */
	public void invoke(Class<?> clazz, Injector injector) throws Throwable {
		handle.invokeWithArguments(Reflections.injectKeys(clazz, parameters, injector));
	}

	/**
	 * Returns an AnnotatedMethodHandle for the specified annotation type.
	 *
	 * @param annotationType The annotation type to retrieve.
	 * @param <A>            The type of the annotation.
	 * @return An AnnotatedMethodHandle for the specified annotation type.
	 * @throws IllegalArgumentException If the method is not annotated with the specified annotation type.
	 */
	public <A extends Annotation> AnnotatedMethodHandle<A> annotatedWith(final Class<A> annotationType) {
		if (!reflect.isAnnotationPresent(annotationType)) {
			throw new IllegalArgumentException("The method " + handle + " is not annotated with @" + annotationType);
		}

		return new AnnotatedMethodHandle<>(
				reflect,
				handle,
				reflect.getAnnotation(annotationType),
				parameters
		);
	}
}
