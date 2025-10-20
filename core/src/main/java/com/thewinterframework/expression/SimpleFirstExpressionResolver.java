package com.thewinterframework.expression;

import com.thewinterframework.component.ComponentUtils;
import com.thewinterframework.utils.TimeUnit;
import com.thewinterframework.utils.reflect.AnnotatedMethodHandle;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class SimpleFirstExpressionResolver implements AnnotationExpressionResolver {

	public final static String COMPLEX_EXPRESSION_PREFIX = "${";

	private final static Map<Class<?>, Function<String, Object>> SIMPLE_EXPRESSIONS = new HashMap<>();

	static {
		SIMPLE_EXPRESSIONS.put(Integer.class, Integer::valueOf);
		SIMPLE_EXPRESSIONS.put(int.class, Integer::valueOf);
		SIMPLE_EXPRESSIONS.put(Long.class, Long::valueOf);
		SIMPLE_EXPRESSIONS.put(long.class, Long::valueOf);
		SIMPLE_EXPRESSIONS.put(Double.class, Double::valueOf);
		SIMPLE_EXPRESSIONS.put(double.class, Double::valueOf);
		SIMPLE_EXPRESSIONS.put(Float.class, Float::valueOf);
		SIMPLE_EXPRESSIONS.put(float.class, Float::valueOf);
		SIMPLE_EXPRESSIONS.put(Boolean.class, Boolean::valueOf);
		SIMPLE_EXPRESSIONS.put(boolean.class, Boolean::valueOf);
		SIMPLE_EXPRESSIONS.put(String.class, s -> s);
		SIMPLE_EXPRESSIONS.put(Character.class, s -> s.charAt(0));
		SIMPLE_EXPRESSIONS.put(char.class, s -> s.charAt(0));
		SIMPLE_EXPRESSIONS.put(TimeUnit.class, TimeUnit::valueOf);
		SIMPLE_EXPRESSIONS.put(Component.class, ComponentUtils::miniMessage);
	}

	protected <T> T resolveSimple(String value, Class<T> returnType) {
		final var simpleResolver = SIMPLE_EXPRESSIONS.get(returnType);
		if (simpleResolver != null) {
			final var resolved = simpleResolver.apply(value);
			if (returnType.isInstance(resolved)) {
				return returnType.cast(resolved);
			}
		}

		return null;
	}

	@Override
	public final <T> @Nullable T resolve(AnnotatedMethodHandle<?> method, String expression, Class<T> returnType) {
		final var simpleResolver = SIMPLE_EXPRESSIONS.get(returnType);
		if (simpleResolver != null && !expression.startsWith(COMPLEX_EXPRESSION_PREFIX)) {
			final var resolved = simpleResolver.apply(expression);
			if (returnType.isInstance(resolved)) {
				return returnType.cast(resolved);
			}
		}

		return resolveComplexExpression(method, expression, returnType);
	}

	@Nullable
	protected abstract <T> T resolveComplexExpression(AnnotatedMethodHandle<?> method, String expression, Class<T> returnType);
}
