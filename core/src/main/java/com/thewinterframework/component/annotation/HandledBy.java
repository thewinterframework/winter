package com.thewinterframework.component.annotation;

import com.thewinterframework.component.handler.ComponentHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Adds a component capability supplied by a handler. */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(HandledBy.List.class)
public @interface HandledBy {
  Class<? extends ComponentHandler> value();

  @Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @interface List {
    HandledBy[] value();
  }
}
