package com.thewinterframework.random;

import java.util.List;
import java.util.Random;

public final class RandomUtils {

	public static final Random RANDOM = new Random();

	private RandomUtils() {
	}

	/**
	 * Get a random integer between min and max
	 *
	 * @param min The minimum value
	 * @param max The maximum value
	 * @return The random integer
	 */
	public static int get(final int min, final int max) {
		return RANDOM.nextInt(min, max);
	}

	/**
	 * Get a random double between min and max
	 *
	 * @param min The minimum value
	 * @param max The maximum value
	 * @return The random double
	 */
	public static double get(final double min, final double max) {
		return RANDOM.nextDouble(min, max);
	}

	/**
	 * Get a random element from the given list
	 *
	 * @param list The list to get the random element from
	 * @return The random element
	 * @param <T> The type of the list
	 */
	public static <T> T get(final List<T> list) {
		return list.get(RANDOM.nextInt(list.size()));
	}

}