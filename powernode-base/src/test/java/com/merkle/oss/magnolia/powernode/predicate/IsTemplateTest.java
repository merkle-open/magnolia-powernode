package com.merkle.oss.magnolia.powernode.predicate;

import info.magnolia.jcr.util.NodeTypes;
import com.merkle.oss.magnolia.powernode.MockNode;
import org.junit.jupiter.api.Test;

import javax.jcr.RepositoryException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IsTemplateTest {

	@Test
	void  test() throws RepositoryException {
		final MockNode node = new MockNode("name");
		assertFalse(new IsTemplate<>("someTemplateId").test(node));
		node.setProperty(NodeTypes.Renderable.TEMPLATE, "someTemplateId");
		assertTrue(new IsTemplate<>("someTemplateId").test(node));
		assertFalse(new IsTemplate<>("someOtherTemplateId").test(node));
	}
}