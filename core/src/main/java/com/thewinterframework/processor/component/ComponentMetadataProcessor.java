package com.thewinterframework.processor.component;

import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import com.thewinterframework.component.ComponentModule;
import com.thewinterframework.component.admission.ComponentDecision;
import com.thewinterframework.component.annotation.Component;
import com.thewinterframework.component.annotation.External;
import com.thewinterframework.component.annotation.SyncLoading;
import com.thewinterframework.component.meta.SyncLoadingIndex;
import com.thewinterframework.component.ExternalBindingsModule;
import com.thewinterframework.service.annotation.expose.Expose;
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
import javax.lang.model.type.TypeMirror;
import javax.tools.StandardLocation;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/** Discovers local components and publishes external component metadata for consumers. */
public final class ComponentMetadataProcessor {

  public static final String EXTERNAL_INDEX = "META-INF/winter/components.index";
  private static final String COMPONENT_ENTRY = "component:";
  private static final String MODULE_ENTRY = "module:";
  public static final String BINDING_ENTRY = "binding:";

  private final Set<String> localComponents = new LinkedHashSet<>();
  private final Set<String> localModules = new LinkedHashSet<>();
  private final Set<String> externalComponents = new LinkedHashSet<>();
  private final Set<String> externalModules = new LinkedHashSet<>();
  private final Set<String> syncComponents = new LinkedHashSet<>();
  private final Map<String, String> externalBindings = new LinkedHashMap<>();
  private boolean indexWritten;
  private boolean syncIndexWritten;
  private List<ComponentCompileInterceptor> compileInterceptors;

  public void scan(final ProcessingEnvironment environment, final RoundEnvironment round) {
    for (final var root : round.getRootElements()) {
      scanElement(environment, root);
    }
  }

  public void finishLibrary(final ProcessingEnvironment environment) {
    writeExternalIndex(environment);
    writeSyncIndex(environment);
  }

  public void finishPlugin(final ProcessorContext context) {
    writeExternalIndex(context.getEnv());
    writeSyncIndex(context.getEnv());
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
    if (!externalBindings.isEmpty()) {
      context.wireModule(ExternalBindingsModule.class);
    }
  }

  private void scanElement(final ProcessingEnvironment environment, final Element element) {
    if (element instanceof final TypeElement type
        && (type.getKind() == ElementKind.CLASS || type.getKind() == ElementKind.RECORD)) {
      if (findComponentAnnotation(type) != null) {
        if (validate(environment, type) && admitted(environment, type)) {
          register(environment, type);
        }
      } else if (isPluginModule(environment, type) && requiresSyncLoading(environment, type)) {
        syncComponents.add(type.getQualifiedName().toString());
      }
    }

    element.getEnclosedElements().stream()
        .filter(child -> child.getKind().isClass() || child.getKind().isInterface())
        .forEach(child -> scanElement(environment, child));
  }

  private boolean isPluginModule(final ProcessingEnvironment environment, final TypeElement type) {
    final var module = environment.getElementUtils().getTypeElement(PluginModule.class.getCanonicalName());
    return module != null && environment.getTypeUtils().isAssignable(
        environment.getTypeUtils().erasure(type.asType()),
        environment.getTypeUtils().erasure(module.asType())
    );
  }

