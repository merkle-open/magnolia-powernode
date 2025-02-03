package com.merkle.oss.magnolia.powernode;

import com.merkle.oss.magnolia.powernode.mock.LocalizedNameProviderMock;

import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockSession;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;

import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class AbstractPowerNodeTest {

	@Test
	void testEquals() {
		final PowerNode node1 = new PowerNode("node1", "1");
		final PowerNode node2 = new PowerNode("node2", "2");
		final PowerNode node3 = new PowerNode("node1", "1");

		assertEquals(node1, node3);
		assertNotEquals(node1, node2);
	}

	@Test
	void testHashCode() {
		final PowerNode node1 = new PowerNode("node1", "1");
		final PowerNode node2 = new PowerNode("node2", "2");
		final PowerNode node3 = new PowerNode("node1", "1");

		assertEquals(node1.hashCode(), node3.hashCode());
		assertNotEquals(node1.hashCode(), node2.hashCode());
	}

	private static class PowerNode extends AbstractPowerNode<PowerNode> {
		private PowerNode(final String name, final String identifier) {
			this(createMockNode(name, identifier));
		}

		private static MockNode createMockNode(final String name, final String identifier) {
			final MockNode mockNode = new MockNode(new MockSession("testing"));
			mockNode.setName(name);
			mockNode.setIdentifier(identifier);
			return mockNode;
		}

		private PowerNode(final Node node) {
			super(
					new NodeService(
							new LocalizedNameProviderMock(),
							null,
							null,
							new PropertyService(valueFactory -> new ValueConverter(valueFactory, ZoneId::systemDefault))
					),
					node,
					new AbstractPowerNodeDecorator<>() {
						@Override
						protected PowerNode wrapNodeInternal(Node node) {
							return new PowerNode(node);
						}

						@Override
						protected Node unwrapNodeInternal(Node node) {
							return NodeUtil.deepUnwrap(node, PowerNode.class);
						}
					});
		}
	}
}