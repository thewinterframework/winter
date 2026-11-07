# Velocity integration

The `velocity` module runs Winter 3 on Velocity 3.4 with Java 21. It provides `VelocityWinterPlugin`, a proxy scheduler, and loaded-plugin lookup. It reuses the same compile-time component discovery, module lifecycle, Guice injection, interceptors, and `winter.properties` configuration as Paper. Bukkit listeners and Paper YAML bindings belong to the `paper` module and are not installed on Velocity.

## Build setup

Add PaperMC's Maven repository and use the same Winter version for the dependency and annotation processor. Velocity's annotation processor generates `velocity-plugin.json` from `@Plugin`.

```kotlin
repositories {
  mavenCentral()
  maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
  implementation("com.thewinterframework:velocity:3.0.0")
  annotationProcessor("com.thewinterframework:velocity:3.0.0")
  compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
  annotationProcessor("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.processResources {
  exclude("winter.properties")
}
```

Package Winter and its non-platform runtime dependencies with the plugin, or use a supported dependency loader. Keep `velocity-api` provided by the proxy. An API-only Gradle module can use `winter.project.type=API` in its own `winter.properties`; see [Configuration](configuration.md).

## Boot class

Annotate the concrete plugin class with both `@Plugin` and `@WinterBootPlugin`. Velocity injects the constructor parameters, including `@DataDirectory`. Pass them to the Winter base class.

```java
@Plugin(id = "example", name = "Example", version = "1.0.0")
@WinterBootPlugin
public final class ExamplePlugin extends VelocityWinterPlugin {
  @Inject
  public ExamplePlugin(
      final ProxyServer server,
      final Logger logger,
      final @DataDirectory Path dataDirectory
  ) {
    super(server, logger, dataDirectory);
  }

  @Override
  public void onPluginEnable() {
    getSLF4JLogger().info("Example is ready");
  }
}
```

Velocity constructs the plugin first, then fires `ProxyInitializeEvent`. Winter performs SCAN, LOAD, INJECTION, and ENABLE from that event. Do not register listeners or access proxy services in the constructor. `ProxyShutdownEvent` disables Winter modules and cancels tasks. Override `onPluginLoad`, `onPluginEnable`, and `onPluginDisable` for application work. `onPluginLoad` precedes Guice injection; `onPluginEnable` follows it.

## Guice and components

Winter binds `WinterPlugin`, `VelocityWinterPlugin`, `ProxyServer`, `Logger`, and the data directory as a `Path` qualified by either Winter's `@DataFolder` or Velocity's `@DataDirectory`. Component classes are discovered at compile time and bound by `ComponentModule`. `getPlatformManager().isPluginEnabled(id)` checks whether Velocity has loaded a plugin with that ID.

Independent components and modules load on virtual threads by default. Use `@SyncLoading` when work must remain on the lifecycle event thread. Velocity does not have Bukkit's primary-thread rule, and Winter's Bukkit source scan does not mark Velocity API calls synchronous automatically.

## Scheduling

Velocity's scheduler runs tasks asynchronously and has no main thread. Consequently `runSync`, `runDelayedSync`, and `runAtFixedRateSync` use the same scheduler as their async counterparts. `ensureSync`, `getSync`, and `getMainThreadExecutor` run immediately on the caller's thread. Task IDs returned by Winter map to Velocity scheduled tasks and support `cancelTask`; shutdown cancels every task owned by the plugin. `TimeUnit.TICKS` represents 50 milliseconds.

To register additional Velocity listeners, call `getProxyServer().getEventManager().register(this, listener)` from `onPluginEnable` or another post-initialization callback. The main plugin class is registered by Velocity automatically.
