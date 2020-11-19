package com.namics.oss.magnolia.powernode;

import com.namics.oss.magnolia.helper.JcrNodeTreePrinter;
import com.namics.oss.magnolia.powernode.exceptions.PowerNodeException;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.wrapper.ExtendingNodeWrapper;
import info.magnolia.jcr.wrapper.HTMLEscapingNodeWrapper;
import info.magnolia.repository.RepositoryConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.commons.predicate.NodeTypePredicate;
import org.apache.jackrabbit.commons.predicate.Predicate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.jcr.*;
import java.util.List;
import java.util.Optional;

public class PowerNodeTest extends AbstractPowerNodeTest {

	private static final String DEFAULT_TEST_NODE = "/blitzdings/de/new-one/ContentArea/0/ContentArea/node02";

	private PowerNode getPowerNode(String nodePath) {
		Optional<Session> session = powerNodeService.getSystemSession(RepositoryConstants.WEBSITE);
		Assertions.assertTrue(session.isPresent());
		Optional<PowerNode> node = powerNodeService.getNodeByPath(nodePath, session.get());
		Assertions.assertTrue(node.isPresent());
		return node.get();
	}

	/**
	 * Prints out the test-data node tree.
	 * Useful to write new tests with the current test data.
	 */
	@Test
	public void printTestDataTree() {
		PowerNode node = getPowerNode("/blitzdings");
		JcrNodeTreePrinter.print(node, System.out::println);
	}

	@Test
	public void sessionTest() throws Exception {
		// test if the session behaviour is
		// as expected.

		String relPath = "sessionTestNode";
		String nodeType = "mgnl:content";
		String rootNodePath = "/blitzdings/de";

		PowerNode root = getPowerNode(rootNodePath);
		// create test node
		Optional<PowerNode> testNode = root.getOrCreateNode(relPath, nodeType);
		Assertions.assertTrue(testNode.isPresent());
		// logout
		testNode.get().getSession().logout();

		// get root again (new session)
		PowerNode rootAfterLogout = getPowerNode(rootNodePath);
		// check if test node is present (should not, since session was not saved)
		boolean has01 = rootAfterLogout.hasNode(relPath);
		Assertions.assertFalse(has01);

		// create test node again
		Optional<PowerNode> testNode02 = rootAfterLogout.getOrCreateNode(relPath, nodeType);
		Assertions.assertTrue(testNode02.isPresent());
		// save
		testNode02.get().getSession().save();

		// get root again (new Session)
		PowerNode root02AfterLogout = getPowerNode(rootNodePath);
		// check if test node is present (should, since session was saved)
		boolean has02 = root02AfterLogout.hasNode(relPath);
		Assertions.assertTrue(has02);
	}

	@Test
	public void getTemplate_sunshine() {
		PowerNode node = getPowerNode(DEFAULT_TEST_NODE);
		String expectedTemplate = "blitzdings-web-core:components/TextImage";
		String template = node.getTemplate();
		Assertions.assertEquals(expectedTemplate, template);
	}

	@Test
	public void getTemplate_noTemplate() {
		PowerNode node = getPowerNode("/blitzdings/it/new-one/gtmTags");
		String expectedTemplate = StringUtils.EMPTY;
		String template = node.getTemplate();
		Assertions.assertEquals(expectedTemplate, template);
	}

	@Test
	public void isSame_sunshine() {
		PowerNode node = getPowerNode(DEFAULT_TEST_NODE);
		boolean same = node.isSame(node);
		Assertions.assertTrue(same);
	}

	@Test
	public void isSame_notSame() {
		PowerNode node = getPowerNode(DEFAULT_TEST_NODE);
		PowerNode other = getPowerNode("/blitzdings/it/new-one/gtmTags");
		boolean same = node.isSame(other);
		Assertions.assertFalse(same);
	}

	@Test
	public void isSame_wrappedNodes() {
		PowerNode node = getPowerNode(DEFAULT_TEST_NODE);
		Node otherWrapped = new ExtendingNodeWrapper(node);
		otherWrapped = new ExtendingNodeWrapper(otherWrapped);
		otherWrapped = new HTMLEscapingNodeWrapper(otherWrapped, false);
		boolean same = node.isSame(otherWrapped);
		Assertions.assertTrue(same);
	}

