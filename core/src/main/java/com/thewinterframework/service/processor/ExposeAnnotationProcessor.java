package com.thewinterframework.service.processor;

import com.google.auto.service.AutoService;
import com.thewinterframework.processor.context.ProcessorContext;
import com.thewinterframework.processor.handler.WinterAnnotationProcessor;
import com.thewinterframework.processor.template.TemplateBuilder;
import com.thewinterframework.service.annotation.expose.Expose;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.StandardLocation;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@AutoService(WinterAnnotationProcessor.class)
public class ExposeAnnotationProcessor implements WinterAnnotationProcessor {

	public static final Pattern ELEMENT_PATTERN = Pattern.compile("(?:<el>)([\\s\\S]*?)(?:<\\/el>)");

	@Override
	public Set<Class<? extends Annotation>> getSupportedAnnotations() {
		return Set.of(Expose.class);
	}

	@Override
	public void onRoundStart(final ProcessorContext ctx) {
		ctx.wireModule(ctx.getPluginPackageString() + ".ExposeAPIModule");

		if (!ctx.getRoundEnv().processingOver()) {
			return;
		}

		try {
			final var elements = new HashSet<TypeElement>();
			final var resources = ctx.getEnv().getFiler().getResource(StandardLocation.CLASS_PATH, "", "META-INF/winter/exposed-classes.txt");
			try (final var is = resources.openInputStream()) {
				final var classNames = readLines(is);
				for (final var className : classNames) {
					final var element = ctx.getEnv().getElementUtils().getTypeElement(className);
					if (element.getKind().isDeclaredType()) {
						elements.add(element);
					}
				}
			}

			generateFile(ctx, "generated/ExposeImplementationTemplate.java", "Default", elements);

			final var pkg = ctx.getPluginPackageString();
			final var pluginName = ctx.getPluginClass().getSimpleName().toString();
			TemplateBuilder.fromResource("generated/ExposeModuleTemplate.java")
					.placeholder("PACKAGE", pkg)
					.placeholder("PLUGIN", pluginName)
					.write(ctx, pkg + ".ExposeAPIModule");
		} catch (final Exception ex) {
			ctx.getEnv().getMessager().printError("Failed to load exposed classes: " + ex.getMessage());
		}
	}

	private List<String> readLines(final InputStream is) throws IOException {
		final var lines = new ArrayList<String>();
		try (final var reader = new BufferedReader(new InputStreamReader(is))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (!line.isBlank()) {
					lines.add(line.trim());
				}
			}
		}
		return lines;
	}

	@Override
	public void handle(final TypeElement annotation, final Set<? extends Element> elements, final ProcessorContext ctx) {
	}

	@SuppressWarnings("DuplicatedCode")
	public static void generateFile(final ProcessorContext ctx, final String templatePath, final String prefix, final Iterable<TypeElement> elements) {
		final var pkg = ctx.getPluginPackageString();
		final var pluginName = ctx.getPluginClass().getSimpleName().toString();
		TemplateBuilder.fromResource(templatePath)
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
				.write(ctx, pkg + "." + prefix + pluginName + "API");
	}
}