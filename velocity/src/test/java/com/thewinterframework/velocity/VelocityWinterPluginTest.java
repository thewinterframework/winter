package com.thewinterframework.velocity;

import com.google.inject.Key;
import com.google.inject.Guice;
import com.google.inject.Scopes;
import com.thewinterframework.service.ReloadServiceManager;
import com.thewinterframework.velocity.platform.VelocityPlatform;
import com.thewinterframework.velocity.yaml.FileNameImpl;
import com.thewinterframework.velocity.yaml.YamlConfig;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.helpers.NOPLogger;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class VelocityWinterPluginTest {
	@TempDir
	Path directory;

	@Test
	void discoversAndRunsGeneratedComponentsThroughTheVelocityLifecycle() throws Exception {
		final var output = compilePlugin();
		assertTrue(Files.readString(output.resolve("velocity-plugin.json")).contains("winter-test"));
		Files.writeString(output.resolve("config.yml"), "interval: 5\nlisteners: true\n");
		Files.writeString(output.resolve("messages.yml"), "welcome: Hello\n");
		try (final var loader = new URLClassLoader(new URL[]{output.toUri().toURL()}, getClass().getClassLoader())) {
			final var environment = new VelocityTestEnvironment();
			final var plugin = plugin(loader, environment);
			plugin.onProxyInitialize(new ProxyInitializeEvent());
			plugin.onProxyInitialize(new ProxyInitializeEvent());
			assertEquals("winter-test", plugin.namespace());
			assertSame(plugin.getVelocityInjector(), plugin.getInjector().getParent());
			assertSame(environment.server, plugin.getInjector().getInstance(ProxyServer.class));
			assertSame(plugin.getVelocityInjector().getInstance(Logger.class), plugin.getInjector().getInstance(Logger.class));
			assertSame(plugin, plugin.getInjector().getInstance(plugin.getClass()));
			assertEquals(1, loader.loadClass("example.TestPlugin").getField("loads").getInt(null));
			assertEquals(1, loader.loadClass("example.TestPlugin").getField("enables").getInt(null));
			assertTrue(flag(loader, "TestModule", "loaded"));
			assertTrue(flag(loader, "TestModule", "enabled"));
			assertTrue(flag(loader, "TestService", "enabled"));
			assertFalse(flag(loader, "MissingService", "enabled"));
			assertEquals(1, environment.listeners.size());
			final var serviceClass = loader.loadClass("example.TestService");
			final var service = plugin.getInjector().getInstance(serviceClass);
			final var config = plugin.getInjector().getInstance(Key.get(YamlConfig.class, new FileNameImpl("config")));
			assertSame(config, serviceClass.getField("config").get(service));
			assertSame(config, serviceClass.getField("sameConfig").get(service));
			assertEquals(4, environment.tasks.size(), "Repeating tasks and both forms of daily schedules must be discovered");
			assertTrue(environment.tasks.stream().anyMatch(t -> t.repeat.equals(Duration.ofSeconds(5))));
			assertTrue(environment.tasks.stream().anyMatch(t -> t.repeat.equals(Duration.ofMillis(100))));
			environment.tasks.forEach(VelocityTestEnvironment.Task::execute);
			assertEquals(2, serviceClass.getField("runs").getInt(null));
			assertTrue(plugin.getInjector().getInstance(ReloadServiceManager.class).reload().result());
			assertTrue(flag(loader, "TestService", "reloaded"));
			plugin.onProxyShutdown(new ProxyShutdownEvent());
			plugin.onProxyShutdown(new ProxyShutdownEvent());
			assertTrue(flag(loader, "TestService", "disabled"));
			assertTrue(flag(loader, "TestModule", "disabled"));
			assertEquals(1, loader.loadClass("example.TestPlugin").getField("disables").getInt(null));
			assertTrue(environment.listeners.isEmpty());
			assertTrue(environment.tasks.stream().allMatch(t -> t.cancelled));
		}
	}

	@Test
	void cleansUpTasksAndListenersAfterAnEnableFailure() throws Exception {
		final var output = compilePlugin();
		Files.writeString(output.resolve("config.yml"), "interval: 5\nlisteners: true\n");
		Files.writeString(output.resolve("messages.yml"), "welcome: Hello\n");
		try (final var loader = new URLClassLoader(new URL[]{output.toUri().toURL()}, getClass().getClassLoader())) {
			final var environment = new VelocityTestEnvironment();
			final var plugin = plugin(loader, environment);
			loader.loadClass("example.TestPlugin").getField("failEnable").setBoolean(null, true);
			assertThrows(IllegalStateException.class, () -> plugin.onProxyInitialize(new ProxyInitializeEvent()));
			assertTrue(environment.listeners.isEmpty());
			assertTrue(environment.tasks.stream().allMatch(t -> t.cancelled));
			assertTrue(flag(loader, "TestService", "disabled"));
		}
	}

	@Test
	void distinguishesInstalledPluginsFromConstructedInstances() {
		final var environment = new VelocityTestEnvironment();
		environment.registerPlugin("installed", null);
		environment.registerPlugin("constructed", new Object());
		final var platform = new VelocityPlatform(environment.server);
		assertTrue(platform.isPluginPresent("installed"));
		assertFalse(platform.isPluginEnabled("installed"));
		assertTrue(platform.isPluginEnabled("constructed"));
		assertFalse(platform.isPluginPresent("missing"));
	}

	private VelocityWinterPlugin plugin(final ClassLoader loader, final VelocityTestEnvironment environment) throws Exception {
		environment.registerPlugin("winter-test", null);
		final var pluginClass = loader.loadClass("example.TestPlugin").asSubclass(VelocityWinterPlugin.class);
		final var parent = Guice.createInjector(binder -> {
			binder.bind(pluginClass).in(Scopes.SINGLETON);
			binder.bind(ProxyServer.class).toInstance(environment.server);
			binder.bind(Logger.class).toInstance(NOPLogger.NOP_LOGGER);
			binder.bind(Path.class).annotatedWith(DataDirectory.class).toInstance(directory.resolve("data"));
			binder.bind(PluginContainer.class).toInstance(environment.plugins.get("winter-test"));
			binder.bind(UUID.class).toInstance(UUID.randomUUID());
		});
		final var plugin = parent.getInstance(pluginClass);
		environment.registerPlugin("winter-test", plugin);
		environment.registerPlugin("dependency", null);
		return plugin;
	}

	private boolean flag(final ClassLoader loader, final String type, final String field) throws Exception {
		return loader.loadClass("example." + type).getField(field).getBoolean(null);
	}

	private Path compilePlugin() throws Exception {
		final var compiler = ToolProvider.getSystemJavaCompiler();
		assertNotNull(compiler);
		final var output = Files.createDirectories(directory.resolve("classes"));
		final var generated = Files.createDirectories(directory.resolve("generated"));
		final var fixtures = Path.of(getClass().getResource("/fixtures").toURI());
		final var diagnostics = new DiagnosticCollector<JavaFileObject>();
		try (final var manager = compiler.getStandardFileManager(diagnostics, null, null);
			 final var files = Files.list(fixtures)) {
			final var sources = manager.getJavaFileObjectsFromPaths(files.filter(p -> p.toString().endsWith(".java")).toList());
			final var options = List.of("-classpath", System.getProperty("winter.test.classpath"), "-d", output.toString(),
					"-s", generated.toString(), "-parameters");
			assertTrue(compiler.getTask(null, manager, diagnostics, options, null, sources).call(), () -> diagnostics.getDiagnostics().toString());
		}
		return output;
	}
}
