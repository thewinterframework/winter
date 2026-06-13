package com.thewinterframework.expression.converter;

public interface Converter<S, T> {
	T convert(S source);
}