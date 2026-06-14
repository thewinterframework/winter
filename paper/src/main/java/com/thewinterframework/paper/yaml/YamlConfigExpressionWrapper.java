package com.thewinterframework.paper.yaml;

import org.bukkit.configuration.file.YamlConfiguration;

public record YamlConfigExpressionWrapper(YamlConfiguration config) {
	public Object get(final String path) {
		return config.get(path);
	}
}