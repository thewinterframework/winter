// This file is generated automatically by thewinterframework. Do not modify this file manually.
package <PACKAGE_NAME>;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.thewinterframework.paper.yaml.FileName;
import com.thewinterframework.paper.yaml.YamlConfig;
import com.thewinterframework.plugin.module.PluginModule;
import org.bukkit.plugin.Plugin;

public class <CLASS_NAME> extends AbstractModule implements PluginModule {

	<el>
	@Provides
	@Singleton
	@FileName("<FILE_NAME>")
	public YamlConfig provide<NAME>(Plugin plugin) {
		return YamlConfig.configuration(plugin, "<FILE_NAME>");
	}
	</el>
}