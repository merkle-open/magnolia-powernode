package com.merkle.oss.magnolia.powernode.predicate;

import com.merkle.oss.magnolia.powernode.MockNode;
import org.junit.jupiter.api.Test;

import javax.jcr.nodetype.NodeType;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class IsPrimaryNodeTypeTest {

	@Test
	void  test() {
		final MockNode node = new MockNode("name");
		final NodeType nodeType = mock(NodeType.class);
		doAnswer(invocationOnMock ->
				Objects.equals(invocationOnMock.getArgument(0), "someNodeType")
		).when(nodeType).isNodeType(any());
		node.setPrimaryNodeType(nodeType);

		assertTrue(new IsPrimaryNodeType<>("someNodeType").test(node));
		assertFalse(new IsPrimaryNodeType<>("someOtherNodeType").test(node));
	}
}