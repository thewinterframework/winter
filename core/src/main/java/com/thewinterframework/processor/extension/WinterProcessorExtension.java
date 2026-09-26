package com.thewinterframework.processor.extension;

import com.thewinterframework.processor.context.ProcessorContext;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

/**
 * Service-provider interface for addon annotation processors.
 *
 * <p>Register implementations through {@link java.util.ServiceLoader}. Annotation names are
 * strings so an addon can remain optional without forcing its annotation types into Winter's
 * processor class loader.</p>
 */
public interface WinterProcessorExtension {

	Set<String> supportedAnnotationNames();

	default int order() {
		return 0;
	}

	default void initialize(final ProcessingEnvironment environment) {
	}

	default void onRoundStarted(final ProcessorContext context) {
	}

	void process(
			TypeElement annotation,
			Set<? extends Element> elements,
			ProcessorContext context
	);

	default void onRoundFinished(final ProcessorContext context) {
	}

	default void onProcessingFinished(final ProcessorContext context) {
	}
}
