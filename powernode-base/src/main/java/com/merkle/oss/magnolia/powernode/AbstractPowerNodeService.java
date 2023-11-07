package com.merkle.oss.magnolia.powernode;

import javax.jcr.Node;
import javax.jcr.Session;
import java.util.Optional;

public abstract class AbstractPowerNodeService<N extends AbstractPowerNode<N>> {
	private final NodeService nodeService;
	private final AbstractPowerNodeDecorator<N> powerNodeDecorator;

	protected AbstractPowerNodeService(
			final NodeService nodeService,
			final AbstractPowerNodeDecorator<N> powerNodeDecorator
	) {
		this.nodeService = nodeService;
		this.powerNodeDecorator = powerNodeDecorator;
	}

	public N convertToPowerNode(final Node node) {
		return powerNodeDecorator.wrapNode(node);
	}

	public Optional<Session> getSession(final String workspace) {
		return nodeService.getSession(workspace);
	}

	public Optional<Session> getSystemSession(final String workspace) {
		return nodeService.getSystemSession(workspace);
	}

	public Optional<N> getByIdentifier(final String workspace, final String identifier) {
		return nodeService.getByIdentifier(workspace, identifier).map(powerNodeDecorator::wrapNode);
	}

	public Optional<N> getByIdentifier(final Session session, final String identifier) {
		return nodeService.getByIdentifier(session, identifier).map(powerNodeDecorator::wrapNode);
	}

	public Optional<N> getByPath(final String workspace, final String path) {
		return nodeService.getByPath(workspace, path).map(powerNodeDecorator::wrapNode);
	}

	public Optional<N> getByPath(final Session session, final String path) {
		return nodeService.getByPath(session, path).map(powerNodeDecorator::wrapNode);
	}
}