package com.thewinterframework.plugin;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Represents the data folder of the plugin.
 */
@BindingAnnotation
@Retention(RUNTIME)
public @interface DataFolder {
}
