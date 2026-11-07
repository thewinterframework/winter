package com.thewinterframework.velocity;

import com.thewinterframework.utils.TimeUnit;
import com.thewinterframework.velocity.scheduler.VelocityPluginScheduler;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import com.velocitypowered.api.scheduler.Scheduler;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VelocityPluginSchedulerTest {

  @Test
  void convertsDelaysAndCancelsTasksByWinterId() {
    final var canceled = new AtomicBoolean();
    final var delay = new AtomicReference<Duration>();
    final ScheduledTask task = proxy(ScheduledTask.class, (method, args) -> {
      if (method.equals("cancel")) {
        canceled.set(true);
        return null;
      }
      throw new UnsupportedOperationException(method);
    });
    final var builderRef = new AtomicReference<Scheduler.TaskBuilder>();
    final Scheduler.TaskBuilder builder = proxy(Scheduler.TaskBuilder.class, (method, args) -> switch (method) {
      case "delay" -> {
        delay.set((Duration) args[0]);
        yield builderRef.get();
      }
      case "schedule" -> task;
      default -> throw new UnsupportedOperationException(method);
    });
    builderRef.set(builder);
    final Scheduler velocityScheduler = proxy(Scheduler.class, (method, args) -> switch (method) {
      case "buildTask" -> builder;
      case "tasksByPlugin" -> List.of(task);
      default -> throw new UnsupportedOperationException(method);
    });
    final ProxyServer server = proxy(ProxyServer.class, (method, args) -> {
      if (method.equals("getScheduler")) {
        return velocityScheduler;
      }
      throw new UnsupportedOperationException(method);
    });
    final var scheduler = new VelocityPluginScheduler(server, new Object());

    final int taskId = scheduler.runDelayedSync(() -> { }, 2, TimeUnit.SECONDS);
    assertEquals(Duration.ofSeconds(2), delay.get());
    assertFalse(canceled.get());

    scheduler.cancelTask(taskId);
    assertTrue(canceled.get());
    scheduler.cancelAllTasks();
    assertThrows(IllegalStateException.class, () -> scheduler.runAsync(() -> { }));
  }

  private static <T> T proxy(final Class<T> type, final Invocation invocation) {
    return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
        (instance, method, args) -> invocation.call(method.getName(), args)));
  }

  @FunctionalInterface
  private interface Invocation {
    Object call(String method, Object[] args);
  }
}
