# Lifecycle and modules

`PaperWinterPlugin` owns Bukkit's final lifecycle methods. `PluginModuleManager` manages discovered modules; `ComponentModule` manages accepted components.

## Plugin phases

| Phase | Work | Injector available? |
| --- | --- | --- |
| `SCAN` | Read the generated `ProcessorModuleProvider` and register modules and their dependencies. | No |
| `LOAD` | Run module `onLoad` and plugin `onPluginLoad`. Component discovery, LOAD conditions, and early admission happen here. | No |
| `INJECTION` | Create the Guice injector, inject the plugin, then inject registered modules. | During the phase, after creation |
| `ENABLE` | Run module `onEnable` and plugin `onPluginEnable`. `ComponentModule` evaluates ENABLE admission, creates accepted component instances, and executes component handlers. | Yes |
| `DISABLE` | Disable loaded modules in reverse order, run `onPluginDisable` when the plugin reached injection, and cancel Paper tasks. | Usually |

`PluginLifecycleInterceptor` observes all five phases and receives before, after, and failure callbacks. `WinterPlugin` provides overridable `onPluginLoad`, `onPluginEnable`, and `onPluginDisable` hooks. Guice injection is unavailable in the load hook.

## Plugin modules

Implement `PluginModule` and mark the class `@Component` for automatic discovery:

```java
@Component
@SyncLoading
public final class DatabaseModule implements PluginModule {
  @Override
  public boolean onLoad(final WinterPlugin plugin) {
    return true;
  }

  @Override
  public void configure(final Binder binder) {
    binder.bind(DatabaseClient.class).to(DefaultDatabaseClient.class);
  }

  @Override
  public boolean onEnable(final WinterPlugin plugin) {
    return true;
  }
}
```

`depends(plugin)` returns modules that must run before this one. `before(plugin)` returns modules that must run after this one. Component modules may also use `@LoadAfter` and `@LoadBefore` for module-to-module edges. A module must have an accessible no-argument constructor because Winter creates it before the injector exists. Injected fields become available after `injectModules`, not in `onLoad` or `configure`.

Independent module LOAD and ENABLE work uses virtual threads by default. `@SyncLoading` and the compile-time Bukkit/Paper source scan make a module synchronous. Synchronous work waits for earlier asynchronous work. DISABLE runs in reverse order, attempts each loaded module, and closes the module executor.

## Component phases

`ComponentPhase` has `LOAD`, `INJECTION`, `ENABLE`, and `DISABLE`. LOAD conditions and both LOAD and INJECTION admission decisions happen before binding. At ENABLE, Winter injects extensions, evaluates ENABLE conditions and admission, then constructs selected component instances during the INJECTION callback phase. It runs ENABLE handlers next. Only enabled components receive DISABLE handler callbacks, in reverse order.

`ComponentContext.instance()` requires an injector and cannot be called from LOAD admission or LOAD callbacks. A component's constructor can safely request other Guice bindings after the injector exists. A handler can implement `onLoad`, `onInjection`, `onEnable`, and `onDisable`.

## Failure and cleanup

A failed module LOAD prevents normal startup; Winter disables modules whose load completed. A failed ENABLE prevents the plugin's successful enable hook. Disable continues through later modules and component callbacks while preserving failures. Paper cancels the plugin's scheduled tasks during disable. Own resources acquired during LOAD should be released by the module's `onDisable`, even if the component did not reach ENABLE.
