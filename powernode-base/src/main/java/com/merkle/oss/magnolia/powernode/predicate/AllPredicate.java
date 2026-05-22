package com.merkle.oss.magnolia.powernode.predicate;

import java.util.Set;
import java.util.function.Predicate;

public class AllPredicate<V> implements Predicate<V> {
	private final Set<Predicate<V>> predicates;

	public AllPredicate(final Predicate<V>... predicates) {
		this(Set.of(predicates));
	}
	public AllPredicate(final Set<Predicate<V>> predicates) {
		this.predicates = predicates;
	}

	@Override
	public boolean test(final V value) {
		return predicates.stream().allMatch(predicate -> predicate.test(value));
	}
}
