package com.thewinterframework.service.meta.scheduler;

import com.thewinterframework.plugin.WinterPlugin;
import com.thewinterframework.utils.Reflections.AnnotatedMethodHandle;
import com.thewinterframework.utils.TimeUnit;

public record RepeatingTaskMethod(Class<?> service, AnnotatedMethodHandle<?> method, long delay, long every, TimeUnit unit, boolean async) implements SchedulerMethod {
	@Override
	public int schedule(WinterPlugin plugin) {
		return plugin.scheduleRepeatingTask(
				() -> {
					try {
						method.invoke(service, plugin.getInjector());
					} catch (Throwable e) {
						plugin.getSLF4JLogger().error("Error while executing repeating task", e);
					}
				},
				delay,
				every,
				unit,
				async
		);
	}
}