	@Test
	public void isSame_wrappedNodesDoublePower() {
		PowerNode node = getPowerNode(DEFAULT_TEST_NODE);
		Node otherWrapped = new ExtendingNodeWrapper(node);
		otherWrapped = new ExtendingNodeWrapper(otherWrapped);
		otherWrapped = powerNodeService.convertToPowerNode(otherWrapped);
		otherWrapped = new HTMLEscapingNodeWrapper(otherWrapped, false);
		boolean same = node.isSame(otherWrapped);
		Assertions.assertTrue(same);
	}

	@Test
	public void rename_sunshine() {
		PowerNode node = getPowerNode(DEFAULT_TEST_NODE);
		Assertions.assertEquals("node02", node.getName());
		Assertions.assertEquals(DEFAULT_TEST_NODE, node.getPath());

		String newName = "node03";
		node.rename(newName);

		Assertions.assertEquals(newName, node.getName());
		Assertions.assertEquals("/blitzdings/de/new-one/ContentArea/0/ContentArea/" + newName, node.getPath());
	}

	@Test
	public void rename_sameName() {
		PowerNode node = getPowerNode(DEFAULT_TEST_NODE);
		Assertions.assertEquals("node02", node.getName());
		Assertions.assertEquals(DEFAULT_TEST_NODE, node.getPath());

		String newName = "node02";
		node.rename(newName);

		Assertions.assertEquals(newName, node.getName());
		Assertions.assertEquals(DEFAULT_TEST_NODE, node.getPath());
	}

	@Test
	public void rename_invalidJcrName() {
		PowerNode node = getPowerNode(DEFAULT_TEST_NODE);
		Assertions.assertEquals("node02", node.getName());
		Assertions.assertEquals(DEFAULT_TEST_NODE, node.getPath());

		String newName = "node*04%";
		String newNameValid = powerNodeService.createValidNodeName(newName, "-");
		node.rename(newName);

		Assertions.assertEquals(newNameValid, node.getName());
		Assertions.assertEquals("/blitzdings/de/new-one/ContentArea/0/ContentArea/" + newNameValid, node.getPath());
	}

	@Test
	public void rename_noName() {
		PowerNode node = getPowerNode(DEFAULT_TEST_NODE);
		Assertions.assertEquals("node02", node.getName());
		Assertions.assertEquals(DEFAULT_TEST_NODE, node.getPath());

		String newName = "";
		node.rename(newName);

		Assertions.assertEquals("node02", node.getName());
		Assertions.assertEquals(DEFAULT_TEST_NODE, node.getPath());

		node.rename(null);

		Assertions.assertEquals("node02", node.getName());
		Assertions.assertEquals(DEFAULT_TEST_NODE, node.getPath());
	}

	@Test
	public void getWorkspaceName_sunshine() {
		PowerNode node = getPowerNode(DEFAULT_TEST_NODE);
		String workspaceName = node.getWorkspaceName();
		Assertions.assertEquals(getWorkspaceName(), workspaceName);
	}

	@Test
	public void getWorkspace_sunshine() {
		PowerNode node = getPowerNode(DEFAULT_TEST_NODE);
		Workspace workspace = node.getWorkspace();
		Assertions.assertEquals(getWorkspaceName(), workspace.getName());
	}

	@Test
	public void createPath_sunshine() {
		PowerNode node = getPowerNode(DEFAULT_TEST_NODE);
		String new02 = "new02";
		String new03 = "new03";

		String subPath = "/new01/new02/new03";
		Optional<PowerNode> newNode = node.createPath(subPath, NodeTypes.Content.NAME);

		Assertions.assertTrue(newNode.isPresent());
		Assertions.assertEquals(DEFAULT_TEST_NODE + subPath, newNode.get().getPath());
		Assertions.assertEquals(new03, newNode.get().getName());
		Assertions.assertEquals(NodeTypes.Content.NAME, newNode.get().getPrimaryNodeType().getName());

		Assertions.assertEquals(new02, newNode.get().getParent().getName());
		Assertions.assertEquals(NodeTypes.Content.NAME, newNode.get().getParent().getPrimaryNodeType().getName());
	}

