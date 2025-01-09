package com.thewinterframework.service.meta.scheduler;

import com.thewinterframework.plugin.WinterPlugin;
import com.thewinterframework.utils.Reflections.AnnotatedMethodHandle;

public interface SchedulerMethod {

	AnnotatedMethodHandle<?> method();

	int schedule(WinterPlugin plugin);

}
