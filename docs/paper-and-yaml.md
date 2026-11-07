# Paper integration

The `paper` module provides `PaperWinterPlugin`, Paper scheduler and platform adapters, listener registration, and file-backed YAML configuration.

## Paper lifecycle and listeners

Extend `PaperWinterPlugin` and annotate the boot class with `@WinterBootPlugin`. Point `plugin.yml` at that class. Winter's final Bukkit lifecycle methods manage module startup, Guice creation, cleanup, and task cancellation. Your hooks are `onPluginLoad`, `onPluginEnable`, and `onPluginDisable`.

```java
@Component
public final class JoinListener implements Listener {
  @EventHandler
  public void onJoin(final PlayerJoinEvent event) {
    // Paper event handling runs on the event's designated thread.
  }
}
```

The listener handler registers accepted Bukkit `Listener` components when enabled. Bukkit listener classes are marked synchronous for lifecycle loading. The legacy `@ListenerComponent` annotation is still available as a deprecated stereotype.

## YAML files

Place `messages.yml` in the plugin jar and request a `YamlConfig` with the `@FileName` qualifier:

```java
@Component
public final class Messages {
  private final YamlConfig config;

  @Inject
  public Messages(@FileName("messages") final YamlConfig config) {
    this.config = config;
  }
}
```

The annotation processor generates a Guice binding for each distinct `@FileName` on fields or parameters. On first use, `YamlConfig.configuration(plugin, "messages")` copies `messages.yml` to the plugin data folder when absent and loads it. `config.load()` reloads from disk; `config.save()` writes the current in-memory data. A missing default resource or invalid YAML throws `IllegalStateException`.

This runtime YAML configuration is separate from compile-time `winter.properties`. `YamlConfig.configuration` also registers the loaded file in `ExpressionResolver`, making values available to expression-based annotations. See [Configuration](configuration.md).

## Data folder and main-thread work

Inject `@DataFolder Path` for the plugin data directory. Use `PluginScheduler.ensureSync` or `getSync` to interact with Bukkit state from virtual-thread component work. Prefer `@SyncLoading` on a component whose lifecycle itself must run on the server thread.
