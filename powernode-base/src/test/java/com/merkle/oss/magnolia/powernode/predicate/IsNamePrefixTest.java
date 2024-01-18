package com.merkle.oss.magnolia.powernode.predicate;

import com.merkle.oss.magnolia.powernode.mock.MockNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IsNamePrefixTest {

	@Test
	void  test() {
		final MockNode node = new MockNode("someName");
		assertTrue(new IsNamePrefix<>("some").test(node));
		assertFalse(new IsNamePrefix<>("other").test(node));
	}
}