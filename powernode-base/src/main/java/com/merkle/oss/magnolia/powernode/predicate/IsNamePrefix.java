package com.merkle.oss.magnolia.powernode.predicate;

import com.merkle.oss.magnolia.powernode.RepositoryExceptionDelegator;

import javax.jcr.Node;
import java.util.function.Predicate;

public class IsNamePrefix<N extends Node> extends RepositoryExceptionDelegator implements Predicate<N> {
	private final String namePrefix;

	public IsNamePrefix(final String namePrefix) {
		this.namePrefix = namePrefix;
	}

	@Override
	public boolean test(final N node) {
		return getOrThrow(node::getName).startsWith(namePrefix);
	}
}
