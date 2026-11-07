# Dependency injection

Winter uses Guice. `PaperWinterPlugin` creates the injector after module LOAD, using the platform bindings, discovered `PluginModule` instances, and the plugin itself as a Guice module.

## Built-in bindings

The Paper boot module binds the current instance as `WinterPlugin`, Bukkit `Plugin`, and `JavaPlugin`; its SLF4J `Logger`; and the plugin data directory as a `Path` qualified with `@DataFolder`. `ComponentModule` binds accepted component classes as singletons and exposes `ComponentManager`, `ServiceManager`, and `ReloadServiceManager`.

```java
@Component
public final class MessageStore {
  private final Path dataFolder;

  @Inject
  public MessageStore(@DataFolder final Path dataFolder) {
    this.dataFolder = dataFolder;
  }
}
```

`@Component` on a class binds its concrete type. An interface needs an additional binding. Options include a `PluginModule.configure` method, `@Primary` on an implementation, a Guice provider, or an exposed external contract.

## Bind an interface

`@Primary` on a component links an interface to its implementation. By default it chooses the first directly implemented interface; specify the intended interface when there is more than one:

```java
@Component
@Primary(ProfileRepository.class)
public final class SqlProfileRepository implements ProfileRepository {
}
```

For custom setup, implement `PluginModule.configure(Binder)` or override `PaperWinterPlugin.configure(Binder)`. Avoid declaring the same interface binding through multiple routes; Guice rejects duplicate explicit bindings.

## Providers

A provider component can directly implement `com.google.inject.Provider<T>`. The provider handler reads its declared `Provider<T>` interface and binds `T` through that provider:

```java
@Component
public final class TokenProvider implements Provider<Token> {
  @Override
  public Token get() {
    return new Token();
  }
}
```

The current component handler expects `Provider<T>` to be declared directly on that class. The old `@ProviderComponent` annotation is a deprecated component stereotype that explicitly selects the handler.

## Injection timing

Plugin and module member injection happens after `Guice.createInjector`. Accepted components are instantiated in the component INJECTION phase during module ENABLE. Do not request `plugin.getInjector()` in `onPluginLoad`, module `onLoad`, LOAD conditions, or LOAD admission decisions. Runtime `@FileName` YAML bindings and `@Expose`/`@External` links are described in [Configuration](configuration.md) and [Exposed APIs and multi-module builds](expose-and-multi-module.md).
