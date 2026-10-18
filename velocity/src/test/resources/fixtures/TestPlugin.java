package example;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.thewinterframework.plugin.WinterBootPlugin;
import com.thewinterframework.velocity.VelocityWinterPlugin;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
@WinterBootPlugin
@Plugin(id = "winter-test", name = "Winter Test", version = "1.0", dependencies = @Dependency(id = "dependency", optional = true))
public class TestPlugin extends VelocityWinterPlugin {
	public static int loads;
	public static int enables;
	public static int disables;
	public static boolean failEnable;

	@Inject
	public TestPlugin(Injector injector) {
		super(injector);
	}

	@Override
	public void onPluginLoad() {
		if (getInjector() != null) throw new IllegalStateException("Injector available too early");
		loads++;
	}

	@Override
	public void onPluginEnable() {
		if (getInjector() == null) throw new IllegalStateException("Missing injector");
		enables++;
		if (failEnable) throw new IllegalStateException("Expected enable failure");
	}

	@Override
	public void onPluginDisable() {
		disables++;
	}
}
