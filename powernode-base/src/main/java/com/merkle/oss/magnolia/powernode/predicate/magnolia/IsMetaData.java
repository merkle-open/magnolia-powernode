package com.merkle.oss.magnolia.powernode.predicate.magnolia;

import com.merkle.oss.magnolia.powernode.RepositoryExceptionDelegator;
import com.merkle.oss.magnolia.powernode.predicate.IsNamePrefix;
import com.merkle.oss.magnolia.powernode.predicate.IsPrimaryNodeType;
import info.magnolia.jcr.util.NodeTypes;

import javax.jcr.Node;
import java.util.function.Predicate;

public class IsMetaData<N extends Node> extends RepositoryExceptionDelegator implements Predicate<N> {
	private final Predicate<N> predicate = new IsNamePrefix<N>(NodeTypes.JCR_PREFIX)
			.or(new IsNamePrefix<>(NodeTypes.REP_PREFIX))
			.or(new IsPrimaryNodeType<>(NodeTypes.MetaData.NAME));

	@Override
	public boolean test(final N node) {
		return predicate.test(node);
	}
}
