package com.merkle.oss.magnolia.powernode.predicate;

import com.merkle.oss.magnolia.powernode.mock.MockNode;
import org.junit.jupiter.api.Test;

import javax.jcr.RepositoryException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HasPropertyTest {

	@Test
	void  test() throws RepositoryException {
		final MockNode node = new MockNode("name");
		node.setProperty("someKey", "someValue");
		assertTrue(new HasProperty<>("someKey").test(node));
		assertFalse(new HasProperty<>("someOtherKey").test(node));
	}
}