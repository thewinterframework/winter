package com.thewinterframework.configurate.feedback.media.impl;

import com.thewinterframework.configurate.feedback.media.FeedbackMedia;
import com.thewinterframework.configurate.feedback.media.MediaType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.Sound.Emitter;
import net.kyori.adventure.sound.Sound.Source;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.Objects;

public record SoundMedia(Sound sound) implements FeedbackMedia {
    @Override
    public void sendMedia(Audience audience, TagResolver... resolvers) {
        audience.playSound(sound, Emitter.self());
    }

    @Override
    public MediaType type() {
        return MediaType.SOUND;
    }

    public static class SoundMediaSerializer implements TypeSerializer<FeedbackMedia> {
        @Override @SuppressWarnings("all")
        public FeedbackMedia deserialize(Type type, ConfigurationNode node) throws SerializationException {;
            return new SoundMedia(
                    Sound.sound(
                            Key.key(Objects.requireNonNull(node.node("name").getString())),
                            Source.valueOf(node.node("source").getString(Source.MASTER.toString()).toUpperCase()),
                            node.node("volume").getFloat(1.0f),
                            node.node("pitch").getFloat(1.0f)
                    )
            );
        }

        @Override
        public void serialize(Type type, @Nullable FeedbackMedia obj, ConfigurationNode node) throws SerializationException {
            if (!(obj instanceof SoundMedia media)) {
                throw new SerializationException("Invalid media type");
            }

            node.node("type").set(MediaType.SOUND.name().toLowerCase());
            node.node("name").set(media.sound().name().asString());
            node.node("source").set(media.sound().source().name().toLowerCase());
            node.node("volume").set(media.sound().volume());
            node.node("pitch").set(media.sound().pitch());
        }
    }
}
