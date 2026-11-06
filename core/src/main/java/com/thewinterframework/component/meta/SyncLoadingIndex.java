package com.thewinterframework.component.meta;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/** Reads compile-time main-thread classifications from each plugin class loader. */
public final class SyncLoadingIndex {

  public static final String RESOURCE = "META-INF/winter/sync-components.index";
  private static final Map<ClassLoader, Set<String>> CACHE = Collections.synchronizedMap(new WeakHashMap<>());

  private SyncLoadingIndex() {
  }

  public static boolean contains(final Class<?> type) {
    final var loader = type.getClassLoader();
    return loader != null && CACHE.computeIfAbsent(loader, SyncLoadingIndex::read).contains(type.getName());
  }

  private static Set<String> read(final ClassLoader loader) {
    final Set<String> names = new HashSet<>();
    try {
      final var resources = loader.getResources(RESOURCE);
      while (resources.hasMoreElements()) {
        try (final var reader = new BufferedReader(new InputStreamReader(
            resources.nextElement().openStream(), StandardCharsets.UTF_8))) {
          reader.lines().map(String::trim).filter(line -> !line.isEmpty()).forEach(names::add);
        }
      }
    } catch (final IOException exception) {
      throw new IllegalStateException("Cannot read Winter sync-loading metadata", exception);
    }
    return Set.copyOf(names);
  }
}
