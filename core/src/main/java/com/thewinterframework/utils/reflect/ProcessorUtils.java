package com.thewinterframework.utils.reflect;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

public final class ProcessorUtils {
	private ProcessorUtils() {
	}

	public static boolean isChild(final TypeMirror child, final String rootQualifiedName) {
		final var childType = (DeclaredType) child;
		final var childElement = (TypeElement) childType.asElement();

		return childElement.getInterfaces().stream()
				.map(interfaces -> (DeclaredType) interfaces)
				.map(DeclaredType::asElement)
				.map(TypeElement.class::cast)
				.map(TypeElement::getQualifiedName)
				.map(Name::toString)
				.anyMatch(name -> name.equals(rootQualifiedName));
	}

	public static boolean isChild(final TypeMirror child, final TypeMirror root) {
		final var rootType = (DeclaredType) root;
		final var rootElement = (TypeElement) rootType.asElement();
		final var rootQualifiedName = rootElement.getQualifiedName().toString();

		return isChild(child, rootQualifiedName);
	}

	public static Element asElement(final ProcessingEnvironment env, final Class<?> type) {
		return env.getElementUtils().getTypeElement(type.getCanonicalName());
	}
}
