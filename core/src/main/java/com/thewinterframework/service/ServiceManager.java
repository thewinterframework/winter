package com.thewinterframework.service;

import com.google.inject.Injector;
import com.thewinterframework.plugin.WinterPlugin;
import com.thewinterframework.service.annotation.lifecycle.OnDisable;
import com.thewinterframework.service.annotation.lifecycle.OnEnable;
import com.thewinterframework.service.annotation.lifecycle.OnReload;
import com.thewinterframework.service.annotation.scheduler.RepeatingTask;
import com.thewinterframework.service.annotation.scheduler.ScheduledAt;
import com.thewinterframework.service.annotation.scheduler.ScheduledAtContainer;
import com.thewinterframework.service.meta.ServiceMeta;
import com.thewinterframework.service.meta.lifecycle.LifeCycleMethod;
import com.thewinterframework.service.meta.lifecycle.ReflectLifeCycleMethod;
import com.thewinterframework.service.meta.lifecycle.RunnableLifeCycleMethod;
import com.thewinterframework.service.meta.scheduler.RepeatingTaskMethod;
import com.thewinterframework.service.meta.scheduler.ScheduledAtMethod;
import com.thewinterframework.service.meta.scheduler.SchedulerMethod;
import com.thewinterframework.utils.Reflections;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.DepthFirstIterator;

import java.util.*;

/**
 * <p> This class is responsible for managing services. </p>
 */
public class ServiceManager {

	private final Map<Class<?>, ServiceMeta> metaByService = new HashMap<>();

	private final Graph<LifeCycleMethod, DefaultEdge> onEnableGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
	private final Graph<LifeCycleMethod, DefaultEdge> onDisableGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
	private final Graph<LifeCycleMethod, DefaultEdge> onReloadGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
	private final List<Integer> schedulersIds = new ArrayList<>();

	/**
	 * Register a service with the service manager.
	 *
	 * @param service The service to register.
	 */
	//TODO: This method is too long, consider refactoring
	public void registerService(final @NotNull Class<?> service) throws IllegalAccessException, NoSuchMethodException {
		final var onEnableMethods = Reflections.findMethodsWith(service, OnEnable.class)
				.stream()
				.map(annotatedMethodHandle -> new ReflectLifeCycleMethod(service, annotatedMethodHandle, new Class[]{}, annotatedMethodHandle.annotation().after()))
				.toList();

		final var onDisableMethods = Reflections.findMethodsWith(service, OnDisable.class)
				.stream()
				.map(annotatedMethodHandle -> new ReflectLifeCycleMethod(service, annotatedMethodHandle, annotatedMethodHandle.annotation().before(), new Class[]{}))
				.toList();

		final var onReloadMethods = Reflections.findMethodsWith(service, OnReload.class)
				.stream()
				.map(annotatedMethodHandle -> new ReflectLifeCycleMethod(service, annotatedMethodHandle, annotatedMethodHandle.annotation().before(), annotatedMethodHandle.annotation().after()))
				.toList();

		final var schedulerMethods = new ArrayList<SchedulerMethod>();
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

		schedulerMethods.addAll(repeatingTaskMethods);

		final var scheduledAtMethods = Reflections.findMethodsWith(service, ScheduledAtContainer.class)
				.stream()
				.map(annotatedMethodHandle -> {
					final var scheduledAtContainer = annotatedMethodHandle.annotation();
					final var schedules = Arrays.stream(scheduledAtContainer.value())
							.map(scheduledAt -> new ScheduledAtMethod.ScheduledAtTime(scheduledAt.hour(), scheduledAt.minute(), scheduledAt.second()))
							.toList();

					return new ScheduledAtMethod(service, annotatedMethodHandle, schedules, Arrays.stream(scheduledAtContainer.value()).anyMatch(ScheduledAt::async));
				})
				.toList();

		schedulerMethods.addAll(scheduledAtMethods);

		final var serviceMeta = new ServiceMeta(service, onEnableMethods, onDisableMethods, onReloadMethods, schedulerMethods);
		metaByService.put(service, serviceMeta);
	}

