package com.merkle.oss.magnolia.powernode.predicate;

import java.util.Set;
import java.util.function.Predicate;

public class AnyPredicate<V> implements Predicate<V> {
	private final Set<Predicate<V>> predicates;

	public AnyPredicate(final Predicate<V>... predicates) {
		this(Set.of(predicates));
	}
	public AnyPredicate(final Set<Predicate<V>> predicates) {
		this.predicates = predicates;
	}

	@Override
	public boolean test(final V value) {
		return predicates.stream().anyMatch(predicate -> predicate.test(value));
	}
}
