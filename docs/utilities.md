# Text, expressions, and utilities

Winter includes helpers that are independent of the component discovery mechanism.

## Adventure components

`ComponentUtils` parses and serializes MiniMessage, legacy `§` and `&` formats, and plain text. It resolves strings, numbers, iterables, Adventure `Component` instances, and custom types into components. `ComponentJoiner` joins multiple components with a chosen delimiter; `Tags` contains common tag helpers.

```java
final Component title = ComponentUtils.miniMessage("<gold>Welcome</gold>");
final String plain = ComponentUtils.plainText(title);
```

Register a global MiniMessage tag resolver, object resolver, or component modifier with the `register...` methods. They return `ComponentUtils.Registration`, an `AutoCloseable` you should close during plugin shutdown. Close resolver overrides in reverse registration order to restore the previous resolver correctly. The older `add...` methods remain but do not return an ownership handle.

```java
private ComponentUtils.Registration registration;

void install(final TagResolver resolver) {
  registration = ComponentUtils.registerGlobalTagResolver(resolver);
}

void remove() {
  if (registration != null) {
    registration.close();
  }
}
```

## Expressions

`WinterPlugin.getExpressionResolver()` defaults to `JexlExpressionResolver`. It evaluates JEXL expressions against named contexts and converts the result to the requested type. You can call `addContext(name, value)` and `addConverter(source, target, converter)`. The default resolver includes number, boolean, `TimeUnit`, and MiniMessage-to-Adventure conversions. `YamlConfig.configuration` adds its YAML wrapper as a context under the file name.

Expressions power `@RequiresExpr`, `@RepeatingTask`, and `@ScheduledAt`. They are evaluated at the phase indicated by each feature. Validate application-controlled expressions before enabling a plugin: the default JEXL resolver is strict and permits unrestricted introspection.

## Time and small helpers

`TimeUnit` includes Minecraft `TICKS` alongside Java-style temporal units. A tick is 50 milliseconds. `toTicks(amount)` uses exact integer arithmetic, truncates fractional ticks toward zero, and throws on overflow. `NumberUtils` formats numeric text; `Tuple` is a small value pair. These utilities do not create scheduler tasks by themselves; use [Scheduling](scheduling.md) for execution.
