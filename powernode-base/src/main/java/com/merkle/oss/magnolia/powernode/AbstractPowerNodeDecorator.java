package com.merkle.oss.magnolia.powernode;

import info.magnolia.jcr.decoration.AbstractContentDecorator;

import javax.annotation.Nullable;
import javax.jcr.Node;
import javax.jcr.NodeIterator;

public abstract class AbstractPowerNodeDecorator<N extends AbstractPowerNode<N>> extends AbstractContentDecorator {

	protected abstract N wrapNodeInternal(final Node node);

	@Override
	@Nullable
	public N wrapNode(@Nullable Node node) {
		if(node != null && !isDecorating(node)) {
			return wrapNodeInternal(node);
		}
		return (N)node;
	}

	@Override
	public PowerNodeIterator<N> wrapNodeIterator(final NodeIterator nodeIterator) {
		return new PowerNodeIterator<>(this, nodeIterator);
	}

	@Override
	public boolean isMultipleWrapEnabled() {
		return false;
	}
}
