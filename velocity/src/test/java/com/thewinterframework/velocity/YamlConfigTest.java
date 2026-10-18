package com.thewinterframework.velocity;

import com.thewinterframework.velocity.yaml.FileName;
import com.thewinterframework.velocity.yaml.FileNameImpl;
import com.thewinterframework.velocity.yaml.YamlConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class YamlConfigTest {
	@TempDir
	Path directory;

	@Test
	void roundTripsNestedValuesAndReloadsChanges() throws Exception {
		final var path = directory.resolve("nested/config.yml");
		final var config = YamlConfig.create(path);
		config.load();
		config.set("feature.enabled", true);
		config.set("feature.limit", 42);
		config.set("message", "¡Hola, invierno! ❄");
		config.set("servers", List.of("lobby", "survival"));
		config.set("section", Map.of("key", "value"));
		config.set("section.key", "updated");
		config.save();
		final var reloaded = YamlConfig.create(path);
		reloaded.load();
		assertTrue(reloaded.getBoolean("feature.enabled"));
		assertEquals(42, reloaded.getInt("feature.limit"));
		assertEquals(42L, reloaded.getLong("feature.limit"));
		assertEquals(42.0, reloaded.getDouble("feature.limit"));
		assertEquals("¡Hola, invierno! ❄", reloaded.getString("message"));
		assertEquals(List.of("lobby", "survival"), reloaded.getStringList("servers"));
		assertEquals("updated", reloaded.getString("section.key"));
		assertEquals("fallback", reloaded.getString("missing", "fallback"));
		reloaded.set("section.key", null);
		assertFalse(reloaded.contains("section.key"));
		Files.writeString(path, "replacement: true\n");
		reloaded.load();
		assertFalse(reloaded.contains("feature.limit"));
		assertTrue(reloaded.getBoolean("replacement"));
	}

	@Test
	void rejectsMalformedAndNonMappingYamlWithoutReplacingTheCurrentValues() throws Exception {
		final var path = directory.resolve("config.yml");
		final var config = YamlConfig.create(path);
		config.set("valid", true);
		Files.writeString(path, "broken: [\n");
		assertThrows(IllegalStateException.class, config::load);
		assertTrue(config.getBoolean("valid"));
		Files.writeString(path, "- list\n- root\n");
		assertThrows(IllegalStateException.class, config::load);
	}

	@Test
	void generatedQualifierMatchesTheRuntimeAnnotation() throws Exception {
		final var annotation = getClass().getDeclaredField("qualified").getAnnotation(FileName.class);
		final var generated = new FileNameImpl("config");
		assertEquals(annotation, generated);
		assertEquals(generated, annotation);
		assertEquals(annotation.hashCode(), generated.hashCode());
	}

	@FileName("config")
	private YamlConfig qualified;
}
