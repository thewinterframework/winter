package com.thewinterframework.component.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a Winter-managed component.
 *
 * <p>The annotation can be placed directly on a class or on another annotation to create a
 * domain-specific component stereotype. Components are singletons by default.</p>
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Component {
}
