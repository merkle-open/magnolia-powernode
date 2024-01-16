package com.merkle.oss.magnolia.powernode;

import com.google.common.collect.Lists;
import info.magnolia.jcr.nodebuilder.ErrorHandler;
import info.magnolia.jcr.nodebuilder.NodeOperation;
import info.magnolia.jcr.nodebuilder.StrictErrorHandler;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.wrapper.DelegateNodeWrapper;
import info.magnolia.test.mock.jcr.MockPropertyIterator;
import info.magnolia.test.mock.jcr.MockSession;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import javax.annotation.Nullable;
import javax.jcr.Node;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NodeOperationFactoryTest {
	private NodeOperationFactory nodeOperationFactory;
	private MockSession session;

	@BeforeEach
	void setUp() {
		session = new MockSession("testing");
		nodeOperationFactory = new NodeOperationFactory(new NodeService(
				new LocalizedNameProviderMock(),
				null,
				null,
				new PropertyService(valueFactory -> new ValueConverter(valueFactory, ZoneId::systemDefault))
		));
	}

	@Test
	void copy() throws RepositoryException {
		//actual copy can't be tested, since MockSession is not implementing exportSystemView which is required by NodeUtil.copyInSession
		try (MockedStatic<NodeUtil> nodeUtil = mockStatic(NodeUtil.class)) {
			nodeUtil.when(() -> NodeUtil.copyInSession(any(), anyString())).then(invocationOnMock -> {
				String destination = invocationOnMock.getArgument(1);
				session.getRootNode().addNode(StringUtils.removeStart(destination, "/"));
				return null;
			});

			final Node node = session.getRootNode().addNode("someNode", "someNodeType");
			node.addNode("child1", "someNodeType");
			node.addNode("child2", "someNodeType");
			execute(nodeOperationFactory.copy("child1", "child2/child1"), node, "/someNode/child2/child1");
		}
	}

	@Test
	void copyAbsolute() throws RepositoryException {
		//actual copy can't be tested, since MockSession is not implementing exportSystemView which is required by NodeUtil.copyInSession
		try (MockedStatic<NodeUtil> nodeUtil = mockStatic(NodeUtil.class)) {
			nodeUtil.when(() -> NodeUtil.copyInSession(any(), anyString())).then(invocationOnMock -> {
				String destination = invocationOnMock.getArgument(1);
				session.getRootNode().addNode(StringUtils.removeStart(destination, "/"));
				return null;
			});

			final Node node = session.getRootNode().addNode("someNode", "someNodeType");
			node.addNode("child1", "someNodeType");
			node.addNode("child2", "someNodeType");
			execute(nodeOperationFactory.copyAbsolute("/someNode/child1", "/someNode/child2/child1"), node, "/someNode/child2/child1");
		}
	}

	@Test
	void getChild_exists() throws RepositoryException {
		final Node node = session.getRootNode().addNode("someNode", "someNodeType");
		final Node child = node.addNode("child", "someNodeType");
		execute(nodeOperationFactory.getChild("child"), node, child);
	}

	@Test
	void getChild_doesNotExist() throws RepositoryException {
		final Node node = session.getRootNode().addNode("someNode", "someNodeType");
		assertThrows(NullPointerException.class, () ->
				execute(nodeOperationFactory.getChild("nonExistingChild"), node)
		);
	}

	@Test
	void getOrAddNode_exists_get() throws RepositoryException {
		final Node node = session.getRootNode().addNode("someNode", "someNodeType");
		final Node child = node.addNode("child", "someNodeType");
		execute(nodeOperationFactory.getOrAddNode("child", "someNodeType"), node, child);
	}

	@Test
	void getOrAddNode_doesNotExist_add() throws RepositoryException {
		final Node node = session.getRootNode().addNode("someNode", "someNodeType");
		execute(nodeOperationFactory.getOrAddNode("child", null), node, "/someNode/child");
	}

	@Test
	void removeNode() throws RepositoryException {
		final Node node = session.getRootNode().addNode("someNode", "someNodeType");
		node.addNode("child", "someNodeType");

		execute(nodeOperationFactory.removeNode("child"), node, node);
		assertFalse(node.getNodes().hasNext());
	}

	@Test
	void orderBefore() throws RepositoryException {
		final Node node = session.getRootNode().addNode("someNode", "someNodeType");
		final Node child1 = node.addNode("child1", "someNodeType");
		final Node child2 = node.addNode("child2", "someNodeType");

		execute(nodeOperationFactory.orderBefore("child1"), child2, child2);

		assertEquals(
				List.of(child2, child1),
				Lists.newArrayList(node.getNodes())
		);
	}

	@Test
	void orderFirst() throws RepositoryException {
		final Node node = session.getRootNode().addNode("someNode", "someNodeType");
		final Node child1 = node.addNode("child1", "someNodeType");
		final Node child2 = node.addNode("child2", "someNodeType");
		final Node child3 = node.addNode("child3", "someNodeType");

		execute(nodeOperationFactory.orderFirst(), child3, child3);

		assertEquals(
				List.of(child3, child1, child2),
				Lists.newArrayList(node.getNodes())
		);
	}

	@Test
	void orderAfter() throws RepositoryException {
		final Node node = session.getRootNode().addNode("someNode", "someNodeType");
		final Node child1 = node.addNode("child1", "someNodeType");
		final Node child2 = node.addNode("child2", "someNodeType");

		execute(nodeOperationFactory.orderAfter("child2"), child1, child1);

		assertEquals(
				List.of(child2, child1),
				Lists.newArrayList(node.getNodes())
		);
	}

	@Test
	void orderLast() throws RepositoryException {
		final Node node = session.getRootNode().addNode("someNode", "someNodeType");
		final Node child1 = node.addNode("child1", "someNodeType");
		final Node child2 = node.addNode("child2", "someNodeType");
		final Node child3 = node.addNode("child3", "someNodeType");

		execute(nodeOperationFactory.orderLast(), child1, child1);

		assertEquals(
				List.of(child2, child3, child1),
				Lists.newArrayList(node.getNodes())
		);
	}

	@Test
	void setProperty() throws RepositoryException {
		final Node node = session.getRootNode().addNode("someNode", "someNodeType");
		node.setProperty("someKey", "initialValue");
		execute(nodeOperationFactory.setProperty("someKey", "someValue", ValueConverter::toValue), node, node);
		execute(nodeOperationFactory.setProperty("someKey", Locale.CANADA_FRENCH, "someValue", ValueConverter::toValue), node, node);
		assertEquals("someValue", node.getProperty("someKey").getString());
		assertEquals("someValue", node.getProperty("someKey_fr-CA").getString());
	}

	@Test
	void setPropertyOnlyIfMissing() throws RepositoryException {
		final Node node = session.getRootNode().addNode("someNode", "someNodeType");
		execute(nodeOperationFactory.setPropertyOnlyIfMissing("someKey", "someValue", ValueConverter::toValue), node, node);
		assertEquals("someValue", node.getProperty("someKey").getString());
	}

	@Test
	void setPropertyOnlyIfMissing_present() throws RepositoryException {
		final Node node = session.getRootNode().addNode("someNode", "someNodeType");
		node.setProperty("someKey", "initialValue");
		execute(nodeOperationFactory.setPropertyOnlyIfMissing("someKey", "someValue", ValueConverter::toValue), node, node);
		assertEquals("initialValue", node.getProperty("someKey").getString());
	}

	@Test
	void setPropertyOnlyIfMissing_localized() throws RepositoryException {
		final Node node = session.getRootNode().addNode("someNode", "someNodeType");
		execute(nodeOperationFactory.setPropertyOnlyIfMissing("someKey", Locale.CANADA_FRENCH, "someValue", ValueConverter::toValue), node, node);
		assertEquals("someValue", node.getProperty("someKey_fr-CA").getString());
	}

	@Test
	void setPropertyOnlyIfMissing_localized_present() throws RepositoryException {
		final Node node = session.getRootNode().addNode("someNode", "someNodeType");
		node.setProperty("someKey_fr-CA", "initialValue");
		execute(nodeOperationFactory.setPropertyOnlyIfMissing("someKey", Locale.CANADA_FRENCH, "someValue", ValueConverter::toValue), node, node);
		assertEquals("initialValue", node.getProperty("someKey_fr-CA").getString());
	}

	@Test
	void setClassProperty() throws RepositoryException {
		final Node node = session.getRootNode().addNode("someNode", "someNodeType");
		execute(nodeOperationFactory.setClassProperty(String.class), node, node);
		assertEquals("java.lang.String", node.getProperty("class").getString());
	}

	@Test
	void setEnabledProperty() throws RepositoryException {
		final Node node = session.getRootNode().addNode("someNode", "someNodeType");
		execute(nodeOperationFactory.setEnabledProperty(true), node, node);
		assertTrue(node.getProperty("enabled").getBoolean());
	}

	@Test
	void removeProperty() throws RepositoryException {
		final Node node = session.getRootNode().addNode("someNode", "someNodeType");
		node.setProperty("someKey", "someValue");
		node.setProperty("someKey_fr-CA", "someValue");
		execute(nodeOperationFactory.removeProperty("someKey"), node, node);
		execute(nodeOperationFactory.removeProperty("someKey", Locale.CANADA_FRENCH), node, node);
		assertFalse(node.hasProperty("someKey"));
		assertFalse(node.hasProperty("someKey_fr-CA"));
	}

	@Test
	void clearProperties() throws RepositoryException {
		final Node node = session.getRootNode().addNode("someNode", "someNodeType");
		node.setProperty("someKey", "someValue");
		node.setProperty("someKey2", "someValue2");
		// since MockPropertyIterator is not creating a copy of the list a ConcurrentModificationException would be thrown if node would not be wrapped
		final DelegateNodeWrapper wrappedNode = new DelegateNodeWrapper(node) {
			@Override
			public PropertyIterator getProperties() throws RepositoryException {
				return new MockPropertyIterator(Lists.newArrayList(super.getProperties()));
			}
		};
		execute(nodeOperationFactory.clearProperties(), wrappedNode, wrappedNode);
		assertFalse(wrappedNode.hasProperty("someKey"));
		assertTrue(Lists.newArrayList(wrappedNode.getProperties()).isEmpty());
	}


	private void execute(final NodeOperation operation, final Node context, final String expectedNewContextPath) throws RepositoryException {
		assertEquals(expectedNewContextPath, execute(operation, context).getPath());
	}

	private void execute(final NodeOperation operation, final Node context, final Node expectedNewContext) {
		assertEquals(expectedNewContext, execute(operation, context));
	}

	/**
	 * @return context after operation
	 */
	private Node execute(final NodeOperation operation, final Node context) {
		final TestNodeOperation testNodeOperation = new TestNodeOperation();
		operation
				.then(testNodeOperation)
				.exec(context, new StrictErrorHandler());
		return testNodeOperation.getNode().orElseThrow(() -> new RuntimeException("context not present!"));
	}

	private static class TestNodeOperation implements NodeOperation {
		@Nullable
		private Node node;

		@Override
		public NodeOperation then(final NodeOperation... nodeOperations) {
			return null;
		}
		@Override
		public void exec(final Node node, final ErrorHandler errorHandler) {
			this.node = node;
		}
		public Optional<Node> getNode() {
			return Optional.ofNullable(node);
		}
	}
}