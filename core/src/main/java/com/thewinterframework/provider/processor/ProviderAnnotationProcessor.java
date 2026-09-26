package com.thewinterframework.provider.processor;

import com.thewinterframework.processor.clazz.ClassWireProcessor;
import com.thewinterframework.processor.context.ProcessorContext;
import com.thewinterframework.provider.ProviderModule;
import com.thewinterframework.provider.annotation.ProviderComponent;

import java.lang.annotation.Annotation;

@Deprecated(forRemoval = false)
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
