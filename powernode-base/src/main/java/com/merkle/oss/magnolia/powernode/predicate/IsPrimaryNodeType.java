package com.merkle.oss.magnolia.powernode.predicate;

import java.util.Set;
import java.util.function.Predicate;

import javax.jcr.Node;

import com.merkle.oss.magnolia.powernode.RepositoryExceptionDelegator;

public class IsPrimaryNodeType<N extends Node> extends RepositoryExceptionDelegator implements Predicate<N> {
    private final Set<String> nodeTypes;

    public IsPrimaryNodeType(final String... nodeTypes) {
		this(Set.of(nodeTypes));
	}
	public IsPrimaryNodeType(final Set<String> nodeTypes) {
        this.nodeTypes = nodeTypes;
    }

	@Override
	public boolean test(final N node) {
		return nodeTypes.stream().anyMatch(nodeType ->
				get(node::getPrimaryNodeType).map(primaryNodeType -> primaryNodeType.isNodeType(nodeType)).orElse(false)
		);
	}
}
