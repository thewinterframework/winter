package com.thewinterframework.velocity.listener.processor;

import com.google.auto.service.AutoService;
import com.thewinterframework.processor.clazz.ClassWireProcessor;
import com.thewinterframework.processor.context.ProcessorContext;
import com.thewinterframework.processor.handler.WinterAnnotationProcessor;
import com.thewinterframework.velocity.listener.ListenerComponent;
import com.thewinterframework.velocity.listener.module.ListenerModule;

import java.lang.annotation.Annotation;

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
}
