package com.merkle.oss.magnolia.powernode;

import org.apache.jackrabbit.value.ValueFactoryImpl;

import javax.annotation.Nullable;
import javax.inject.Provider;
import javax.jcr.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

public class ValueConverter {
	private final ValueFactory factory;
	private final Provider<ZoneId> zoneIdProvider;

	public ValueConverter(final ValueFactory factory, final Provider<ZoneId> zoneIdProvider) {
		this.factory = factory;
		this.zoneIdProvider = zoneIdProvider;
	}

	public Optional<Value> toValue(@Nullable final BigDecimal value) {
		return Optional.ofNullable(value).map(factory::createValue);
	}

	public Optional<Value> toValue(@Nullable final String value) {
		return Optional.ofNullable(value).map(factory::createValue);
	}

	public Optional<Value> toValue(@Nullable final Double value) {
		return Optional.ofNullable(value).map(factory::createValue);
	}

	public Optional<Value> toValue(@Nullable final Long value) {
		return Optional.ofNullable(value).map(factory::createValue);
	}

	public Optional<Value> toValue(@Nullable final Integer value) {
		return Optional.ofNullable(value).map(factory::createValue);
	}

	public Optional<Value> toValue(@Nullable final Boolean value) {
		return Optional.ofNullable(value).map(factory::createValue);
	}

	public Optional<Value> toValue(@Nullable final Date value) {
		return Optional.ofNullable(value)
				.map(date -> {
					final Calendar calendar = Calendar.getInstance();
					calendar.setTime(value);
					return calendar;
				})
				.map(factory::createValue);
	}

	public Optional<Value> toValue(@Nullable final LocalDate value) {
		return Optional.ofNullable(value)
				.map(localDate -> localDate.atStartOfDay(zoneIdProvider.get()))
				.map(ZonedDateTime::toInstant)
				.map(Date::from)
				.flatMap(this::toValue);
	}

	public Optional<Value> toValue(@Nullable final LocalDateTime value) {
		return Optional.ofNullable(value)
				.map(localDateTime -> localDateTime.atZone(zoneIdProvider.get()))
				.map(ZonedDateTime::toInstant)
				.map(Date::from)
				.flatMap(this::toValue);
	}

	public Optional<Value> toValue(@Nullable final Binary value) {
		return Optional.ofNullable(value).map(factory::createValue);
	}


	public Optional<BigDecimal> getDecimal(final Value value) throws RepositoryException {
		return getPropertyOptional(value::getDecimal);
	}

	public Optional<String> getString(final Value value) throws RepositoryException {
		return getPropertyOptional(value::getString);
	}

	public Optional<Double> getDouble(final Value value) throws RepositoryException {
		return getPropertyOptional(value::getDouble);
	}

	public Optional<Long> getLong(final Value value) throws RepositoryException {
		return getPropertyOptional(value::getLong);
	}

	public Optional<Integer> getInteger(final Value value) throws RepositoryException {
		return getLong(value).map(Long::intValue);
	}

	public Optional<Boolean> getBoolean(final Value value) throws RepositoryException {
		return getPropertyOptional(value::getBoolean);
	}

	public Optional<Date> getDate(final Value value) throws RepositoryException {
		return getPropertyOptional(() -> value.getDate().getTime());
	}

	public Optional<LocalDate> getLocalDate(final Value value) throws RepositoryException {
		return getPropertyOptional(() ->
				LocalDate.ofInstant(Instant.ofEpochMilli(value.getDate().getTimeInMillis()), zoneIdProvider.get())
		);
	}

	public Optional<LocalDateTime> getLocalDateTime(final Value value) throws RepositoryException {
		return getPropertyOptional(() ->
				LocalDateTime.ofInstant(Instant.ofEpochMilli(value.getDate().getTimeInMillis()), zoneIdProvider.get())
		);
	}

	public Optional<Binary> getBinary(final Value value) throws RepositoryException {
		return getPropertyOptional(() ->
				ValueFactoryImpl.getInstance().createBinary(value.getBinary().getStream())
		);
	}

	private <T> Optional<T> getPropertyOptional(final PathNotFoundProvider<T> provider) throws RepositoryException {
		try {
			return Optional.ofNullable(provider.get());
		} catch (ValueFormatException e) {
			return Optional.empty();
		}
	}

	interface PathNotFoundProvider<T> {
		T get() throws RepositoryException;
	}

	public interface Factory {
		ValueConverter create(final ValueFactory valueFactory);
	}
}
