package example;

import com.google.inject.Inject;
import com.thewinterframework.plugin.DataFolder;
import com.thewinterframework.service.annotation.Service;
import com.thewinterframework.service.annotation.lifecycle.OnEnable;
import com.thewinterframework.service.annotation.lifecycle.OnDisable;
import com.thewinterframework.service.annotation.lifecycle.OnReload;
import com.thewinterframework.service.annotation.scheduler.RepeatingTask;
import com.thewinterframework.service.annotation.scheduler.ScheduledAt;
import com.thewinterframework.velocity.VelocityWinterPlugin;
import com.thewinterframework.velocity.yaml.FileName;
import com.thewinterframework.velocity.yaml.YamlConfig;
import com.thewinterframework.wire.condition.common.plugin.RequiresPlugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;

import java.nio.file.Path;
import java.util.UUID;

@Service
@RequiresPlugin("dependency")
public class TestService {
	public static boolean enabled;
	public static boolean disabled;
	public static boolean reloaded;
	public static int runs;
	public final YamlConfig config;
	@Inject @FileName("config") public YamlConfig sameConfig;

	@Inject
	public TestService(@FileName("config") YamlConfig config, @FileName("messages") YamlConfig messages,
					   ProxyServer server, VelocityWinterPlugin plugin, TestPlugin concrete,
					   @DataFolder Path folder, @DataDirectory Path directory, String provided, Integer answer, UUID inherited) {
		this.config = config;
		if (server != plugin.getServer() || plugin != concrete || !folder.equals(directory)
				|| !provided.equals("provided") || answer != 42 || !messages.getString("welcome").equals("Hello")
				|| inherited != plugin.getVelocityInjector().getInstance(UUID.class)) {
			throw new IllegalStateException("Invalid platform bindings");
		}
	}

	@OnEnable
	public void enable() { enabled = true; }

	@OnDisable
	public void disable() { disabled = true; }

	@OnReload
	public void reload() { reloaded = true; }

	@RepeatingTask(every = "config.get('interval')", unit = "SECONDS")
	public void repeat() { runs++; }

	@RepeatingTask(every = "100", async = "true")
	public void defaultUnit() { runs++; }

	@ScheduledAt(hour = "12")
	public void singleSchedule() { }

	@ScheduledAt(hour = "12")
	@ScheduledAt(hour = "18", async = "true")
	public void multipleSchedules() { }
}
