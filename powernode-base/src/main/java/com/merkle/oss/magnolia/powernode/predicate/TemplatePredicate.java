package com.merkle.oss.magnolia.powernode.predicate;

import com.merkle.oss.magnolia.powernode.AbstractPowerNode;

import java.util.function.Predicate;

public class TemplatePredicate<N extends AbstractPowerNode<N>> implements Predicate<N> {
	private final String templateId;

	public TemplatePredicate(final String templateId) {
		this.templateId = templateId;
	}

	@Override
	public boolean test(final N node) {
		return node.getTemplate().map(templateId::equals).orElse(false);
	}
}