	/**
	 * Add a runnable to the on reload graph.
	 *
	 * @param service The service to add the runnable to.
	 * @param method  The method to add.
	 */
	public void addReloadMethod(final @NotNull Class<?> service, final @NotNull Runnable method) {
		onReloadGraph.addVertex(new RunnableLifeCycleMethod(service, method, new Class[]{}, new Class[]{}));
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

			for (final var onReloadMethod : serviceMeta.onReloadMethods()) {
				onReloadGraph.addVertex(onReloadMethod);

				for (final var dependency : onReloadMethod.before()) {
					metaByService.get(dependency)
							.onReloadMethods()
							.forEach(dependencyMethod -> {
								onReloadGraph.addVertex(dependencyMethod); // if the dependency is not registered, it will be added to the graph
								onReloadGraph.addEdge(dependencyMethod, onReloadMethod);
							});
				}

				for (final var dependency : onReloadMethod.after()) {
					metaByService.get(dependency)
							.onReloadMethods()
							.forEach(dependencyMethod -> {
								onReloadGraph.addVertex(dependencyMethod); // if the dependency is not registered, it will be added to the graph
								onReloadGraph.addEdge(onReloadMethod, dependencyMethod);
							});
				}
			}
		}
	}

	/**
	 * Reload the services.
	 *
	 * @param injector The injector to use.
	 */
	public HandleServicesResult reloadServices(Injector injector) {
		return handleServices(injector, onReloadGraph);
	}

	/**
	 * Enable the services.
	 *
	 * @param injector The injector to use.
	 * @return The result of the operation.
	 */
	public HandleServicesResult enableServices(Injector injector) {
		return handleServices(injector, onEnableGraph);
	}

	/**
	 * Start the schedulers.
	 *
	 * @param plugin The plugin to use.
	 */
	public void startSchedulers(WinterPlugin plugin) {
		for (final var serviceMeta : metaByService.values()) {
			for (final var schedulerMethod : serviceMeta.schedulerMethods()) {
				schedulersIds.add(schedulerMethod.schedule(plugin));
			}
		}
	}

	/**
	 * Stop the tasks.
	 *
	 * @param plugin The plugin to use.
	 */
	public void stopTasks(WinterPlugin plugin) {
		for (final var schedulerId : schedulersIds) {
			plugin.cancelTask(schedulerId);
		}
	}

	/**
	 * Disable the services.
	 *
	 * @param injector The injector to use.
	 * @return The result of the operation.
	 */
	public HandleServicesResult disableServices(Injector injector) {
		return handleServices(injector, onDisableGraph);
	}

	/**
	 * Handle the services.
	 *
	 * @param injector The injector to use.
	 * @param graph    The graph to use.
	 * @return The result of the operation.
	 */
	private static HandleServicesResult handleServices(final Injector injector, final Graph<LifeCycleMethod, DefaultEdge> graph) {
		final var cycleDetector = new CycleDetector<>(graph);
		if (cycleDetector.detectCycles()) {
			return new HandleServicesResult(false, cycleDetector.findCycles(), null);
		}

		final var iterator = new DepthFirstIterator<>(graph);
		try {
			while (iterator.hasNext()) {
				final var meta = iterator.next();
				if (meta instanceof ReflectLifeCycleMethod reflectLifeCycleMethod) {
					final var method = reflectLifeCycleMethod.method();
					method.invoke(reflectLifeCycleMethod.service(), injector);
				} else if (meta instanceof RunnableLifeCycleMethod runnableLifeCycleMethod) {
					runnableLifeCycleMethod.method().run();
				}
			}
		} catch (Throwable throwable) {
			return new HandleServicesResult(false, Set.of(), throwable);
		}

		return new HandleServicesResult(true, Set.of(), null);
	}

	/**
	 * The result of handling services.
	 */
	public record HandleServicesResult(boolean result, Set<LifeCycleMethod> cycles, @Nullable Throwable throwable) {}

	/**
	 * Get the services.
	 *
	 * @return The services.
	 */
	public Collection<ServiceMeta> services() {
		return metaByService.values();
	}

}
