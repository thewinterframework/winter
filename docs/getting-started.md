# Getting started

Winter 3 targets Java 21 and Paper. The repository publishes `core` and `paper`; the Paper artifact exposes the core API. Use the same Winter version for ordinary compilation and annotation processing.

## Gradle setup

```kotlin
plugins {
  java
}

repositories {
  mavenCentral()
  maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
  api("com.thewinterframework:paper:3.0.0")
  annotationProcessor("com.thewinterframework:paper:3.0.0")
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.processResources {
  exclude("winter.properties")
}
```

The dependency must also be available to the server at runtime, either through your plugin packaging or the server's plugin dependency setup. `annotationProcessor` is required: Winter creates the discovery classes that startup loads.

## Boot class

```java
import com.thewinterframework.paper.PaperWinterPlugin;
import com.thewinterframework.plugin.WinterBootPlugin;

@WinterBootPlugin
public final class ExamplePlugin extends PaperWinterPlugin {
  @Override
  public void onPluginEnable() {
    getSLF4JLogger().info("Example is ready");
  }
}
```

Set `main` in `plugin.yml` to the fully qualified boot class. `PaperWinterPlugin` owns the final Bukkit `onLoad`, `onEnable`, and `onDisable` methods. Override `onPluginLoad`, `onPluginEnable`, and `onPluginDisable` for your plugin logic. Guice injection is available only after the injector has been created during the enable path; see [Lifecycle and modules](lifecycle.md).

## First component

```java
import com.google.inject.Inject;
import com.thewinterframework.component.annotation.Component;

@Component
public final class ProfileService {
  private final ProfileRepository repository;

  @Inject
  public ProfileService(final ProfileRepository repository) {
    this.repository = repository;
  }
}
```

Winter discovers the class at compile time and binds it as a Guice singleton. An interface such as `ProfileRepository` needs a binding from an [exposed external component](expose-and-multi-module.md), a [provider or `@Primary` implementation](dependency-injection.md), or your own Guice module.

Components run independently on virtual threads by default. Add `@SyncLoading` when the component touches Paper/Bukkit state, including through another service; see [Components](components.md).

## Build and verify

Run `./gradlew clean build` (`gradlew.bat clean build` on Windows). If startup cannot find a generated module provider, check the annotation processor dependency and `@WinterBootPlugin`; see [Troubleshooting](troubleshooting.md).
