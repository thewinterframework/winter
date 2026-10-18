package example;

import com.google.inject.Binder;
import com.thewinterframework.module.annotation.ModuleComponent;
import com.thewinterframework.plugin.WinterPlugin;
import com.thewinterframework.plugin.module.PluginModule;
import com.thewinterframework.wire.condition.common.plugin.RequiresPlugin;

@ModuleComponent
@RequiresPlugin("dependency")
public class TestModule implements PluginModule {
	public static boolean loaded;
	public static boolean enabled;
	public static boolean disabled;

	public boolean onLoad(WinterPlugin plugin) { loaded = true; return true; }
	public boolean onEnable(WinterPlugin plugin) { enabled = true; return true; }
	public boolean onDisable(WinterPlugin plugin) { disabled = true; return true; }
	public void configure(Binder binder) { binder.bind(Integer.class).toInstance(42); }
}
