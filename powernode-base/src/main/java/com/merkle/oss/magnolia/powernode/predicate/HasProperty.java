package com.merkle.oss.magnolia.powernode.predicate;

import com.merkle.oss.magnolia.powernode.RepositoryExceptionDelegator;

import javax.jcr.Node;
import java.util.function.Predicate;

public class HasProperty<N extends Node> extends RepositoryExceptionDelegator implements Predicate<N> {
	private final String propertyName;

	public HasProperty(final String propertyName) {
		this.propertyName = propertyName;
	}

	@Override
	public boolean test(final N n) {
		return getOrThrow(() -> n.hasProperty(propertyName));
	}
}
