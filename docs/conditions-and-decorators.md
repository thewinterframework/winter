# Conditions and decorators

Conditions determine whether a component joins a lifecycle phase. Decorators discover component annotations on types and methods and may add bindings or callbacks.

## Built-in conditions

```java
@Component
@RequiresPlugin("Vault")
public final class EconomyBridge {
}
```

`@RequiresPlugin` evaluates during LOAD through the platform plugin manager. A failed LOAD condition prevents registration and binding. `@RequiresExpr("settings.enabled")` evaluates a JEXL boolean expression during ENABLE, after the injector exists. A failed ENABLE condition leaves the class bound but skips component injection and enable/disable callbacks.

Create a condition by annotating your annotation type with `@Requires(MyCondition.class)` and implementing `ComponentCondition`:

```java
public final class MyCondition implements ComponentCondition {
  @Override
  public Stage getStage() {
    return Stage.ENABLE;
  }

  @Override
  public boolean matches(final ConditionContext context, final Annotation annotation) {
    return true;
  }
}
```

LOAD conditions are constructed reflectively because Guice does not yet exist. ENABLE conditions are obtained from Guice. `Stage.DISABLE` exists in the enum, but component admission currently evaluates conditions at LOAD and ENABLE. Use module or decorator cleanup for disable behavior.

## Built-in lifecycle decorators

`@OnEnable` and `@OnDisable` mark component methods for plugin enable and disable callbacks. `@OnReload` adds a method to `ReloadServiceManager.reload()`; code can also add a `Runnable` through `addOnReload`. The annotations expose class dependency arrays such as `after`/`before` where supported. They are method decorators, so the containing class still needs `@Component` or a component stereotype.

```java
@Component
public final class CacheComponent {
  @OnEnable
  public void start() {
  }

  @OnDisable
  public void stop() {
  }
}
```

`@Primary` is a type decorator that adds an interface binding; see [Dependency injection](dependency-injection.md). `@RepeatingTask` and `@ScheduledAt` are method decorators; see [Scheduling](scheduling.md).

## Define a decorator

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
  public void onDiscover(final Class<?> component,
                         final AnnotatedMethodHandle<Audited> method) {
    // Record metadata for a later lifecycle callback.
  }
}
```

A decorator handler can reject a component with `decide(ComponentPhase, ComponentContext)`, discover type/method annotations, configure Guice, and receive plugin load/enable/disable callbacks. Admission runs before discovery side effects are committed. See [Extension points](extensions.md).
