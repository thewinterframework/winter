package example;

import com.thewinterframework.module.annotation.ModuleComponent;
import com.thewinterframework.plugin.module.PluginModule;
import com.thewinterframework.wire.condition.common.plugin.RequiresPlugin;

@ModuleComponent
@RequiresPlugin("missing")
public class MissingModule implements PluginModule {
	public MissingModule() { throw new IllegalStateException("Absent integration was instantiated"); }
}
