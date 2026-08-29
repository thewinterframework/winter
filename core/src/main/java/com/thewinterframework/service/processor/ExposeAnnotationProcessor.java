package com.thewinterframework.service.processor;

import com.google.auto.service.AutoService;
import com.thewinterframework.processor.context.ProcessorContext;
import com.thewinterframework.processor.handler.WinterAnnotationProcessor;
import com.thewinterframework.processor.template.TemplateBuilder;
import com.thewinterframework.service.annotation.expose.Expose;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static javax.lang.model.util.ElementFilter.methodsIn;

@AutoService(WinterAnnotationProcessor.class)
public class ExposeAnnotationProcessor implements WinterAnnotationProcessor {

	public static final Pattern ELEMENT_PATTERN = Pattern.compile("(?:<el>)([\\s\\S]*?)(?:<\\/el>)");

	@Override
	public Set<Class<? extends Annotation>> getSupportedAnnotations() {
		return Set.of(Expose.class);
	}

	@Override
	public void onRoundStart(final ProcessorContext ctx) {
		if (!ctx.getRoundEnv().processingOver()) {
			return;
		}

		final var pkg = ctx.getPluginPackageString();
		final var pluginName = ctx.getPluginClass().getSimpleName().toString();
		final var apiElement = ctx.getEnv().getElementUtils().getTypeElement(pkg + "." + pluginName + "API");
		if (apiElement == null) {
			return;
		}

		final var methods = exposedMethods(apiElement, ctx);
		if (methods.isEmpty()) {
			return;
		}

		ctx.wireModule(pkg + ".ExposeAPIModule");
		generateFile(ctx, "generated/ExposeImplementationTemplate.java", "Default", methods);

		TemplateBuilder.fromResource("generated/ExposeModuleTemplate.java")
				.placeholder("PACKAGE", pkg)
				.placeholder("PLUGIN", pluginName)
				.write(ctx, pkg + ".ExposeAPIModule");
	}

	private List<ExposedMethod> exposedMethods(final TypeElement apiElement, final ProcessorContext ctx) {
		return methodsIn(apiElement.getEnclosedElements()).stream()
				.map(method -> exposedMethod(method, ctx))
				.sorted(Comparator.comparing(ExposedMethod::name))
				.collect(Collectors.toCollection(ArrayList::new));
	}

	private ExposedMethod exposedMethod(final ExecutableElement method, final ProcessorContext ctx) {
		final var returnElement = ctx.getEnv().getTypeUtils().asElement(method.getReturnType());
		if (!(returnElement instanceof TypeElement typeElement)) {
			throw new IllegalArgumentException(
					"Exposed API method %s must return a declared type".formatted(method.getSimpleName())
			);
		}

		return new ExposedMethod(method.getSimpleName().toString(), typeElement);
	}

	@Override
	public void handle(final TypeElement annotation, final Set<? extends Element> elements, final ProcessorContext ctx) {
	}

	@SuppressWarnings("DuplicatedCode")
	public static void generateFile(final ProcessorContext ctx, final String templatePath, final String prefix, final Iterable<ExposedMethod> methods) {
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

						for (final var method : methods) {
							builder.append(block
									.replace("<TYPE>", method.returnType().getQualifiedName())
									.replace("<METHOD_NAME>", method.name())
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

	public record ExposedMethod(String name, TypeElement returnType) {
	}
}
