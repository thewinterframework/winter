package com.thewinterframework.service.processor;

import com.google.auto.service.AutoService;
import com.thewinterframework.processor.clazz.ClassWireProcessor;
import com.thewinterframework.processor.context.ProcessorContext;
import com.thewinterframework.processor.handler.WinterAnnotationProcessor;
import com.thewinterframework.service.ServiceModule;
import com.thewinterframework.service.annotation.Service;

import java.lang.annotation.Annotation;

/**
 * Annotation processor for {@link Service}.
 */
@AutoService(WinterAnnotationProcessor.class)
public class ServiceAnnotationProcessor extends ClassWireProcessor {
	@Override
	public void onRoundStart(final ProcessorContext ctx) {
		ctx.wireModule(ServiceModule.class);
	}

	@Override
	protected Class<? extends Annotation> wiredAnnotation() {
		return Service.class;
	}
}
