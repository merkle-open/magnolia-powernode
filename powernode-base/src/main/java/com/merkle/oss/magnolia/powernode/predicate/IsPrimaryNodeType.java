package com.merkle.oss.magnolia.powernode.predicate;

import com.merkle.oss.magnolia.powernode.RepositoryExceptionDelegator;

import javax.jcr.Node;
import java.util.function.Predicate;

public class IsPrimaryNodeType<N extends Node> extends RepositoryExceptionDelegator implements Predicate<N> {
	private final String nodeType;

	public IsPrimaryNodeType(final String nodeType) {
		this.nodeType = nodeType;
	}

	@Override
	public boolean test(final N node) {
		return getOrThrow(node::getPrimaryNodeType).isNodeType(nodeType);
	}
}
