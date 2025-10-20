package com.thewinterframework.expression;

/**
 * Represents a parsed expression with a configuration name and a path.
 */
public record ConfigParsedExpression(String configName, String path) {

	/**
	 * Parses the given expression into a ParsedExpression object.
	 *
	 * @param expression the expression to parse
	 * @return the parsed expression
	 * @throws IllegalArgumentException if the expression format is invalid
	 */
	public static ConfigParsedExpression parse(String expression) {
		if (!expression.startsWith("${") || !expression.endsWith("}")) {
			throw new IllegalArgumentException("Invalid expression format: " + expression);
		}

		final var inner = expression.substring(2, expression.length() - 1);
		final var parts = inner.split("\\.", 2);

		if (parts.length < 2 || parts[1].isEmpty()) {
			throw new IllegalArgumentException("Expression " + expression + " must be in the format ${configName.path}");
		}

		final var configName = parts[0];
		final var path = parts[1];

		return new ConfigParsedExpression(configName, path);
	}

}
