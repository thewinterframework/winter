package com.thewinterframework.test.config;


import com.thewinterframework.configurate.config.Configurate;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@Configurate("test")
@ConfigSerializable
public record ConfigTest(
		String test
) {
}
