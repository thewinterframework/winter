package com.thewinterframework.wire.condition.common.expression;

import com.thewinterframework.plugin.module.Stage;
import com.thewinterframework.wire.condition.ComponentCondition;
import com.thewinterframework.wire.condition.context.ConditionContext;

import java.lang.annotation.Annotation;

public class RequiresExpressionCondition implements ComponentCondition {
	@Override
	public Stage getStage() {
		return Stage.ENABLE;
	}

	@Override
	public boolean matches(final ConditionContext context, final Annotation rawAnnotation) {
		final var annotation = (RequiresExpr) rawAnnotation;
		final var resolver = context.getPlugin().getExpressionResolver();
		final var expression = annotation.value();

		return Boolean.TRUE.equals(resolver.resolve(expression, Boolean.class));
	}
}
