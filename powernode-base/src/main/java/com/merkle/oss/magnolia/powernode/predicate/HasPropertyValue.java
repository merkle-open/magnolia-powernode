package com.merkle.oss.magnolia.powernode.predicate;

import com.merkle.oss.magnolia.powernode.AbstractPowerNode;
import com.merkle.oss.magnolia.powernode.PropertyService;

import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class HasPropertyValue<N extends AbstractPowerNode<N>, T> implements Predicate<N> {
	private final Function<N, Optional<T>> provider;
	private final T value;

	public HasPropertyValue(final String propertyName, final PropertyService.PropertyProvider<T> provider, final T value) {
		this.value = value;
		this.provider = n -> n.getProperty(propertyName, provider);
	}

	public HasPropertyValue(final String propertyName, final Locale locale, final PropertyService.PropertyProvider<T> provider, final T value) {
		this.value = value;
		this.provider = n -> n.getProperty(propertyName, locale, provider);
	}

	@Override
	public boolean test(final N n) {
		return provider.apply(n).map(value::equals).orElse(false);
	}
}
