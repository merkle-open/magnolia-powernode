package com.merkle.oss.magnolia.powernode.predicate;

import com.merkle.oss.magnolia.powernode.RepositoryExceptionDelegator;
import info.magnolia.jcr.util.NodeTypes;

import javax.jcr.Node;
import java.util.Objects;
import java.util.function.Predicate;

public class IsTemplate<N extends Node> extends RepositoryExceptionDelegator implements Predicate<N> {
	private final String templateId;

	public IsTemplate(final String templateId) {
		this.templateId = templateId;
	}

	@Override
	public boolean test(final N node) {
		return get(() -> node.getProperty(NodeTypes.Renderable.TEMPLATE).getString()).map(templateId::equals).orElse(false);
	}
}
