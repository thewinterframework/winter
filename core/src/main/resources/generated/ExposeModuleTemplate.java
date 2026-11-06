package <PACKAGE>;

import com.google.inject.Binder;
import com.google.inject.Scopes;
import com.thewinterframework.plugin.WinterPlugin;
import com.thewinterframework.processor.module.ProcessorModule;
import com.thewinterframework.component.annotation.SyncLoading;

@SyncLoading
public class ExposeAPIModule implements ProcessorModule {

  @Override
  public void configure(final Binder binder) {
    binder.bind(<PLUGIN>API.class).to(Default<PLUGIN>API.class).in(Scopes.SINGLETON);
  }

  @Override
  public boolean onEnable(final WinterPlugin plugin) {
    final var apiImpl = plugin.getInjector().getInstance(<PLUGIN>API.class);
    <PLUGIN>API.Provider.setInstance(apiImpl);
    return true;
  }
}
