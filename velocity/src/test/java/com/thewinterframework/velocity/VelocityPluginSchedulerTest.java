package com.thewinterframework.velocity;

import com.thewinterframework.utils.TimeUnit;
import com.thewinterframework.velocity.scheduler.VelocityPluginScheduler;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class VelocityPluginSchedulerTest {

	@Test
	void convertsTicksAndCancelsByWinterId() {
		final var environment = new VelocityTestEnvironment();
		final var scheduler = new VelocityPluginScheduler(this, environment.server);
		final var count = new AtomicInteger();
		final var id = scheduler.runAtFixedRateSync(count::incrementAndGet, 2, 5, TimeUnit.TICKS);
		final var task = environment.tasks.getFirst();
		assertEquals(Duration.ofMillis(100), task.delay);
		assertEquals(Duration.ofMillis(250), task.repeat);
		task.execute();
		task.execute();
		assertEquals(2, count.get());
		scheduler.cancelTask(id);
		task.execute();
		assertEquals(2, count.get());
	}

	@Test
	void runsSyncAndAsyncVariantsOnTheVelocityScheduler() {
		final var environment = new VelocityTestEnvironment();
		final var scheduler = new VelocityPluginScheduler(this, environment.server);
		final var count = new AtomicInteger();
		scheduler.runAsync(count::incrementAndGet);
		scheduler.runSync(count::incrementAndGet);
		scheduler.runDelayedAsync(count::incrementAndGet, 1, TimeUnit.MINUTES);
		scheduler.runDelayedSync(count::incrementAndGet, 1, TimeUnit.SECONDS);
		scheduler.runAtFixedRateAsync(count::incrementAndGet, 0, 1, TimeUnit.SECONDS);
		scheduler.ensureSync(count::incrementAndGet);
		scheduler.getMainThreadExecutor().execute(count::incrementAndGet);
		assertEquals(0, count.get());
		environment.tasks.forEach(VelocityTestEnvironment.Task::execute);
		assertEquals(7, count.get());
		assertEquals(Duration.ofMinutes(1), environment.tasks.get(2).delay);
		scheduler.cancelAllTasks();
		assertTrue(environment.tasks.get(4).cancelled);
	}

	@Test
	void completesFuturesAndPropagatesSupplierFailures() {
		final var environment = new VelocityTestEnvironment();
		final var scheduler = new VelocityPluginScheduler(this, environment.server);
		final var success = scheduler.getSync(() -> "result");
		final var failure = scheduler.getSync(() -> { throw new IllegalStateException("failure"); });
		assertFalse(success.isDone());
		environment.tasks.forEach(VelocityTestEnvironment.Task::execute);
		assertEquals("result", success.join());
		assertInstanceOf(IllegalStateException.class, assertThrows(CompletionException.class, failure::join).getCause());
	}

	@Test
	void forgetsOneShotTasksEvenWhenTheyFinishBeforeScheduleReturns() {
		final var environment = new VelocityTestEnvironment();
		environment.executeImmediately = true;
		final var scheduler = new VelocityPluginScheduler(this, environment.server);
		final var id = scheduler.runSync(() -> {});
		scheduler.cancelTask(id);
		assertFalse(environment.tasks.getFirst().cancelled);
	}

	@Test
	void onlyCancelsTasksOwnedByThisPlugin() {
		final var environment = new VelocityTestEnvironment();
		final var scheduler = new VelocityPluginScheduler(this, environment.server);
		final var other = new VelocityPluginScheduler(new Object(), environment.server);
		scheduler.runDelayedSync(() -> {}, 1, TimeUnit.SECONDS);
		other.runSync(() -> {});
		scheduler.cancelAllTasks();
		assertTrue(environment.tasks.getFirst().cancelled);
		assertFalse(environment.tasks.getLast().cancelled);
		assertThrows(IllegalArgumentException.class, () -> scheduler.runAtFixedRateAsync(() -> {}, 0, 0, TimeUnit.TICKS));
		assertThrows(IllegalArgumentException.class, () -> scheduler.runDelayedSync(() -> {}, -1, TimeUnit.SECONDS));
	}
}
