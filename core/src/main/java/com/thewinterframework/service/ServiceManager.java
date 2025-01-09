package com.thewinterframework.service;

import com.google.inject.Injector;
import com.thewinterframework.plugin.WinterPlugin;
import com.thewinterframework.service.annotation.lifecycle.OnDisable;
import com.thewinterframework.service.annotation.lifecycle.OnEnable;
import com.thewinterframework.service.annotation.scheduler.RepeatingTask;
import com.thewinterframework.service.meta.ServiceMeta;
import com.thewinterframework.service.meta.lifecycle.LifeCycleMethod;
import com.thewinterframework.service.meta.scheduler.RepeatingTaskMethod;
import com.thewinterframework.utils.Reflections;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;

import java.util.*;

public class ServiceManager {

	private final Map<Class<?>, ServiceMeta> metaByService = new HashMap<>();

	private final Graph<LifeCycleMethod, DefaultEdge> onEnableGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
	private final Graph<LifeCycleMethod, DefaultEdge> onDisableGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
	private final List<Integer> schedulersIds = new ArrayList<>();

	/**
	 * Register a service with the service manager.
	 *
	 * @param service The service to register.
	 */
	public void registerService(final @NotNull Class<?> service) throws IllegalAccessException, NoSuchMethodException {
		final var onEnableMethods = Reflections.findMethodsWith(service, OnEnable.class)
				.stream()
				.map(annotatedMethodHandle -> new LifeCycleMethod(service, annotatedMethodHandle, new Class[]{}, annotatedMethodHandle.annotation().after()))
				.toList();
		final var onDisableMethods = Reflections.findMethodsWith(service, OnDisable.class)
				.stream()
				.map(annotatedMethodHandle -> new LifeCycleMethod(service, annotatedMethodHandle, annotatedMethodHandle.annotation().before(), new Class[]{}))
				.toList();
		final var repeatingTaskMethods = Reflections.findMethodsWith(service, RepeatingTask.class)
				.stream()
				.map(annotatedMethodHandle ->
						new RepeatingTaskMethod(
								service,
								annotatedMethodHandle,
								annotatedMethodHandle.annotation().delay(),
								annotatedMethodHandle.annotation().every(),
								annotatedMethodHandle.annotation().unit(),
								annotatedMethodHandle.annotation().async()
						)
				)
				.toList();

		final var serviceMeta = new ServiceMeta(service, onEnableMethods, onDisableMethods, repeatingTaskMethods);
		metaByService.put(service, serviceMeta);
	}

	/**
	 * Build the graphs for the onEnable and onDisable methods.
	 */
	public void builtGraphs() {
		for (final var serviceMeta : metaByService.values()) {
			for (final var onEnableMethod : serviceMeta.onEnableMethods()) {
				onEnableGraph.addVertex(onEnableMethod);

				for (final var dependency : onEnableMethod.after()) {
					metaByService.get(dependency)
							.onEnableMethods()
							.forEach(dependencyMethod -> {
								onEnableGraph.addVertex(dependencyMethod); // if the dependency is not registered, it will be added to the graph
								onEnableGraph.addEdge(dependencyMethod, onEnableMethod);
							});
				}
			}

			for (final var onDisableMethod : serviceMeta.onDisableMethods()) {
				onDisableGraph.addVertex(onDisableMethod);

				for (final var dependency : onDisableMethod.before()) {
					metaByService.get(dependency)
							.onDisableMethods()
							.forEach(dependencyMethod -> {
								onDisableGraph.addVertex(dependencyMethod); // if the dependency is not registered, it will be added to the graph
								onDisableGraph.addEdge(onDisableMethod, dependencyMethod);
							});
				}
			}
		}
	}

	public HandleServicesResult enableServices(Injector injector) {
		return handleServices(injector, onEnableGraph);
	}

	public void startSchedulers(WinterPlugin plugin) {
		for (final var serviceMeta : metaByService.values()) {
			for (final var schedulerMethod : serviceMeta.schedulerMethods()) {
				schedulersIds.add(schedulerMethod.schedule(plugin));
			}
		}
	}

	public void stopTasks(WinterPlugin plugin) {
		for (final var schedulerId : schedulersIds) {
			plugin.cancelTask(schedulerId);
		}
	}

	public HandleServicesResult disableServices(Injector injector) {
		return handleServices(injector, onDisableGraph);
	}

	private static HandleServicesResult handleServices(final Injector injector, final Graph<LifeCycleMethod, DefaultEdge> graph) {
		final var cycleDetector = new CycleDetector<>(graph);
		if (cycleDetector.detectCycles()) {
			return new HandleServicesResult(false, cycleDetector.findCycles(), null);
		}

		final var iterator = new TopologicalOrderIterator<>(graph);
		try {
			while (iterator.hasNext()) {
				final var meta = iterator.next();
				final var method = meta.method();
				method.invoke(meta.service(), injector);
			}
		} catch (Throwable throwable) {
			return new HandleServicesResult(false, Set.of(), throwable);
		}

		return new HandleServicesResult(true, Set.of(), null);
	}

	public record HandleServicesResult(boolean result, Set<LifeCycleMethod> cycles, @Nullable Throwable throwable) {}

	public Collection<ServiceMeta> services() {
		return metaByService.values();
	}

}
