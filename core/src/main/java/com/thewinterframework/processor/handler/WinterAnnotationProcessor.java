package com.thewinterframework.processor.handler;

import com.thewinterframework.processor.context.ProcessorContext;
import com.thewinterframework.processor.extension.WinterProcessorExtension;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.Set;

@Deprecated(forRemoval = false)
public interface WinterAnnotationProcessor extends WinterProcessorExtension {

	Set<Class<? extends Annotation>> getSupportedAnnotations();

	@Override
	default Set<String> supportedAnnotationNames() {
		return getSupportedAnnotations().stream().map(Class::getCanonicalName).collect(java.util.stream.Collectors.toSet());
	}

	default void onInit(final ProcessingEnvironment env) {
	}

	@Override
	default void initialize(final ProcessingEnvironment environment) {
		onInit(environment);
	}

	default void onRoundStart(final ProcessorContext ctx) {
	}

	@Override
	default void onRoundStarted(final ProcessorContext context) {
		onRoundStart(context);
	}

	void handle(
			final TypeElement annotation,
			final Set<? extends Element> elements,
			final ProcessorContext ctx
	);

	@Override
	default void process(
			final TypeElement annotation,
			final Set<? extends Element> elements,
			final ProcessorContext context
	) {
		handle(annotation, elements, context);
	}

	default void onRoundEnd(final ProcessorContext ctx) {
	}

	@Override
	default void onRoundFinished(final ProcessorContext context) {
		onRoundEnd(context);
	}

	default void onProcessingComplete(final ProcessorContext ctx) {
	}

	@Override
	default void onProcessingFinished(final ProcessorContext context) {
		onProcessingComplete(context);
	}
}
