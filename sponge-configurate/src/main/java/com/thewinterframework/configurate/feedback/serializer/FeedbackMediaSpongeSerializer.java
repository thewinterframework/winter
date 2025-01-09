package com.thewinterframework.configurate.feedback.serializer;

import com.thewinterframework.configurate.feedback.media.FeedbackMedia;
import com.thewinterframework.configurate.feedback.media.MediaType;
import com.thewinterframework.configurate.feedback.media.impl.*;
import com.thewinterframework.configurate.serializer.ConfigurateSerializer;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.EnumMap;

@ConfigurateSerializer
public class FeedbackMediaSpongeSerializer implements TypeSerializer<FeedbackMedia> {

    private final static EnumMap<MediaType, TypeSerializer<FeedbackMedia>> CHILD_SERIALIZERS = new EnumMap<>(MediaType.class);

    static  {
        CHILD_SERIALIZERS.put(MediaType.CHAT, new ChatMedia.ChatMediaSerializer());
        CHILD_SERIALIZERS.put(MediaType.BOSSBAR, new BossBarMedia.BossBarMediaSerializer());
        CHILD_SERIALIZERS.put(MediaType.ACTIONBAR, new ActionBarMedia.ActionBarMediaSerializer());
        CHILD_SERIALIZERS.put(MediaType.TITLE, new TitleMedia.TitleMediaSerializer());
        CHILD_SERIALIZERS.put(MediaType.SOUND, new SoundMedia.SoundMediaSerializer());
    }

    @Override
    public FeedbackMedia deserialize(Type type, ConfigurationNode node) throws SerializationException {
        final var typeNode = node.node("type");
        if (typeNode.empty() || typeNode.virtual()) {
            if (node.isList()) {
                return new ChatMedia(node.getList(String.class));
            }

            return new ChatMedia(node.getString(node.path().toString()));
        }

        final var mediaType = MediaType.valueOf(typeNode.getString(MediaType.CHAT.toString()).toUpperCase());
        return CHILD_SERIALIZERS.get(mediaType).deserialize(type, node);
    }

    @Override
    public void serialize(Type type, @Nullable FeedbackMedia obj, ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            return;
        }

        final var mediaType = obj.type();
        final var parent = node.parent();
        if (obj instanceof ChatMedia media && (parent == null || !parent.isList())) { // simple format - node: "message"
            if (media.messages().size() == 1) {
                node.set(media.messages().get(0));
                return;
            }

            node.setList(String.class, media.messages());
            return;
        }

        CHILD_SERIALIZERS.get(mediaType).serialize(type, obj, node);
    }
}
