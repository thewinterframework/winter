package com.thewinterframework.test.listener;

import com.google.inject.Inject;
import com.thewinterframework.configurate.Container;
import com.thewinterframework.paper.listener.ListenerComponent;
import com.thewinterframework.paper.yaml.FileName;
import com.thewinterframework.paper.yaml.YamlConfig;
import com.thewinterframework.test.config.ConfigTest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@ListenerComponent
public class TestListener implements Listener {

	@Inject
	@FileName("config.yml")
	private YamlConfig myConfig;

	@Inject
	private Container<ConfigTest> configTestContainer;

	@EventHandler
	void onEnable(PlayerJoinEvent event) {
		System.out.println(myConfig.getString("test"));
		System.out.println(configTestContainer.get().test());
	}

}
