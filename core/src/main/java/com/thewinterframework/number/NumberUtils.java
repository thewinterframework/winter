package com.thewinterframework.number;

import java.text.DecimalFormat;

/**
 * Utility methods for numbers.
 */
public class NumberUtils {

	/**
	 * Format a number with a specific number of decimals.
	 * @param number The number to format.
	 * @param decimals The number of decimals to format the number with.
	 * @return The formatted number.
	 */
	public static String formatNumber(double number, int decimals) {
		StringBuilder pattern = new StringBuilder("0");
		if (decimals > 0) {
			pattern.append(".");
			pattern.append("0".repeat(decimals));
		}
		DecimalFormat decimalFormat = new DecimalFormat(pattern.toString());
		return decimalFormat.format(number);
	}

	/**
	 * Abbreviate a number to a more readable format. For example, 1000 becomes 1k.
	 * @param number The number to abbreviate.
	 * @return The abbreviated number.
	 */
	public static String abbreviateNumber(long number) {
		if (number < 1000) return String.valueOf(number);
		int exp = (int) (Math.log(number) / Math.log(1000));
		char suffix = "kMBTPE".charAt(exp - 1);
		return String.format("%.1f%c", number / Math.pow(1000, exp), suffix);
	}

}
