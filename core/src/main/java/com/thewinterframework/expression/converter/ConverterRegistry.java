package com.thewinterframework.expression.converter;

import java.util.HashMap;
import java.util.Map;

public class ConverterRegistry {
	private final Map<ConverterKey<?, ?>, Converter<?, ?>> converters = new HashMap<>();

	public <S, T> void register(final Class<S> source, final Class<T> target, final Converter<S, T> converter) {
		converters.put(new ConverterKey<>(source, target), converter);
	}

	@SuppressWarnings("unchecked")
	public <S, T> T convert(final S source, final Class<T> target) {
		if (target.isInstance(source)) return (T) source;

		final var converter = (Converter<S, T>) converters.get(new ConverterKey<>(source.getClass(), target));
		if (converter == null) {
			throw new IllegalArgumentException("No converter found for " + source.getClass() + " to " + target);
		}
		return converter.convert(source);
	}

	private record ConverterKey<S, T>(Class<S> source, Class<T> target) {
	}
}
