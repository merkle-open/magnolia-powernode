package com.merkle.oss.magnolia.powernode.predicate;

import java.util.function.Predicate;

import com.merkle.oss.magnolia.powernode.AbstractPowerNode;

public class ParentNodeDelegatePredicate<N extends AbstractPowerNode<N>> implements Predicate<N> {
	private final Predicate<N> predicate;

	public ParentNodeDelegatePredicate(final Predicate<N> predicate) {
		this.predicate = predicate;
	}

	@Override
	public boolean test(final N node) {
		return node.getParentOptional().map(predicate::test).orElse(false);
	}
}
