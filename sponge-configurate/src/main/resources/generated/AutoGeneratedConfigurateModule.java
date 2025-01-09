package <PACKAGE_NAME>;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.AbstractModule;
import com.thewinterframework.configurate.Container;
import com.thewinterframework.configurate.serializer.ConfigurateSerializersRegistry;
import com.thewinterframework.plugin.DataFolder;
import com.thewinterframework.plugin.module.PluginModule;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;

public class ConfigurationsModule extends AbstractModule implements PluginModule {
	<el>
	@Provides
	@Singleton
	public Container<<CONFIG_OBJECT>> provide<CONFIG_NAME>Container(Logger logger, @DataFolder Path dataFolder, ConfigurateSerializersRegistry registry) throws IOException {
		return Container.load(logger, dataFolder, <CONFIG_OBJECT>.class, "<CONFIG_NAME>.yml", configurationOptions -> configurationOptions.serializers(builder -> builder.registerAll(registry.getSerializers())));
	}
	</el>
}