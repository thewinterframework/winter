package com.thewinterframework.component.annotation;

import com.thewinterframework.component.interceptor.ComponentInterceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Applies a runtime interceptor to a component. */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(InterceptedBy.List.class)
public @interface InterceptedBy {
  Class<? extends ComponentInterceptor> value();

  @Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @interface List {
    InterceptedBy[] value();
  }
}
