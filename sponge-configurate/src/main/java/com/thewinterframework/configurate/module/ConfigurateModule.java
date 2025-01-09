package com.thewinterframework.configurate.module;

import com.google.inject.Binder;
import com.google.inject.Scopes;
import com.thewinterframework.configurate.serializer.ConfigurateSerializer;
import com.thewinterframework.configurate.serializer.ConfigurateSerializersRegistry;
import com.thewinterframework.configurate.serializer.processor.ConfigurateSerializerAnnotationProcessor;
import com.thewinterframework.plugin.WinterPlugin;
import com.thewinterframework.plugin.module.PluginModule;
import com.thewinterframework.utils.Reflections;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class ConfigurateModule implements PluginModule {

	private final ConfigurateSerializersRegistry configurateSerializersRegistry = new ConfigurateSerializersRegistry();
	private final List<Class<? extends TypeSerializer<?>>> discoveredSerializers = new ArrayList<>();

	@Override
	public void configure(Binder binder) {
		binder.bindScope(ConfigurateSerializer.class, Scopes.SINGLETON);
		binder.bind(ConfigurateSerializersRegistry.class).toInstance(configurateSerializersRegistry);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean onLoad(WinterPlugin plugin) {
		try {
			final var serializers = ConfigurateSerializerAnnotationProcessor.scan(plugin.getClass(), ConfigurateSerializer.class).getClassList();
			for (final var clazz : serializers) {
				final var serializer = (Class<? extends TypeSerializer<?>>) clazz;
				discoveredSerializers.add(serializer);
			}
		} catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
			plugin.getSLF4JLogger().error("Failed to scan for module components", e);
			throw new RuntimeException(e);
		}

		return true;
	}

	@Override
	public boolean onEnable(WinterPlugin plugin) {
		for (final var discoveredSerializer : discoveredSerializers) {
			final var serializer = plugin.getInjector().getInstance(discoveredSerializer);
			final var type = Reflections.getGenericType(discoveredSerializer, TypeSerializer.class, 0);
			configurateSerializersRegistry.registerSerializer(type, serializer);
		}

		return true;
	}
}
