package com.thewinterframework.service.decorator.lifecycle;

import com.thewinterframework.plugin.WinterPlugin;
import com.thewinterframework.service.annotation.lifecycle.OnEnable;

public class OnEnableDecoratorHandler extends LifeCycleDecoratorHandler<OnEnable> {
	@Override
	public Class<OnEnable> getAnnotationType() {
		return OnEnable.class;
	}

	@Override
	public void onPluginEnable(WinterPlugin plugin) {
		executeInternally(plugin);
	}

	@Override
	protected Class<?>[] extractAfterAnnotations(OnEnable annotation) {
		return annotation.after();
	}

	@Override
	protected Class<?>[] extractBeforeAnnotations(OnEnable annotation) {
		return new Class[0];
	}
}
