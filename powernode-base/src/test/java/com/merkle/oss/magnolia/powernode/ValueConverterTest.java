package com.merkle.oss.magnolia.powernode;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;

import javax.jcr.Binary;
import javax.jcr.RepositoryException;

import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.commons.SimpleValueFactory;
import org.apache.jackrabbit.util.ISO8601;
import org.apache.jackrabbit.value.BinaryImpl;
import org.apache.jackrabbit.value.StringValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.inject.Provider;

class ValueConverterTest {
	private final Provider<ZoneId> zoneIdProvider = ZoneId::systemDefault;
	private ValueConverter valueConverter;

	@BeforeEach
	void setUp() {
		valueConverter = new ValueConverter(new SimpleValueFactory(), zoneIdProvider);
	}


	@Test
	void convertDecimal() {
		final BigDecimal input = new BigDecimal(42);
		assertEquals(
				Optional.of(input),
				valueConverter.toValue(input).flatMap(value -> getOrThrow(() -> valueConverter.getDecimal(value)))
		);
	}

	@Test
	void getDecimal() throws RepositoryException {
		assertTrue(valueConverter.getDecimal(new StringValue("noDecimal")).isEmpty());
		assertEquals(
				Optional.of(new BigDecimal(42)),
				valueConverter.getDecimal(new StringValue("42"))
		);
	}

	@Test
	void convertString() {
		final String input = "someString";
		assertEquals(
				Optional.of(input),
				valueConverter.toValue(input).flatMap(value -> getOrThrow(() -> valueConverter.getString(value)))
		);
	}

	@Test
	void getString() throws RepositoryException {
		assertEquals(
				Optional.of("someString"),
				valueConverter.getString(new StringValue("someString"))
		);
	}

	@Test
	void convertDouble() {
		final double input = 42.0;
		assertEquals(
				Optional.of(input),
				valueConverter.toValue(input).flatMap(value -> getOrThrow(() -> valueConverter.getDouble(value)))
		);
	}

	@Test
	void getDouble() throws RepositoryException {
		assertTrue(valueConverter.getDouble(new StringValue("noDouble")).isEmpty());
		assertEquals(
				Optional.of(42.42),
				valueConverter.getDouble(new StringValue("42.42"))
		);
	}

	@Test
	void convertLong() {
		final long input = 42L;
		assertEquals(
				Optional.of(input),
				valueConverter.toValue(input).flatMap(value -> getOrThrow(() -> valueConverter.getLong(value)))
		);
	}

	@Test
	void getLong() throws RepositoryException {
		assertTrue(valueConverter.getLong(new StringValue("noLong")).isEmpty());
		assertEquals(
				Optional.of(42L),
				valueConverter.getLong(new StringValue("42"))
		);
	}

	@Test
	void convertInteger() {
		final int input = 42;
		assertEquals(
				Optional.of(input),
				valueConverter.toValue(input).flatMap(value -> getOrThrow(() -> valueConverter.getInteger(value)))
		);
	}

	@Test
	void getInteger() throws RepositoryException {
		assertTrue(valueConverter.getInteger(new StringValue("noInteger")).isEmpty());
		assertEquals(
				Optional.of(42),
				valueConverter.getInteger(new StringValue("42"))
		);
	}

	@Test
	void convertBoolean() {
		final boolean input = true;
		assertEquals(
				Optional.of(input),
				valueConverter.toValue(input).flatMap(value -> getOrThrow(() -> valueConverter.getBoolean(value)))
		);
	}

	@Test
	void getBoolean() throws RepositoryException {
		assertEquals(
				Optional.of(true),
				valueConverter.getBoolean(new StringValue("true"))
		);
	}

	@Test
	void convertDate() {
		final Date input = new Date();
		assertEquals(
				Optional.of(input),
				valueConverter.toValue(input).flatMap(value -> getOrThrow(() -> valueConverter.getDate(value)))
		);
	}

	@Test
	void getDate() throws RepositoryException {
		assertTrue(valueConverter.getDate(new StringValue("noDate")).isEmpty());
		final ZonedDateTime zonedDateTime = LocalDateTime.of(2023, Month.NOVEMBER, 6, 7, 45).atZone(zoneIdProvider.get());
		assertEquals(
				Optional.of(new Date(zonedDateTime.toInstant().toEpochMilli())),
				valueConverter.getDate(new StringValue(ISO8601.format(zonedDateTime.toInstant().toEpochMilli())))
		);
	}

