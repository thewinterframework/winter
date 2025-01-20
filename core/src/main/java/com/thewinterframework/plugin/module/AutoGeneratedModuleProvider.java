package com.thewinterframework.plugin.module;

import com.google.common.reflect.ClassPath;
import com.thewinterframework.plugin.WinterPlugin;
import com.thewinterframework.processor.AbstractWinterAnnotationProcessor;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * This class should be extended by any module that is auto-generated by the WinterBoot plugin.
 */
public interface AutoGeneratedModuleProvider {

	/**
	 * Scans the auto-generated modules for the given plugin.
	 *
	 * @param plugin the plugin to scan for auto-generated modules.
	 * @return a list of auto-generated modules for the given plugin.
	 */
	@SuppressWarnings("unchecked")
	static List<AutoGeneratedModuleProvider> scan(Class<? extends WinterPlugin> plugin) throws IOException {
		final var modulesPackage = AbstractWinterAnnotationProcessor.getAutoGeneratedModulesPackage(plugin);

		return ClassPath.from(plugin.getClassLoader()).getTopLevelClasses(modulesPackage).stream()
				.map(ClassPath.ClassInfo::load)
				.filter(AutoGeneratedModuleProvider.class::isAssignableFrom)
				.map(clazz -> (Class<AutoGeneratedModuleProvider>) clazz)
				.map(AutoGeneratedModuleProvider::createInstance)
				.toList();
	}

	/**
	 * Creates an instance of the given class.
	 *
	 * @param clazz the class to create an instance of.
	 * @param <T>   the type of the class.
	 * @return an instance of the given class.
	 */
	static <T> T createInstance(Class<T> clazz) {
		try {
			return clazz.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return the class of the module that is auto-generated by the WinterBoot plugin, and should be used by the WinterBoot plugin.
	 */
	Class<? extends PluginModule> getModuleClass();
}
