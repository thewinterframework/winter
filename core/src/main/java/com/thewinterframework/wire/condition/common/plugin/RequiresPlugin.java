package com.thewinterframework.wire.condition.common.plugin;

import com.thewinterframework.wire.condition.annotation.Requires;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Registers a component only when the named plugin is present at load time.
 * Declare the corresponding (optional) dependency in the platform's plugin
 * descriptor as well, so its classes are visible and its lifecycle is ordered.
 * On Velocity, the value is the plugin ID.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Requires(RequiresPluginCondition.class)
public @interface RequiresPlugin {
	String value();
}
