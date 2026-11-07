# Extension points

Winter exposes compile-time and runtime extension APIs. Implement only the capability your addon needs, and register global implementations with Java `ServiceLoader` (or a service-file generator such as AutoService).

## Component handlers

`ComponentHandler` adds a role to matching components. `supports(type)` selects classes automatically; `@HandledBy(MyHandler.class)` selects a handler explicitly. `configure(Binder, components)` can add Guice bindings. Lifecycle callbacks are `onLoad`, `onInjection`, `onEnable`, and `onDisable`.

```java
public final class EndpointHandler implements ComponentHandler {
  @Override
  public boolean supports(final Class<?> type) {
    return Endpoint.class.isAssignableFrom(type);
  }

  @Override
  public void onEnable(final ComponentContext context) {
    final Endpoint endpoint = (Endpoint) context.instance();
    endpoint.start();
  }
}
```

To install globally, add `META-INF/services/com.thewinterframework.component.handler.ComponentHandler` containing the implementation's fully qualified class name. Handlers run by `order()` and class name. A handler selected only by an annotation can be created without a service file; it needs a no-argument constructor.

## Runtime component interceptors

`ComponentInterceptor` can admit or discard a candidate and observe its lifecycle. Global interceptors use `META-INF/services/com.thewinterframework.component.interceptor.ComponentInterceptor`; local ones use repeatable `@InterceptedBy`.

```java
public final class FeatureGate implements ComponentInterceptor {
  @Override
  public ComponentDecision decide(final ComponentPhase phase, final ComponentContext context) {
    if (phase == ComponentPhase.ENABLE && !featureIsEnabled(context.componentType())) {
      return ComponentDecision.DISCARD;
    }
    return ComponentDecision.CONTINUE;
  }

  @Override
  public void onFailure(final ComponentPhase phase,
                        final ComponentContext context,
                        final Throwable failure) {
    // Report the original failure.
  }
}
```

`decide(context)` is a compatibility shortcut evaluated at LOAD. The phased overload handles LOAD, INJECTION, and ENABLE. LOAD/INJECTION admission runs before binding, so `context.instance()` is unavailable there. ENABLE admission happens after the injector exists but before component INJECTION and ENABLE callbacks. `before`, `after`, and `onFailure` observe LOAD, INJECTION, ENABLE, and DISABLE execution. Returning `DISCARD` from admission does not throw and does not roll back resources already acquired elsewhere.

## Compile-time component interceptors

Implement `ComponentCompileInterceptor.decide(ProcessingEnvironment, TypeElement)` and register it in `META-INF/services/com.thewinterframework.processor.component.ComponentCompileInterceptor`. Returning `DISCARD` omits the type from generated component and external indexes. Compile-time interceptors run in priority and class-name order. They should inspect compiler elements and avoid loading application classes reflectively.

## Component decorators

Mark an annotation with `@ComponentDecorator(MyHandler.class)`. A `ComponentDecoratorHandler<A>` can reject components at admission, discover type/method annotations, configure Guice, and respond to plugin load/enable/disable. Its `getAnnotationType()` must return the decorated annotation type. See [Conditions and decorators](conditions-and-decorators.md) for a complete declaration.

## Plugin lifecycle interceptors

`PluginLifecycleInterceptor` observes `PluginPhase.SCAN`, `LOAD`, `INJECTION`, `ENABLE`, and `DISABLE`. Register it in `META-INF/services/com.thewinterframework.plugin.lifecycle.PluginLifecycleInterceptor`. Before callbacks run by `order()` then class name; after callbacks run in reverse order. On failure, each interceptor receives the original throwable, with failures from notification callbacks suppressed onto it.

## Annotation processor extensions

Implement `WinterProcessorExtension` for code generation. Return annotation names from `supportedAnnotationNames()` so an optional addon annotation need not be loaded by Winter's processor class loader. `initialize` runs once, round start/end callbacks surround `process`, and `onProcessingFinished` runs at the final round. `ProcessorContext.wireModule(...)` adds a module to the generated provider. Register the implementation in `META-INF/services/com.thewinterframework.processor.extension.WinterProcessorExtension`.

The old `WinterAnnotationProcessor` adapts to this interface and remains for migration. Processor implementations should produce deterministic output, avoid duplicate file creation across rounds, and keep generated names stable.
