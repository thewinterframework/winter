package com.thewinterframework.service.processor;

import com.thewinterframework.processor.WinterProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.ToolProvider;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExposeAnnotationProcessorAPITest {

  @TempDir
  Path directory;

  @Test
  void bypassModeUsesPluginSourcePackage() throws Exception {
    final var source = directory.resolve("src/example/ExamplePlugin.java");
    Files.createDirectories(source.getParent());
    Files.writeString(source, """
        package example;

        import com.thewinterframework.plugin.WinterBootPlugin;
        import com.thewinterframework.service.annotation.expose.Expose;

        @WinterBootPlugin
        public final class ExamplePlugin {}

        @Expose
        class PublicService {}
        """, StandardCharsets.UTF_8);
    final var generated = directory.resolve("generated");
    final var classes = directory.resolve("build/classes/java/main");
    final var properties = directory.resolve("src/main/resources/winter.properties");
    Files.createDirectories(generated);
    Files.createDirectories(classes);
    Files.createDirectories(properties.getParent());
    Files.writeString(properties, "winter.expose.bypass-api=true\nwinter.debug=true\n", StandardCharsets.UTF_8);

    final var compiler = ToolProvider.getSystemJavaCompiler();
    try (final var files = compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8)) {
      final var task = compiler.getTask(
          null,
          files,
          null,
          List.of("-s", generated.toString(), "-d", classes.toString(),
              "-classpath", System.getProperty("java.class.path")),
          null,
          files.getJavaFileObjects(source)
      );
      task.setProcessors(List.of(new ExposeAnnotationProcessorAPI(), new WinterProcessor()));
      assertEquals(Boolean.TRUE, task.call());
    }

    assertTrue(Files.exists(generated.resolve("example/ExamplePluginAPI.java")));
    assertTrue(Files.exists(classes.resolve("META-INF/winter/debug/example.ExamplePlugin")));
    assertTrue(Files.exists(classes.resolve("example/ExposeAPIModule.class")),
        () -> {
          try (final var files = Files.walk(generated)) {
            return files.map(Path::toString).toList().toString();
          } catch (final Exception exception) {
            return exception.toString();
          }
        });
  }

  @Test
  void nestedModuleReadsItsOwnWinterProperties() throws Exception {
    final var parentProperties = directory.resolve("src/main/resources/winter.properties");
    Files.createDirectories(parentProperties.getParent());
    Files.writeString(parentProperties, "winter.expose.bypass-api=true\n", StandardCharsets.UTF_8);

    final var module = directory.resolve("child");
    Files.createDirectories(module);
    Files.writeString(module.resolve("build.gradle.kts"), "plugins { java }\n", StandardCharsets.UTF_8);
    final var moduleProperties = module.resolve("src/main/resources/winter.properties");
    Files.createDirectories(moduleProperties.getParent());
    Files.writeString(moduleProperties, """
        winter.project.type=API
        winter.plugin.package=child.api
        winter.plugin.name=Child
        """, StandardCharsets.UTF_8);
    final var source = module.resolve("src/main/java/example/SharedService.java");
    Files.createDirectories(source.getParent());
    Files.writeString(source, """
        package example;
        import com.thewinterframework.service.annotation.expose.Expose;
        @Expose public final class SharedService {}
        """, StandardCharsets.UTF_8);
    final var generated = module.resolve("build/generated/sources/annotationProcessor/java/main");
    final var classes = module.resolve("build/classes/java/main");
    Files.createDirectories(generated);
    Files.createDirectories(classes);

    final var compiler = ToolProvider.getSystemJavaCompiler();
    try (final var files = compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8)) {
      final var task = compiler.getTask(null, files, null,
          List.of("-proc:only", "-s", generated.toString(), "-d", classes.toString(),
              "-classpath", System.getProperty("java.class.path")),
          null, files.getJavaFileObjects(source));
      task.setProcessors(List.of(new ExposeAnnotationProcessorAPI()));
      assertEquals(Boolean.TRUE, task.call());
    }
    assertTrue(Files.exists(generated.resolve("child/api/ChildAPI.java")));
  }
}
