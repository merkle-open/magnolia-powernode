package com.merkle.oss.magnolia.powernode.predicate.magnolia;

import com.merkle.oss.magnolia.powernode.predicate.IsPrimaryNodeType;
import info.magnolia.jcr.util.NodeTypes;

import javax.jcr.Node;

public class IsArea<N extends Node> extends IsPrimaryNodeType<N> {
	public IsArea() {
		super(NodeTypes.Area.NAME);
	}
}
