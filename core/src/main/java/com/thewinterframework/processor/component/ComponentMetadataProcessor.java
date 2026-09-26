package com.thewinterframework.processor.component;

import com.thewinterframework.component.ComponentModule;
import com.thewinterframework.component.annotation.Component;
import com.thewinterframework.component.annotation.ComponentScope;
import com.thewinterframework.plugin.module.PluginModule;
import com.thewinterframework.processor.WinterProcessor;
import com.thewinterframework.processor.context.ProcessorContext;

import javax.annotation.processing.FilerException;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.Modifier;
import javax.tools.StandardLocation;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Set;

/** Discovers local components and publishes external component metadata for consumers. */
public final class ComponentMetadataProcessor {

	public static final String EXTERNAL_INDEX = "META-INF/winter/components.index";
	private static final String COMPONENT_ENTRY = "component:";
	private static final String MODULE_ENTRY = "module:";

	private final Set<String> localComponents = new LinkedHashSet<>();
	private final Set<String> localModules = new LinkedHashSet<>();
	private final Set<String> externalComponents = new LinkedHashSet<>();
	private final Set<String> externalModules = new LinkedHashSet<>();
	private boolean indexWritten;

	public void scan(final ProcessingEnvironment environment, final RoundEnvironment round) {
		for (final var root : round.getRootElements()) {
			scanElement(environment, root);
		}
	}

	public void finishLibrary(final ProcessingEnvironment environment) {
		writeExternalIndex(environment);
	}

	public void finishPlugin(final ProcessorContext context) {
		writeExternalIndex(context.getEnv());
		loadExternalIndexes();

		final var allComponents = new LinkedHashSet<>(localComponents);
		allComponents.addAll(externalComponents);
		if (!allComponents.isEmpty()) {
			context.wireModule(ComponentModule.class);
			final var classes = allComponents.stream().sorted().map(name -> name + ".class").toList();
			WinterProcessor.writeClassWire(
					context,
					com.thewinterframework.processor.clazz.ClassWireProcessor.wiredClassName(Component.class),
					context.getWinterModulePackageString(),
					classes
			);
		}

		final var modules = new LinkedHashSet<>(localModules);
		modules.addAll(externalModules);
		modules.stream().sorted().forEach(context::wireModule);
	}

	private void scanElement(final ProcessingEnvironment environment, final Element element) {
		if (element instanceof final TypeElement type
				&& (type.getKind() == ElementKind.CLASS || type.getKind() == ElementKind.RECORD)) {
			final var component = findComponentAnnotation(type);
			if (component != null) {
				if (validate(environment, type)) {
					register(environment, type, component);
				}
			}
		}

		element.getEnclosedElements().stream()
				.filter(child -> child.getKind().isClass() || child.getKind().isInterface())
				.forEach(child -> scanElement(environment, child));
	}

	private boolean validate(final ProcessingEnvironment environment, final TypeElement type) {
		if (!type.getModifiers().contains(Modifier.PUBLIC)) {
			environment.getMessager().printError("Winter components must be public", type);
			return false;
		}
		if (type.getModifiers().contains(Modifier.ABSTRACT)) {
			environment.getMessager().printError("Winter components must be concrete", type);
			return false;
		}
		if (type.getNestingKind().isNested() && !type.getModifiers().contains(Modifier.STATIC)) {
			environment.getMessager().printError("Nested Winter components must be static", type);
			return false;
		}
		return true;
	}

	private void register(
			final ProcessingEnvironment environment,
			final TypeElement type,
			final AnnotationMirror component
	) {
		final var pluginModule = environment.getElementUtils().getTypeElement(PluginModule.class.getCanonicalName());
		final var isModule = pluginModule != null && environment.getTypeUtils().isAssignable(
				environment.getTypeUtils().erasure(type.asType()),
				environment.getTypeUtils().erasure(pluginModule.asType())
		);
		final var name = type.getQualifiedName().toString();
		final var external = readScope(environment, component) == ComponentScope.EXTERNAL;

		if (isModule) {
			localModules.add(name);
			if (external) {
				externalModules.add(name);
			}
			return;
		}

		localComponents.add(name);
		if (external) {
			externalComponents.add(name);
		}
	}

	private AnnotationMirror findComponentAnnotation(final TypeElement type) {
		for (final var annotation : type.getAnnotationMirrors()) {
			if (isComponentAnnotation(annotation)) {
				return annotation;
			}
		}

		for (final var annotation : type.getAnnotationMirrors()) {
			final var annotationType = (TypeElement) annotation.getAnnotationType().asElement();
			for (final var metaAnnotation : annotationType.getAnnotationMirrors()) {
				if (isComponentAnnotation(metaAnnotation)) {
					return metaAnnotation;
				}
			}
		}
		return null;
	}

	private boolean isComponentAnnotation(final AnnotationMirror annotation) {
		return annotation.getAnnotationType().toString().equals(Component.class.getCanonicalName());
	}

	private ComponentScope readScope(
			final ProcessingEnvironment environment,
			final AnnotationMirror component
	) {
		return environment.getElementUtils().getElementValuesWithDefaults(component).entrySet().stream()
				.filter(entry -> entry.getKey().getSimpleName().contentEquals("scope"))
				.map(entry -> ComponentScope.valueOf(entry.getValue().getValue().toString()))
				.findFirst()
				.orElse(ComponentScope.LOCAL);
	}

	private void writeExternalIndex(final ProcessingEnvironment environment) {
		if (indexWritten || externalComponents.isEmpty() && externalModules.isEmpty()) {
			return;
		}
		indexWritten = true;

		try {
			final var resource = environment.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", EXTERNAL_INDEX);
			try (Writer writer = resource.openWriter()) {
				for (final var component : externalComponents.stream().sorted().toList()) {
					writer.write(COMPONENT_ENTRY + component + System.lineSeparator());
				}
				for (final var module : externalModules.stream().sorted().toList()) {
					writer.write(MODULE_ENTRY + module + System.lineSeparator());
				}
			}
		} catch (final FilerException ignored) {
			// Another processing round already created the index.
		} catch (final IOException exception) {
			environment.getMessager().printError("Cannot write Winter external component index: " + exception.getMessage());
		}
	}

	private void loadExternalIndexes() {
		try {
			final var resources = new LinkedHashSet<java.net.URL>();
			final var contextLoader = Thread.currentThread().getContextClassLoader();
			if (contextLoader != null) {
				contextLoader.getResources(EXTERNAL_INDEX).asIterator().forEachRemaining(resources::add);
			}
			ComponentMetadataProcessor.class.getClassLoader().getResources(EXTERNAL_INDEX)
					.asIterator().forEachRemaining(resources::add);
			for (final var resource : resources) {
				try (final var reader = new BufferedReader(new InputStreamReader(resource.openStream(), StandardCharsets.UTF_8))) {
					reader.lines().map(String::trim).filter(line -> !line.isEmpty()).forEach(this::readEntry);
				}
			}
		} catch (final IOException exception) {
			throw new IllegalStateException("Cannot read Winter external component indexes", exception);
		}
	}

	private void readEntry(final String entry) {
		if (entry.startsWith(COMPONENT_ENTRY)) {
			externalComponents.add(entry.substring(COMPONENT_ENTRY.length()));
		} else if (entry.startsWith(MODULE_ENTRY)) {
			externalModules.add(entry.substring(MODULE_ENTRY.length()));
		}
	}
}
