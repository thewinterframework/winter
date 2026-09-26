package com.thewinterframework.component.annotation;

/**
 * Controls where an annotated component is installed.
 */
public enum ComponentScope {
	/** Install the component only in the project that declares it. */
	LOCAL,
	/** Export the component so consumers can install it from their annotation processor path. */
	EXTERNAL
}
