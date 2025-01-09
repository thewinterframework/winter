package com.thewinterframework.configurate;

import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

public final class Container<C> {
	private final AtomicReference<C> config;
	private final YamlConfigurationLoader loader;
	private final ConfigurationNode node;
	private final Class<C> clazz;
	private final TypeToken<C> typeToken;
	private final ObjectMapper<C> mapper;
	private final Logger logger;

	private Container(
			final C config,
			final Class<C> clazz,
			final TypeToken<C> typeToken,
			final YamlConfigurationLoader loader,
			final ConfigurationNode node,
			final ObjectMapper<C> mapper,
			final Logger logger
	) {
		this.config = new AtomicReference<>(config);
		this.loader = loader;
		this.clazz = clazz;
		this.typeToken = typeToken;
		this.node = node;
		this.mapper = mapper;
		this.logger = logger;
	}

	public boolean reload() {
		try {
			final ConfigurationNode node = loader.load();
			final C newConfig = mapper.load(node);
			node.set(typeToken, newConfig);
			config.set(newConfig);
			return true;
		} catch (final IOException exception) {
			logger.error("Could not reload {} configuration file", clazz.getSimpleName(), exception);
			return false;
		}
	}

	public @NotNull C get() {
		return this.config.get();
	}

	public ConfigurationNode getNode() {
		return this.node;
	}

	public static <C> Container<C> load(
			final Logger logger,
			final Path path,
			final Class<C> clazz,
			final String file,
			final UnaryOperator<ConfigurationOptions> options
	) throws IOException {
		try {
			final var fileName = file.endsWith(".yml") ? file : file + ".yml";
			if (!Files.exists(path)) {
				Files.createDirectories(path);
			}

			final var configPath = generateFile(clazz, path.resolve(fileName), fileName);
			final YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
					.defaultOptions(options)
					.nodeStyle(NodeStyle.BLOCK)
					.path(configPath)
					.build();

			final var mapper = ObjectMapper.factory().get(clazz);
			final var typeToken = TypeToken.get(clazz);

			final ConfigurationNode node = loader.load();
			final C newConfig = node.get(typeToken);

			node.set(typeToken, newConfig);
			return new Container<>(newConfig, clazz, typeToken, loader, node, mapper, logger);
		} catch (final IOException exception) {
			logger.error("Could not load {} configuration file", clazz.getSimpleName(), exception);
			throw exception;
		}
	}

	public static <C> Container<C> load(
			final Logger logger,
			final Path path,
			final Class<C> clazz,
			final String file
	) throws IOException {
		return load(logger, path, clazz, file, opts -> opts.shouldCopyDefaults(true));
	}

	private static Path generateFile(final Class<?> clazz, final Path path, final String name) throws IOException {
		if (Files.exists(path)) {
			return path;
		}

		try (final var rsc = clazz.getClassLoader().getResourceAsStream(name)) {
			if (rsc == null) {
				throw new IOException("Could not find resource " + path.getFileName());
			}

			Files.copy(rsc, path);
			return path;
		}
	}
}
