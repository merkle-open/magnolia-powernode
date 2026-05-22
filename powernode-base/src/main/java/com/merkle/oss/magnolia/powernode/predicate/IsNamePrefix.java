package com.merkle.oss.magnolia.powernode.predicate;

import com.merkle.oss.magnolia.powernode.RepositoryExceptionDelegator;

import javax.jcr.Node;

import java.util.Set;
import java.util.function.Predicate;

public class IsNamePrefix<N extends Node> extends RepositoryExceptionDelegator implements Predicate<N> {
	private final Set<String> namePrefixes;

	public IsNamePrefix(final String... namePrefixes) {
		this(Set.of(namePrefixes));
	}
	public IsNamePrefix(final Set<String> namePrefixes) {
		this.namePrefixes = namePrefixes;
	}

	@Override
	public boolean test(final N node) {
		return namePrefixes.stream().anyMatch(namePrefix ->
				get(node::getName).map(name -> name.startsWith(namePrefix)).orElse(false)
		);
	}
}
