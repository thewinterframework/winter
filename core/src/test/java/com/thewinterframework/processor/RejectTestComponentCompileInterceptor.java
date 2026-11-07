package com.thewinterframework.processor;

import com.thewinterframework.component.admission.ComponentDecision;
import com.thewinterframework.processor.component.ComponentCompileInterceptor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

/** Test-only processor extension. */
public final class RejectTestComponentCompileInterceptor implements ComponentCompileInterceptor {

  @Override
  public ComponentDecision decide(final ProcessingEnvironment environment, final TypeElement component) {
    return component.getSimpleName().contentEquals("DiscardedComponent")
        ? ComponentDecision.DISCARD : ComponentDecision.CONTINUE;
  }
}
