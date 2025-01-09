package com.thewinterframework.module.processor;

import com.thewinterframework.module.PluginComponentModule;
import com.thewinterframework.module.annotation.ModuleComponent;
import com.thewinterframework.plugin.module.PluginModule;
import com.thewinterframework.processor.provider.ClassListProviderAnnotationProcessor;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.Element;
import java.lang.annotation.Annotation;
import java.util.Set;

public class ModuleComponentAnnotationProcessor extends ClassListProviderAnnotationProcessor {
	@Override
	protected boolean filterClass(Element element) {
		return isChild(element.asType(), asElement(PluginModule.class).asType());
	}

	@Override
	protected Set<Class<? extends Annotation>> getSupportedAnnotations() {
		return Set.of(ModuleComponent.class);
	}

	@Override
	protected @Nullable Class<? extends PluginModule> requiredModule() {
		return PluginComponentModule.class;
	}
}
