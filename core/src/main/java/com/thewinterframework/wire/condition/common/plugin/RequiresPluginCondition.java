package com.thewinterframework.wire.condition.common.plugin;

import com.thewinterframework.plugin.module.Stage;
import com.thewinterframework.wire.condition.ComponentCondition;
import com.thewinterframework.wire.condition.context.ConditionContext;

import java.lang.annotation.Annotation;

public class RequiresPluginCondition implements ComponentCondition {
	@Override
	public Stage getStage() {
		return Stage.LOAD;
	}

	@Override
	public boolean matches(final ConditionContext context, final Annotation annotation) {
		final var requires = (RequiresPlugin) annotation;
		return context.getPlugin().getPlatformManager().isPluginEnabled(requires.value());
	}
}
