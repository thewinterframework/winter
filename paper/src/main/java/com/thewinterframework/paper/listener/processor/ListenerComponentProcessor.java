package com.thewinterframework.paper.listener.processor;

import com.thewinterframework.paper.listener.ListenerComponent;
import com.thewinterframework.paper.listener.module.ListenerModule;
import com.thewinterframework.plugin.module.PluginModule;
import com.thewinterframework.processor.provider.ClassListProviderAnnotationProcessor;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.Element;
import java.lang.annotation.Annotation;
import java.util.Set;

public class ListenerComponentProcessor extends ClassListProviderAnnotationProcessor {
	@Override
	protected boolean filterClass(Element element) {
		return isChild(element.asType(), "org.bukkit.event.Listener");
	}

	@Override
	protected Set<Class<? extends Annotation>> getSupportedAnnotations() {
		return Set.of(ListenerComponent.class);
	}

	@Override
	protected @Nullable Class<? extends PluginModule> requiredModule() {
		return ListenerModule.class;
	}
}
