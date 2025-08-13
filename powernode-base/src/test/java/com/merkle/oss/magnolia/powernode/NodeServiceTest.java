package com.merkle.oss.magnolia.powernode;

import com.google.common.collect.Lists;
import com.machinezoo.noexception.Exceptions;
import com.merkle.oss.magnolia.powernode.mock.LocalizedNameProviderMock;
import com.merkle.oss.magnolia.powernode.mock.JcrSessionProviderMock;
import com.merkle.oss.magnolia.powernode.mock.MockSession;
import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.jcr.util.NodeNameHelper;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.jcr.wrapper.I18nNodeWrapper;
import info.magnolia.test.ComponentsTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class NodeServiceTest {
	private MockSession session;
	private NodeService nodeService;

	@BeforeEach
	void setUp() {
		final NodeNameHelper nodeNameHelper = mock(NodeNameHelper.class);
		doAnswer(invocationOnMock -> invocationOnMock.getArgument(0)).when(nodeNameHelper).getValidatedName(anyString());
		final JcrSessionProviderMock jcrSessionProvider = new JcrSessionProviderMock();
		session = new MockSession("testing");
		jcrSessionProvider.mock(session);
		jcrSessionProvider.mockSystem(new MockSession("testing"));
		nodeService = new NodeService(
				new LocalizedNameProviderMock(),
				nodeNameHelper,
				jcrSessionProvider,
				new PropertyService(valueFactory -> new ValueConverter(valueFactory, ZoneId::systemDefault))
		);
	}

	@Test
	void getSession() {
		assertEquals(
				Optional.of("testing"),
				nodeService.getSession("testing").map(Session::getWorkspace).map(Workspace::getName)
		);
	}

	@Test
	void getSystemSession() {
		assertEquals(
				Optional.of("testing"),
				nodeService.getSystemSession("testing").map(Session::getWorkspace).map(Workspace::getName)
		);
	}

	@Test
	void getByIdentifier() throws RepositoryException {
		final Node node = session.getRootNode().addNode("someNode", "someNodeType");
		assertEquals(
				Optional.of(node),
				nodeService.getByIdentifier(session.getWorkspace().getName(), node.getIdentifier())
		);
		assertEquals(
				Optional.of(node),
				nodeService.getByIdentifier(session, node.getIdentifier())
		);
		assertTrue(nodeService.getByIdentifier(session, "unknownIdentifier").isEmpty());
	}

	@Test
	void getByPath() throws RepositoryException {
		final Node node = session.getRootNode().addNode("someNode", "someNodeType");
		assertEquals(
				Optional.of(node),
				nodeService.getByPath(session.getWorkspace().getName(), "/someNode")
		);
		assertEquals(
				Optional.of(node),
				nodeService.getByPath(session, "/someNode")
		);
		assertTrue(nodeService.getByPath(session, "/unknownPath").isEmpty());
	}

	@Test
	void rename() throws RepositoryException {
		final Node node1 = session.getRootNode().addNode("node1", "someNodeType");
		final Node node2 = session.getRootNode().addNode("node2", "someNodeType");
		final Node node3 = node2.addNode("node3", "someNodeType");
		try (MockedStatic<NodeTypes.LastModified> lastModified = mockStatic(NodeTypes.LastModified.class)) {
			nodeService.rename(node1, "node1Renamed");
			nodeService.rename(node3, "node3Renamed");
			lastModified.verify(() -> NodeTypes.LastModified.update(node1));
			lastModified.verify(() -> NodeTypes.LastModified.update(node3));
			Optional<Node> node1Renamed = nodeService.getByPath(session, "/node1Renamed");
			Optional<Node> node3Renamed = nodeService.getByPath(session, "/node2/node3Renamed");
			assertTrue(node1Renamed.isPresent());
			assertTrue(node3Renamed.isPresent());

			//assert order
			assertEquals(
					List.of(node1Renamed.get(), node2),
					Lists.newArrayList((Iterator<Node>) session.getRootNode().getNodes())
			);
		}
	}

	@Test
	void move() throws RepositoryException {
		final Node node1 = session.getRootNode().addNode("node1", "someNodeType");
		final Node node2 = session.getRootNode().addNode("node2", "someNodeType");
		try (MockedStatic<NodeTypes.LastModified> lastModified = mockStatic(NodeTypes.LastModified.class)) {
			nodeService.move(node1, node2);
			lastModified.verify(() -> NodeTypes.LastModified.update(node1));
			lastModified.verify(() -> NodeTypes.LastModified.update(node2));
			assertTrue(nodeService.getByPath(session, "/node2/node1").isPresent());
		}
	}

	@Test
	void copy() throws RepositoryException {
		final Node src = session.getRootNode().addNode("node1", "someNodeType");
		src.setProperty("property1", 42);
		src.setProperty("property2", new String[]{"test"});
		src.setProperty(NodeTypes.Renderable.TEMPLATE, "someTemplateId");
		src.setProperty("excludedProperty", "test42");
		src.addNode("subNode1", "someNodeType").addNode("subNode2", "someOtherNodeType");
		final Node dst = session.getRootNode().addNode("node2", "someNodeType");

		final ArrayList<Map.Entry<Node, Node>> copies = new ArrayList<>();
		nodeService.copy(
				src,
				dst,
				child -> !Exceptions.wrap().get(child::getPrimaryNodeType).isNodeType("someOtherNodeType"),
				property -> !Exceptions.wrap().get(property::getName).equals("excludedProperty"),
				(copySrc, copy) -> copies.add(Map.entry(copySrc, copy))
		);

		final Optional<Node> copy = nodeService.getByPath(session, "/node2/node1");
		assertTrue(copy.isPresent());
		assertEquals(42, copy.get().getProperty("property1").getLong());
		assertEquals(List.of("test"), PropertyUtil.getValuesStringList(copy.get().getProperty("property2").getValues()));
		assertEquals("someTemplateId", copy.get().getProperty(NodeTypes.Renderable.TEMPLATE).getString());
		assertFalse(copy.get().hasProperty("excludedProperty"));
		assertTrue(copy.get().hasNode("subNode1"));
		final Node subNode1 = copy.get().getNode("subNode1");
		assertFalse(subNode1.hasNode("subNode2"));
		assertEquals(List.of(Map.entry(src, copy.get()), Map.entry(src.getNode("subNode1"), subNode1)), copies);
	}

	@Test
	void getOrAddChild() throws RepositoryException {
		final Node node = session.getRootNode().addNode("node", "someNodeType");
		assertEquals(
				"/node/child/grandchild",
				nodeService.getOrAddChild(node, NodeTypes.ContentNode.NAME, "child/grandchild").getPath()
		);
	}

	@Test
	void getOrAddChild_localized() throws RepositoryException {
		final Node node = session.getRootNode().addNode("node", "someNodeType");
		assertEquals(
				"/node/child_fr-CA/grandchild_fr-CA",
				nodeService.getOrAddChild(node, NodeTypes.ContentNode.NAME, "child/grandchild", Locale.CANADA_FRENCH).getPath()
		);
	}

	@Test
	void getChild() throws RepositoryException {
		final Node node = session.getRootNode().addNode("node", "someNodeType");
		final Node child = node.addNode("child", "someNodeType");

		assertEquals(
				Optional.of(child),
				nodeService.getChild(node, "child")
		);
		assertTrue(nodeService.getChild(node, "nonExistingChild").isEmpty());
	}

	@Test
	void getChild_localized() throws RepositoryException {
		final Node node = session.getRootNode().addNode("node", "someNodeType");
		final Node child1 = node.addNode("child1_fr-CA", "someNodeType");
		final Node child2 = child1.addNode("child2_fr-CA", "someNodeType");

		assertEquals(
				Optional.of(child2),
				nodeService.getChild(node, "child1/child2", Locale.CANADA_FRENCH)
		);
	}

	@Test
	void streamChildren() throws RepositoryException {
		final Node node = session.getRootNode().addNode("node", "someNodeType");
		final Node child1 = node.addNode("child1", "someNodeType");
		final Node child2 = node.addNode("child2", "someOtherNodeType");
		node.addNode(NodeTypes.JCR_PREFIX+"child", "someNodeType"); //should be filtered (metaData)
		node.addNode(NodeTypes.REP_PREFIX+"child", "someNodeType"); //should be filtered (metaData)
		node.addNode("child", NodeTypes.MetaData.NAME); //should be filtered (metaData)
		assertEquals(
				List.of(child1, child2),
				nodeService.streamChildren(node).collect(Collectors.toList())
		);

		assertEquals(
				List.of(child2),
				nodeService.streamChildren(node, new NodeTypePredicate("someOtherNodeType")).collect(Collectors.toList())
		);
	}

	/**
	 * node
	 *  |__child1
	 *       |__grandChild1
	 *  |__child2
	 */
	@Test
	void streamChildrenRecursive() throws RepositoryException {
		final Node node = session.getRootNode().addNode("node", "someNodeType");
		final Node child1 = node.addNode("child1", "someNodeType");
		final Node child2 = node.addNode("child2", "someOtherNodeType");
		final Node grandChild1 = child1.addNode("grandChild1", "someOtherNodeType");
		assertEquals(
				List.of(child1, grandChild1, child2),
				nodeService.streamChildrenRecursive(node).collect(Collectors.toList())
		);

		assertEquals(
				List.of(grandChild1, child2),
				nodeService.streamChildrenRecursive(node, new NodeTypePredicate("someOtherNodeType")).collect(Collectors.toList())
		);
	}

	@Test
	void getParent() throws RepositoryException {
		final Node node = session.getRootNode().addNode("node", "someNodeType");
		final Node child = node.addNode("child", "someNodeType");
		assertEquals(
				Optional.of(node),
				nodeService.getParent(child)
		);
		assertTrue(nodeService.getParent(session.getRootNode()).isEmpty());
	}

	@Test
	void getAncestor() throws RepositoryException {
		final Node node = session.getRootNode().addNode("node", "someNodeType");
		final Node child = node.addNode("child", "someOtherNodeType");
		final Node grandChild = child.addNode("grandChild", "someNodeType");
		assertEquals(
				Optional.of(child),
				nodeService.getAncestor(grandChild, new NodeTypePredicate("someOtherNodeType"))
		);
		assertEquals(
				Optional.of(node),
				nodeService.getAncestor(grandChild, new NodeTypePredicate("someNodeType"))
		);
		assertTrue(nodeService.getAncestor(grandChild, new NodeTypePredicate("someNonExistingNodeType")).isEmpty());
	}

	@Test
	void getAncestorOrSelf() throws RepositoryException {
		final Node node = session.getRootNode().addNode("node", "someNodeType");
		final Node child = node.addNode("child", "someOtherNodeType");
		final Node grandChild = child.addNode("grandChild", "someNodeType");
		assertEquals(
				Optional.of(child),
				nodeService.getAncestorOrSelf(grandChild, new NodeTypePredicate("someOtherNodeType"))
		);
		assertEquals(
				Optional.of(grandChild),
				nodeService.getAncestorOrSelf(grandChild, new NodeTypePredicate("someNodeType"))
		);
		assertTrue(nodeService.getAncestorOrSelf(grandChild, new NodeTypePredicate("someNonExistingNodeType")).isEmpty());
	}

	@Test
	void property() throws RepositoryException {
		final Node node = session.getRootNode().addNode("node", "someNodeType");
		nodeService.setProperty(node, "someKey", "someValue", ValueConverter::toValue);
		assertTrue(nodeService.hasProperty(node, "someKey"));
		assertEquals(
				Optional.of("someValue"),
				nodeService.getProperty(node, "someKey", ValueConverter::getString)
		);
		nodeService.removeProperty(node, "someKey");
		assertFalse(nodeService.hasProperty(node, "someKey"));
	}

	@Test
	void property_localized() throws RepositoryException {
		testPropertyLocalized(session.getRootNode().addNode("node", "someNodeType"));
	}

	@Test
	void property_localized_wrappedWithI18nNodeWrapper() throws RepositoryException {
		ComponentsTestUtil.setImplementation(I18nContentSupport.class, DefaultI18nContentSupport.class); // for repository
		testPropertyLocalized(new I18nNodeWrapper(session.getRootNode().addNode("node", "someNodeType")));
	}

	private void testPropertyLocalized(final Node node) {
		nodeService.setProperty(node, "someKey", Locale.CANADA, "someValue", ValueConverter::toValue);
		assertTrue(nodeService.hasProperty(node, "someKey", Locale.CANADA));
		assertEquals(
				Optional.of("someValue"),
				nodeService.getProperty(node, "someKey", Locale.CANADA, ValueConverter::getString)
		);
		nodeService.removeProperty(node, "someKey", Locale.CANADA);
		assertFalse(nodeService.hasProperty(node, "someKey", Locale.CANADA));
	}

	@Test
	void multiProperty() throws RepositoryException {
		final Node node = session.getRootNode().addNode("node", "someNodeType");
		nodeService.setMultiProperty(node, "someKey", List.of("someValue", "someOtherValue"), ValueConverter::toValue);
		assertTrue(nodeService.hasProperty(node, "someKey"));
		assertEquals(
				List.of("someValue", "someOtherValue"),
				nodeService.streamMultiProperty(node, "someKey", ValueConverter::getString).collect(Collectors.toList())
		);
		nodeService.removeProperty(node, "someKey");
		assertFalse(nodeService.hasProperty(node, "someKey"));
	}

	@Test
	void multiProperty_localized() throws RepositoryException {
		testMultiPropertyLocalized(session.getRootNode().addNode("node", "someNodeType"));
	}

	@Test
	void multiProperty_localized_wrappedWithI18nNodeWrapper() throws RepositoryException {
		ComponentsTestUtil.setImplementation(I18nContentSupport.class, DefaultI18nContentSupport.class); // for repository
		testMultiPropertyLocalized(new I18nNodeWrapper(session.getRootNode().addNode("node", "someNodeType")));
	}

	private void testMultiPropertyLocalized(final Node node) {
		nodeService.setMultiProperty(node, "someKey", Locale.CANADA, List.of("someValue", "someOtherValue"), ValueConverter::toValue);
		assertTrue(nodeService.hasProperty(node, "someKey", Locale.CANADA));
		assertEquals(
				List.of("someValue", "someOtherValue"),
				nodeService.streamMultiProperty(node, "someKey", Locale.CANADA, ValueConverter::getString).collect(Collectors.toList())
		);
		nodeService.removeProperty(node, "someKey", Locale.CANADA);
		assertFalse(nodeService.hasProperty(node, "someKey", Locale.CANADA));
	}

	private static class NodeTypePredicate implements Predicate<Node> {
		private final String nodeType;

		public NodeTypePredicate(final String nodeType) {
			this.nodeType = nodeType;
		}

		@Override
		public boolean test(final Node node) {
			try {
				return nodeType.equals(node.getPrimaryNodeType().getName());
			} catch (RepositoryException e) {
				return false;
			}
		}
	}
}
