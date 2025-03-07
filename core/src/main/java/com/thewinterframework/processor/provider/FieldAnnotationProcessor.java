package com.thewinterframework.processor.provider;

import com.thewinterframework.processor.AbstractWinterAnnotationProcessor;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Annotation processor for field annotations.
 */
public abstract class FieldAnnotationProcessor extends AbstractWinterAnnotationProcessor {

	@Override
	protected boolean process(Element pluginClass, Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		for (final var annotation : annotations) {
			final var fields = roundEnv.getElementsAnnotatedWith(annotation).stream()
					.filter(this::isField)
					.map(element -> (VariableElement) element)
					.collect(Collectors.toSet());

			processFields(pluginClass, fields, roundEnv);
		}

		return true;
	}

	/**
	 * Processes the fields.
	 *
	 * @param pluginClass the plugin class.
	 * @param fields      the fields to process.
	 * @param roundEnv    the round environment.
	 */
	protected abstract void processFields(Element pluginClass, Set<VariableElement> fields, RoundEnvironment roundEnv);
}
