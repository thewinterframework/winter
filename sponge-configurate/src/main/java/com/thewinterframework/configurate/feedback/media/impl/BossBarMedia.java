package com.thewinterframework.configurate.feedback.media.impl;

import com.thewinterframework.component.ComponentUtils;
import com.thewinterframework.configurate.feedback.media.FeedbackMedia;
import com.thewinterframework.configurate.feedback.media.MediaType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.Objects;

public record BossBarMedia(String message, float progress, Color color, Overlay overlay) implements FeedbackMedia {
    @Override
    public void sendMedia(Audience audience, TagResolver... resolvers) {
        final var name = ComponentUtils.miniMessage(message, resolvers);
        final var bossBar = BossBar.bossBar(name, progress, color, overlay);
        audience.showBossBar(bossBar);
    }

    @Override
    public MediaType type() {
        return MediaType.BOSSBAR;
    }

    public static class BossBarMediaSerializer implements TypeSerializer<FeedbackMedia> {
        @Override
        public FeedbackMedia deserialize(Type type, ConfigurationNode node) {
            return new BossBarMedia(
                Objects.requireNonNull(node.node("message").getString()),
                node.node("progress").getFloat(1.0f),
                Color.valueOf(node.node("color").getString(Color.PURPLE.name()).toUpperCase()),
                Overlay.valueOf(node.node("overlay").getString(Overlay.PROGRESS.name()).toUpperCase())
            );
        }

        @Override
        public void serialize(Type type, @Nullable FeedbackMedia obj, ConfigurationNode node) throws SerializationException {
            if (!(obj instanceof BossBarMedia media)) {
                throw new SerializationException("Invalid media type");
            }

            node.node("type").set(MediaType.BOSSBAR.name().toLowerCase());
            node.node("message").set(media.message());
            node.node("progress").set(media.progress());
            node.node("color").set(media.color().name().toLowerCase());
            node.node("overlay").set(media.overlay().name().toLowerCase());
        }
    }
}
