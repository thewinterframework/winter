package com.thewinterframework.processor.component;

import com.thewinterframework.component.admission.ComponentDecision;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

/** Compile-time admission policy discovered through {@link java.util.ServiceLoader}. */
public interface ComponentCompileInterceptor {

  default int order() {
    return 0;
  }

  ComponentDecision decide(ProcessingEnvironment environment, TypeElement component);
}
