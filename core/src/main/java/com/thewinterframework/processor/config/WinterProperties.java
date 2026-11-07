package com.thewinterframework.processor.config;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

/** Compile-time configuration loaded from the current module's resource directory. */
public final class WinterProperties {

  private static final String FILE_NAME = "winter.properties";
  private final Properties values;
  private final Map<String, String> legacyOptions;

  private WinterProperties(final Properties values, final Map<String, String> legacyOptions) {
    this.values = values;
    this.legacyOptions = legacyOptions;
  }

  public static WinterProperties load(final ProcessingEnvironment environment) {
    final var values = new Properties();
    final var path = find(environment);
    if (path != null) {
      try (final var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
        values.load(reader);
      } catch (final IOException exception) {
        throw new IllegalStateException("Cannot read Winter configuration at " + path, exception);
      }
    }
    return new WinterProperties(values, environment.getOptions());
  }

  public String get(final String key, final String legacyKey, final String defaultValue) {
    return values.getProperty(key, legacyOptions.getOrDefault(legacyKey, defaultValue)).trim();
  }

  public boolean enabled(final String key, final String legacyKey, final boolean defaultValue) {
    return Boolean.parseBoolean(get(key, legacyKey, Boolean.toString(defaultValue)));
  }

  private static Path find(final ProcessingEnvironment environment) {
    try {
      final var resource = environment.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", FILE_NAME);
      for (Path directory = Path.of(resource.toUri()).toAbsolutePath().getParent(); directory != null;
           directory = directory.getParent()) {
        final var candidate = directory.resolve("src/main/resources").resolve(FILE_NAME);
        if (Files.isRegularFile(candidate)) {
          return candidate;
        }
        if (Files.isRegularFile(directory.resolve("build.gradle"))
            || Files.isRegularFile(directory.resolve("build.gradle.kts"))) {
          return null;
        }
      }
    } catch (final IOException | IllegalArgumentException exception) {
      throw new IllegalStateException("Cannot locate Winter compile-time configuration", exception);
    }
    return null;
  }
}
