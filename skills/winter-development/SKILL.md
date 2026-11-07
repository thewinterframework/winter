---
name: winter-development
description: Build, migrate, or review Java 21 Paper and Velocity plugins using The Winter Framework 3.x. Use for projects that depend on com.thewinterframework:paper or com.thewinterframework:velocity; do not apply to ordinary platform plugins without Winter or to Winter 2.x without an explicit migration task.
---

# Winter Development

Build against the target project's resolved Winter version. The rules below describe Winter 3; inspect the checked-out framework source or the [versioned guides](https://github.com/thewinterframework/winter/tree/3.0.0/docs) when exact signatures or behavior matter. Preserve the consumer's existing build tool, package layout, and platform version.

## Select the platform

- Paper: depend on the `paper` artifact. The boot class extends `PaperWinterPlugin`, has `@WinterBootPlugin`, and is named in `plugin.yml`. Implement application work through Winter's plugin hooks or components; Bukkit lifecycle methods on the base class are final. Read the [Paper guide](https://github.com/thewinterframework/winter/blob/3.0.0/docs/paper-and-yaml.md) for listeners and YAML bindings.
- Velocity: depend on the `velocity` artifact. The concrete boot class extends `VelocityWinterPlugin` and has both `@Plugin` and `@WinterBootPlugin`. Its injected constructor passes `ProxyServer`, `Logger`, and `@DataDirectory Path` to the base class. Include Velocity's annotation processor to generate `velocity-plugin.json`. Register proxy listeners after initialization. Read the [Velocity guide](https://github.com/thewinterframework/winter/blob/3.0.0/docs/velocity.md) for setup and bindings.
- Keep platform-neutral contracts in a module that depends on `core` when a split is useful. Do not bring Paper APIs into a Velocity plugin or the reverse.

## Use generated wiring

- Put the matching Winter artifact on both the compile/runtime dependency path and the `annotationProcessor` path. The processor generates the module provider and component indexes that startup requires. Producer libraries exposing components must also be on the consumer's annotation processor path; their implementations must be available at runtime.
- Put compile-time Winter settings in each relevant module's `src/main/resources/winter.properties`, and exclude that file from the runtime artifact. Use `winter.project.type=API` for an API-only module and `IMPL` for an implementation module. Use `winter.expose.bypass-api=true` only for the documented single-module API case. See [configuration](https://github.com/thewinterframework/winter/blob/3.0.0/docs/configuration.md) and [multi-module exports](https://github.com/thewinterframework/winter/blob/3.0.0/docs/expose-and-multi-module.md).
- `@Component` is a marker. Put priority, ordering, handlers, interceptors, and synchronous loading in `@ComponentPriority`, `@LoadAfter`, `@LoadBefore`, `@HandledBy`, `@InterceptedBy`, and `@SyncLoading`. Use `@External` for a producer implementation and `@Expose` for a public API contract; `@External` already implies a component. Prefer these annotations for new code over deprecated service/listener/provider/module stereotypes.
- Prefer constructor injection and final dependencies. Bind interfaces through the generated external bindings, a provider, `@Primary`, or a Guice module. Keep the boot class focused on wiring and lifecycle ownership.

## Respect lifecycle and threads

- `onPluginLoad` runs before Guice injection; `onPluginEnable` runs after it. Release plugin-owned tasks, listeners, connections, and other resources during disable. Use `@OnEnable`, `@OnDisable`, and `@OnReload` on managed components when those callbacks fit the feature.
- Independent components and modules load in parallel by default. Mark work that must stay on the lifecycle thread with `@SyncLoading`. Winter's compiler detects direct Bukkit/Paper references, but cannot prove that indirect calls are safe; keep Bukkit world and entity access on the Paper thread.
- Velocity has no main scheduler thread. Its `runSync` scheduling family uses Velocity's asynchronous scheduler; `ensureSync`, `getSync`, and `getMainThreadExecutor` execute on the caller's thread. Never assume those methods move work to a special proxy thread.
- Component admission can discard candidates during compilation or at LOAD, INJECTION, and ENABLE. Use the documented interceptor/condition APIs when the feature needs conditional discovery; avoid side effects before a candidate is admitted. See [components](https://github.com/thewinterframework/winter/blob/3.0.0/docs/components.md) and [extensions](https://github.com/thewinterframework/winter/blob/3.0.0/docs/extensions.md).

## Verify the integration

Use the repository's Gradle wrapper and Java 21 toolchain. Run focused tests for changed behavior and a build that exercises annotation processing. When startup reports a missing generated provider or binding, inspect the annotation processor classpath, `@WinterBootPlugin`, and generated indexes before adding manual registration. Keep examples and resources such as `plugin.yml`, `winter.properties`, and runtime YAML aligned with the implementation.
