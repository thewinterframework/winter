package com.thewinterframework.plugin;

/** Routine framework messages enabled by a compile-time Winter property. */
public final class WinterDiagnostics {

  private static final ClassValue<Boolean> DEBUG = new ClassValue<>() {
    @Override
    protected Boolean computeValue(final Class<?> pluginType) {
      final var resource = "META-INF/winter/debug/" + pluginType.getName();
      return pluginType.getClassLoader().getResource(resource) != null;
    }
  };

  private WinterDiagnostics() {
  }

  public static void log(final WinterPlugin plugin, final String message, final Object... arguments) {
    if (DEBUG.get(plugin.getClass())) {
      plugin.getSLF4JLogger().info(message, arguments);
    } else {
      plugin.getSLF4JLogger().debug(message, arguments);
    }
  }
}
