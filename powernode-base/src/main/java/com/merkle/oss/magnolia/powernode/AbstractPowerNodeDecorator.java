package com.merkle.oss.magnolia.powernode;

import info.magnolia.jcr.decoration.AbstractContentDecorator;

import javax.annotation.Nullable;
import javax.jcr.Node;
import javax.jcr.NodeIterator;

public abstract class AbstractPowerNodeDecorator<N extends AbstractPowerNode<N>> extends AbstractContentDecorator {

	protected abstract N wrapNodeInternal(final Node node);
	protected abstract Node unwrapNodeInternal(final Node node);

	@Override
	@Nullable
	public N wrapNode(@Nullable final Node node) {
		if(node == null) {
			return null;
		}
		if(node instanceof AbstractPowerNode) {
			return (N) node;
		}
		return wrapNodeInternal(unwrap(node));
	}

	private Node unwrap(final Node node) {
		if(isDecorating(node)) {
			return unwrapNodeInternal(node);
		}
		return node;
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
