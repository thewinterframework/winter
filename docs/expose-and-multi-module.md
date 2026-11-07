# Exposed APIs and multi-module builds

`@Expose` publishes an API type. `@External` publishes a component implementation for a consuming plugin. Winter generates an API interface, a Guice-backed default implementation, a Guice module for that API, and indexes used by consumers.

## Example layout

```text
settings.gradle.kts
api/src/main/resources/winter.properties
api/src/main/java/example/api/ProfileRepository.java
implementation/src/main/java/example/impl/SqlProfileRepository.java
consumer/src/main/java/example/consumer/ConsumerPlugin.java
```

In `api/src/main/resources/winter.properties`:

```properties
winter.project.type=API
winter.plugin.package=example.consumer
winter.plugin.name=ConsumerPlugin
```

In `api/src/main/java/example/api/ProfileRepository.java`:

```java
package example.api;

@Expose
public interface ProfileRepository {
  Profile find(UUID playerId);
}
```

The API module's processor generates `example.consumer.ConsumerPluginAPI` with a `getProfileRepository()` method. The generated API name and package must match the consumer boot class when the consumer uses the generated `ExposeAPIModule`.

In the implementation module:

```java
package example.impl;

@External
public final class SqlProfileRepository implements ProfileRepository {
  // Implement find(UUID).
}
```

`@External` already implies `@Component`. A component may directly implement an `@Expose` interface or inherit it through a superclass or interface. Only exposed **interfaces** receive automatic contract-to-implementation Guice links. Multiple external implementations of the same contract are rejected.

The consumer boot class:

```java
package example.consumer;

@WinterBootPlugin
public final class ConsumerPlugin extends PaperWinterPlugin {
}
```

Use normal compile/runtime dependencies **and** annotation processor dependencies in the consumer:

```kotlin
dependencies {
  api("com.thewinterframework:paper:3.0.0")
  annotationProcessor("com.thewinterframework:paper:3.0.0")

  implementation(project(":api"))
  implementation(project(":implementation"))
  annotationProcessor(project(":implementation"))
}
```

The API and implementation modules also need Winter on their own annotation processor paths. The API module uses `winter.project.type=API`; the implementation module uses `IMPL` or the default. Exclude `winter.properties` from each runtime artifact as shown in [Configuration](configuration.md).

## Generated behavior

The implementation artifact contains `META-INF/winter/components.index` entries for its external components and exposed contract bindings. The consumer processor reads those indexes, adds the external components to its component wire, and wires `ExternalBindingsModule`. Guice can then inject `ProfileRepository` into consumer components. The generated `ExposeAPIModule` binds `ConsumerPluginAPI` to `DefaultConsumerPluginAPI` and sets `ConsumerPluginAPI.Provider` when enabled.

This mechanism creates Guice bindings in the **consumer's** injector. The producer implementation jar must be visible to the consumer at runtime. If a producer is present only at compile time, startup will fail while loading classes or resources.

## Single-module API

If an API and boot plugin are compiled together, set `winter.expose.bypass-api=true` in that module's `winter.properties`. The API generator derives the package and name from `@WinterBootPlugin`; no separate `API` module is required. This setting applies to compile-time generation only.

## Diagnostics

- Missing generated API: verify `@Expose`, the processor dependency, and `winter.project.type` or `winter.expose.bypass-api`.
- Missing external component: verify `@External`, producer compilation, and the implementation artifact on the consumer's annotation processor path.
- `ClassNotFoundException` at runtime: make the implementation artifact visible to the plugin class loader.
- Guice duplicate binding: choose one implementation per exposed interface and avoid binding the same contract manually.
