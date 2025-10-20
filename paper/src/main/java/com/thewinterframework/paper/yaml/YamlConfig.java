package com.thewinterframework.paper.yaml;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * A {@link YamlConfiguration} attached to a file, which may or
 * may not reflect a default (in-jar) configuration. (resources)
 *
 * @author yusshu (Andre Roldan)
 */
public final class YamlConfig extends YamlConfiguration {
	private final Path path;
	private final @Nullable Supplier<InputStream> resource;

	private YamlConfig(final @NotNull Path path, final @Nullable Supplier<InputStream> resource) {
		this.path = requireNonNull(path, "path");
		this.resource = resource;
	}

	/**
	 * Loads/reloads the configuration from the file to memory.
	 *
	 * <p>If the file doesn't exist AND this configuration reflects a default
	 * one (from resources), the default one will copied and loaded.</p>
	 *
	 * <p>All the values contained within this configuration will be removed,
	 * leaving only settings and defaults, and the new values will be loaded
	 * from the given string.</p>
	 *
	 * @throws IllegalStateException If configuration is invalid or an I/O
	 * exception occurs.
	 */
	public void load() {
		if (!Files.exists(path) && resource != null) {
			createParentDirs();
			try (final var input = resource.get()) {
				Files.copy(input, path);
			} catch (final IOException e) {
				throw new IllegalStateException("Failed to copy default configuration from resource to " + path, e);
			}
		}

		// updates the configuration with the file contents
		try (final var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			load(reader);
		} catch (final InvalidConfigurationException e) {
			throw new IllegalStateException("Failed to read invalid YAML configuration from " + path, e);
		} catch (final IOException e) {
			throw new IllegalStateException("Failed to read YAML from " + path, e);
		}
	}

	/**
	 * Saves the configuration from memory to the file.
	 *
	 * <p>Note that this method will try to create parent folders
	 * if they don't exist.</p>
	 *
	 * @throws IllegalStateException If couldn't create file or
	 * an I/O exception occurs.
	 */
	public void save() {
		// create path parent dirs
		createParentDirs();

		// serialize to string
		final var data = saveToString();

		// write string
		try (final var writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			writer.write(data);
		} catch (final IOException e) {
			throw new IllegalStateException("Failed to write YAML to " + path, e);
		}
	}

	private void createParentDirs() {
		try {
			final var parent = path.getParent();
			if (parent != null) {
				Files.createDirectories(path.getParent());
				if (!Files.isDirectory(parent)) {
					throw new IllegalStateException("Unable to create parent directories for " + path);
				}
			}
		} catch (IOException e) {
			throw new IllegalStateException("Failed to create parent directories for " + path, e);
		}
	}

	/**
	 * Creates a new {@link YamlConfig} attached to the
	 * given path, note that this method <b>will not load</b> the
	 * configuration from the file.
	 *
	 * @param path the path
	 * @return the new configuration
	 * @throws NullPointerException If path is null
	 */
	public static @NotNull YamlConfig create(final @NotNull Path path) {
		return new YamlConfig(path, null);
	}

	/**
	 * Creates a new {@link YamlConfig} representing the given
	 * configuration. Note that this method <b>will perform an initial
	 * configuration load</b> from the file (or from the resource if the
	 * file doesn't exist).
	 *
	 * <p>The file will be created if it doesn't exist.</p>
	 *
	 * <p>The file will be at the plugin data folder, with the given name.</p>
	 *
	 * <p>The default configuration must be a plugin resource, with the same name.</p>
	 *
	 * @param plugin The owner plugin
	 * @param configurationFileName The configuration file name
	 * @return The new configuration
	 */
	public static @NotNull YamlConfig configuration(final @NotNull Plugin plugin, final @NotNull String configurationFileName) {
		requireNonNull(plugin, "plugin");
		requireNonNull(configurationFileName, "configurationFileName");
		final Supplier<InputStream> resource = () -> plugin.getResource(configurationFileName + ".yml");
		final var path = plugin.getDataFolder().toPath().resolve(configurationFileName + ".yml");
		final var config = new YamlConfig(path, resource);
		config.load(); // first load
		return config;
	}
}
