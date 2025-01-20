package com.thewinterframework.service.meta;

import com.thewinterframework.service.meta.lifecycle.LifeCycleMethod;
import com.thewinterframework.service.meta.scheduler.SchedulerMethod;

import java.util.List;

/**
 * Represents the metadata of a service.
 */
public record ServiceMeta(
		Class<?> clazz,
		List<LifeCycleMethod> onEnableMethods,
		List<LifeCycleMethod> onDisableMethods,
		List<? extends SchedulerMethod> schedulerMethods
) {}
