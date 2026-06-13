package com.thewinterframework.provider.processor;

import com.google.auto.service.AutoService;
import com.thewinterframework.processor.clazz.ClassWireProcessor;
import com.thewinterframework.processor.context.ProcessorContext;
import com.thewinterframework.processor.handler.WinterAnnotationProcessor;
import com.thewinterframework.provider.ProviderModule;
import com.thewinterframework.provider.annotation.ProviderComponent;

import java.lang.annotation.Annotation;

@AutoService(WinterAnnotationProcessor.class)
public class ProviderAnnotationProcessor extends ClassWireProcessor {
	@Override
	protected Class<? extends Annotation> wiredAnnotation() {
		return ProviderComponent.class;
	}

	@Override
	public void onRoundStart(final ProcessorContext ctx) {
		ctx.wireModule(ProviderModule.class);
	}
}
