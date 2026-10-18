[![Core Current](https://img.shields.io/maven-central/v/com.thewinterframework/core)](https://central.sonatype.com/artifact/com.thewinterframework/core)

# ❄️ The Winter Framework ❄️

**The Winter Framework** is a modern, dependency-injection-first plugin framework for Minecraft servers. Heavily
inspired by [Spring Boot](https://github.com/spring-projects/spring-boot), it is designed to minimize boilerplate code,
allowing developers to focus on building features rather than managing infrastructure.

Winter Core uses Adventure 5.2.0 and targets Java 21. The Paper module targets Paper 26.2
(`26.2.build.134-stable`) and requires Java 25. Build the project with the included Gradle 9.1.0 wrapper,
with JDK 21 and JDK 25 available for the respective module toolchains.

---

## 🚀 Getting Started

To integrate Winter into your project, add the following to your `build.gradle.kts`:

```kotlin
plugins {
    `java-library` // Required to use the 'api' scope
}

dependencies {
    // Choose 'paper' or 'velocity' and replace [VERSION] with your desired release
    api("com.thewinterframework:paper:[VERSION]")
    annotationProcessor("com.thewinterframework:paper:[VERSION]")
}
```

> **Note:** The `annotationProcessor` is critical for Winter's component scanning and dependency injection to function
> correctly.

---

## 🧩 Creating a Winter Plugin

A Winter plugin starts by defining a main class that extends `PaperWinterPlugin` and is annotated with
`@WinterBootPlugin`.

```java

@WinterBootPlugin
public class MyPlugin extends PaperWinterPlugin {
    // Your plugin logic here
}
```

---

## Velocity

The `velocity` module targets Velocity 3.4 and Java 21. Use the Velocity API version appropriate for your proxy:

```kotlin
repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    api("com.thewinterframework:velocity:[VERSION]")
    annotationProcessor("com.thewinterframework:velocity:[VERSION]")
}
```

Bundle Winter and its runtime dependencies in your plugin JAR, for example using Shadow. Velocity's annotation processor
generates `velocity-plugin.json`, while Winter's processor generates component and module discovery.

```java
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.thewinterframework.plugin.WinterBootPlugin;
import com.thewinterframework.velocity.VelocityWinterPlugin;
import com.velocitypowered.api.plugin.Plugin;

@WinterBootPlugin
@Plugin(id = "my-plugin", name = "My Plugin", version = "1.0.0")
public final class MyPlugin extends VelocityWinterPlugin {
    @Inject
    public MyPlugin(final Injector velocityInjector) {
        super(velocityInjector);
    }
}
```

Winter creates a **child of Velocity's plugin injector** during `ProxyInitializeEvent`. Platform bindings such as
`ProxyServer`, `Logger`, `PluginContainer`, `@DataDirectory Path`, and the singleton plugin instance are inherited.
The child adds Winter services, providers, modules, `@DataFolder Path`, `PluginScheduler`, `PlatformPluginManager`, and
`ExpressionResolver`. `getVelocityInjector()` returns the original injector; `getInjector()` returns the child once
Winter starts. Put Winter dependencies in managed components: Velocity constructs the main plugin before the child exists.

`onPluginLoad()` and module loading run during initialization before the child injector is created. `onPluginEnable()`
runs after injection and module enabling. Shutdown runs module/service disable hooks, calls `onPluginDisable()`, cancels
the plugin's tasks, and unregisters its listeners. Services, lifecycle/reload hooks, providers, modules, expressions,
`@Primary`, and exposed APIs use the shared `core` module.

For listeners, use `com.thewinterframework.velocity.listener.ListenerComponent` with Velocity's `@Subscribe` methods;
Velocity listeners do not implement Bukkit's `Listener` interface. They are injected as singletons and registered automatically.

Velocity has no main thread. Both `sync` and `async` scheduler methods, including `getMainThreadExecutor()`, use Velocity's
scheduler thread pool. Callers must account for concurrency. Winter task IDs support individual cancellation, and
`TimeUnit.TICKS` represents 50 milliseconds per tick.

For YAML, inject `com.thewinterframework.velocity.yaml.YamlConfig` with the Velocity module's `@FileName("config")`.
Configurations load before injector creation, copy matching `.yml` classpath resources on first use, and create an empty
file when no default exists. This Bukkit-independent API provides `load()`, `save()`, `get()`, `set()`, dotted paths,
typed getters, and lists. Files are available in expressions such as `config.get('feature.enabled')`.

---

## 📦 Plugin Modules

Plugin Modules allow you to encapsulate functionality and hook into the plugin lifecycle. By implementing
`PluginModule`, you gain access to `onLoad`, `onEnable`, and `onDisable` hooks. Since `PluginModule` extends Guice's
`Module` class, you can perform manual dependency bindings here.

```java

@ModuleComponent
public class TestModule implements PluginModule {

    @Override
    public boolean onEnable(final WinterPlugin plugin) {
        plugin.getSLF4JLogger().info("Module enabled!");
        return true;
    }
}
```

> [!CAUTION]
> `onLoad` runs **before** the Guice injector is initialized. `onEnable` and `onDisable` run **after** the injector is
> ready, allowing you to safely use `@Inject` within those methods.

---

## ⚙️ Services & Lifecycle

### Defining Services

Annotate a class with `@Service` to register it as a Singleton managed by Winter.

```java

@Service
public class MyFirstService {
    private final Plugin plugin;

    @Inject
    public MyFirstService(final Plugin plugin) {
        this.plugin = plugin;
    }
}
```

### Lifecycle Hooks

Use `@OnEnable` and `@OnDisable` to handle setup and teardown logic. These methods support automatic parameter
injection.

```java

@Service
public class WarpService {
    @OnEnable
    void loadWarps(final WarpStorage storage, final Logger logger) {
        // Logic executed upon service initialization
    }
}
```

---

## ⏱️ Task Schedulers

Winter simplifies task scheduling through method-level decorators:

* **`@RepeatingTask`**: Executes a method at specific intervals.
* **`@ScheduledAt`**: Executes a method at a specific time (e.g., daily).

```java

@RepeatingTask(every = "1", unit = "MINUTES", async = "true")
public void broadcastMessage(final MyOtherService service) {
    service.broadcast("Hello World!");
}
```

---

## 💡 Advanced Features

### Primary Implementations

If multiple services implement the same interface, use `@Primary` to define which one should be injected by default.

```java

@Service
@Primary
public class DefaultMessageHandler implements MessageHandler { ...
}
```

### Annotation Expressions

Winter supports [JEXL](https://commons.apache.org/proper/commons-jexl/) within annotations, allowing for dynamic
configuration values.

```java

@RepeatingTask(every = "settings.timerInterval() * 2", unit = "SECONDS")
public void timer() { ...}
```

### Conditional Loading

Use conditional annotations to control whether a component should be registered.

```java

@ListenerComponent
@RequiresExpr("settings.joinEnabled()")
public class JoinListener implements Listener { ...
}
```

---

`@RequiresPlugin("PluginName")` checks whether a dependency is **present during loading**; Paper plugins may not yet be
enabled at that point. On Velocity, use the dependency's plugin ID. Also declare optional dependencies in `plugin.yml`
(`softdepend`) or Velocity's `@Plugin(dependencies = @Dependency(id = "dependency", optional = true))` to establish
class visibility and loading order. This condition applies to services, listeners, providers, and module components.

---

## 🛠️ Custom Decorators & Conditions

### 1. Creating Custom Service Decorators

Service decorators allow you to modify or intercept the lifecycle of a service registration.

**The Handler:**

```java
public class PrimaryHandler implements ServiceDecoratorHandler<Primary> {
    @Override
    public Class<Primary> getAnnotationType() {
        return Primary.class;
    }

    @Override
    public void onDiscoverOnType(final Class<?> service, final Primary annotation) {
        // Logic to modify binding
    }
}
```

**The Annotation:**

```java

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ServiceDecorator(PrimaryHandler.class)
public @interface Primary {
    Class<?> value() default Void.class;
}
```

### 2. Creating Custom Conditional Annotations

**The Condition:**

```java
public class RequiresExpressionCondition implements ComponentCondition {
    @Override
    public boolean matches(final ConditionContext context, final Annotation rawAnnotation) {
        final var annotation = (RequiresExpr) rawAnnotation;
        final var resolver = context.getPlugin().getExpressionResolver();
        return Boolean.TRUE.equals(resolver.resolve(annotation.value(), Boolean.class));
    }
}
```

**The Annotation:**

```java

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Requires(RequiresExpressionCondition.class)
public @interface RequiresExpr {
    String value();
}
```

---

## 📂 Configuration & Commands

* **YamlConfig**: Inject `YamlConfig` into any component and use the `@FileName` annotation to bind it to a file.
* **Commands**: Integrate the [Winter Command Module](https://github.com/thewinterframework/command) to
  leverage [Incendo Cloud](https://github.com/Incendo/cloud) for clean, auto-registered command structures.

> **Tip:** For advanced file handling, we highly recommend
> the [SpongePowered Configurate Module](https://github.com/thewinterframework/configuration).
