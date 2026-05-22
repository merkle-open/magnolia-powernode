package com.merkle.oss.magnolia.powernode.predicate;

import java.util.Set;
import java.util.function.Predicate;

public class NonePredicate<V> implements Predicate<V> {
	private final Predicate<V> predicate;

	public NonePredicate(final Predicate<V>... predicates) {
		this(Set.of(predicates));
	}
	public NonePredicate(final Set<Predicate<V>> predicates) {
		predicate = new AnyPredicate<>(predicates).negate();
	}

	@Override
	public boolean test(final V value) {
		return predicate.test(value);
	}
}
