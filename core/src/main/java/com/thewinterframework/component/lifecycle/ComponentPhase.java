package com.thewinterframework.component.lifecycle;

/** Lifecycle phases exposed to component interceptors. */
public enum ComponentPhase {
  LOAD,
  INJECTION,
  ENABLE,
  DISABLE
}
