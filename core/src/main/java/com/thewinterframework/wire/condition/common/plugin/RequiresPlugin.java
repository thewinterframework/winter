package com.thewinterframework.wire.condition.common.plugin;

import com.thewinterframework.wire.condition.annotation.Requires;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Requires(RequiresPluginCondition.class)
public @interface RequiresPlugin {
	String value();
}
