[![Core Current](https://img.shields.io/maven-central/v/com.thewinterframework/core)](https://central.sonatype.com/artifact/com.thewinterframework/core)

# ❄️ The Winter Framework ❄️

**The Winter Framework** is a modern, dependency-injection-first plugin framework for Minecraft servers. Heavily
inspired by [Spring Boot](https://github.com/spring-projects/spring-boot), it is designed to minimize boilerplate code,
allowing developers to focus on building features rather than managing infrastructure.

---

## 🚀 Getting Started

To integrate Winter into your project, add the following to your `build.gradle.kts`:

```kotlin
plugins {
    `java-library` // Required to use the 'api' scope
}

dependencies {
    // Replace [PLATFORM] with 'paper' and [VERSION] with your desired release
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