  private boolean admitted(final ProcessingEnvironment environment, final TypeElement type) {
    if (compileInterceptors == null) {
      compileInterceptors = ServiceLoader.load(ComponentCompileInterceptor.class,
              ComponentMetadataProcessor.class.getClassLoader()).stream()
          .map(ServiceLoader.Provider::get)
          .sorted(Comparator.comparingInt(ComponentCompileInterceptor::order)
              .thenComparing(interceptor -> interceptor.getClass().getName()))
          .toList();
    }
    for (final var interceptor : compileInterceptors) {
      if (interceptor.decide(environment, type) == ComponentDecision.DISCARD) {
        return false;
      }
    }
    return true;
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
      final TypeElement type
  ) {
    final var isModule = isPluginModule(environment, type);
    final var name = type.getQualifiedName().toString();
    final var external = hasMetadata(type, External.class.getCanonicalName());
    if (requiresSyncLoading(environment, type)) {
      syncComponents.add(name);
    }

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
      for (final var exposed : exposedContracts(environment, type)) {
        final var previous = externalBindings.putIfAbsent(exposed.getQualifiedName().toString(), name);
        if (previous != null && !previous.equals(name)) {
          environment.getMessager().printError(
              "Exposed contract " + exposed.getQualifiedName() + " has multiple @External implementations: "
                  + previous + " and " + name,
              type
          );
        }
      }
    }
  }

  private Set<TypeElement> exposedContracts(final ProcessingEnvironment environment, final TypeElement type) {
    final Set<TypeElement> exposed = new LinkedHashSet<>();
    final Set<String> visited = new LinkedHashSet<>();
    collectExposedContracts(environment, type.asType(), exposed, visited);
    return exposed;
  }

  private void collectExposedContracts(
      final ProcessingEnvironment environment,
      final TypeMirror type,
      final Set<TypeElement> exposed,
      final Set<String> visited
  ) {
    final var element = environment.getTypeUtils().asElement(type);
    if (!(element instanceof TypeElement current) || !visited.add(current.getQualifiedName().toString())) {
      return;
    }
    if (current.getKind() == ElementKind.INTERFACE
        && hasMetadata(current, Expose.class.getCanonicalName())) {
      exposed.add(current);
    }
    for (final var parent : environment.getTypeUtils().directSupertypes(type)) {
      collectExposedContracts(environment, parent, exposed, visited);
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

  private boolean hasMetadata(final TypeElement type, final String annotationName) {
    for (final var annotation : type.getAnnotationMirrors()) {
      if (annotation.getAnnotationType().toString().equals(annotationName)) {
        return true;
      }
      final var annotationType = (TypeElement) annotation.getAnnotationType().asElement();
      if (annotationType.getAnnotationMirrors().stream()
          .anyMatch(meta -> meta.getAnnotationType().toString().equals(annotationName))) {
        return true;
      }
    }
    return false;
  }

  private boolean requiresSyncLoading(final ProcessingEnvironment environment, final TypeElement type) {
    if (hasMetadata(type, SyncLoading.class.getCanonicalName())) {
      return true;
    }
    final var listener = environment.getElementUtils().getTypeElement("org.bukkit.event.Listener");
    if (listener != null && environment.getTypeUtils().isAssignable(type.asType(), listener.asType())) {
      return true;
    }
    final Trees trees;
    try {
      trees = Trees.instance(environment);
    } catch (final IllegalArgumentException unavailable) {
      return true;
    }
    final var path = trees.getPath(type);
    if (path == null) {
      return true;
    }
    final var unsafe = new AtomicBoolean();
    new TreePathScanner<Void, Void>() {
      @Override
      public Void visitIdentifier(final IdentifierTree tree, final Void unused) {
        inspect();
        return super.visitIdentifier(tree, unused);
      }

      @Override
      public Void visitMemberSelect(final MemberSelectTree tree, final Void unused) {
        inspect();
        return super.visitMemberSelect(tree, unused);
      }

      private void inspect() {
        for (var element = trees.getElement(getCurrentPath()); element != null;
             element = element.getEnclosingElement()) {
          if (element instanceof TypeElement owner) {
            final var name = owner.getQualifiedName().toString();
            if (name.startsWith("org.bukkit.") || name.startsWith("io.papermc.paper.")
                || name.startsWith("com.destroystokyo.paper.")) {
              unsafe.set(true);
            }
            return;
          }
        }
      }
    }.scan(path, null);
    return unsafe.get();
  }

  private void writeSyncIndex(final ProcessingEnvironment environment) {
    if (syncIndexWritten || syncComponents.isEmpty()) {
      return;
    }
    syncIndexWritten = true;
    try {
      final var resource = environment.getFiler().createResource(
          StandardLocation.CLASS_OUTPUT, "", SyncLoadingIndex.RESOURCE);
      try (final Writer writer = resource.openWriter()) {
        for (final var name : syncComponents.stream().sorted().toList()) {
          writer.write(name + System.lineSeparator());
        }
      }
    } catch (final IOException exception) {
      environment.getMessager().printError("Cannot write Winter sync-loading index: " + exception.getMessage());
    }
  }

  private void writeExternalIndex(final ProcessingEnvironment environment) {
    if (indexWritten || externalComponents.isEmpty() && externalModules.isEmpty() && externalBindings.isEmpty()) {
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
        for (final var binding : externalBindings.entrySet().stream()
            .sorted(Map.Entry.comparingByKey()).toList()) {
          writer.write(BINDING_ENTRY + binding.getKey() + "=" + binding.getValue() + System.lineSeparator());
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
    } else if (entry.startsWith(BINDING_ENTRY)) {
      final var binding = entry.substring(BINDING_ENTRY.length());
      final var separator = binding.indexOf('=');
      if (separator < 1 || separator == binding.length() - 1) {
        throw new IllegalStateException("Malformed Winter external binding: " + entry);
      }
      final var contract = binding.substring(0, separator);
      final var implementation = binding.substring(separator + 1);
      final var previous = externalBindings.putIfAbsent(contract, implementation);
      if (previous != null && !previous.equals(implementation)) {
        throw new IllegalStateException("Conflicting @External bindings for " + contract + ": "
            + previous + " and " + implementation);
      }
    }
  }
}
