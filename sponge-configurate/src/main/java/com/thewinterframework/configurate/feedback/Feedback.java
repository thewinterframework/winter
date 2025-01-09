package com.thewinterframework.configurate.feedback;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public interface Feedback {
    void sendFeedback(Audience audience, TagResolver... resolver);
}
