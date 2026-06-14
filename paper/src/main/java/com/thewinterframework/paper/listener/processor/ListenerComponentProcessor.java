package com.thewinterframework.paper.listener.processor;

import com.google.auto.service.AutoService;
import com.thewinterframework.paper.listener.ListenerComponent;
import com.thewinterframework.paper.listener.module.ListenerModule;
import com.thewinterframework.processor.clazz.ClassWireProcessor;
import com.thewinterframework.processor.context.ProcessorContext;
import com.thewinterframework.processor.handler.WinterAnnotationProcessor;
import com.thewinterframework.utils.reflect.ProcessorUtils;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;

/**
 * A processor that provides a list of listener components.
 */
@AutoService(WinterAnnotationProcessor.class)
public class ListenerComponentProcessor extends ClassWireProcessor {
	@Override
	protected Class<? extends Annotation> wiredAnnotation() {
		return ListenerComponent.class;
	}

	@Override
	public void onRoundStart(final ProcessorContext ctx) {
		ctx.wireModule(ListenerModule.class);
	}

	@Override
	protected boolean filter(final TypeElement annotation, final Element element, final ProcessorContext ctx) {
		return ProcessorUtils.isChild(element.asType(), "org.bukkit.event.Listener");
	}
}
