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
import org.apache.commons.jexl3.introspection.JexlPermissions;
import org.jetbrains.annotations.Nullable;

public class JexlExpressionResolver implements ExpressionResolver {

	private final JexlEngine jexl = new JexlBuilder()
			.cache(512)
			.strict(true)
			.silent(false)
			.permissions(JexlPermissions.UNRESTRICTED)
			.create();

	private final JexlContext context = new MapContext();
	private final ConverterRegistry converters = new ConverterRegistry();

	public JexlExpressionResolver() {
		addConverter(String.class, TimeUnit.class, TimeUnit::valueOf);
		addConverter(Number.class, Long.class, Number::longValue);
		addConverter(Number.class, Integer.class, Number::intValue);
		addConverter(Number.class, Double.class, Number::doubleValue);
		addConverter(Number.class, Float.class, Number::floatValue);
		addConverter(Number.class, Short.class, Number::shortValue);
		addConverter(Number.class, Byte.class, Number::byteValue);
		addConverter(String.class, Boolean.class, Boolean::parseBoolean);
		addConverter(Integer.class, Long.class, Long::valueOf);
		addConverter(Integer.class, Double.class, Double::valueOf);
		addConverter(Integer.class, Float.class, Float::valueOf);
		addConverter(Long.class, Integer.class, Long::intValue);
		addConverter(Long.class, Double.class, Double::valueOf);
		addConverter(Long.class, Float.class, Float::valueOf);
		addConverter(Double.class, Integer.class, Double::intValue);
		addConverter(Double.class, Long.class, Double::longValue);
		addConverter(Double.class, Float.class, Double::floatValue);
		addConverter(Float.class, Integer.class, Float::intValue);
		addConverter(Float.class, Long.class, Float::longValue);
		addConverter(Float.class, Double.class, Float::doubleValue);
		addConverter(String.class, Component.class, ComponentUtils::miniMessage);

		for (final var value : TimeUnit.values()) {
			addContext(value.name(), value);
			addContext(value.name().toLowerCase(), value);
		}
	}

	@Override
	public <T> @Nullable T resolve(final String expression, final Class<T> expectedReturnType) {
		final var expr = jexl.createExpression(expression);
		final var result = expr.evaluate(context);

		return converters.convert(result, expectedReturnType);
	}

	@Override
	public void addContext(final String key, final Object value) {
		this.context.set(key, value);
	}

	@Override
	public <S, T> void addConverter(final Class<S> source, final Class<T> target, final Converter<S, T> converter) {
		this.converters.register(source, target, converter);
	}
}