	@Test
	public void createPath_invalidJcrPath() {
		PowerNode node = getPowerNode(DEFAULT_TEST_NODE);
		String invalidNewNodeName = "new**01";
		String validNewNodeName = "new02";

		String pathWithInvalidNodeName = "/" + invalidNewNodeName + "/" + validNewNodeName;
		Optional<PowerNode> newNode = node.createPath(pathWithInvalidNodeName, NodeTypes.Content.NAME);

		Assertions.assertTrue(newNode.isPresent());
		Assertions.assertEquals(DEFAULT_TEST_NODE + "/new-01/new02", newNode.get().getPath());
		Assertions.assertEquals(validNewNodeName, newNode.get().getName());
		Assertions.assertEquals("new-01", newNode.get().getParent().getName());
	}

	@Test
	public void createPath_noNodeType() {
		PowerNode node = getPowerNode(DEFAULT_TEST_NODE);

		Optional<PowerNode> newNode01 = node.createPath("new01", "");
		Assertions.assertFalse(newNode01.isPresent());

		Optional<PowerNode> newNode02 = node.createPath("new01", null);
		Assertions.assertFalse(newNode02.isPresent());
	}

	@Test
	public void createPath_emptyPath() {
		PowerNode node = getPowerNode(DEFAULT_TEST_NODE);

		Optional<PowerNode> newNode = node.createPath("", NodeTypes.Content.NAME);

		Assertions.assertTrue(newNode.isPresent());
		Assertions.assertEquals(DEFAULT_TEST_NODE, newNode.get().getPath());

		Optional<PowerNode> newNode02 = node.createPath(null, NodeTypes.Content.NAME);

		Assertions.assertTrue(newNode02.isPresent());
		Assertions.assertEquals(DEFAULT_TEST_NODE, newNode02.get().getPath());
	}

	@Test
	public void getOrCreateNode_sunshine() throws RepositoryException {
		// test only sunshine since the create part is
		// realized and tested via 'createPath'
		PowerNode defaultNode = getPowerNode(DEFAULT_TEST_NODE);
		PowerNode parentNode = getPowerNode("/blitzdings/de/new-one/ContentArea/0/ContentArea");

		Optional<PowerNode> nodeGet = parentNode.getOrCreateNode("node02", NodeTypes.Content.NAME);

		Assertions.assertTrue(nodeGet.isPresent());
		Assertions.assertEquals(DEFAULT_TEST_NODE, nodeGet.get().getPath());
		Assertions.assertTrue(nodeGet.get().isSame(defaultNode));

		Optional<PowerNode> nodeCreate = defaultNode.getOrCreateNode("new-node", NodeTypes.Content.NAME);

		Assertions.assertTrue(nodeCreate.isPresent());
		nodeCreate.get().getSession().save();
		Assertions.assertEquals(DEFAULT_TEST_NODE + "/new-node", nodeCreate.get().getPath());
	}

	@Test
	public void getByName_sunshine() {
		PowerNode defaultNode = getPowerNode(DEFAULT_TEST_NODE);
		PowerNode parentNode = getPowerNode("/blitzdings/de/new-one/ContentArea/0/ContentArea");
		PowerNode parentParentNode = getPowerNode("/blitzdings/de/new-one/ContentArea/0");

		// test get node (level1, existing)
		Optional<PowerNode> nodeGet = parentNode.getNodeByName("node02");
		Assertions.assertTrue(nodeGet.isPresent());
		Assertions.assertEquals(DEFAULT_TEST_NODE, nodeGet.get().getPath());
		Assertions.assertTrue(nodeGet.get().isSame(defaultNode));

		// test get node (level2, existing)
		Optional<PowerNode> nodeGetPath = parentParentNode.getNodeByName("ContentArea/node02");
		Assertions.assertTrue(nodeGetPath.isPresent());
		Assertions.assertEquals(DEFAULT_TEST_NODE, nodeGet.get().getPath());
		Assertions.assertTrue(nodeGet.get().isSame(defaultNode));

		// test get node (non existing)
		Optional<PowerNode> nodeCreate = defaultNode.getNodeByName("not-existing");
		Assertions.assertFalse(nodeCreate.isPresent());
	}

