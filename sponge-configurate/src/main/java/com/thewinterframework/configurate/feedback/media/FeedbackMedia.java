package com.thewinterframework.configurate.feedback.media;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public interface FeedbackMedia {

    void sendMedia(final Audience audience, final TagResolver... resolvers);

    MediaType type();

}
