package com.thewinterframework.utils;

import com.google.inject.BindingAnnotation;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Named;
import jakarta.inject.Qualifier;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for reflection operations.
 */
public class Reflections {

	/**
	 * Represents a method handle annotated with a specific annotation.
	 *
	 * @param <T> The annotation type.
	 */
	public record AnnotatedMethodHandle<T>(MethodHandle handle, T annotation, List<Key<?>> parameters) {
		/**
		 * Invokes the method handle with the given parameters.
		 *
		 * @param clazz The class to invoke the method on.
		 * @param injector The injector to get the instances from.
		 * @throws Throwable If an error occurs while invoking the method.
		 */
		public void invoke(Class<?> clazz, Injector injector) throws Throwable {
			handle.invokeWithArguments(injectKeys(clazz, parameters, injector));
		}
	}

	/**
	 * Finds all methods in a class that are annotated with a specific annotation.
	 *
	 * @param clazz The class to search in.
	 * @param annotation The annotation to search for.
	 * @param <T> The annotation type.
	 * @return A list of annotated method handles.
	 * @throws IllegalAccessException If the method cannot be accessed.
	 * @throws NoSuchMethodException If the method does not exist.
	 */
	public static <T extends Annotation> List<AnnotatedMethodHandle<T>> findMethodsWith(Class<?> clazz, Class<T> annotation) throws IllegalAccessException, NoSuchMethodException {
		final var list = new ArrayList<AnnotatedMethodHandle<T>>();
		for (final var method : clazz.getDeclaredMethods()) {
			if (method.isAnnotationPresent(annotation)) {
				final var handle = MethodHandles.privateLookupIn(clazz, MethodHandles.lookup())
						.findVirtual(clazz, method.getName(), MethodType.methodType(method.getReturnType(), method.getParameterTypes()));

				list.add(
						new AnnotatedMethodHandle<>(
								handle,
								method.getAnnotation(annotation),
								findMethodParameters(method)
						)
				);
			}
		}

		return list;
	}

	/**
	 * Finds the parameters of a method.
	 *
	 * @param method The method to find the parameters of.
	 * @return A list of keys representing the parameters.
	 */
	public static List<Key<?>> findMethodParameters(Method method) {
		final var parameters = method.getGenericParameterTypes();
		final var typeParameters = method.getParameters();
		final var keys = new ArrayList<Key<?>>();

		for (int i = 0; i < parameters.length; i++) {
			final var untypedParameter = typeParameters[i];
			final var typedParameter = parameters[i];

			if (untypedParameter.getAnnotations().length != 0) {
				final var bindingAnnotation = findBindingAnnotation(untypedParameter);
				if (bindingAnnotation != null) {
					keys.add(Key.get(typedParameter, bindingAnnotation));
					continue;
				}
			}

			keys.add(Key.get(typedParameter));
		}

		return keys;
	}

	/**
	 * Injects the keys into the injector and returns the objects.
	 *
	 * @param instance The instance to inject the keys into.
	 * @param keys The keys to inject.
	 * @param injector The injector to get the instances from.
	 * @return The objects.
	 */
	public static Object[] injectKeys(Class<?> instance, List<Key<?>> keys, Injector injector) {
		final var objects = new Object[keys.size() + 1];
		objects[0] = injector.getInstance(instance);
		for (int i = 0; i < keys.size(); i++) {
			objects[i + 1] = injector.getInstance(keys.get(i));
		}
		return objects;
	}

	/**
	 * Gets the generic type of class that implements an interface.
	 *
	 * @param clazz The class to get the generic type from.
	 * @param interfaze The interface to get the generic type from.
	 * @param index The index of the generic type.
	 * @return The generic type.
	 */
	public static Type getGenericType(Class<?> clazz, Class<?> interfaze, int index) {
		for (final var type : clazz.getGenericInterfaces()) {
			if (type instanceof ParameterizedType parameterizedType) {
				final var rawType = parameterizedType.getRawType();
				if (rawType instanceof Class<?> && interfaze.isAssignableFrom((Class<?>) rawType)) {
					return parameterizedType.getActualTypeArguments()[index];
				}
			}
		}
		return null;
	}

	/**
	 * Converts a type to a class.
	 *
	 * @param type The type to convert.
	 * @return The class.
	 */
	public static Class<?> toClass(Type type) {
		if (type instanceof Class) {
			return (Class<?>) type;
		} else if (type instanceof ParameterizedType) {
			return (Class<?>) ((ParameterizedType) type).getRawType();
		}
		throw new IllegalArgumentException("Unsupported type: " + type);
	}

	/**
	 * Finds the binding annotation of an element.
	 *
	 * @param element The element to find the binding annotation of.
	 * @return The binding annotation.
	 */
	public static Annotation findBindingAnnotation(AnnotatedElement element) {
		for (final var annotation : element.getAnnotations()) {
			final var annotationType = annotation.annotationType();
			if (annotationType.equals(Named.class) ||
					annotationType.equals(jakarta.inject.Named.class) ||
					annotationType.isAnnotationPresent(BindingAnnotation.class) ||
					annotationType.isAnnotationPresent(Qualifier.class)
			) {
				return annotation;
			}
		}

		return null;
	}

}
