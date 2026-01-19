package com.thewinterframework.provider.annotation;

import com.google.inject.ScopeAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a component is {@link com.google.inject.Provider}.
 * A class annotated with this annotation will be automatically
 * registered as a provider component in the Winter Framework.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ScopeAnnotation
public @interface ProviderComponent {
}
