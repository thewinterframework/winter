package com.thewinterframework.configurate.serializer.common;

import com.thewinterframework.component.ComponentUtils;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class ComponentSerializer implements TypeSerializer<Component> {
	@Override
	public Component deserialize(@NotNull Type type, ConfigurationNode node) {
		final var stringValue = node.getString();
		if (stringValue == null) {
			return null;
		}

		return ComponentUtils.miniMessage(stringValue);
	}

	@Override
	public void serialize(@NotNull Type type, @Nullable Component obj, @NotNull ConfigurationNode node) throws SerializationException {
		if (obj == null) {
			node.set(null);
			return;
		}

		node.set(String.class, ComponentUtils.miniMessage(obj));
	}
}
