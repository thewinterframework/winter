package com.thewinterframework.provider.processor;

import com.thewinterframework.plugin.module.PluginModule;
import com.thewinterframework.processor.provider.ClassListProviderAnnotationProcessor;
import com.thewinterframework.provider.ProviderModule;
import com.thewinterframework.provider.annotation.ProviderComponent;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * Annotation processor for {@link com.thewinterframework.provider.annotation.ProviderComponent}
 */
public final class ProviderAnnotationProcessor extends ClassListProviderAnnotationProcessor {

	@Override
	protected Set<Class<? extends Annotation>> getSupportedAnnotations() {
		return Set.of(ProviderComponent.class);
	}

	@Override
	protected @NotNull Class<? extends PluginModule> requiredModule() {
		return ProviderModule.class;
	}
}
