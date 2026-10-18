package com.thewinterframework.velocity;

import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.PluginDescription;
import com.velocitypowered.api.plugin.PluginManager;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import com.velocitypowered.api.scheduler.Scheduler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

final class VelocityTestEnvironment {

	final Map<String, PluginContainer> plugins = new LinkedHashMap<>();
	final Map<String, Object> pluginInstances = new LinkedHashMap<>();
	final Map<Object, Object> listeners = new IdentityHashMap<>();
	final List<Task> tasks = new ArrayList<>();
	boolean executeImmediately;
	final PluginManager pluginManager = stub(PluginManager.class, (proxy, method, args) -> switch (method.getName()) {
		case "getPlugin" -> Optional.ofNullable(plugins.get(args[0]));
		case "getPlugins" -> plugins.values();
		case "fromInstance" -> plugins.values().stream().filter(p -> p.getInstance().orElse(null) == args[0]).findFirst();
		default -> throw new UnsupportedOperationException(method.toString());
	});
	final EventManager eventManager = stub(EventManager.class, (proxy, method, args) -> {
		switch (method.getName()) {
			case "register" -> listeners.put(args[1], args[0]);
			case "unregisterListener" -> listeners.remove(args[1]);
			case "unregisterListeners" -> listeners.values().removeIf(owner -> owner == args[0]);
			default -> throw new UnsupportedOperationException(method.toString());
		}
		return null;
	});
	final Scheduler scheduler = stub(Scheduler.class, (proxy, method, args) -> switch (method.getName()) {
		case "buildTask" -> new Task(args[0], (Runnable) args[1]).builder;
		case "tasksByPlugin" -> tasks.stream().filter(t -> t.owner == args[0] && !t.cancelled && !t.completed).map(t -> t.handle).toList();
		default -> throw new UnsupportedOperationException(method.toString());
	});
	final ProxyServer server = stub(ProxyServer.class, (proxy, method, args) -> switch (method.getName()) {
		case "getPluginManager" -> pluginManager;
		case "getEventManager" -> eventManager;
		case "getScheduler" -> scheduler;
		default -> throw new UnsupportedOperationException(method.toString());
	});

	void registerPlugin(final String id, final Object instance) {
		pluginInstances.put(id, instance);
		if (plugins.containsKey(id)) return;
		final var description = stub(PluginDescription.class, (proxy, method, args) -> switch (method.getName()) {
			case "getId" -> id;
			default -> throw new UnsupportedOperationException(method.toString());
		});
		plugins.put(id, stub(PluginContainer.class, (proxy, method, args) -> switch (method.getName()) {
			case "getDescription" -> description;
			case "getInstance" -> Optional.ofNullable(pluginInstances.get(id));
			default -> throw new UnsupportedOperationException(method.toString());
		}));
	}

	static <T> T stub(final Class<T> type, final InvocationHandler handler) {
		return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (proxy, method, args) -> {
			if (method.getDeclaringClass() == Object.class) {
				return switch (method.getName()) {
					case "equals" -> proxy == args[0];
					case "hashCode" -> System.identityHashCode(proxy);
					case "toString" -> "Test " + type.getSimpleName();
					default -> throw new UnsupportedOperationException(method.toString());
				};
			}
			return handler.invoke(proxy, method, args);
		}));
	}

	final class Task {
		final Object owner;
		final Runnable action;
		Duration delay = Duration.ZERO;
		Duration repeat = Duration.ZERO;
		boolean cancelled;
		boolean completed;
		final ScheduledTask handle = stub(ScheduledTask.class, (proxy, method, args) -> {
			if (method.getName().equals("cancel")) {
				cancelled = true;
				return null;
			}
			throw new UnsupportedOperationException(method.toString());
		});
		final Scheduler.TaskBuilder builder = stub(Scheduler.TaskBuilder.class, (proxy, method, args) -> {
			switch (method.getName()) {
				case "delay" -> delay = duration(args);
				case "repeat" -> repeat = duration(args);
				case "schedule" -> {
					tasks.add(this);
					if (executeImmediately) execute();
					return handle;
				}
				default -> throw new UnsupportedOperationException(method.toString());
			}
			return proxy;
		});

		Task(final Object owner, final Runnable action) {
			this.owner = owner;
			this.action = action;
		}

		void execute() {
			if (cancelled) return;
			try {
				action.run();
			} finally {
				completed = repeat.isZero();
			}
		}

		private Duration duration(final Object[] args) {
			return args[0] instanceof Duration duration ? duration : Duration.ofNanos(((TimeUnit) args[1]).toNanos((Long) args[0]));
		}
	}
}
