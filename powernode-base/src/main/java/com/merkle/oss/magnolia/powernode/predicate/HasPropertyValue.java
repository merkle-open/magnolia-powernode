package com.merkle.oss.magnolia.powernode.predicate;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import com.merkle.oss.magnolia.powernode.AbstractPowerNode;
import com.merkle.oss.magnolia.powernode.PropertyService;

public class HasPropertyValue<N extends AbstractPowerNode<N>, T> implements Predicate<N> {
	private final Function<N, Optional<T>> provider;
	private final Set<T> values;

	public HasPropertyValue(final String propertyName, final PropertyService.PropertyProvider<T> provider, final T... values) {
		this(propertyName, provider, Set.of(values));
	}
	public HasPropertyValue(final String propertyName, final PropertyService.PropertyProvider<T> provider, final Set<T> values) {
		this.values = values;
		this.provider = n -> n.getProperty(propertyName, provider);
	}

	public HasPropertyValue(final String propertyName, final Locale locale, final PropertyService.PropertyProvider<T> provider, final T... values) {
		this(propertyName, locale, provider, Set.of(values));
	}
	public HasPropertyValue(final String propertyName, final Locale locale, final PropertyService.PropertyProvider<T> provider, final Set<T> values) {
		this.values = values;
		this.provider = n -> n.getProperty(propertyName, locale, provider);
	}

	@Override
	public boolean test(final N n) {
		return provider.apply(n).map(values::contains).orElse(false);
	}
}