	@Test
	public void overwriteOrCreateNode_sunshine() {
		// test only sunshine since the create part is
		// realized and tested via 'createPath'

		PowerNode parent = getPowerNode(DEFAULT_TEST_NODE);
		String childName = "testNode";
		String nodeType = "mgnl:content";

		// create node
		Optional<PowerNode> childCreated = parent.overwriteOrCreateNode(childName, nodeType);
		Assertions.assertTrue(childCreated.isPresent());

		// set property
		childCreated.get().setProperty("wasCreated", true);
		Assertions.assertTrue(childCreated.get().hasProperty("wasCreated"));

		// get same node
		Optional<PowerNode> childGet = parent.getOrCreateNode(childName, nodeType);
		Assertions.assertTrue(childGet.isPresent());
		// property still there
		Assertions.assertTrue(childGet.get().hasProperty("wasCreated"));

		// override same node
		Optional<PowerNode> childOverridden = parent.overwriteOrCreateNode(childName, nodeType);
		Assertions.assertTrue(childOverridden.isPresent());
		// property gone
		Assertions.assertFalse(childOverridden.get().hasProperty("wasCreated"));
	}

	@Test
	public void getAncestors_sunshine() {
		PowerNode node = getPowerNode(DEFAULT_TEST_NODE);
		List<PowerNode> ancestors = node.getAncestors();

		Assertions.assertEquals(6, ancestors.size());

		PowerNode first = ancestors.get(0);
		Assertions.assertEquals("ContentArea", first.getName());

		PowerNode last = ancestors.get(ancestors.size() - 1);
		Assertions.assertEquals("blitzdings", last.getName());

		PowerNode parent = node.getParent();
		Assertions.assertTrue(ancestors.get(0).isSame(parent));
		parent = parent.getParent();
		Assertions.assertTrue(ancestors.get(1).isSame(parent));
		parent = parent.getParent();
		Assertions.assertTrue(ancestors.get(2).isSame(parent));
		parent = parent.getParent();
		Assertions.assertTrue(ancestors.get(3).isSame(parent));
		parent = parent.getParent();
		Assertions.assertTrue(ancestors.get(4).isSame(parent));
		parent = parent.getParent();
		Assertions.assertTrue(ancestors.get(5).isSame(parent));
	}

	@Test
	public void getAncestors_noAncestors() {
		PowerNode node = getPowerNode("/blitzdings");
		List<PowerNode> ancestors = node.getAncestors();

		Assertions.assertEquals(0, ancestors.size());
	}

	@Test
	public void getAncestorByLevel_sunshine() {
		PowerNode node = getPowerNode(DEFAULT_TEST_NODE);

		// level 0 -> Root node
		Optional<PowerNode> ancestorLevel0 = node.getAncestorByLevel(0);
		Assertions.assertTrue(ancestorLevel0.isPresent());
		PowerNode level0 = ancestorLevel0.get();
		Assertions.assertEquals("", level0.getName());

		// level 1 -> website root
		Optional<PowerNode> ancestorLevel1 = node.getAncestorByLevel(1);
		Assertions.assertTrue(ancestorLevel1.isPresent());
		PowerNode level1 = ancestorLevel1.get();
		Assertions.assertEquals("blitzdings", level1.getName());

		// level 2 -> language root
		Optional<PowerNode> ancestorLevel2 = node.getAncestorByLevel(2);
		Assertions.assertTrue(ancestorLevel2.isPresent());
		PowerNode level2 = ancestorLevel2.get();
		Assertions.assertEquals("de", level2.getName());

		// level 3 -> ancestor level 3
		Optional<PowerNode> ancestorLevel3 = node.getAncestorByLevel(3);
		Assertions.assertTrue(ancestorLevel3.isPresent());
		PowerNode level3 = ancestorLevel3.get();
		Assertions.assertEquals("new-one", level3.getName());

		// level 6 -> ancestor level 6 (in this test case -> parent)
		Optional<PowerNode> ancestorLevel6 = node.getAncestorByLevel(6);
		Assertions.assertTrue(ancestorLevel6.isPresent());
		PowerNode level6 = ancestorLevel6.get();
		Assertions.assertEquals("ContentArea", level6.getName());

		// level 7 -> ancestor level 7 (in this test case -> same)
		Optional<PowerNode> ancestorLevel7 = node.getAncestorByLevel(7);
		Assertions.assertTrue(ancestorLevel7.isPresent());
		PowerNode level7 = ancestorLevel7.get();
		Assertions.assertEquals("node02", level7.getName());
	}

