package com.thewinterframework.wire.condition.common.expression;

import com.thewinterframework.wire.condition.annotation.Requires;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Requires(RequiresExpressionCondition.class)
public @interface RequiresExpr {
	String value();
}
