package com.thewinterframework.command;

import com.thewinterframework.plugin.module.PluginModule;
import com.thewinterframework.processor.provider.ClassListProviderAnnotationProcessor;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.Set;

public class CommandComponentProcessor extends ClassListProviderAnnotationProcessor {

	@Override
	protected Set<Class<? extends Annotation>> getSupportedAnnotations() {
		return Set.of(CommandComponent.class);
	}

	@Override
	protected @Nullable Class<? extends PluginModule> requiredModule() {
		return LampModule.class;
	}
}