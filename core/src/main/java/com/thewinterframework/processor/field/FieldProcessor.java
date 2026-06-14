package com.thewinterframework.processor.field;

import com.thewinterframework.processor.context.ProcessorContext;
import com.thewinterframework.processor.handler.WinterAnnotationProcessor;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class FieldProcessor implements WinterAnnotationProcessor {
	@Override
	public void handle(final TypeElement annotation, final Set<? extends Element> elements, final ProcessorContext ctx) {
		final var fields = elements.stream()
				.filter(e -> e.getKind() == ElementKind.FIELD)
				.map(e -> (VariableElement) e)
				.collect(Collectors.toSet());

		if (!fields.isEmpty()) {
			processFields(annotation, fields, ctx);
		}
	}

	protected abstract void processFields(TypeElement annotation, Set<VariableElement> fields, ProcessorContext ctx);
}
