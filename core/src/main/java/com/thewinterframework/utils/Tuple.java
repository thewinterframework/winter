package com.thewinterframework.utils;

/**
 * Represents a tuple of two values.
 *
 * @param <F> The type of the first value.
 * @param <S> The type of the second value.
 */
public record Tuple<F, S>(F first, S second) {
	public static <F, S> Tuple<F, S> of(F first, S second) {
		return new Tuple<>(first, second);
	}
}
