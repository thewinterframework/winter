package com.thewinterframework.plugin.lifecycle;

/** Public boundaries of a Winter plugin lifecycle. */
public enum PluginPhase {
  SCAN,
  LOAD,
  INJECTION,
  ENABLE,
  DISABLE
}
