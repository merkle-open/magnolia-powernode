package com.merkle.oss.magnolia.powernode.predicate.magnolia;

import info.magnolia.jcr.util.NodeTypes;

import java.util.Set;
import java.util.function.Predicate;

import com.merkle.oss.magnolia.powernode.AbstractPowerNode;

public class IsTemplate<N extends AbstractPowerNode<N>> implements Predicate<N> {
	private final Set<String> templateIds;

	public IsTemplate(final String... templateIds) {
		this(Set.of(templateIds));
	}
	public IsTemplate(final Set<String> templateIds) {
        this.templateIds = templateIds;
    }

	@Override
	public boolean test(final N node) {
		return node.get(NodeTypes.Renderable::getTemplate).map(templateIds::contains).orElse(false);
	}
}
