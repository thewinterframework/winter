package com.thewinterframework.velocity.yaml;

public record YamlConfigExpressionWrapper(YamlConfig config) {
	public Object get(final String path) {
		return config.get(path);
	}
}
