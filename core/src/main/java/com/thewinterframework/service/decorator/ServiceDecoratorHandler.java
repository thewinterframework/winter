package com.thewinterframework.service.decorator;

import com.thewinterframework.component.decorator.ComponentDecoratorHandler;

import java.lang.annotation.Annotation;

/**
 * @deprecated Use {@link ComponentDecoratorHandler}; decorators are no longer limited to services.
 */
@Deprecated(forRemoval = false)
public interface ServiceDecoratorHandler<A extends Annotation> extends ComponentDecoratorHandler<A> {
}
