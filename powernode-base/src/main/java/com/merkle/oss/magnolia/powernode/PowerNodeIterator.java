package com.merkle.oss.magnolia.powernode;

import org.apache.jackrabbit.commons.iterator.NodeIteratorAdapter;

import javax.jcr.NodeIterator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class PowerNodeIterator<N extends AbstractPowerNode<N>> extends NodeIteratorAdapter {
	private final AbstractPowerNodeDecorator<N> powerNodeDecorator;

	PowerNodeIterator(
			final AbstractPowerNodeDecorator<N> powerNodeDecorator,
			final NodeIterator iterator
	) {
		super(iterator);
		this.powerNodeDecorator = powerNodeDecorator;
	}

	@Override
	public N nextNode() throws NoSuchElementException {
		return powerNodeDecorator.wrapNode(super.nextNode());
	}

	public Stream<N> toStream() {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize((Iterator<N>)this, Spliterator.ORDERED), false);
	}
}
