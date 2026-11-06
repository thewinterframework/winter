package com.thewinterframework.component.meta;

import com.thewinterframework.component.annotation.Component;
import com.thewinterframework.component.annotation.ComponentScope;
import com.thewinterframework.component.annotation.ComponentPriority;
import com.thewinterframework.component.annotation.External;
import com.thewinterframework.component.annotation.LoadAfter;
import com.thewinterframework.component.annotation.LoadBefore;
import com.thewinterframework.component.annotation.SyncLoading;
import com.thewinterframework.component.handler.ComponentHandler;
import com.thewinterframework.component.interceptor.ComponentInterceptor;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/** Immutable runtime metadata for a component. */
public record ComponentDescriptor(
    Class<?> type,
    ComponentScope scope,
    int order,
    List<Class<?>> after,
    List<Class<?>> before,
    boolean async,
    List<Class<? extends ComponentHandler>> handlers,
    List<Class<? extends ComponentInterceptor>> interceptors
) {

  public ComponentDescriptor {
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(scope, "scope");
    after = List.copyOf(after);
    before = List.copyOf(before);
    handlers = List.copyOf(handlers);
    interceptors = List.copyOf(interceptors);
  }

  public static ComponentDescriptor from(final Class<?> type) {
    if (!isComponent(type)) {
      throw new IllegalArgumentException(type.getName() + " is not annotated with @Component");
    }

    final var priority = findMetadata(type, ComponentPriority.class);
    final var loadAfter = findMetadata(type, LoadAfter.class);
    final var loadBefore = findMetadata(type, LoadBefore.class);
    return new ComponentDescriptor(
        type,
        hasMetadata(type, External.class) ? ComponentScope.EXTERNAL : ComponentScope.LOCAL,
        priority == null ? 0 : priority.value(),
        loadAfter == null ? List.of() : List.of(loadAfter.value()),
        loadBefore == null ? List.of() : List.of(loadBefore.value()),
        !hasMetadata(type, SyncLoading.class) && !SyncLoadingIndex.contains(type),
        metadataTypes(type, com.thewinterframework.component.annotation.HandledBy.class)
            .map(com.thewinterframework.component.annotation.HandledBy::value).toList(),
        metadataTypes(type, com.thewinterframework.component.annotation.InterceptedBy.class)
            .map(com.thewinterframework.component.annotation.InterceptedBy::value).toList()
    );
  }

  public static boolean isComponent(final Class<?> type) {
    return type.isAnnotationPresent(Component.class) || stereotypeTypes(type).findAny().isPresent();
  }

  private static <A extends Annotation> A findMetadata(final Class<?> type, final Class<A> annotationType) {
    final var direct = type.getDeclaredAnnotation(annotationType);
    if (direct != null) {
      return direct;
    }
    return stereotypeTypes(type)
        .map(stereotype -> stereotype.getDeclaredAnnotation(annotationType))
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(null);
  }

  private static boolean hasMetadata(final Class<?> type, final Class<? extends Annotation> annotationType) {
    return type.isAnnotationPresent(annotationType)
        || stereotypeTypes(type).anyMatch(stereotype -> stereotype.isAnnotationPresent(annotationType));
  }

  private static <A extends Annotation> Stream<A> metadataTypes(final Class<?> type, final Class<A> annotationType) {
    return Stream.concat(
        Arrays.stream(type.getDeclaredAnnotationsByType(annotationType)),
        stereotypeTypes(type).flatMap(stereotype -> Arrays.stream(stereotype.getDeclaredAnnotationsByType(annotationType)))
    ).distinct();
  }

  private static Stream<Class<? extends Annotation>> stereotypeTypes(final Class<?> type) {
    return Arrays.stream(type.getDeclaredAnnotations())
        .map(Annotation::annotationType)
        .filter(annotationType -> annotationType.isAnnotationPresent(Component.class))
        .sorted(java.util.Comparator.comparing(Class::getName));
  }
}
