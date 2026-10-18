package com.thewinterframework.velocity.yaml;

import com.thewinterframework.velocity.VelocityWinterPlugin;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * File-backed YAML configuration for Velocity, with dotted paths and typed
 * accessors. Unlike Paper's configuration, this class has no Bukkit dependency.
 */
public final class YamlConfig {

	private final Path path;
	private final Supplier<InputStream> resource;
	private Map<String, Object> values = new LinkedHashMap<>();

	private YamlConfig(final Path path, final Supplier<InputStream> resource) {
		this.path = requireNonNull(path, "path");
		this.resource = resource;
	}

	public void load() {
		try {
			if (!Files.exists(path)) {
				createParentDirs();
				try (final var input = resource == null ? null : resource.get()) {
					if (input != null) {
						Files.copy(input, path);
					} else {
						Files.createFile(path);
					}
				}
			}
			try (final var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
				final var loaded = new Yaml(new SafeConstructor(new LoaderOptions())).load(reader);
				if (loaded != null && !(loaded instanceof Map)) {
					throw new IllegalStateException("The YAML root must be a mapping: " + path);
				}
				values = loaded == null ? new LinkedHashMap<>() : stringMap((Map<?, ?>) loaded);
			}
		} catch (final IOException | YAMLException e) {
			throw new IllegalStateException("Failed to load YAML from " + path, e);
		}
	}

	public void save() {
		try {
			createParentDirs();
			final var options = new DumperOptions();
			options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
			try (final var writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
				new Yaml(options).dump(values, writer);
			}
		} catch (final IOException | YAMLException e) {
			throw new IllegalStateException("Failed to save YAML to " + path, e);
		}
	}

	public Object get(final String key) {
		Object current = values;
		for (final var part : key.split("\\.")) {
			if (!(current instanceof Map<?, ?> section)) {
				return null;
			}
			current = section.get(part);
		}
		return current;
	}

	public Object get(final String key, final Object fallback) {
		final var value = get(key);
		return value == null ? fallback : value;
	}

	public boolean contains(final String key) {
		return get(key) != null;
	}

	public String getString(final String key) {
		return getString(key, null);
	}

	public String getString(final String key, final String fallback) {
		final var value = get(key);
		return value == null ? fallback : value.toString();
	}

	public boolean getBoolean(final String key) {
		return getBoolean(key, false);
	}

	public boolean getBoolean(final String key, final boolean fallback) {
		return get(key) instanceof Boolean value ? value : fallback;
	}

	public int getInt(final String key) {
		return getInt(key, 0);
	}

	public int getInt(final String key, final int fallback) {
		return get(key) instanceof Number value ? value.intValue() : fallback;
	}

	public long getLong(final String key) {
		return getLong(key, 0);
	}

	public long getLong(final String key, final long fallback) {
		return get(key) instanceof Number value ? value.longValue() : fallback;
	}

	public double getDouble(final String key) {
		return getDouble(key, 0);
	}

	public double getDouble(final String key, final double fallback) {
		return get(key) instanceof Number value ? value.doubleValue() : fallback;
	}

	public List<?> getList(final String key) {
		return get(key) instanceof List<?> list ? new ArrayList<>(list) : List.of();
	}

	public List<String> getStringList(final String key) {
		return getList(key).stream().filter(value -> value instanceof String || value instanceof Number || value instanceof Boolean)
				.map(Object::toString).toList();
	}

	public Set<String> getKeys() {
		return Set.copyOf(values.keySet());
	}

	public Map<String, Object> getValues() {
		return new LinkedHashMap<>(values);
	}

	@SuppressWarnings("unchecked")
	public void set(final String key, final Object value) {
		final var parts = key.split("\\.");
		var section = values;
		for (var i = 0; i < parts.length - 1; i++) {
			final var child = section.get(parts[i]);
			if (child instanceof Map<?, ?> map) {
				section = (Map<String, Object>) map;
				continue;
			}
			if (value == null) {
				return;
			}
			final var next = new LinkedHashMap<String, Object>();
			section.put(parts[i], next);
			section = next;
		}
		if (value == null) {
			section.remove(parts[parts.length - 1]);
			return;
		}
		section.put(parts[parts.length - 1], value instanceof Map<?, ?> map ? stringMap(map) : value);
	}

	private static Map<String, Object> stringMap(final Map<?, ?> source) {
		final var result = new LinkedHashMap<String, Object>();
		for (final var entry : source.entrySet()) {
			if (!(entry.getKey() instanceof String key)) {
				throw new IllegalStateException("YAML configuration keys must be strings");
			}
			final var value = entry.getValue();
			result.put(key, value instanceof Map<?, ?> map ? stringMap(map) : value);
		}
		return result;
	}

	private void createParentDirs() throws IOException {
		if (path.getParent() != null) {
			Files.createDirectories(path.getParent());
		}
	}

	/** Creates a configuration without loading it. */
	public static YamlConfig create(final Path path) {
		return new YamlConfig(path, null);
	}

	/** Loads a file, copying the matching classpath resource on first use. */
	public static YamlConfig configuration(final VelocityWinterPlugin plugin, final String fileName) {
		requireNonNull(fileName, "fileName");
		final var directory = plugin.getDataDirectory().toAbsolutePath().normalize();
		final var path = directory.resolve(fileName + ".yml").normalize();
		if (!path.startsWith(directory)) {
			throw new IllegalArgumentException("The configuration must be inside the plugin data directory");
		}
		final var config = new YamlConfig(path, () -> plugin.getClass().getClassLoader().getResourceAsStream(fileName + ".yml"));
		config.load();
		plugin.getExpressionResolver().addContext(fileName, new YamlConfigExpressionWrapper(config));
		return config;
	}
}
