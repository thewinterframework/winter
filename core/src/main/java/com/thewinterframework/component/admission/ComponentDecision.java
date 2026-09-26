package com.thewinterframework.component.admission;

/**
 * Result of evaluating a component during the admission stage.
 */
public enum ComponentDecision {

	/** Continue registering the component. */
	CONTINUE,

	/** Exclude the component from dependency injection and every lifecycle phase. */
	DISCARD
}
