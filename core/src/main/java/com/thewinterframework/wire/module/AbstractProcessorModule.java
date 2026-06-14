package com.thewinterframework.wire.module;

import com.google.inject.Injector;
import com.thewinterframework.plugin.WinterPlugin;
import com.thewinterframework.plugin.module.Stage;
import com.thewinterframework.processor.clazz.ClassWireProcessor;
import com.thewinterframework.processor.module.ProcessorModule;
import com.thewinterframework.processor.wire.ClassListWire;
import com.thewinterframework.wire.condition.annotation.Requires;
import com.thewinterframework.wire.condition.context.ConditionContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractProcessorModule implements ProcessorModule {

	protected final Set<Class<?>> activeComponents = new HashSet<>();
	protected final Class<? extends Annotation> moduleAnnotation;
	protected ClassListWire wire;

	public AbstractProcessorModule(final Class<? extends Annotation> moduleAnnotation) {
		this.moduleAnnotation = moduleAnnotation;
	}

	protected void initWire(final WinterPlugin plugin) throws Exception {
		if (wire != null) {
			return;
		}

		final var wiredClass = Class.forName(ClassWireProcessor.canonicalWiredClassName(plugin, moduleAnnotation));
		this.wire = (ClassListWire) wiredClass.getConstructors()[0].newInstance();
	}

	protected boolean evaluateConditions(final AnnotatedElement componentClass, final Stage targetStage, final ConditionContext context, final Injector injector) throws Exception {
		for (final var annotation : componentClass.getAnnotations()) {
			final var conditional = annotation.annotationType().getAnnotation(Requires.class);
			if (conditional != null) {
				final var componentClazz = conditional.value();
				final var conditionEvaluator = targetStage == Stage.LOAD ?
						componentClazz.getDeclaredConstructor().newInstance() :
						injector.getInstance(componentClazz);

				if (conditionEvaluator.getStage() == targetStage) {
					if (!conditionEvaluator.matches(context, annotation)) {
						return false;
					}
				}
			}
		}
		return true;
	}

	protected void registerComponent(final WinterPlugin plugin, final Class<?> componentClass) throws Exception {
	}

	protected void enableComponent(final WinterPlugin plugin, final Class<?> componentClass) throws Exception {
	}

	protected void disableComponent(final WinterPlugin plugin, final Class<?> componentClass) throws Exception {
	}

	@Override
	public boolean onLoad(final WinterPlugin plugin) throws Exception {
		final var start = System.currentTimeMillis();
		initWire(plugin);

		for (final var componentClass : wire.getWiredClasses()) {
			final var loadContext = new ConditionContext(plugin, componentClass);

			if (evaluateConditions(componentClass, Stage.LOAD, loadContext, null)) {
				registerComponent(plugin, componentClass);
				activeComponents.add(componentClass);
			} else {
				plugin.getSLF4JLogger().debug("Skipped LOAD (Condition failed) for {}", componentClass.getSimpleName());
			}
		}

		plugin.getSLF4JLogger().debug("Loaded {} {} components in {}ms", activeComponents.size(), moduleAnnotation.getSimpleName(), System.currentTimeMillis() - start);
		return true;
	}

	@Override
	public boolean onEnable(final WinterPlugin plugin) throws Exception {
		final var start = System.currentTimeMillis();
		var enabledCount = 0;

		for (final var componentClass : activeComponents) {
			final var enableContext = new ConditionContext(plugin, componentClass);

			if (evaluateConditions(componentClass, Stage.ENABLE, enableContext, plugin.getInjector())) {
				enableComponent(plugin, componentClass);
				enabledCount++;
			} else {
				plugin.getSLF4JLogger().debug("Skipped ENABLE (Condition failed) for {}", componentClass.getSimpleName());
			}
		}

		plugin.getSLF4JLogger().debug("Enabled {} {} components in {}ms", enabledCount, moduleAnnotation.getSimpleName(), System.currentTimeMillis() - start);
		return true;
	}

	@Override
	public boolean onDisable(final WinterPlugin plugin) throws Exception {
		final var start = System.currentTimeMillis();
		var disabledCount = 0;

		for (final var componentClass : activeComponents) {
			disableComponent(plugin, componentClass);
			disabledCount++;
		}

		activeComponents.clear();
		plugin.getSLF4JLogger().debug("Disabled {} {} components in {}ms", disabledCount, moduleAnnotation.getSimpleName(), System.currentTimeMillis() - start);
		return true;
	}
}

