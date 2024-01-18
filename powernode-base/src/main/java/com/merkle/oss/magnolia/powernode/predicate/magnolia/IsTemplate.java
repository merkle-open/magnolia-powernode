package com.merkle.oss.magnolia.powernode.predicate.magnolia;

import com.merkle.oss.magnolia.powernode.AbstractPowerNode;
import com.merkle.oss.magnolia.powernode.RepositoryExceptionDelegator;
import com.merkle.oss.magnolia.powernode.ValueConverter;
import com.merkle.oss.magnolia.powernode.predicate.HasPropertyValue;
import info.magnolia.jcr.util.NodeTypes;

import javax.jcr.Node;
import java.util.Objects;
import java.util.function.Predicate;

public class IsTemplate<N extends AbstractPowerNode<N>> extends HasPropertyValue<N, String> {
	public IsTemplate(final String templateId) {
		super(NodeTypes.Renderable.TEMPLATE, ValueConverter::getString, templateId);
	}
}
