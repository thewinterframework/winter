# Migration to Winter 3.0

Winter 3 unifies services, listeners, providers, and plugin modules under `@Component`. This is a source-level breaking change for code that used attributes on the 3.0 preview component annotation.

| Previous declaration | Winter 3 declaration |
| --- | --- |
| `@Service` | `@Component` |
| `@ListenerComponent` | `@Component` on a Bukkit `Listener` |
| `@ProviderComponent` | `@Component` on a direct `Provider<T>` implementation |
| `@ModuleComponent` | `@Component` on a `PluginModule` |
| `@Component(order = n)` | `@Component @ComponentPriority(n)` |
| `@Component(after = A.class)` | `@Component @LoadAfter(A.class)` |
| `@Component(before = B.class)` | `@Component @LoadBefore(B.class)` |
| `@Component(async = false)` | `@Component @SyncLoading` |
| `@Component(async = true)` | `@Component` (parallel by default) |
| `@Component(scope = EXTERNAL)` | `@External` |
| `@Component(handlers = H.class)` | `@Component @HandledBy(H.class)` |
| `@Component(interceptors = I.class)` | `@Component @InterceptedBy(I.class)` |
| Compiler `-Aplugin.*` / `-Abypass.api` | Module-local `src/main/resources/winter.properties` |

The old service/listener/provider/module annotations remain deprecated stereotypes for gradual migration. `ServiceDecoratorHandler` and `WinterAnnotationProcessor` remain compatibility interfaces. New extensions should use `ComponentDecoratorHandler` and `WinterProcessorExtension`.

## Behavior changes to check

1. **Threading:** independent components and modules now run in parallel by default. Add `@SyncLoading` for main-thread work and for calls that reach Bukkit/Paper indirectly. The compiler's source scan detects only direct references.
2. **Admission:** compile-time and runtime interceptors can discard components. A LOAD/INJECTION discard prevents binding; an ENABLE discard prevents enable/disable callbacks but leaves the earlier binding in place.
3. **Cleanup:** legacy processor modules disable only components that reached ENABLE. Release resources acquired in module LOAD from the module's `onDisable` hook.
4. **Exports:** use `@External` on implementations and `@Expose` on public interfaces to publish automatic Guice links. Keep producer artifacts available on the consumer's annotation processor path and runtime class loader.
5. **Configuration:** exclude `winter.properties` from runtime resources. Keep Java `-parameters` if a separate command library needs reflected parameter names.

Run `./gradlew clean build`, inspect the generated classes/indexes, then test plugin startup and shutdown on the target Paper server. The [Troubleshooting](troubleshooting.md) guide maps common failures to those stages.
