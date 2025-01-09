package com.thewinterframework.configurate.serializer;

import org.spongepowered.configurate.serialize.TypeSerializer;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class ConfigurateSerializersRegistry {

	private final Map<Type, TypeSerializer<?>> serializers = new HashMap<>();

	public void registerSerializer(Type clazz, TypeSerializer<?> serializer) {
		serializers.put(clazz, serializer);
	}

	@SuppressWarnings("unchecked")
	public TypeSerializerCollection getSerializers() {
		final var collection = TypeSerializerCollection.defaults().childBuilder();
		for (final var entry : serializers.entrySet()) {
			collection.register((Class<Object>) entry.getKey(), (TypeSerializer<Object>) entry.getValue());
		}

		return collection.build();
	}

}
