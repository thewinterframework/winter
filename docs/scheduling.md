# Scheduling

`WinterPlugin.getScheduler()` returns `PluginScheduler`. Paper implements it through Bukkit's scheduler; Velocity uses its proxy scheduler. Both cancel plugin tasks during disable. Velocity has no main scheduler thread, so its sync and async scheduling variants both use Velocity's asynchronous scheduler. `ensureSync`, `getSync`, and `getMainThreadExecutor` execute on the caller's thread on Velocity. See [Velocity integration](velocity.md).

## Direct scheduler API

| Method family | Purpose |
| --- | --- |
| `runSync`, `runAsync` | Submit one task immediately; return its task ID. |
| `runDelayedSync`, `runDelayedAsync` | Submit once after a delay. |
| `runAtFixedRateSync`, `runAtFixedRateAsync` | Submit a repeating task with an initial delay and interval. |
| `cancelTask`, `cancelAllTasks` | Cancel scheduled work. |
| `ensureSync` | Run immediately on the primary thread or schedule a sync task otherwise. |
| `getSync` | Return a `CompletableFuture<T>` for a supplier executed on the primary thread. |
| `getMainThreadExecutor` | Return an `Executor` for main-thread work. |

Use the synchronous variants for Bukkit/Paper state. `getSync` completes exceptionally if the supplier or scheduling fails. Do not block the server thread waiting for a future that requires the same thread.

```java
final int taskId = plugin.getScheduler().runDelayedSync(
    () -> refreshScoreboard(), 20, TimeUnit.TICKS);
```

## Timed component methods

Annotate a method on a component. Values are strings because Winter resolves them as expressions when the plugin enables:

```java
@Component
public final class MaintenanceTasks {
  @RepeatingTask(every = "5", delay = "1", unit = "SECONDS")
  public void refresh() {
  }

  @ScheduledAt(hour = "3", minute = "30")
  public void nightlyCleanup() {
  }
}
```

`@RepeatingTask` defaults to zero delay, `MILLIS`, and synchronous execution. Set `async = "true"` only for work safe off the main thread. `@ScheduledAt` is repeatable; its handler checks once per second in the plugin's `getZoneId()`. If any schedule on one method is asynchronous, that method's time check runs asynchronously. Scheduled method failures are logged. Component source scanning for Bukkit/Paper references controls lifecycle loading, not the scheduler's explicit `async` option.

## Time units

Winter's `TimeUnit` implements `TemporalUnit`. `TICKS` means 50 milliseconds; `TimeUnit.SECONDS.toTicks(1)` returns 20. Conversion truncates fractional ticks toward zero and throws on an out-of-range `long` result. Other units delegate temporal operations to Java's `ChronoUnit`. For recurring Paper tasks, choose a positive interval after conversion to ticks.
