package com.thewinterframework.utils;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeUnitTest {

	@Test
	void delegatesStandardUnitsWithoutRecursion() {
		final var start = LocalDateTime.of(2026, 1, 1, 0, 0);
		assertTrue(TimeUnit.SECONDS.isSupportedBy(start));
		assertEquals(start.plusSeconds(2), TimeUnit.SECONDS.addTo(start, 2));
		assertEquals(2, TimeUnit.SECONDS.between(start, start.plusSeconds(2)));
	}

	@Test
	void supportsMinecraftTicksOnInstant() {
		final var start = Instant.EPOCH;
		assertEquals(start.plusMillis(100), TimeUnit.TICKS.addTo(start, 2));
		assertEquals(2, TimeUnit.TICKS.between(start, start.plusMillis(100)));
	}

	@Test
	void convertsSubMillisecondUnitsAndRejectsOverflow() {
		assertEquals(1, TimeUnit.NANOS.toTicks(50_000_000));
		assertEquals(1, TimeUnit.MICROS.toTicks(50_000));
		assertThrows(ArithmeticException.class, () -> TimeUnit.YEARS.toTicks(Long.MAX_VALUE));
	}
}
