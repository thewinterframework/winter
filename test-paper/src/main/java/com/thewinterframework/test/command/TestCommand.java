package com.thewinterframework.test.command;

import com.google.inject.Inject;
import com.thewinterframework.command.CommandComponent;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.parameters.EntitySelector;

@CommandComponent
public class TestCommand {

	private final Logger service;

	@Inject
	public TestCommand(Logger service) {
		this.service = service;
	}

	@Command("test")
	public void test(BukkitCommandActor actor) {
		service.info("Test command executed by {}", actor.name());
	}

	@Command("greet")
	public void greet(BukkitCommandActor actor) {
		actor.reply("Hello, " + actor.name() + "!");
	}

	@Command({"testtp", "ttp"})
	public void teleport(Player sender, EntitySelector<LivingEntity> target, double x, double y, double z) {
		Location location = new Location(sender.getWorld(), x, y, z);
		for (LivingEntity entity : target)
			entity.teleport(location);
	}

}
