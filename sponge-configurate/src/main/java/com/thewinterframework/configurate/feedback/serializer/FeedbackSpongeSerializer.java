package com.thewinterframework.configurate.feedback.serializer;

import com.thewinterframework.configurate.feedback.Feedback;
import com.thewinterframework.configurate.feedback.FeedbackImpl;
import com.thewinterframework.configurate.feedback.FeedbackMediaContainer;
import com.thewinterframework.configurate.feedback.media.FeedbackMedia;
import com.thewinterframework.configurate.serializer.ConfigurateSerializer;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

@ConfigurateSerializer
public class FeedbackSpongeSerializer implements TypeSerializer<Feedback> {

    @Override
    public Feedback deserialize(Type type, ConfigurationNode node) throws SerializationException {
        return new FeedbackImpl(node.getList(FeedbackMedia.class));
    }

    @Override
    public void serialize(Type type, Feedback obj, ConfigurationNode node) throws SerializationException {
        if (obj instanceof FeedbackMediaContainer container) {
            node.setList(FeedbackMedia.class, container.medias());
        } else {
            throw new SerializationException("Invalid feedback type");
        }
    }
}
