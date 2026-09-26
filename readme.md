[![Core Current](https://img.shields.io/maven-central/v/com.thewinterframework/core)](https://central.sonatype.com/artifact/com.thewinterframework/core)

# The Winter Framework 3.0

Winter is a dependency-injection-first framework for Minecraft plugins. Version 3.0 replaces the separate service,
listener, provider, command, and module discovery paths with one extensible component model.

## 3.0 highlights

- One `@Component` annotation for services, listeners, command objects, providers, modules, and addon-defined roles.
- Composed component annotations for domain-specific APIs without creating another discovery pipeline.
- External component indexes that let a library install components in a consuming plugin.
- Deterministic ordering for every component and module through `order`, `before`, and `after`.
- Explicit asynchronous lifecycle loading for independent components and modules.
- `ComponentHandler`, `ComponentInterceptor`, and `ComponentDecoratorHandler` extension points.
- A service-loader based processor API that uses annotation names instead of eagerly loading optional addon types.
- Existing decorators now work on every component. A single `@ScheduledAt` declaration is discovered correctly.

The legacy `@Service`, `@ListenerComponent`, `@ProviderComponent`, and `@ModuleComponent` annotations remain as
deprecated compatibility stereotypes. New code should use `@Component`.

## Requirements

- Java 21
- A platform module such as `paper`
- The same Winter version on the compile classpath and annotation processor path

```kotlin
dependencies {
    api("com.thewinterframework:paper:3.0.0")
    annotationProcessor("com.thewinterframework:paper:3.0.0")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}
```

## Bootstrapping a Paper plugin

```java
@WinterBootPlugin
public final class ExamplePlugin extends PaperWinterPlugin {
}
```

Point `plugin.yml` at this class. `PaperWinterPlugin` owns the platform lifecycle and initializes generated Winter
metadata before creating the Guice injector.

## Unified components

Any class can be a managed singleton:

```java
@Component
public final class ProfileService {
    private final ProfileRepository repository;

    @Inject
    public ProfileService(final ProfileRepository repository) {
        this.repository = repository;
    }
}
```

Platform handlers infer extra capabilities. A Paper listener needs no listener-specific component annotation:

```java
@Component
public final class JoinListener implements Listener {
    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        // Handle the event.
    }
}
```

Conditions and existing lifecycle decorators apply to the same component:

```java
@Component
@RequiresPlugin("Vault")
public final class EconomyBridge {
    @OnEnable
    void connect() {
    }

    @OnDisable
    void disconnect() {
    }
}
```

## Ordering and asynchronous loading

Lower numeric orders run first. Explicit dependency edges take precedence over numeric order.

```java
@Component(order = -100)
public final class ConfigurationComponent {
}

@Component(after = ConfigurationComponent.class)
public final class RepositoryComponent {
}
```

Independent opt-in components can perform lifecycle work on Java 21 virtual threads:

```java
@Component(async = true, after = ConfigurationComponent.class)
public final class RemoteCatalogComponent {
    @OnEnable
    void loadCatalog() {
        // Network or storage work only. Do not access unsafe Bukkit state here.
    }
}
```

Winter waits for asynchronous work before advancing the plugin lifecycle. A synchronous component creates a barrier
for asynchronous components scheduled before it. Cycles are rejected with a descriptive startup error.

Modules use the same metadata:

```java
@Component(async = true, order = 50)
public final class MetricsModule implements PluginModule {
    @Override
    public boolean onLoad(final WinterPlugin plugin) {
        return true;
    }
}
```

Asynchronous loading is never implicit. Enable it only for code that is safe away from the Paper server thread.

## External components

An addon can export a component to every consuming Winter plugin:

```java
@Component(scope = ComponentScope.EXTERNAL)
public final class SharedIntegration {
}
```

The producer's Winter annotation processor writes `META-INF/winter/components.index`. The consumer installs entries
from that index when the producer is present on both its compile and annotation processor paths:

```kotlin
dependencies {
    compileOnlyApi("com.example:shared-integration:1.0.0")
    annotationProcessor("com.example:shared-integration:1.0.0")

    api("com.thewinterframework:paper:3.0.0")
    annotationProcessor("com.thewinterframework:paper:3.0.0")
}
```

The exported classes must also be available to the plugin class loader at runtime. This is normally provided by the
server plugin dependency or by packaging the addon in the consumer.

External scope also works with composed annotations:

```java
@Component(scope = ComponentScope.EXTERNAL)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MyAddonComponent {
}
```

## Component handlers

Handlers add a capability to matching components. Winter loads handlers with `ServiceLoader`; a component may also
name a handler explicitly through `@Component(handlers = ...)`.

```java
public final class MessageHandler implements ComponentHandler {
    @Override
    public boolean supports(final Class<?> componentType) {
        return MessageEndpoint.class.isAssignableFrom(componentType);
    }

    @Override
    public void onEnable(final ComponentContext context) {
        final var endpoint = (MessageEndpoint) context.instance();
        // Register the endpoint.
    }
}
```

Publish the implementation as `META-INF/services/com.thewinterframework.component.handler.ComponentHandler`, or use
Google AutoService. This is the integration point for command frameworks and other addons: users annotate their class
with `@Component`, while the addon owns registration behavior.

## Interceptors

Interceptors wrap `LOAD`, `ENABLE`, and `DISABLE` for a component:

```java
public final class TimingInterceptor implements ComponentInterceptor {
    @Override
    public void before(final ComponentPhase phase, final ComponentContext context) {
        // Start timing.
    }

    @Override
    public void after(final ComponentPhase phase, final ComponentContext context) {
        // Record timing.
    }
}

@Component(interceptors = TimingInterceptor.class)
public final class TimedComponent {
}
```

Interceptors declared with `ServiceLoader` are global. Explicit interceptors apply only to the component that declares
them. Failure callbacks receive the phase, component context, and original exception.

## Component decorators

Decorators discover type or method annotations and participate in plugin lifecycle:

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ComponentDecorator(AuditedHandler.class)
public @interface Audited {
}

public final class AuditedHandler implements ComponentDecoratorHandler<Audited> {
    @Override
    public Class<Audited> getAnnotationType() {
        return Audited.class;
    }

    @Override
    public void onDiscover(
            final Class<?> component,
            final AnnotatedMethodHandle<Audited> method
    ) {
        // Store validated metadata for a later lifecycle phase.
    }
}
```

`@OnEnable`, `@OnDisable`, `@OnReload`, `@RepeatingTask`, `@ScheduledAt`, and `@Primary` now use this component-wide
pipeline. The old `ServiceDecorator` API remains available for binary migration.

## Processor extensions

Code-generating addons can implement `WinterProcessorExtension` and register it with `ServiceLoader`:

```java
public final class AddonProcessor implements WinterProcessorExtension {
    @Override
    public Set<String> supportedAnnotationNames() {
        return Set.of("com.example.GenerateAdapter");
    }

    @Override
    public void process(
            final TypeElement annotation,
            final Set<? extends Element> elements,
            final ProcessorContext context
    ) {
        // Generate addon metadata and call context.wireModule(...) when required.
    }
}
```

Extensions have deterministic priority through `order()` and round lifecycle callbacks. The legacy
`WinterAnnotationProcessor` interface adapts to this API, so existing processors can migrate incrementally.

## Migration from 2.x

| 2.x API | 3.0 API |
| --- | --- |
| `@Service` | `@Component` |
| `@ListenerComponent` | `@Component` on a `Listener` |
| `@ProviderComponent` | `@Component` on a Guice `Provider<T>` |
| `@ModuleComponent` | `@Component` on a `PluginModule` |
| Addon-specific component processor | `ComponentHandler` plus an optional composed `@Component` annotation |
| `ServiceDecoratorHandler` | `ComponentDecoratorHandler` |
| `WinterAnnotationProcessor` | `WinterProcessorExtension` |

The old component annotations are deprecated rather than removed. Migrate new code first, then remove aliases when
all consumers have moved to Winter 3.0.
