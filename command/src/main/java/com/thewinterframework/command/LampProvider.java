package com.thewinterframework.command;

import revxrsal.commands.Lamp;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

public interface LampProvider {
	Lamp<BukkitCommandActor> getLamp();
}
