package com.thewinterframework.service.meta;

import com.thewinterframework.service.meta.lifecycle.LifeCycleMethod;
import com.thewinterframework.service.meta.scheduler.SchedulerMethod;

import java.util.List;

/**
 * Represents the metadata of a service.
 */
public record ServiceMeta(
		Class<?> clazz,
		List<? extends LifeCycleMethod> onEnableMethods,
		List<? extends LifeCycleMethod> onDisableMethods,
		List<? extends LifeCycleMethod> onReloadMethods,
		List<? extends SchedulerMethod> schedulerMethods
) {}
