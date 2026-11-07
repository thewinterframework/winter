# The Winter Framework 3.0

Winter provides compile-time discovery, Guice injection, and a managed lifecycle for Java 21 Paper plugins. Its component model supports library and multi-module projects.

Read the [complete documentation](docs/README.md) for setup, configuration, components, lifecycle, Guice, exported APIs, scheduling, Paper integration, extension points, migration, and troubleshooting. The same guides are available in the [project wiki](https://github.com/thewinterframework/winter/wiki).

## Setup

Use the same Winter version for compilation and annotation processing:

```kotlin
dependencies {
  api("com.thewinterframework:paper:3.0.0")
  annotationProcessor("com.thewinterframework:paper:3.0.0")
}

tasks.processResources {
  exclude("winter.properties")
}
```

Winter's annotation processors read `src/main/resources/winter.properties` during compilation. The Gradle exclusion keeps this project-specific file out of the runtime artifact. Available keys:

```properties
winter.project.type=IMPL
winter.plugin.package=com.example.api
winter.plugin.name=ExamplePlugin
winter.expose.bypass-api=false
winter.debug=false
```

Use `winter.project.type=API` in an API-only module and `IMPL` otherwise. In an API-only module, set `winter.plugin.package` and `winter.plugin.name` to name the generated interface. In a single-module plugin, `winter.expose.bypass-api=true` derives that name and package from its `@WinterBootPlugin` class. `winter.debug=true` generates a marker that makes routine Winter messages visible at INFO; otherwise they use DEBUG. Errors remain visible. Each Gradle module reads its own file.

Legacy compiler options remain supported for migration. `-parameters` is a Java compiler flag, not a Winter option; keep it when a command framework relies on Java parameter names.

```java
@WinterBootPlugin
public final class ExamplePlugin extends PaperWinterPlugin {
}
```

Point `plugin.yml` at the boot class. Winter generates module and component indexes at compile time.

## Components

`@Component` is a marker. Put metadata in separate annotations:

```java
@Component
@ComponentPriority(-100)
public final class ConfigurationComponent {
}

@Component
@LoadAfter(ConfigurationComponent.class)
public final class RepositoryComponent {
  @Inject
  public RepositoryComponent(final ConfigurationComponent configuration) {
  }
}
```

`@LoadBefore` adds the reverse ordering edge. Components and independent plugin modules run on Java 21 virtual threads by default. `@SyncLoading` runs a component or module on the lifecycle thread and waits for earlier work:

```java
@Component
@SyncLoading
public final class MainThreadComponent {
}
```

The processor also marks components synchronous when their source directly references Bukkit or Paper types, or implements Bukkit's `Listener`. This scan cannot prove a transitive call is thread-safe. Mark components that reach Bukkit indirectly with `@SyncLoading`.

Conditions and lifecycle decorators work on components. A Paper `Listener` annotated with `@Component` is handled by the Paper module. The old `@Service`, `@ListenerComponent`, `@ProviderComponent`, and `@ModuleComponent` annotations remain as deprecated stereotypes.

## API and external dependencies

An API module may declare an exposed contract:

```java
@Expose
public interface ProfileRepository {
  Profile find(UUID playerId);
}
```

Its `winter.properties` should set `winter.project.type=API`, `winter.plugin.package`, and `winter.plugin.name`. The implementation module compiles against that API and exports a component:

```java
@External
public final class MongoProfileRepository implements ProfileRepository {
  // Implement the contract.
}
```

Winter writes `META-INF/winter/components.index` with the external component and its exposed interface binding. The consuming plugin discovers the index at compile time, wires `ExternalBindingsModule` automatically, and Guice can inject `ProfileRepository`. The generated `ExposeAPIModule` also binds the generated plugin API interface to its default implementation. Producer artifacts must be on the consumer's compile and annotation processor paths, and the implementation must be available to the consumer class loader at runtime. Conflicting exports of one contract fail with an error.

```kotlin
dependencies {
  implementation(project(":api"))
  implementation(project(":implementation"))
  annotationProcessor(project(":implementation"))
}
```

`@External` is a component stereotype and may be composed on another annotation. An external component without an `@Expose` interface is discovered but has no interface binding generated.

## Extensions and admission

`ComponentHandler` adds behavior to matching components; register it with `ServiceLoader`, or name it with `@HandledBy(MyHandler.class)`. `@InterceptedBy(MyInterceptor.class)` applies an interceptor to one component. `ComponentInterceptor` and `ComponentDecoratorHandler` can return `ComponentDecision.DISCARD` at `LOAD`, `INJECTION`, or `ENABLE`. LOAD and INJECTION decisions occur before Guice binding; ENABLE decisions prevent enable and disable callbacks for that component. Interceptors also receive `before`, `after`, and `onFailure` callbacks for `LOAD`, `INJECTION`, `ENABLE`, and `DISABLE`. The older `decide(ComponentContext)` method still applies at LOAD.

Compile-time policies implement `ComponentCompileInterceptor` and register through `META-INF/services/com.thewinterframework.processor.component.ComponentCompileInterceptor`. Returning `DISCARD` prevents the component from entering generated indexes. General code generation extensions implement `WinterProcessorExtension` through `ServiceLoader`; these receive round callbacks and can wire plugin modules.

`PluginLifecycleInterceptor` observes `SCAN`, `LOAD`, `INJECTION`, `ENABLE`, and `DISABLE` for the Paper plugin. Add it through `META-INF/services/com.thewinterframework.plugin.lifecycle.PluginLifecycleInterceptor`. Interceptors run by priority and class name; `after` runs in reverse order, and `onFailure` receives the original failure.

## Lifecycle behavior

Winter resolves explicit `@LoadAfter` and `@LoadBefore` dependencies before starting independent work. A `@SyncLoading` component or module waits for preceding parallel work. Component instances are created during INJECTION, before ENABLE handlers run. When a module fails to load, Winter disables modules whose load completed successfully. During shutdown it attempts every disable callback and preserves failures.

`@OnEnable`, `@OnDisable`, `@OnReload`, `@RepeatingTask`, `@ScheduledAt`, and `@Primary` use the component pipeline. `ServiceDecoratorHandler` and `WinterAnnotationProcessor` remain available for migration.