	@Test
	public void getAncestorByLevel_levelToLow() {
		PowerNode node = getPowerNode(DEFAULT_TEST_NODE);

		// level 8 -> ancestor level 8 (in this test case -> child)
		Optional<PowerNode> ancestorLevel8 = node.getAncestorByLevel(8);
		Assertions.assertFalse(ancestorLevel8.isPresent());
	}

	@Test
	public void getAncestorByLevel_invalidLevels() {
		PowerNode node = getPowerNode(DEFAULT_TEST_NODE);

		Optional<PowerNode> ancestorNegative = node.getAncestorByLevel(-1);
		Assertions.assertFalse(ancestorNegative.isPresent());
	}

	@Test
	public void isDescendant_sunshine() {
		PowerNode descendant = getPowerNode(DEFAULT_TEST_NODE);
		PowerNode ancestor = getPowerNode("/blitzdings/de");
		PowerNode otherNode = getPowerNode("/blitzdings/fr");

		boolean isDescendant01 = descendant.isDescendantOf(ancestor);
		Assertions.assertTrue(isDescendant01);

		boolean isDescendant02 = descendant.isDescendantOf(otherNode);
		Assertions.assertFalse(isDescendant02);
	}

	@Test
	public void isDescendant_wrongParameters() {
		PowerNode descendant = getPowerNode(DEFAULT_TEST_NODE);

		boolean isDescendant01 = descendant.isDescendantOf(null);
		Assertions.assertFalse(isDescendant01);
	}

	@Test
	public void getChildNodes_sunshine() {
		PowerNode root = getPowerNode("/blitzdings");
		List<PowerNode> childNodes = root.getChildNodes();
		Assertions.assertEquals(3, childNodes.size());

		PowerNode de = getPowerNode("/blitzdings/de");
		PowerNode fr = getPowerNode("/blitzdings/fr");
		PowerNode it = getPowerNode("/blitzdings/it");

		Assertions.assertTrue(childNodes.contains(de));
		Assertions.assertTrue(childNodes.contains(fr));
		Assertions.assertTrue(childNodes.contains(it));
	}

	@Test
	public void getChildNodes_noChildren() {
		PowerNode node = getPowerNode(DEFAULT_TEST_NODE + "/child02");
		List<PowerNode> childNodes = node.getChildNodes();
		Assertions.assertTrue(childNodes.isEmpty());
	}

	@Test
	public void getChildNodesWithNodeType_sunshine() {
		PowerNode root = getPowerNode("/blitzdings");
		String typePage = "mgnl:page";
		String typeContent = "mgnl:content";

		root.addNode("test01", typeContent);
		root.addNode("test02", typeContent);

		List<PowerNode> childNodes = root.getChildNodes();
		Assertions.assertEquals(5, childNodes.size());

		List<PowerNode> childNodesFilteredPage = root.getChildNodes(typePage);
		Assertions.assertEquals(3, childNodesFilteredPage.size());

		List<PowerNode> childNodesFilteredContent = root.getChildNodes(typeContent);
		Assertions.assertEquals(2, childNodesFilteredContent.size());
	}

	@Test
	public void getChildNodesWithNodeType_noMatchingNodeType() {
		PowerNode root = getPowerNode("/blitzdings");
		String typeComponent = "mgnl:component";

		List<PowerNode> childNodesFiltered = root.getChildNodes(typeComponent);
		Assertions.assertTrue(childNodesFiltered.isEmpty());
	}

	@Test
	public void getChildNodesWithNodeType_noNodeType() {
		PowerNode root = getPowerNode("/blitzdings");

		List<PowerNode> childNodesFiltered = root.getChildNodes(StringUtils.EMPTY);
		Assertions.assertTrue(childNodesFiltered.isEmpty());
	}

	@Test
	public void getChildNodesWithPredicate_sunshine() {
		PowerNode root = getPowerNode("/blitzdings");
		String typePage = "mgnl:page";
		String typeContent = "mgnl:content";
		Predicate pagePredicate = new NodeTypePredicate(typePage, false);
		Predicate contentPredicate = new NodeTypePredicate(typeContent, false);

		root.addNode("test01", typeContent);
		root.addNode("test02", typeContent);

		List<PowerNode> childNodes = root.getChildNodes();
		Assertions.assertEquals(5, childNodes.size());

		List<PowerNode> childNodesFilteredPage = root.getChildNodes(pagePredicate);
		Assertions.assertEquals(3, childNodesFilteredPage.size());

		List<PowerNode> childNodesFilteredContent = root.getChildNodes(contentPredicate);
		Assertions.assertEquals(2, childNodesFilteredContent.size());
	}

