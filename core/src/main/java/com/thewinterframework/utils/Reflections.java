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

public class Reflections {

	public record AnnotatedMethodHandle<T>(MethodHandle handle, T annotation, List<Key<?>> parameters) {
		public void invoke(Class<?> clazz, Injector injector) throws Throwable {
			handle.invokeWithArguments(injectKeys(clazz, parameters, injector));
		}
	}

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

	public static Object[] injectKeys(Class<?> instance, List<Key<?>> keys, Injector injector) {
		final var objects = new Object[keys.size() + 1];
		objects[0] = injector.getInstance(instance);
		for (int i = 0; i < keys.size(); i++) {
			objects[i + 1] = injector.getInstance(keys.get(i));
		}
		return objects;
	}

	public static Type getGenericType(Class<?> clazz, Class<?> interfaze, int index) {
		for (final var type : clazz.getGenericInterfaces()) {
			if (type.getTypeName().equals(interfaze.getTypeName())) {
				return ((ParameterizedType) type).getActualTypeArguments()[index];
			}
		}
		return null;
	}

	public static Class<?> toClass(Type type) {
		if (type instanceof Class) {
			return (Class<?>) type;
		} else if (type instanceof ParameterizedType) {
			return (Class<?>) ((ParameterizedType) type).getRawType();
		}
		throw new IllegalArgumentException("Unsupported type: " + type);
	}

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
