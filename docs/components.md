# Components

Winter's unified component model manages services, Paper listeners, providers, plugin modules, and addon-defined roles. A concrete, public class becomes a component with the marker `@Component`. Nested components must be static. Guice binds accepted components as singletons.

## Metadata annotations

| Annotation | Effect |
| --- | --- |
| `@Component` | Marks a class or another annotation as a component stereotype. It has no attributes. |
| `@ComponentPriority(int)` | Lower values are considered first when ordering is otherwise unconstrained. Default is `0`. |
| `@LoadAfter({A.class})` | Makes the component wait for listed components. |
| `@LoadBefore({B.class})` | Makes the listed components wait for this component. |
| `@SyncLoading` | Runs lifecycle work on the lifecycle thread, with a barrier for earlier parallel work. |
| `@External` | Exports the component to consuming Winter plugins; it is itself a component stereotype. |
| `@HandledBy(Handler.class)` | Selects a `ComponentHandler`; repeatable. |
| `@InterceptedBy(Interceptor.class)` | Selects a runtime `ComponentInterceptor`; repeatable. |

Metadata can be placed directly on a class or on a one-level component stereotype. Direct metadata takes precedence for single-value annotations. Handler and interceptor declarations from the class and stereotype are combined.

```java
@Component
@ComponentPriority(-50)
@LoadAfter(ConfigurationComponent.class)
@InterceptedBy(AuditInterceptor.class)
public final class CatalogComponent {
}
```

```java
@Component
@HandledBy(EndpointHandler.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Endpoint {
}

@Endpoint
public final class StatusEndpoint {
}
```

## Ordering and concurrency

Dependency edges take precedence over numeric priority. Winter rejects self-dependencies, duplicate components, and cycles. An edge to a component not present in this plugin's accepted set has no ordering effect.

Independent components run lifecycle phases on Java 21 virtual threads by default. A synchronous component waits for earlier work and then runs on the lifecycle thread. The compiler marks direct references to `org.bukkit`, `io.papermc.paper`, and `com.destroystokyo.paper` synchronous, and also marks Bukkit `Listener` implementations. It cannot inspect indirect calls through your own service. Add `@SyncLoading` whenever such a call could reach unsafe server state.

The same rule applies to plugin modules. Explicit module dependencies are resolved by `PluginModuleManager`; [Lifecycle and modules](lifecycle.md) describes the execution phases.

## Special roles

- A `@Component` class implementing Bukkit `Listener` is registered by the Paper handler. The deprecated `@ListenerComponent` stereotype still works and is synchronous.
- A `@Component` class directly implementing Guice `Provider<T>` can be used as a provider. The deprecated `@ProviderComponent` stereotype selects the provider handler explicitly. See [Dependency injection](dependency-injection.md).
- A `@Component` class implementing `PluginModule` is discovered as a module. The deprecated `@ModuleComponent` remains available.
- An `@External` class is published to other plugins. An interface annotated `@Expose` that it implements becomes an automatic Guice link. See [Exposed APIs and multi-module builds](expose-and-multi-module.md).

## Admission

Components may be discarded by a compile-time interceptor before metadata is generated; by runtime interceptors or decorators at LOAD and INJECTION before Guice binding; or at ENABLE before enable callbacks. An ENABLE discard does not retroactively remove an already registered Guice binding. See [Extension points](extensions.md) for the APIs and [Lifecycle and modules](lifecycle.md) for timing.
