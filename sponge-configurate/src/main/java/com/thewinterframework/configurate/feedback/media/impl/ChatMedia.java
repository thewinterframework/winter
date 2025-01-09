package com.thewinterframework.configurate.feedback.media.impl;

import com.thewinterframework.component.ComponentUtils;
import com.thewinterframework.configurate.feedback.media.FeedbackMedia;
import com.thewinterframework.configurate.feedback.media.MediaType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.List;

public record ChatMedia(List<String> messages) implements FeedbackMedia {
    public ChatMedia(String message) {
        this(List.of(message));
    }

    @Override
    public void sendMedia(final Audience audience, final TagResolver... resolvers) {
        messages.forEach(message -> audience.sendMessage(ComponentUtils.miniMessage(message, resolvers)));
    }

    @Override
    public MediaType type() {
        return MediaType.CHAT;
    }

    public static class ChatMediaSerializer implements TypeSerializer<FeedbackMedia> {
        @Override
        public FeedbackMedia deserialize(@NotNull Type type, ConfigurationNode node) throws SerializationException {
            final var msgNode = node.node("message");
            if (msgNode.isList()) {
                return new ChatMedia(msgNode.getList(String.class));
            }

            return new ChatMedia(msgNode.getString(msgNode.path().toString()));
        }

        @Override
        public void serialize(Type type, @Nullable FeedbackMedia obj, ConfigurationNode node) throws SerializationException {
            if (!(obj instanceof ChatMedia media)) {
                throw new SerializationException("Invalid media type");
            }

            node.node("type").set(MediaType.CHAT.name().toLowerCase());

            if (media.messages().size() == 1) {
                node.node("message").set(media.messages().get(0));
                return;
            }

            node.node("message").setList(String.class, media.messages());
        }
    }
}
