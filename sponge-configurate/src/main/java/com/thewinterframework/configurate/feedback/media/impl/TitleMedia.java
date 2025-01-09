package com.thewinterframework.configurate.feedback.media.impl;

import com.thewinterframework.component.ComponentUtils;
import com.thewinterframework.configurate.feedback.media.FeedbackMedia;
import com.thewinterframework.configurate.feedback.media.MediaType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.Objects;

public record TitleMedia(String title, @Nullable String subtitle, Duration fadeIn, Duration stay, Duration fadeOut) implements FeedbackMedia {

    @Override
    public void sendMedia(final Audience audience, final TagResolver... resolvers) {
        final var titleComponent = ComponentUtils.miniMessage(title, resolvers);
        final var subtitleComponent = subtitle != null ? ComponentUtils.miniMessage(subtitle, resolvers) : Component.text("");

        final var title = Title.title(titleComponent, subtitleComponent, Times.times(fadeIn, stay, fadeOut));
        audience.showTitle(title);
    }

    @Override
    public MediaType type() {
        return MediaType.TITLE;
    }

    public static class TitleMediaSerializer implements TypeSerializer<FeedbackMedia> {

        @Override
        public FeedbackMedia deserialize(Type type, ConfigurationNode node) {
            return new TitleMedia(
                    Objects.requireNonNull(node.node("title").getString()),
                    node.node("subtitle").getString(),
                    Duration.ofSeconds(node.node("fade-in").getLong(1)),
                    Duration.ofSeconds(node.node("stay").getLong(1)),
                    Duration.ofSeconds(node.node("fade-out").getLong(1))
            );
        }

        @Override
        public void serialize(Type type, @Nullable FeedbackMedia obj, ConfigurationNode node) throws SerializationException {
            if (!(obj instanceof TitleMedia media)) {
                throw new SerializationException("Invalid media type");
            }

            node.node("type").set(MediaType.TITLE.name().toLowerCase());
            node.node("title").set(media.title());
            node.node("subtitle").set(media.subtitle());
            node.node("fade-in").set(media.fadeIn().getSeconds());
            node.node("stay").set(media.stay().getSeconds());
            node.node("fade-out").set(media.fadeOut().getSeconds());
        }
    }
}
