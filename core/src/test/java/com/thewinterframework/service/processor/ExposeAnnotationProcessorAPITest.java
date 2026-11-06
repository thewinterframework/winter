package com.thewinterframework.service.processor;

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
    final var classes = directory.resolve("classes");
    Files.createDirectories(generated);
    Files.createDirectories(classes);

    final var compiler = ToolProvider.getSystemJavaCompiler();
    try (final var files = compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8)) {
      final var task = compiler.getTask(
          null,
          files,
          null,
          List.of("-proc:only", "-Abypass.api=true", "-s", generated.toString(), "-d", classes.toString(),
              "-classpath", System.getProperty("java.class.path")),
          null,
          files.getJavaFileObjects(source)
      );
      task.setProcessors(List.of(new ExposeAnnotationProcessorAPI()));
      assertEquals(Boolean.TRUE, task.call());
    }

    assertTrue(Files.exists(generated.resolve("example/ExamplePluginAPI.java")));
  }
}
