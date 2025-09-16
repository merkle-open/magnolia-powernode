package com.merkle.oss.magnolia.powernode;

import info.magnolia.jcr.decoration.AbstractContentDecorator;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.wrapper.I18nNodeWrapper;

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
		return wrapNodeInternal(unwrap(unwrapI18n(node)));
	}

	private Node unwrap(final Node node) {
        if(isDecorating(node)) {
			return unwrapNodeInternal(node);
		}
		return node;
	}

    protected Node unwrapI18n(final Node node) {
        if (NodeUtil.isWrappedWith(node, I18nNodeWrapper.class)) {
            return NodeUtil.deepUnwrapAll(node, I18nNodeWrapper.class);
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
