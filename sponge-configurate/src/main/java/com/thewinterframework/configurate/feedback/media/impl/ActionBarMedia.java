package com.thewinterframework.configurate.feedback.media.impl;

import com.thewinterframework.component.ComponentUtils;
import com.thewinterframework.configurate.feedback.media.FeedbackMedia;
import com.thewinterframework.configurate.feedback.media.MediaType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.Objects;

public record ActionBarMedia(String message) implements FeedbackMedia {

    @Override
    public void sendMedia(final Audience audience, final TagResolver... resolvers) {
        audience.sendActionBar(ComponentUtils.miniMessage(message, resolvers));
    }

    @Override
    public MediaType type() {
        return MediaType.ACTIONBAR;
    }

    public static class ActionBarMediaSerializer implements TypeSerializer<FeedbackMedia> {
        @Override
        public FeedbackMedia deserialize(Type type, ConfigurationNode node) {
            return new ActionBarMedia(Objects.requireNonNull(node.node("message").getString()));
        }

        @Override
        public void serialize(Type type, @Nullable FeedbackMedia obj, ConfigurationNode node) throws SerializationException {
            if (!(obj instanceof ActionBarMedia media)) {
                throw new SerializationException("Invalid media type");
            }

            node.node("type").set(MediaType.ACTIONBAR.name().toLowerCase());
            node.node("message").set(media.message());
        }
    }

}