	@Test
	public void getChildNodesWithPredicate_noMatchingPredicate() {
		PowerNode root = getPowerNode("/blitzdings");
		String typeComponent = "mgnl:component";
		Predicate componentPredicate = new NodeTypePredicate(typeComponent, false);

		List<PowerNode> childNodesFiltered = root.getChildNodes(componentPredicate);
		Assertions.assertTrue(childNodesFiltered.isEmpty());
	}

	@Test
	public void getChildNodesWithPredicate_noPredicate() {
		PowerNode root = getPowerNode("/blitzdings");

		List<PowerNode> childNodesFiltered = root.getChildNodes(Predicate.FALSE);
		Assertions.assertTrue(childNodesFiltered.isEmpty());
	}

	@Test
	public void getChildNodesRecursiveWithNodeType_sunshine() {
		PowerNode parent = getPowerNode("/blitzdings");
		String typePage = "mgnl:page";

		// get all pages recursive
		List<PowerNode> childNodesRecursivePage = parent.getChildNodesRecursive(typePage);
		Assertions.assertEquals(12, childNodesRecursivePage.size());

		// create a page with a uncommon type deep in the tree
		String typeFolder = "mgnl:folder";
		parent.createPath("/de/new-one/ContentArea/0/ContentArea/node02/custom", typeFolder);
		// get only this node when traversing recursively
		List<PowerNode> childNodesRecursiveUser = parent.getChildNodesRecursive(typeFolder);
		Assertions.assertEquals(1, childNodesRecursiveUser.size());
	}

	@Test
	public void getChildNodesRecursiveWithNodeType_noNodeType() {
		PowerNode root = getPowerNode("/blitzdings");

		List<PowerNode> childNodesFiltered = root.getChildNodesRecursive(StringUtils.EMPTY);
		Assertions.assertTrue(childNodesFiltered.isEmpty());
	}

	@Test
	public void getChildNodesRecursiveWithPredicate_sunshine() {
		PowerNode parent = getPowerNode("/blitzdings");
		String typePage = "mgnl:page";
		Predicate pagePredicate = new NodeTypePredicate(typePage, false);

		// get all pages recursive
		List<PowerNode> childNodesRecursivePage = parent.getChildNodesRecursive(pagePredicate);
		Assertions.assertEquals(12, childNodesRecursivePage.size());

		// create a page with a uncommon type deep in the tree
		String typeFolder = "mgnl:folder";
		parent.createPath("/de/new-one/ContentArea/0/ContentArea/node02/custom", typeFolder);
		// get only this node when traversing recursively
		List<PowerNode> childNodesRecursiveUser = parent.getChildNodesRecursive(typeFolder);
		Assertions.assertEquals(1, childNodesRecursiveUser.size());
	}

	@Test
	public void getChildNodesRecursiveWithPredicate_noPredicate() {
		PowerNode root = getPowerNode("/blitzdings");

		List<PowerNode> childNodesFiltered = root.getChildNodesRecursive(Predicate.FALSE);
		Assertions.assertTrue(childNodesFiltered.isEmpty());
	}

	@Test
	public void getSiblings_sunshine() {
		PowerNode de = getPowerNode("/blitzdings/de");
		List<PowerNode> siblings = de.getSiblings();
		Assertions.assertEquals(2, siblings.size());

		PowerNode fr = getPowerNode("/blitzdings/fr");
		PowerNode it = getPowerNode("/blitzdings/it");

		siblings.remove(fr);
		siblings.remove(it);

		Assertions.assertTrue(siblings.isEmpty());
	}

	@Test
	public void getSiblings_noSiblings() {
		PowerNode root = getPowerNode("/blitzdings");
		List<PowerNode> siblings = root.getSiblings();
		Assertions.assertTrue(siblings.isEmpty());
		Assertions.assertEquals(0, siblings.size());
	}

	@Test
	public void getSiblings_noParent() {
		// workspace root has no parent to get
		// siblings from, and also has never siblings,
		// thus it fail with an ItemNotFoundException.
		PowerNode workspaceRoot = getPowerNode("/");
		PowerNodeException e = Assertions.assertThrows(
				PowerNodeException.class,
				workspaceRoot::getSiblings
		);

		Assertions.assertEquals(PowerNodeException.Type.JCR_REPOSITORY, e.getType());
		Assertions.assertEquals(ItemNotFoundException.class, e.getCause().getCause().getClass());
	}

