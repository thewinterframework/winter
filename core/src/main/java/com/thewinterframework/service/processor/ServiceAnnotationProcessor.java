package com.thewinterframework.service.processor;

import com.thewinterframework.plugin.module.PluginModule;
import com.thewinterframework.processor.provider.ClassListProviderAnnotationProcessor;
import com.thewinterframework.service.ServiceModule;
import com.thewinterframework.service.annotation.Service;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * Annotation processor for {@link Service}.
 */
public class ServiceAnnotationProcessor extends ClassListProviderAnnotationProcessor {

	@Override
	protected Set<Class<? extends Annotation>> getSupportedAnnotations() {
		return Set.of(Service.class);
	}

	@Override
	protected @Nullable Class<? extends PluginModule> requiredModule() {
		return ServiceModule.class;
	}
}
