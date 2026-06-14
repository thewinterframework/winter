package com.thewinterframework.wire.condition.annotation;

import com.thewinterframework.wire.condition.ComponentCondition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.ANNOTATION_TYPE) // Importante: Se aplica sobre otras anotaciones
@Retention(RetentionPolicy.RUNTIME)
public @interface Requires {
	Class<? extends ComponentCondition> value();
}