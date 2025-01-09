package com.thewinterframework.test.services;

import com.google.inject.Inject;
import com.thewinterframework.paper.yaml.FileName;
import com.thewinterframework.paper.yaml.YamlConfig;
import com.thewinterframework.service.annotation.Service;
import com.thewinterframework.service.annotation.lifecycle.OnDisable;
import com.thewinterframework.service.annotation.lifecycle.OnEnable;
import org.slf4j.Logger;

@Service
public class MyOtherService {

	@FileName("config.yml")
	@Inject private YamlConfig myConfig;

	@FileName(value = "messages.yml")
	@Inject private YamlConfig messages;

	@OnEnable
	void onEnable(Logger logger) {
		logger.info(myConfig.getString("test"));
		logger.info(messages.getString("test2"));
	}

	@OnDisable(before = TestService.class)
	void onDisable(Logger logger) {
		logger.info("MyOtherService disabled!");
	}
}
