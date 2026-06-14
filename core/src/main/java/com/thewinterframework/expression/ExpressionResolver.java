package com.thewinterframework.expression;

import com.thewinterframework.expression.converter.Converter;
import org.jetbrains.annotations.Nullable;

/**
 * Resolves annotation expressions to their corresponding values.
 */
public interface ExpressionResolver {

	/**
	 * Resolves the given expression for the specified method and return type.
	 *
	 * @param expression         the expression to resolve
	 * @param expectedReturnType the expected return type
	 * @param <T>                the type of the resolved value
	 * @return the resolved value, or null if it cannot be resolved
	 */
	@Nullable
	<T> T resolve(final String expression, final Class<T> expectedReturnType);

	/**
	 * Add a context to the expression resolver.
	 *
	 * @param key   The key of the context
	 * @param value The value/provider of the context
	 */
	void addContext(final String key, final Object value);

	<S, T> void addConverter(final Class<S> source, final Class<T> target, final Converter<S, T> converter);
}
