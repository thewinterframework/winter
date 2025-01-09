package com.thewinterframework.command;

import revxrsal.commands.Lamp;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

public class LampInstance implements LampProvider {

	private Lamp<BukkitCommandActor> lamp;

	@Override
	public Lamp<BukkitCommandActor> getLamp() {
		return lamp;
	}

	public void setLamp(Lamp<BukkitCommandActor> lamp) {
		this.lamp = lamp;
	}
}
