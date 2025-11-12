package com.merkle.oss.magnolia.powernode.mock;


import jakarta.annotation.Nullable;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import java.util.Objects;

public class MockNode extends info.magnolia.test.mock.jcr.MockNode {
	@Nullable
	private NodeType primaryNodeType;

	MockNode(final MockSession session) {
		super(session);
		setPrimaryType("rep:root");
	}

	public MockNode(final String name) {
		this(name, "nt:base");
	}

	public MockNode(final String name, final String primaryNodeType) {
		super(name);
		setPrimaryType(primaryNodeType);
	}

	@Override
	public Node addNode(final String relPath, final String primaryNodeTypeName) throws RepositoryException {
		String nodeName = relPath;
		info.magnolia.test.mock.jcr.MockNode nodesParent = this;
		int lastSlashsPosition = relPath.lastIndexOf("/");
		if (lastSlashsPosition >= 0) {
			String relPathToNode = relPath.substring(0, lastSlashsPosition);
			nodesParent = (info.magnolia.test.mock.jcr.MockNode)this.getNode(relPathToNode);
			nodeName = relPath.substring(lastSlashsPosition + 1);
		}

		MockNode newChild = new MockNode(nodeName, primaryNodeTypeName);
		nodesParent.addNode(newChild);
		return newChild;
	}

	@Override
	public NodeType getPrimaryNodeType() {
		return primaryNodeType;
	}

	@Override
	public void setPrimaryType(final String primaryType) {
		setPrimaryNodeType(new MockNodeType(primaryType));
	}

	@Override
	public void setPrimaryNodeType(final NodeType primaryNodeType) {
		this.primaryNodeType = primaryNodeType;
	}

	@Override
	public Session getSession() {
		final Session session = super.getSession();
		return session != null ? session : new MockSession("testing");
	}

	public static class MockNodeType extends info.magnolia.test.mock.MockNodeType {
		private final String nodeTypeName;

		public MockNodeType(final String nodeTypeName) {
			super(nodeTypeName);
			this.nodeTypeName = nodeTypeName;
		}

		@Override
		public boolean isNodeType(final String nodeTypeName) {
			return Objects.equals(this.nodeTypeName, nodeTypeName);
		}
	}
}
