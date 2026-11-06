package com.thewinterframework.service.processor;

import com.google.auto.service.AutoService;
import com.thewinterframework.plugin.WinterBootPlugin;
import com.thewinterframework.processor.WinterProcessor;
import com.thewinterframework.processor.context.ProcessorContext;
import com.thewinterframework.processor.template.TemplateBuilder;
import com.thewinterframework.service.annotation.expose.Expose;
import com.thewinterframework.utils.reflect.ProcessorUtils;
import com.thewinterframework.utils.reflect.ProcessorUtils.ProjectType;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static com.thewinterframework.service.processor.ExposeAnnotationProcessor.ELEMENT_PATTERN;

@AutoService(Processor.class)
@SupportedOptions({
    "plugin.type",
    "plugin.package",
    "plugin.name",
    "bypass.api"
})
public class ExposeAnnotationProcessorAPI extends AbstractProcessor {

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
    if (ProcessorUtils.getProjectType(processingEnv) == ProjectType.IMPL && !isAPIBypassed()) {
      return false;
    }

    if (roundEnv.processingOver() || annotations.isEmpty()) {
      return false;
    }

    final var pluginPkg = getPluginPkg(roundEnv);
    final var pluginName = getPluginName(roundEnv);

    final var ctx = new ProcessorContext(processingEnv, null, roundEnv);

    final var validElements = roundEnv.getElementsAnnotatedWith(Expose.class).stream()
        .filter(e -> e.getKind().isDeclaredType())
        .map(e -> (TypeElement) e)
        .collect(Collectors.toCollection(() -> new TreeSet<>(
            Comparator.comparing(element -> element.getQualifiedName().toString())
        )));

    if (validElements.isEmpty()) {
      return false;
    }

    generateInterface(ctx, pluginPkg, pluginName, validElements);

    try {
      final var builder = new StringBuilder();
      for (final var element : validElements) {
        builder.append(element.getQualifiedName().toString()).append("\n");
      }

      final var file = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/winter/exposed-classes.txt");
      try (final var writer = file.openWriter()) {
        writer.write(builder.toString());
      }
    } catch (final IOException e) {
      processingEnv.getMessager().printError("Failed to write exposed classes to file: " + e.getMessage());
      return false;
    }

    return true;
  }

  private boolean isAPIBypassed() {
    return Boolean.parseBoolean(getOption("bypass.api", "false"));
  }

  private String getPluginPkg(final RoundEnvironment roundEnv) {
    if (isAPIBypassed()) {
      final Element pluginClass = WinterProcessor.getPluginClass(roundEnv, processingEnv);
      if (pluginClass == null) {
        throw new IllegalStateException("bypass.api requires a @WinterBootPlugin class");
      }
      return processingEnv.getElementUtils().getPackageOf(pluginClass).getQualifiedName().toString();
    }

    return getOption("plugin.package", "com.default.api");
  }

  private String getPluginName(final RoundEnvironment roundEnv) {
    if (isAPIBypassed()) {
      return WinterProcessor.getPluginClass(roundEnv, processingEnv).getSimpleName().toString();
    }

    return getOption("plugin.name", "Default");
  }

  private String getOption(final String key, final String defaultValue) {
    return processingEnv.getOptions().getOrDefault(key, defaultValue);
  }

  @SuppressWarnings("DuplicatedCode")
  private void generateInterface(final ProcessorContext ctx, final String pkg, final String pluginName, final Iterable<TypeElement> elements) {
    TemplateBuilder.fromResource("generated/ExposeInterfaceTemplate.java")
        .placeholder("PACKAGE", pkg)
        .placeholder("PLUGIN", pluginName)
        .custom(content -> {
          final var matcher = ELEMENT_PATTERN.matcher(content);
          final var result = new StringBuilder();
          int lastEnd = 0;

          while (matcher.find()) {
            result.append(content, lastEnd, matcher.start());
            final var block = matcher.group(1);
            final var builder = new StringBuilder();

            for (final var element : elements) {
              final String typeName = element.getSimpleName().toString();
              builder.append(block
                  .replace("<TYPE>", element.getQualifiedName())
                  .replace("<TYPE_NAME>", typeName)
              ).append(System.lineSeparator());
            }
            result.append(builder);
            lastEnd = matcher.end();
          }
          result.append(content.substring(lastEnd));
          return result.toString();
        })
        .write(ctx, pkg + "." + pluginName + "API");
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Set.of(Expose.class.getCanonicalName(), WinterBootPlugin.class.getCanonicalName());
  }
}
