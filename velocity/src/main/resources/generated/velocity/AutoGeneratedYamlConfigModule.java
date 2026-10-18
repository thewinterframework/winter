// This file is generated automatically by thewinterframework.
package <PACKAGE_NAME>;

import com.google.inject.Binder;
import com.thewinterframework.plugin.WinterPlugin;
import com.thewinterframework.processor.module.ProcessorModule;
import com.thewinterframework.velocity.VelocityWinterPlugin;
import com.thewinterframework.velocity.yaml.FileNameImpl;
import com.thewinterframework.velocity.yaml.YamlConfig;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VelocityYamlConfigModule implements ProcessorModule {

	private final Map<String, YamlConfig> configurations = new LinkedHashMap<>();

	@Override
	public boolean onLoad(final WinterPlugin plugin) {
		for (final var fileName : List.of(<FILE_NAMES>)) {
			configurations.put(fileName, YamlConfig.configuration((VelocityWinterPlugin) plugin, fileName));
		}
		return true;
	}

	@Override
	public void configure(final Binder binder) {
		configurations.forEach((fileName, config) ->
				binder.bind(YamlConfig.class).annotatedWith(new FileNameImpl(fileName)).toInstance(config));
	}
}
