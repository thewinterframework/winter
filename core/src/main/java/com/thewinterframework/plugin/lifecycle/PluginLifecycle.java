package com.thewinterframework.plugin.lifecycle;

import com.thewinterframework.plugin.WinterPlugin;

import java.util.Comparator;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;

/** Executes lifecycle extensions in stable order and preserves failures. */
public final class PluginLifecycle {

  private final WinterPlugin plugin;
  private final List<PluginLifecycleInterceptor> interceptors;

  public PluginLifecycle(final WinterPlugin plugin) {
    this(plugin, ServiceLoader.load(PluginLifecycleInterceptor.class, plugin.getClass().getClassLoader())
        .stream().map(ServiceLoader.Provider::get).toList());
  }

  public PluginLifecycle(final WinterPlugin plugin,
                         final Collection<? extends PluginLifecycleInterceptor> interceptors) {
    this.plugin = Objects.requireNonNull(plugin, "plugin");
    this.interceptors = interceptors.stream()
        .map(interceptor -> (PluginLifecycleInterceptor) interceptor)
        .sorted(Comparator.comparingInt(PluginLifecycleInterceptor::order)
            .thenComparing(interceptor -> interceptor.getClass().getName()))
        .toList();
  }

  public void run(final PluginPhase phase, final CheckedAction action) throws Exception {
    try {
      for (final var interceptor : interceptors) {
        interceptor.before(phase, plugin);
      }
      action.run();
      for (var index = interceptors.size() - 1; index >= 0; index--) {
        interceptors.get(index).after(phase, plugin);
      }
    } catch (final Exception | Error failure) {
      for (final var interceptor : interceptors) {
        try {
          interceptor.onFailure(phase, plugin, failure);
        } catch (final Throwable callbackFailure) {
          failure.addSuppressed(callbackFailure);
        }
      }
      throw failure;
    }
  }

  @FunctionalInterface
  public interface CheckedAction {
    void run() throws Exception;
  }
}
