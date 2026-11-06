package com.thewinterframework.component;

import com.google.inject.Binder;
import com.thewinterframework.component.annotation.SyncLoading;
import com.thewinterframework.processor.component.ComponentMetadataProcessor;
import com.thewinterframework.processor.module.ProcessorModule;
import com.thewinterframework.plugin.WinterPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/** Installs Guice links exported by dependency modules and discovered at compile time. */
@SyncLoading
public final class ExternalBindingsModule implements ProcessorModule {

  private ClassLoader pluginClassLoader;

  @Override
  public boolean onLoad(final WinterPlugin plugin) {
    pluginClassLoader = plugin.getClass().getClassLoader();
    return true;
  }

  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  public void configure(final Binder binder) {
    final var classLoader = pluginClassLoader != null ? pluginClassLoader
        : Thread.currentThread().getContextClassLoader() == null
            ? getClass().getClassLoader() : Thread.currentThread().getContextClassLoader();
    final Map<String, String> bindings = new LinkedHashMap<>();
    try {
      final var resources = classLoader.getResources(ComponentMetadataProcessor.EXTERNAL_INDEX);
      while (resources.hasMoreElements()) {
        try (final var reader = new BufferedReader(new InputStreamReader(
            resources.nextElement().openStream(), StandardCharsets.UTF_8))) {
          for (final var line : reader.lines().toList()) {
            if (!line.startsWith(ComponentMetadataProcessor.BINDING_ENTRY)) {
              continue;
            }
            final var binding = line.substring(ComponentMetadataProcessor.BINDING_ENTRY.length());
            final var separator = binding.indexOf('=');
            if (separator < 1 || separator == binding.length() - 1) {
              throw new IllegalStateException("Malformed Winter binding: " + line);
            }
            final var contract = binding.substring(0, separator);
            final var implementation = binding.substring(separator + 1);
            final var previous = bindings.putIfAbsent(contract, implementation);
            if (previous != null && !previous.equals(implementation)) {
              throw new IllegalStateException("Conflicting Winter bindings for " + contract);
            }
          }
        }
      }
      for (final var entry : bindings.entrySet()) {
        final var contract = classLoader.loadClass(entry.getKey());
        final var implementation = classLoader.loadClass(entry.getValue());
        if (!contract.isInterface() || !contract.isAssignableFrom(implementation)) {
          throw new IllegalStateException("Invalid Winter binding " + entry);
        }
        binder.bind((Class) contract).to((Class) implementation);
      }
    } catch (final IOException | ClassNotFoundException exception) {
      throw new IllegalStateException("Cannot install Winter external bindings", exception);
    }
  }
}
