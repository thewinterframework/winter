package com.thewinterframework.expression;

import com.thewinterframework.component.ComponentUtils;
import com.thewinterframework.expression.converter.Converter;
import com.thewinterframework.expression.converter.ConverterRegistry;
import com.thewinterframework.utils.TimeUnit;
import net.kyori.adventure.text.Component;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.MapContext;
import org.jetbrains.annotations.Nullable;

public class JexlExpressionResolver implements ExpressionResolver {

	private final JexlEngine jexl = new JexlBuilder()
			.cache(512)
			.strict(true)
			.create();

	private final JexlContext context = new MapContext();
	private final ConverterRegistry converters = new ConverterRegistry();

	public JexlExpressionResolver() {
		this.converters.register(String.class, TimeUnit.class, TimeUnit::valueOf);
		this.converters.register(Number.class, Long.class, Number::longValue);
		this.converters.register(Number.class, Integer.class, Number::intValue);
		this.converters.register(Number.class, Double.class, Number::doubleValue);
		this.converters.register(Number.class, Float.class, Number::floatValue);
		this.converters.register(Number.class, Short.class, Number::shortValue);
		this.converters.register(Number.class, Byte.class, Number::byteValue);
		this.converters.register(String.class, Boolean.class, Boolean::parseBoolean);
		this.converters.register(String.class, Component.class, ComponentUtils::miniMessage);
	}

	@Override
	public <T> @Nullable T resolve(final String expression, final Class<T> expectedReturnType) {
		final var expr = jexl.createExpression(expression);
		final var result = expr.evaluate(context);
		return converters.convert(result, expectedReturnType);
	}

	@Override
	public void addContext(final String key, final Object value) {
		context.set(key, value);
	}

	@Override
	public <S, T> void addConverter(final Class<S> source, final Class<T> target, final Converter<S, T> converter) {
		converters.register(source, target, converter);
	}
}
