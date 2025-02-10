package com.thewinterframework.paper.listener;

import com.google.inject.ScopeAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A scope annotation for listener components.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ScopeAnnotation
public @interface ListenerComponent {
}
