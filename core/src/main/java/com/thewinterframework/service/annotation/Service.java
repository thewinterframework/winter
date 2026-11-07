package com.thewinterframework.service.annotation;

import com.thewinterframework.component.annotation.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a component is a service <br>
 * and should be injected into the service.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
@Deprecated(forRemoval = false)
public @interface Service {}
