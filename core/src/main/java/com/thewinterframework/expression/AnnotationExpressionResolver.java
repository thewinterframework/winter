package com.thewinterframework.expression;

import com.thewinterframework.utils.reflect.AnnotatedMethodHandle;
import org.jetbrains.annotations.Nullable;

/**
 * Resolves annotation expressions to their corresponding values.
 */
public interface AnnotationExpressionResolver {

	/**
	 * Resolves the given expression for the specified method and return type.
	 *
	 * @param method     the annotated method handle
	 * @param expression the expression to resolve
	 * @param returnType the expected return type
	 * @param <T>        the type of the resolved value
	 * @return the resolved value, or null if it cannot be resolved
	 */
	@Nullable
	<T> T resolve(final AnnotatedMethodHandle<?> method, final String expression, final Class<T> returnType);

}
