package com.thewinterframework.wire.condition.common.plugin;

import com.thewinterframework.plugin.WinterPlugin;
import com.thewinterframework.plugin.module.Stage;
import com.thewinterframework.plugin.platform.PlatformPluginManager;
import com.thewinterframework.wire.condition.context.ConditionContext;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.*;

class RequiresPluginConditionTest {

	@Test
	void acceptsAnInstalledDependencyBeforeItIsEnabled() {
		assertTrue(matches(true, false));
	}

	@Test
	void rejectsAnAbsentDependency() {
		assertFalse(matches(false, false));
	}

	@Test
	void retainsCompatibilityWithExistingPlatformImplementations() {
		final PlatformPluginManager platform = name -> name.equals("Dependency");
		assertTrue(platform.isPluginPresent("Dependency"));
		assertFalse(platform.isPluginPresent("Missing"));
	}

	private boolean matches(final boolean present, final boolean enabled) {
		final var platform = new PlatformPluginManager() {
			@Override
			public boolean isPluginPresent(final String name) {
				assertEquals("Dependency", name);
				return present;
			}

			@Override
			public boolean isPluginEnabled(final String name) {
				return enabled;
			}
		};
		final var plugin = (WinterPlugin) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{WinterPlugin.class},
				(instance, method, args) -> {
					assertEquals("getPlatformManager", method.getName());
					return platform;
				});
		final var condition = new RequiresPluginCondition();
		assertEquals(Stage.LOAD, condition.getStage());
		return condition.matches(new ConditionContext(plugin, Integration.class), Integration.class.getAnnotation(RequiresPlugin.class));
	}

	@RequiresPlugin("Dependency")
	private static final class Integration {
	}
}
