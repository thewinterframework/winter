package com.thewinterframework.utils.graph;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Directed dependency graph with deterministic topological ordering.
 *
 * @param <T> node type
 */
public class DfsGraph<T> implements Iterable<T> {

	private final Map<T, Set<T>> dependencies = new HashMap<>();

	public void addNode(final T node) {
		dependencies.computeIfAbsent(node, unused -> new HashSet<>());
	}

	/** Declares that {@code after} depends on {@code before}. */
	public void addAfter(final T after, final T before) {
		addNode(after);
		addNode(before);
		dependencies.get(after).add(before);
	}

	/** Declares that {@code before} must precede {@code after}. */
	public void addBefore(final T before, final T after) {
		addAfter(after, before);
	}

	public List<T> ordered() {
		return ordered(Comparator.comparing(String::valueOf));
	}

	/** Returns a deterministic topological order using the comparator for ready nodes. */
	public List<T> ordered(final Comparator<? super T> comparator) {
		final var inDegree = new HashMap<T, Integer>();
		final var dependents = new HashMap<T, Set<T>>();
		for (final var entry : dependencies.entrySet()) {
			inDegree.put(entry.getKey(), entry.getValue().size());
			dependents.computeIfAbsent(entry.getKey(), unused -> new HashSet<>());
			for (final var dependency : entry.getValue()) {
				dependents.computeIfAbsent(dependency, unused -> new HashSet<>()).add(entry.getKey());
			}
		}

		final var ready = new PriorityQueue<T>(comparator);
		inDegree.forEach((node, degree) -> {
			if (degree == 0) {
				ready.add(node);
			}
		});

		final var order = new ArrayList<T>(dependencies.size());
		while (!ready.isEmpty()) {
			final var node = ready.remove();
			order.add(node);
			for (final var dependent : dependents.getOrDefault(node, Set.of())) {
				final var remaining = inDegree.compute(dependent, (unused, degree) -> degree - 1);
				if (remaining == 0) {
					ready.add(dependent);
				}
			}
		}

		if (order.size() != dependencies.size()) {
			final var cycle = inDegree.entrySet().stream()
					.filter(entry -> entry.getValue() > 0)
					.map(entry -> String.valueOf(entry.getKey()))
					.sorted()
					.toList();
			throw new IllegalStateException("Cycle detected: " + String.join(" -> ", cycle));
		}
		return List.copyOf(order);
	}

	public Set<T> dependenciesOf(final T node) {
		return Set.copyOf(dependencies.getOrDefault(node, Set.of()));
	}

	@Override
	public @NotNull Iterator<T> iterator() {
		return ordered().iterator();
	}
}
