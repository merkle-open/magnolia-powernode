package com.merkle.oss.magnolia.powernode;

import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import javax.jcr.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class PropertyService {
	private final ValueConverter.Factory valueConverterFactory;

	@Inject
	public PropertyService(final ValueConverter.Factory valueConverterFactory) {
		this.valueConverterFactory = valueConverterFactory;
	}

	<T> Property setProperty(final Node node, final String propertyName, final T value, final ValueFactory<T> valueFactory) throws RepositoryException {
		return node.setProperty(propertyName, valueFactory.create(getValueConverter(node), value).orElse(null));
	}

	<T> Property setMultiProperty(final Node node, final String propertyName, final Iterable<T> values, final ValueFactory<T> valueFactory) throws RepositoryException {
		final List<Value> list = new ArrayList<>();
		final ValueConverter valueConverter = getValueConverter(node);
		for (T value : values) {
			valueFactory.create(valueConverter, value).ifPresent(list::add);
		}
		return node.setProperty(propertyName, list.toArray(new Value[0]));
	}

	Optional<Property> removeProperty(final Node node, final String propertyName) throws RepositoryException {
		@Nullable
		final Property property = getProperty(node, propertyName).orElse(null);
		if(property != null) {
			property.remove();
		}
		return Optional.ofNullable(property);
	}

	<T> Optional<T> getProperty(final Node node, final String propertyName, final PropertyProvider<T> provider) throws RepositoryException {
		@Nullable
		final Property property = getProperty(node, propertyName).orElse(null);
		if(property != null) {
			return provider.get(getValueConverter(node), property.getValue());
		}
		return Optional.empty();
	}

	<T> Stream<T> streamMultiProperty(final Node node, final String propertyName, final PropertyProvider<T> provider) throws RepositoryException {
		@Nullable
		final Property property = getProperty(node, propertyName).orElse(null);
		if(property != null) {
			final Stream.Builder<T> values = Stream.builder();
			if (property.isMultiple()) {
				for (Value value : property.getValues()) {
					provider.get(getValueConverter(node), value).ifPresent(values::add);
				}
			}
			return values.build();
		}
		return Stream.empty();
	}

	private Optional<Property> getProperty(final Node node, final String propertyName) throws RepositoryException {
		try {
			return Optional.of(node.getProperty(propertyName));
		} catch (PathNotFoundException e) {
			return Optional.empty();
		}
	}

	private ValueConverter getValueConverter(final Node node) throws RepositoryException {
		return valueConverterFactory.create(node.getSession().getValueFactory());
	}

	public interface PropertyProvider<T> {
		Optional<T> get(ValueConverter valueConverter, Value value) throws RepositoryException;
	}

	public interface ValueFactory<T> {
		Optional<Value> create(ValueConverter valueConverter, T property) throws RepositoryException;
	}
}
