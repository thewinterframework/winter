package com.thewinterframework.service.decorator.lifecycle;

import com.thewinterframework.plugin.WinterPlugin;
import com.thewinterframework.service.annotation.lifecycle.OnDisable;

public class OnDisableDecoratorHandler extends LifeCycleDecoratorHandler<OnDisable> {
	@Override
	public Class<OnDisable> getAnnotationType() {
		return OnDisable.class;
	}

	@Override
	public void onPluginDisable(final WinterPlugin plugin) {
		executeInternally(plugin);
	}

	@Override
	protected Class<?>[] extractAfterAnnotations(final OnDisable annotation) {
		return new Class[0];
	}

	@Override
	protected Class<?>[] extractBeforeAnnotations(final OnDisable annotation) {
		return annotation.before();
	}
}
