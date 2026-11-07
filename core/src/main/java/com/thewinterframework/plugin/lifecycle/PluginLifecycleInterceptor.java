package com.thewinterframework.plugin.lifecycle;

import com.thewinterframework.plugin.WinterPlugin;

/** Global plugin lifecycle observer registered through {@link java.util.ServiceLoader}. */
public interface PluginLifecycleInterceptor {

  default int order() {
    return 0;
  }

  default void before(PluginPhase phase, WinterPlugin plugin) throws Exception {
  }

  default void after(PluginPhase phase, WinterPlugin plugin) throws Exception {
  }

  default void onFailure(PluginPhase phase, WinterPlugin plugin, Throwable failure) {
  }
}
