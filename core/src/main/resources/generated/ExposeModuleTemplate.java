package <PACKAGE>;

import com.thewinterframework.plugin.WinterPlugin;
import com.thewinterframework.processor.module.ProcessorModule;

public class ExposeAPIModule implements ProcessorModule {

	@Override
	public boolean onEnable(final WinterPlugin plugin) {
		final var apiImpl = plugin.getInjector().getInstance(<PACKAGE>.Default<PLUGIN>API.class);
        <PACKAGE>.<PLUGIN>API.Provider.setInstance(apiImpl);
		return true;
	}
}