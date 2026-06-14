package com.thewinterframework.wire.condition;

import com.thewinterframework.plugin.module.Stage;
import com.thewinterframework.wire.condition.context.ConditionContext;

import java.lang.annotation.Annotation;

public interface ComponentCondition {

	Stage getStage();

	boolean matches(final ConditionContext context, final Annotation annotation);
}