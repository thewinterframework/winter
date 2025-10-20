package com.thewinterframework.utils.graph;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A simple directed graph implementation using Depth-First Search (DFS) for topological sorting.
 * This class allows adding nodes and dependencies, and provides an ordered list of nodes
 * based on their dependencies.
 *
 * @param <T> the type of nodes in the graph
 */
public class DfsGraph<T> implements Iterable<T> {

	/** Map to store the dependencies of each node */
	private final Map<T, Set<T>> dependencies = new HashMap<>();

	/** Adds a node to the graph
	 * @param node the node to be added
	 */
	public void addNode(final T node) {
		dependencies.computeIfAbsent(node, k -> new HashSet<>());
	}

	/**
	 * Adds a dependency indicating that 'after' should come after 'before'
	 * @param after - the node that comes after
	 * @param before - the node that comes before
	 */
	public void addAfter(final T after, final T before) {
		addNode(after);
		addNode(before);
		dependencies.get(after).add(before);
	}

	/**
	 * Adds a dependency indicating that 'before' should come before 'after'
	 * @param before - the node that comes before
	 * @param after - the node that comes after
	 */
	public void addBefore(final T before, final T after) {
		addAfter(after, before);
	}

	/**
	 * Returns a list of nodes ordered based on their dependencies using DFS.
	 * @return a list of nodes in topological order
	 * @throws IllegalStateException if a cycle is detected in the graph
	 */
	public List<T> ordered() {
		final var order = new ArrayList<T>();
		final var visited = new HashSet<T>();
		final var visiting = new HashSet<T>();
		final var path = new ArrayDeque<T>();

		for (final var node : dependencies.keySet()) {
			if (!visited.contains(node)) {
				if (!dfs(node, visited, visiting, order, path)) {
					throw new IllegalStateException("Cycle detected: " + extractCycle(path, node));
				}
			}
		}

		Collections.reverse(order);
		return order;
	}

	/**
	 * Depth-First Search helper method for topological sorting.
	 * @param node the current node being visited
	 * @param visited set of already visited nodes
	 * @param visiting set of nodes currently being visited (to detect cycles)
	 * @param order list to store the topological order
	 * @param path current recursion path
	 * @return true if successful, false if a cycle is detected
	 */
	private boolean dfs(
			final T node,
			final Set<T> visited,
			final Set<T> visiting,
			final List<T> order,
			final Deque<T> path
	) {
		visiting.add(node);
		path.push(node);

		for (final var dep : dependencies.getOrDefault(node, Set.of())) {
			if (visiting.contains(dep)) {
				path.push(dep);
				return false; // cycle found
			}

			if (!visited.contains(dep) && !dfs(dep, visited, visiting, order, path)) {
				return false;
			}
		}

		path.pop();
		visiting.remove(node);
		visited.add(node);
		order.add(node);
		return true;
	}

	/**
	 * Builds a readable representation of the detected cycle from the path stack.
	 */
	private String extractCycle(final Deque<T> path, final T start) {
		final List<T> cycle = new ArrayList<>();
		final Iterator<T> it = path.iterator();

		boolean inCycle = false;
		while (it.hasNext()) {
			T node = it.next();
			if (Objects.equals(node, start)) {
				inCycle = true;
			}
			if (inCycle) {
				cycle.add(node);
			}
		}
		cycle.add(start);

		Collections.reverse(cycle);
		return String.join(" -> ", cycle.stream().map(String::valueOf).toList());
	}

	/**
	 * Returns an iterator over the nodes in topological order.
	 * @return an iterator of nodes
	 */
	@Override
	public @NotNull Iterator<T> iterator() {
		return ordered().iterator();
	}
}
