[![Core Current](https://img.shields.io/maven-central/v/com.thewinterframework/core
)](https://central.sonatype.com/artifact/com.thewinterframework/core)


CHECK 2.0.0 BRANCH!!

# ❄️  The Winter Framework ❄️ 
A Modern, heavily inspired by [Spring Boot](https://github.com/spring-projects/spring-boot) and easy-to-use plugin framework for Minecraft Server Software

## What is Winter? 🤔
Winter is a plugin framework for Minecraft Server Software that is heavily inspired by Spring Boot. It is designed to be easy to use and to allow developers to focus on writing their plugins rather than worrying about the boilerplate code that is required to get a plugin up and running.

## Getting Started 👇
To get started with Winter, you will need to add the Winter dependency to your project. You can do this by adding the following to your `build` file:

```kotlin
plugins {
    `java-library` // MAKE SURE YOU HAVE THIS PLUGIN!! to use api scope
}

dependencies {
    api("com.thewinterframework:[PLATFORM]:[VERSION]")
    annotationProcessor("com.thewinterframework:[PLATFORM]:[VERSION]") // THIS IS HIGHLY IMPORTANT
}
```

At the moment the only platform that is supported is `paper`.

## Creating a Winter Plugin 🧩
Creating a Winter plugin is easy! All you need to do is create a class that extends `PaperWinterPlugin` and add the `@WinterBootPlugin` annotation to the class. Here is an example of a simple Winter plugin:

```java
@WinterBootPlugin
public class MyPlugin extends PaperWinterPlugin {}
```

(in the most cases your main class will be like this everytime, wow that's so clean!)

## Creating your first Plugin Module
Winter allows you to create plugin modules that can be loaded, enabled and disabled at runtime. To create a plugin module, you need to create a class that implements `PluginModule` and add the `@ModuleComponent` annotation to the class. Here is an example of a simple plugin module:

```java
@ModuleComponent
public class TestModule implements PluginModule {

    @Override
    public boolean onLoad(WinterPlugin plugin) {
        plugin.getSLF4JLogger().info("TestModule has been loaded!");
        return true;
    }
    
    @Override
    public boolean onEnable(WinterPlugin plugin) {
        plugin.getSLF4JLogger().info("TestModule has been enabled!");
        return true;
    }

    @Override
    public boolean onDisable(WinterPlugin plugin) {
        plugin.getSLF4JLogger().info("TestModule has been disabled!");
        return true;
    }
}
```

other thing that you need to know is that a `PluginModule` extends `Module` class from [Guice](https://github.com/google/guice), this means that you can bind your dependencies in the module!

> [!CAUTION]
> `onLoad` is called before the guice module is installed in plugin injector. `onEnable` and `onDisable` are called after the guice module is installed in plugin injector so you can use `@Inject` / injections in `onEnable` and `onDisable` methods.

> [!NOTE]
> If you don't know what is guice, you can read the [documentation](https://github.com/google/guice/wiki/GettingStarted)

and that's it! you have created your first Plugin Module.

Although in most plugins you won't use it because you can use pre-made modules for [commands](https://github.com/thewinterframework/command), listeners (which is already included in the platform module), [configurations](https://github.com/thewinterframework/configuration) and more!

## Creating your first Service
Winter allows you to create services that can be injected into your plugin modules. To create a service, you need to create a class that is annotated with `@Service`. Here is an example of a simple service:

```java
@Service
public class MyFirstService {
}
```
`@Service` annotation marks your class as Singleton class! <br>

You can use `@Inject` annotation to inject your dependencies in the service class!
```java
@Service
public class MyFirstService {
    
    private final Plugin plugin;
    
    @Inject
    public MyFirstService(Plugin plugin) {
        this.plugin = plugin;
    }
    
    public void doSomething() {
        plugin.getLogger().info("Hello from MyFirstService!");
    }
    
}
```

### Service LifeCycle Annotations
In your service you can annotate your methods with `@OnEnable` and `@OnDisable` annotations to run your methods when the service is enabled or disabled

```java
@Service
public class MyFirstService {
    
    private final Plugin plugin;
    
    @Inject
    public MyFirstService(Plugin plugin) {
        this.plugin = plugin;
    }
    
    @OnEnable
    public void doSomething() {
        plugin.getLogger().info("Hello from MyFirstService!");
    }
}
```
now `doSomething` method will be called when the service is enabled!

LifeCycle methods parameters are injected so you can add parameters to your methods!
```java
@Service
public class MyFirstService {
    @OnEnable
    void hello(Logger logger) {
        logger.info("Hello from MyFirstService!");
    }
    
    @OnDisable
    void goodBye(Logger logger) {
        logger.info("Goodbye from MyFirstService!");
    }
}
```

is it cool!, isn't it?

in a real example you can use lifecycle methods to load data from a file when the service is enabled!

```java
@Service
public class WarpService {

    private final Map<String, Warp> warpCache = new HashMap<>();

    @OnEnable
    void loadWarps(WarpStorage storage, Logger logger) {
        warpCache.putAll(storage.loadWarps());
        logger.info("Warps loaded!");
    }

    @OnDisable
    void clearCache(Logger logger) {
        warpCache.clear();
        logger.info("Warps cleared!");
    }
}
```

### Service Schedulers
You can use `@RepeatingTask` annotation to run your method in a repeating task!

```java
@Service
public class WarpService {

    private final Map<String, Warp> warpCache = new HashMap<>();

    @RepeatingTask(every = 1, unit = TimeUnit.MINUTES, async = true)
    void helloVisitors(MyOtherService broadCastService) {
        broadCastService.broadcast("Hello Visitors!");
    }
    
    @OnEnable
    void loadWarps(WarpStorage storage, Logger logger) {
        warpCache.putAll(storage.loadWarps());
        logger.info("Warps loaded!");
    }

    @OnDisable
    void clearCache(Logger logger) {
        warpCache.clear();
        logger.info("Warps cleared!");
    }
}
```

## Registering your Listener
Winter allows you to create listeners that can be registered with the server. To create a listener, you need to create a class that extends `Listener` and add the `@ListenerComponent` annotation to the class. Here is an example of a simple listener:

```java
@ListenerComponent
public class TestListener extends Listener {

    private final HelloService helloService;
    
    @Inject
    public TestListener(HelloService helloService) {
        this.helloService = helloService;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        helloService.sayHello(event.getPlayer());
    }
}
```

and that's it! you have created your first Listener.

## Using Bukkit API Yaml Files
Winter allows you to use Bukkit API Yaml Files to store your configurations. To use a Bukkit API Yaml File, you need to inject a `YamlConfig` object in your component and use the `@FileName` annotation to specify the name of the file. Here is an example of a simple Yaml File:

```java
@ListenerComponent
public class TestListener extends Listener {

    private final HelloService helloService;
    private final YamlConfig config;
    
    @Inject
    public TestListener(
            HelloService helloService,
            @FileName("config.yml") YamlConfig config
    ) {
        this.helloService = helloService;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        helloService.sayHello(config.getComponent("hello-message"));
    }
}
```

and that's it!, if you use @FileName annotation your Yaml file will be created automatically in the plugin data folder!

Also check out the [SpongePowered Configurate Module](https://github.com/thewinterframework/configuration) for more advanced configuration options!

## Using Commands
You can use the [Winter Command Module](https://github.com/thewinterframework/command) to create auto-registered commands using [incendo cloud](https://github.com/Incendo/cloud)
