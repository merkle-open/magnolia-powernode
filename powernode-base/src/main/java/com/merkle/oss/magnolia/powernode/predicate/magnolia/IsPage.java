package com.merkle.oss.magnolia.powernode.predicate.magnolia;

import com.merkle.oss.magnolia.powernode.predicate.IsPrimaryNodeType;
import info.magnolia.jcr.util.NodeTypes;

import javax.jcr.Node;

public class IsPage<N extends Node> extends IsPrimaryNodeType<N> {
	public IsPage() {
		super(NodeTypes.Page.NAME);
	}
}