	@Test
	void convertInstant() {
		final Instant input = LocalDateTime.of(2023, Month.NOVEMBER, 6, 7, 45).atZone(zoneIdProvider.get()).toInstant();
		assertEquals(
				Optional.of(input),
				valueConverter.toValue(input).flatMap(value -> getOrThrow(() -> valueConverter.getInstant(value)))
		);
	}

	@Test
	void getInstant() throws RepositoryException {
		assertTrue(valueConverter.getInstant(new StringValue("noDate")).isEmpty());
		final ZonedDateTime zonedDateTime = LocalDateTime.of(2023, Month.NOVEMBER, 6, 7, 45).atZone(zoneIdProvider.get());
		assertEquals(
				Optional.of(zonedDateTime.toInstant()),
				valueConverter.getInstant(new StringValue(ISO8601.format(zonedDateTime.toInstant().toEpochMilli())))
		);
	}

	@Test
	void convertZonedDateTime() {
		final ZonedDateTime input = LocalDateTime.of(2023, Month.NOVEMBER, 6, 7, 45).atZone(zoneIdProvider.get());
		assertEquals(
				Optional.of(input),
				valueConverter.toValue(input).flatMap(value -> getOrThrow(() -> valueConverter.getZonedDateTime(value)))
		);
	}

	@Test
	void getZonedDateTime() throws RepositoryException {
		assertTrue(valueConverter.getZonedDateTime(new StringValue("noDate")).isEmpty());
		final ZonedDateTime zonedDateTime = LocalDateTime.of(2023, Month.NOVEMBER, 6, 7, 45).atZone(zoneIdProvider.get());
		assertEquals(
				Optional.of(zonedDateTime),
				valueConverter.getZonedDateTime(new StringValue(ISO8601.format(zonedDateTime.toInstant().toEpochMilli())))
		);
	}

	@Test
	void convertLocalDate() {
		final LocalDate input = LocalDate.of(2023, Month.NOVEMBER, 6);
		assertEquals(
				Optional.of(input),
				valueConverter.toValue(input).flatMap(value -> getOrThrow(() -> valueConverter.getLocalDate(value)))
		);
	}

	@Test
	void getLocalDate() throws RepositoryException {
		assertTrue(valueConverter.getLocalDate(new StringValue("noDate")).isEmpty());
		final LocalDateTime localDateTime = LocalDateTime.of(2023, Month.NOVEMBER, 6, 7, 45);
		assertEquals(
				Optional.of(localDateTime.toLocalDate()),
				valueConverter.getLocalDate(new StringValue(ISO8601.format(localDateTime.atZone(zoneIdProvider.get()).toInstant().toEpochMilli())))
		);
	}

	@Test
	void convertLocalDateTime() {
		final LocalDateTime input = LocalDateTime.of(2023, Month.NOVEMBER, 6, 7, 45);
		assertEquals(
				Optional.of(input),
				valueConverter.toValue(input).flatMap(value -> getOrThrow(() -> valueConverter.getLocalDateTime(value)))
		);
	}

	@Test
	void getLocalDateTime() throws RepositoryException {
		assertTrue(valueConverter.getLocalDateTime(new StringValue("noDate")).isEmpty());
		final LocalDateTime localDateTime = LocalDateTime.of(2023, Month.NOVEMBER, 6, 7, 45);
		assertEquals(
				Optional.of(localDateTime),
				valueConverter.getLocalDateTime(new StringValue(ISO8601.format(localDateTime.atZone(zoneIdProvider.get()).toInstant().toEpochMilli())))
		);
	}

	@Test
	void convertBinary() throws IOException {
		final Binary input = new BinaryImpl(IOUtils.toInputStream("test", StandardCharsets.UTF_8));
		assertEquals(
				Optional.of("test"),
				valueConverter.toValue(input)
						.flatMap(value -> getOrThrow(() -> valueConverter.getBinary(value)))
						.map(binary -> getOrThrow(() -> IOUtils.toString(binary.getStream(), StandardCharsets.UTF_8)))
		);
	}

	@Test
	void getBinary() throws RepositoryException {
		assertEquals(
				Optional.of("someString"),
				valueConverter
						.getBinary(new StringValue("someString"))
						.map(binary -> getOrThrow(() -> IOUtils.toString(binary.getStream(), StandardCharsets.UTF_8)))
		);
	}

	private <T> T getOrThrow(final ExceptionalProvider<T> provider) {
		try {
			return provider.get();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private interface ExceptionalProvider<T> {
		T get() throws Exception;
	}
}
