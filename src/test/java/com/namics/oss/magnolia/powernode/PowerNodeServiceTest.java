package com.namics.oss.magnolia.powernode;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.repository.RepositoryConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class PowerNodeServiceTest extends AbstractPowerNodeTest {

	private Session getSession(String workspace) {
		return powerNodeService.getSystemSession(workspace).orElse(null);
	}

	@Test
	public void getNodeByUuid_sunshine() {
		String uuid = "fb7708fa-67d3-4d80-a03e-7e2bcf5731e9";
		Optional<PowerNode> node = powerNodeService.getNodeByUuid(uuid, getSession(RepositoryConstants.WEBSITE));
		Assertions.assertTrue(node.isPresent());
		Assertions.assertEquals(uuid, node.get().getUUID());
	}

	@Test
	public void getNodeByUuid_invalidWorkspace() {
		String uuid = "fb7708fa-67d3-4d80-a03e-7e2bcf5731e9";
		Optional<PowerNode> node = powerNodeService.getNodeByUuid(uuid, getSession("nope"));
		Assertions.assertFalse(node.isPresent());
	}

	@Test
	public void getNodeByUuid_invalidUuid() {
		String invalidUuid = "this-is-not-a-uuid";
		Optional<PowerNode> node = powerNodeService.getNodeByUuid(invalidUuid, getSession(RepositoryConstants.WEBSITE));
		Assertions.assertFalse(node.isPresent());
	}

	@Test
	public void getNodeByUuid_wrongUuid() {
		String doesNotExistUuid = "aabb08fa-67d3-4d80-a03e-7e2bcf5731e9";
		Optional<PowerNode> node = powerNodeService.getNodeByUuid(doesNotExistUuid, getSession(RepositoryConstants.WEBSITE));
		Assertions.assertFalse(node.isPresent());
	}

	@Test
	public void getNodeByUuid_wrongParameters() {
		String uuid = "fb7708fa-67d3-4d80-a03e-7e2bcf5731e9";

		Optional<PowerNode> node01 = powerNodeService.getNodeByUuid(null, getSession(RepositoryConstants.WEBSITE));
		Assertions.assertFalse(node01.isPresent());

		Optional<PowerNode> node02 = powerNodeService.getNodeByUuid(uuid, null);
		Assertions.assertFalse(node02.isPresent());

		Optional<PowerNode> node03 = powerNodeService.getNodeByUuid("", getSession(RepositoryConstants.WEBSITE));
		Assertions.assertFalse(node03.isPresent());

		Optional<PowerNode> node04 = powerNodeService.getNodeByUuid(uuid, getSession(""));
		Assertions.assertFalse(node04.isPresent());

		Optional<PowerNode> node05 = powerNodeService.getNodeByUuid(" ", getSession(RepositoryConstants.WEBSITE));
		Assertions.assertFalse(node05.isPresent());

		Optional<PowerNode> node06 = powerNodeService.getNodeByUuid(uuid, getSession("   "));
		Assertions.assertFalse(node06.isPresent());
	}

	@Test
	public void getNodeByPath_sunshine() {
		String nodePath = "/blitzdings/de/new-one/ContentArea/0/ContentArea/node02";
		Optional<PowerNode> node = powerNodeService.getNodeByPath(nodePath, getSession(RepositoryConstants.WEBSITE));
		Assertions.assertTrue(node.isPresent());
		Assertions.assertEquals(nodePath, node.get().getPath());
	}

	@Test
	public void getNodeByPath_invalidWorkspace() {
		String nodePath = "/blitzdings/de/new-one/ContentArea/0/ContentArea/node02";
		Optional<PowerNode> node = powerNodeService.getNodeByPath(nodePath, getSession("nope"));
		Assertions.assertFalse(node.isPresent());
	}

	@Test
	public void getNodeByPath_invalidPath() {
		String invalidNodePath = "/blitzdings/de/new-one/ContentArea/0/ContentArea/node02nopeNope";
		Optional<PowerNode> node = powerNodeService.getNodeByPath(invalidNodePath, getSession(RepositoryConstants.WEBSITE));
		Assertions.assertFalse(node.isPresent());
	}

	@Test
	public void getNodeByPath_wrongParameters() {
		String nodePath = "/blitzdings/de/new-one/ContentArea/0/ContentArea/node02";

		Optional<PowerNode> node01 = powerNodeService.getNodeByPath(null, getSession(RepositoryConstants.WEBSITE));
		Assertions.assertFalse(node01.isPresent());

		Optional<PowerNode> node02 = powerNodeService.getNodeByPath(nodePath, null);
		Assertions.assertFalse(node02.isPresent());

		Optional<PowerNode> node03 = powerNodeService.getNodeByPath("", getSession(RepositoryConstants.WEBSITE));
		Assertions.assertFalse(node03.isPresent());

		Optional<PowerNode> node04 = powerNodeService.getNodeByPath(nodePath, getSession(""));
		Assertions.assertFalse(node04.isPresent());

		Optional<PowerNode> node05 = powerNodeService.getNodeByPath(" ", getSession(RepositoryConstants.WEBSITE));
		Assertions.assertFalse(node05.isPresent());

		Optional<PowerNode> node06 = powerNodeService.getNodeByPath(nodePath, getSession("   "));
		Assertions.assertFalse(node06.isPresent());
	}

	@Test
	public void getWorkspaceRootNode_sunshine() {
		Optional<PowerNode> workspaceRootNode = powerNodeService.getWorkspaceRootNode(getSession(RepositoryConstants.WEBSITE));
		Assertions.assertTrue(workspaceRootNode.isPresent());
		Assertions.assertEquals("/", workspaceRootNode.get().getPath());
		Assertions.assertEquals("blitzdings", workspaceRootNode.get().getNode("blitzdings").getName());
	}

	@Test
	public void getWorkspaceRootNode_invalidWorkspace() {
		Optional<PowerNode> workspaceRootNode = powerNodeService.getWorkspaceRootNode(getSession("nope"));
		Assertions.assertFalse(workspaceRootNode.isPresent());
	}

	@Test
	public void getWorkspaceRootNode_wrongParameters() {
		Optional<PowerNode> workspaceRootNode = powerNodeService.getWorkspaceRootNode(getSession("nope"));
		Assertions.assertFalse(workspaceRootNode.isPresent());
	}

	@Test
	public void nodeExists_sunshine() {
		String nodePath = "/blitzdings/it/new-one/gtmTags";
		boolean exists = powerNodeService.nodeExists(nodePath, getSession(RepositoryConstants.WEBSITE));
		Assertions.assertTrue(exists);
	}

	@Test
	public void nodeExists_doesNotExist() {
		String nodePathDoesNotExist = "/blitzdings/it/new-one/doesNotExist";
		boolean exists = powerNodeService.nodeExists(nodePathDoesNotExist, getSession(RepositoryConstants.WEBSITE));
		Assertions.assertFalse(exists);
	}

	@Test
	public void isValidNodeName() {
		String nameValid01 = "nodeName";
		String nameValid02 = "node?Name";
		String nameValid03 = "0";
		String nameInvalid01 = "node*Name";
		String nameInvalid02 = "hello  % nope";

		Assertions.assertTrue(powerNodeService.isValidNodeName(nameValid01));
		Assertions.assertTrue(powerNodeService.isValidNodeName(nameValid02));
		Assertions.assertTrue(powerNodeService.isValidNodeName(nameValid03));

		Assertions.assertFalse(powerNodeService.isValidNodeName(nameInvalid01));
		Assertions.assertFalse(powerNodeService.isValidNodeName(nameInvalid02));
		Assertions.assertFalse(powerNodeService.isValidNodeName(null));
		Assertions.assertFalse(powerNodeService.isValidNodeName(""));
		Assertions.assertFalse(powerNodeService.isValidNodeName("     "));
	}

	@Test
	public void createValidNodeName() {
		String name01 = "normalName";
		String name02 = "nameWith*Illegal%Chars";
		String name01Valid = powerNodeService.createValidNodeName(name01, "_");
		String name02Valid = powerNodeService.createValidNodeName(name02, "_");
		Assertions.assertEquals(name01, name01Valid);
		Assertions.assertEquals("nameWith_Illegal_Chars", name02Valid);
	}

	@Test
	public void nodeIteratorToStream_sunshine() {
		Optional<PowerNode> parent = powerNodeService.getNodeByPath("/blitzdings/de", getSession(RepositoryConstants.WEBSITE));
		Assertions.assertTrue(parent.isPresent());
		NodeIterator childNodeIterator = parent.get().getNodes();
		Stream<PowerNode> nodeStream = powerNodeService.nodeIteratorToStream(childNodeIterator);
		Assertions.assertEquals(4, nodeStream.count());
	}

	@Test
	public void nodeIteratorToStream_wrongParameters() {
		Stream<PowerNode> nodeStream = powerNodeService.nodeIteratorToStream(null);
		Assertions.assertEquals(0, nodeStream.count());
	}

	@Test
	public void nodeIteratorToList_sunshine() {
		Optional<PowerNode> parent = powerNodeService.getNodeByPath("/blitzdings/de", getSession(RepositoryConstants.WEBSITE));
		Assertions.assertTrue(parent.isPresent());
		NodeIterator childNodeIterator = parent.get().getNodes();
		List<PowerNode> nodeList = powerNodeService.nodeIteratorToList(childNodeIterator);
		Assertions.assertEquals(4, nodeList.size());
	}

	@Test
	public void isValidUuid_sunshine() {
		String validUuid = "b4ab03a9-958a-4f37-8c71-461ca0cea552";
		Assertions.assertTrue(powerNodeService.isValidUuid(validUuid));
	}

	@Test
	public void isValidUuid_invalidUuid() {
		String invalidUuid = "b4ab03a9-958a-4f37-8c71-461ca0ceNOPE-Nope";
		Assertions.assertFalse(powerNodeService.isValidUuid(invalidUuid));
	}

	@Test
	public void isValidUuid_wrongParameters() {
		Assertions.assertFalse(powerNodeService.isValidUuid(null));
	}

	@Test
	public void setMgnlCreated_sunshine() throws RepositoryException {
		Optional<PowerNode> node = powerNodeService.getNodeByPath("/blitzdings/de", getSession(RepositoryConstants.WEBSITE));
		Assertions.assertTrue(node.isPresent());

		Node nakedNode = node.get().unwrap();
		Node newNode = nakedNode.addNode("newNode", "mgnl:content");

		Assertions.assertFalse(newNode.hasProperty(NodeTypes.Created.NAME));
		Assertions.assertFalse(newNode.hasProperty(NodeTypes.Created.CREATED_BY));

		powerNodeService.setMgnlCreated(newNode);

		Assertions.assertTrue(newNode.hasProperty(NodeTypes.Created.NAME));
		Assertions.assertTrue(newNode.hasProperty(NodeTypes.Created.CREATED_BY));
	}

	@Test
	public void updateMgnlModified_sunshine() throws RepositoryException {
		Optional<PowerNode> node = powerNodeService.getNodeByPath("/blitzdings/de", getSession(RepositoryConstants.WEBSITE));
		Assertions.assertTrue(node.isPresent());

		Node nakedNode = node.get().unwrap();
		Node newNode = nakedNode.addNode("newNode", "mgnl:content");

		Assertions.assertFalse(newNode.hasProperty(NodeTypes.LastModified.NAME));
		Assertions.assertFalse(newNode.hasProperty(NodeTypes.LastModified.LAST_MODIFIED_BY));

		powerNodeService.updateMgnlModified(newNode);

		Assertions.assertTrue(newNode.hasProperty(NodeTypes.LastModified.NAME));
		Assertions.assertTrue(newNode.hasProperty(NodeTypes.LastModified.LAST_MODIFIED_BY));
	}

}
