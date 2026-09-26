package com.thewinterframework.component.meta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/** Resolves deterministic component order from priorities and dependency edges. */
public final class ComponentOrder {

	private static final Comparator<ComponentDescriptor> FALLBACK_ORDER = Comparator
			.comparingInt(ComponentDescriptor::order)
			.thenComparing(descriptor -> descriptor.type().getName());

	private ComponentOrder() {
	}

	public static List<ComponentDescriptor> resolve(final Collection<ComponentDescriptor> components) {
		final var byType = new HashMap<Class<?>, ComponentDescriptor>();
		final var outgoing = new HashMap<ComponentDescriptor, Set<ComponentDescriptor>>();
		final var inDegree = new HashMap<ComponentDescriptor, Integer>();

		for (final var component : components) {
			byType.put(component.type(), component);
			outgoing.put(component, new HashSet<>());
			inDegree.put(component, 0);
		}

		for (final var component : components) {
			component.after().forEach(before -> addEdge(byType.get(before), component, outgoing, inDegree));
			component.before().forEach(after -> addEdge(component, byType.get(after), outgoing, inDegree));
		}

		final var ready = new PriorityQueue<>(FALLBACK_ORDER);
		inDegree.forEach((component, degree) -> {
			if (degree == 0) {
				ready.add(component);
			}
		});

		final var ordered = new ArrayList<ComponentDescriptor>(components.size());
		while (!ready.isEmpty()) {
			final var component = ready.remove();
			ordered.add(component);
			for (final var dependent : outgoing.get(component)) {
				final var remaining = inDegree.compute(dependent, (unused, degree) -> degree - 1);
				if (remaining == 0) {
					ready.add(dependent);
				}
			}
		}

		if (ordered.size() != components.size()) {
			final var cycle = inDegree.entrySet().stream()
					.filter(entry -> entry.getValue() > 0)
					.map(entry -> entry.getKey().type().getName())
					.sorted()
					.toList();
			throw new IllegalStateException("Component order contains a cycle: " + String.join(", ", cycle));
		}

		return List.copyOf(ordered);
	}

	private static void addEdge(
			final ComponentDescriptor source,
			final ComponentDescriptor target,
			final Map<ComponentDescriptor, Set<ComponentDescriptor>> outgoing,
			final Map<ComponentDescriptor, Integer> inDegree
	) {
		if (source == null || target == null || source.equals(target)) {
			return;
		}
		if (outgoing.get(source).add(target)) {
			inDegree.compute(target, (unused, degree) -> degree + 1);
		}
	}
}
