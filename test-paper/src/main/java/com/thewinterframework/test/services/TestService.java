package com.thewinterframework.test.services;

import com.google.inject.Inject;
import com.thewinterframework.configurate.Container;
import com.thewinterframework.paper.yaml.FileName;
import com.thewinterframework.paper.yaml.YamlConfig;
import com.thewinterframework.service.annotation.Service;
import com.thewinterframework.service.annotation.lifecycle.OnDisable;
import com.thewinterframework.service.annotation.lifecycle.OnEnable;
import com.thewinterframework.service.annotation.scheduler.RepeatingTask;
import com.thewinterframework.test.config.ConfigTest;
import com.thewinterframework.utils.TimeUnit;
import org.slf4j.Logger;

@Service
public class TestService {

	private final MyOtherService myOtherService;

	@Inject
	public TestService(MyOtherService myOtherService) {
		this.myOtherService = myOtherService;
	}

	@OnEnable(after = MyOtherService.class)
	void onEnable(Logger logger) {
		logger.info("TestService enabled!");
	}

	@OnDisable
	void onDisable(Logger logger) {
		logger.info("TestService disabled!");
	}

	@RepeatingTask(every = 25, unit = TimeUnit.SECONDS)
	void sayHi(Logger logger, @FileName("config.yml") YamlConfig config, Container<ConfigTest> testConfig) {
		logger.info("Hi!");
		logger.info(config.getString("test"));
		logger.info(testConfig.get().test());
	}
}
