package com.thewinterframework.processor.handler;

import com.thewinterframework.processor.context.ProcessorContext;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.Set;

public interface WinterAnnotationProcessor {

	Set<Class<? extends Annotation>> getSupportedAnnotations();

	default void onInit(final ProcessingEnvironment env) {
	}

	default void onRoundStart(final ProcessorContext ctx) {
	}

	void handle(
			final TypeElement annotation,
			final Set<? extends Element> elements,
			final ProcessorContext ctx
	);

	default void onRoundEnd(final ProcessorContext ctx) {
	}
}