	@Test
	public void getSiblingAfter_sunshine() {
		PowerNode de = getPowerNode("/blitzdings/de");
		Optional<PowerNode> siblingAfter = de.getSiblingAfter();
		Assertions.assertTrue(siblingAfter.isPresent());

		PowerNode fr = getPowerNode("/blitzdings/fr");
		Assertions.assertEquals(fr, siblingAfter.get());
	}

	@Test
	public void getSiblingAfter_noSiblings() {
		PowerNode root = getPowerNode("/blitzdings/");
		Optional<PowerNode> siblingAfter = root.getSiblingAfter();
		Assertions.assertFalse(siblingAfter.isPresent());
	}

	@Test
	public void getSiblingAfter_noSiblingAfter() {
		PowerNode it = getPowerNode("/blitzdings/it");
		Optional<PowerNode> siblingAfter = it.getSiblingAfter();
		Assertions.assertFalse(siblingAfter.isPresent());
	}

	@Test
	public void getSiblingByName_sunshine() {
		PowerNode de = getPowerNode("/blitzdings/de");
		Optional<PowerNode> siblingFr = de.getSibling("it");
		Assertions.assertTrue(siblingFr.isPresent());

		PowerNode fr = getPowerNode("/blitzdings/it");
		Assertions.assertEquals(fr, siblingFr.get());
	}

	@Test
	public void getSiblingByName_noSiblingWithThatName() {
		PowerNode de = getPowerNode("/blitzdings/de");
		Optional<PowerNode> siblingFr = de.getSibling("nope");
		Assertions.assertFalse(siblingFr.isPresent());
	}

	@Test
	public void getSiblingByName_wrongParameter() {
		PowerNode de = getPowerNode("/blitzdings/de");
		Optional<PowerNode> siblingFr = de.getSibling(null);
		Assertions.assertFalse(siblingFr.isPresent());
	}

	@Test
	public void getSiblingByName_noParent() {
		// workspace root has no parent to get
		// siblings from, and also has never siblings,
		// thus it fail with an ItemNotFoundException.
		PowerNode workspaceRoot = getPowerNode("/");

		PowerNodeException e = Assertions.assertThrows(
				PowerNodeException.class,
				() -> workspaceRoot.getSibling("brotherOrSister")
		);

		Assertions.assertEquals(PowerNodeException.Type.JCR_REPOSITORY, e.getType());
		Assertions.assertEquals(ItemNotFoundException.class, e.getCause().getCause().getClass());
	}

	@Test
	public void sessionSave_sunshine() {
		String newChild = "newChild";
		String nodeType = "mgnl:content";

		// get node, new session
		PowerNode node01 = getPowerNode(DEFAULT_TEST_NODE);
		// add child
		node01.addNode(newChild, nodeType);
		// child is present
		Assertions.assertTrue(node01.hasNode(newChild));

		// get another node, new session
		PowerNode node02 = getPowerNode(DEFAULT_TEST_NODE);
		// last session was not saved, child is not present
		Assertions.assertFalse(node02.hasNode(newChild));
		// add child again
		node02.addNode(newChild, nodeType);
		// save session
		node02.sessionSave();

		// get another node, new session
		PowerNode node03 = getPowerNode(DEFAULT_TEST_NODE);
		// child is present, since last session was saved
		Assertions.assertTrue(node03.hasNode(newChild));
	}

	@Test
	public void sessionLogout_sunshine() {
		PowerNode node01 = getPowerNode(DEFAULT_TEST_NODE);
		Assertions.assertTrue(node01.getSession().isLive());
		node01.sessionLogout();
		Assertions.assertFalse(node01.getSession().isLive());

		PowerNodeException e = Assertions.assertThrows(
				PowerNodeException.class,
				node01::getName
		);

		Assertions.assertEquals(PowerNodeException.Type.JCR_REPOSITORY, e.getType());
		Assertions.assertEquals(RepositoryException.class, e.getCause().getCause().getClass());
		Assertions.assertEquals("This session has been closed. See the chained exception for a trace of where the session was closed.", e.getCause().getCause().getMessage());
	}

}
