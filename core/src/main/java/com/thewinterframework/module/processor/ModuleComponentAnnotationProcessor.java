package com.thewinterframework.module.processor;

import com.thewinterframework.module.PluginComponentModule;
import com.thewinterframework.module.annotation.ModuleComponent;
import com.thewinterframework.plugin.module.PluginModule;
import com.thewinterframework.processor.clazz.ClassWireProcessor;
import com.thewinterframework.processor.context.ProcessorContext;
import com.thewinterframework.utils.reflect.ProcessorUtils;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;

/**
 * Annotation processor for {@link ModuleComponent}.
 */
public class ModuleComponentAnnotationProcessor extends ClassWireProcessor {

	@Override
	protected Class<? extends Annotation> wiredAnnotation() {
		return ModuleComponent.class;
	}

	@Override
	public void onRoundStart(final ProcessorContext ctx) {
		ctx.wireModule(PluginComponentModule.class);
	}

	@Override
	protected boolean filter(final TypeElement annotation, final Element element, final ProcessorContext ctx) {
		return ProcessorUtils.isChild(element.asType(), ProcessorUtils.asElement(env, PluginModule.class).asType());
	}
}
