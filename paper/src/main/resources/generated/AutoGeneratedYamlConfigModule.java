// This file is generated automatically by thewinterframework. Do not modify this file manually.
package <PACKAGE_NAME>;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.thewinterframework.paper.yaml.FileName;
import com.thewinterframework.paper.yaml.FileNameImpl;
import com.thewinterframework.paper.yaml.YamlConfig;
import com.thewinterframework.processor.module.ProcessorModule;
import org.bukkit.plugin.java.JavaPlugin;

public class <CLASS_NAME> extends AbstractModule implements ProcessorModule {

	@Override
	protected void configure() {
		var plugin = JavaPlugin.getProvidingPlugin(this.getClass());

        <el>
				bind(YamlConfig.class)
						.annotatedWith(new FileNameImpl("<FILE_NAME>"))
						.toInstance(YamlConfig.configuration(plugin, "<FILE_NAME>"));
        </el>
	}
}