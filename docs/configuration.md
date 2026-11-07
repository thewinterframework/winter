# Configuration

Winter has two distinct configuration layers: compile-time framework settings in `winter.properties`, and runtime plugin data such as YAML files.

## Compile-time `winter.properties`

Place this file at `src/main/resources/winter.properties` **in each Gradle module that needs settings**. The annotation processors read the current module's file. A child module does not inherit a parent Gradle project's file. Winter does not load the source file as runtime configuration.

```properties
winter.project.type=IMPL
winter.plugin.package=com.example.plugin
winter.plugin.name=ExamplePlugin
winter.expose.bypass-api=false
winter.debug=false
```

| Key | Default | Meaning |
| --- | --- | --- |
| `winter.project.type` | `IMPL` | `API` runs the API generator without the implementation processor; `IMPL` builds plugin or implementation metadata. |
| `winter.plugin.package` | `com.example.api` | Package of the generated API interface in an API module. Set explicitly for a published API. |
| `winter.plugin.name` | `Default` | Prefix of the generated `<Name>API` interface. Set explicitly for a published API. |
| `winter.expose.bypass-api` | `false` | In a single-module plugin, derive the generated API name/package from its `@WinterBootPlugin` class. Requires that boot class in the same compilation. |
| `winter.debug` | `false` | Generate a plugin-specific debug marker. When true, routine Winter messages appear at INFO; otherwise they use DEBUG. Error logs remain visible. |

The old annotation processor options (`plugin.type`, `plugin.package`, `plugin.name`, and `bypass.api`) remain as fallback values for migration. Prefer the file. `-parameters` is a Java compiler feature and cannot be replaced by this file; add that compiler flag if another library needs Java parameter names.

Gradle normally copies all resources into the final artifact. Exclude this compile-time file explicitly:

```kotlin
tasks.processResources {
  exclude("winter.properties")
}
```

## Runtime YAML on Paper

`YamlConfig.configuration(plugin, "messages")` loads `messages.yml` from the plugin data directory. If it does not exist, Winter copies the same resource from the plugin jar first. `load()` reloads it and `save()` writes it back. `YamlConfig.create(path)` creates an instance without loading it; call `load()` after ensuring the file exists.

The `@FileName("messages")` Guice qualifier requests an automatically generated `YamlConfig` binding:

```java
@Inject
@FileName("messages")
private YamlConfig messages;
```

The YAML annotation processor emits one binding per file name it sees on fields or parameters. Include `messages.yml` as a plugin resource before first launch. `YamlConfig.configuration` also adds that configuration to Winter's expression resolver under the chosen name. See [Paper integration](paper-and-yaml.md) and [Text, expressions, and utilities](utilities.md).
