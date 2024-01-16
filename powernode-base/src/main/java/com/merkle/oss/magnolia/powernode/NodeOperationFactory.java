package com.merkle.oss.magnolia.powernode;

import info.magnolia.jcr.nodebuilder.AbstractNodeOperation;
import info.magnolia.jcr.nodebuilder.ErrorHandler;
import info.magnolia.jcr.nodebuilder.NodeOperation;
import info.magnolia.jcr.predicate.JCRMgnlPropertyHidingPredicate;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.StreamSupport;

public class NodeOperationFactory {
	private static final JCRMgnlPropertyHidingPredicate JCR_MGNL_PROPERTY_HIDING_PREDICATE = new JCRMgnlPropertyHidingPredicate();
	private final NodeService nodeService;

	@Inject
	public NodeOperationFactory(final NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public NodeOperation accept(final BiConsumer<NodeService, Node> consumer) {
		return apply((nodeService, context) -> {
			consumer.accept(nodeService, context);
			return context;
		});
	}

	public NodeOperation apply(final BiFunction<NodeService, Node, Node> operation) {
		return new AbstractNodeOperation() {
			@Override
			protected Node doExec(final Node contextNode, final ErrorHandler errorHandler) {
				return operation.apply(nodeService, contextNode);
			}
		};
	}

	public NodeOperation copy(final String relativeSourcePath, final String relativeDestinationPath) {
		return new AbstractNodeOperation() {
			@Override
			protected Node doExec(final Node context, final ErrorHandler errorHandler) throws RepositoryException {
				return copyAbsolute(
						context.getSession(),
						context.getPath() + "/" + relativeSourcePath,
						context.getPath() + "/" + relativeDestinationPath
				);
			}
		};
	}

	public NodeOperation copyAbsolute(final String source, final String destination) {
		return new AbstractNodeOperation() {
			@Override
			protected Node doExec(final Node context, final ErrorHandler errorHandler) throws RepositoryException {
				return copyAbsolute(context.getSession(), source, destination);
			}
		};
	}

	private Node copyAbsolute(final Session session, final String source, final String destination) throws RepositoryException {
		if (!session.nodeExists(destination)) {
			final Node sourceNode = session.getNode(source);
			NodeUtil.copyInSession(sourceNode, destination);
		}
		return session.getNode(destination);
	}

	public NodeOperation getChild(final String relativePath) {
		return apply((nodeService, context) ->
				nodeService.getChild(context, relativePath).orElseThrow(() ->
					new NullPointerException(nodeService.getOrThrow(context::getPath)+" has no child "+relativePath)
				)
		);
	}

	public NodeOperation getOrAddContentNode(final String relativePath) {
		return getOrAddNode(relativePath, NodeTypes.ContentNode.NAME);
	}
	public NodeOperation getOrAddNode(final String relativePath) {
		return getOrAddNode(relativePath, null);
	}
	public NodeOperation getOrAddNode(final String relativePath, @Nullable final String nodeType) {
		return apply((nodeService, context) -> getOrAddNode(context, relativePath, nodeType));
	}
	private Node getOrAddNode(final Node context, final String relativePath, @Nullable final String nodeType) {
		final Provider<String> nodeTypeProvider = () -> Optional.ofNullable(nodeType).orElseGet(() ->
				nodeService.getOrThrow(() -> context.getPrimaryNodeType().getName())
		);
		return nodeService.getOrAddChild(context, nodeTypeProvider.get(), relativePath);
	}

	public NodeOperation removeNode(final String relativePath) {
		return accept((nodeService, context) ->
				nodeService.getChild(context, relativePath).ifPresent(child -> nodeService.run(child::remove))
		);
	}

	public NodeOperation orderBefore(final String siblingName) {
		return accept((nodeService, context) -> nodeService.run(() -> NodeUtil.orderBefore(context, siblingName)));
	}
	public NodeOperation orderFirst() {
		return accept((nodeService, context) -> nodeService.run(() -> NodeUtil.orderFirst(context)));
	}
	public NodeOperation orderAfter(final String siblingName) {
		return accept((nodeService, context) -> nodeService.run(() -> NodeUtil.orderAfter(context, siblingName)));
	}
	public NodeOperation orderLast() {
		return accept((nodeService, context) -> nodeService.run(() -> NodeUtil.orderLast(context)));
	}

	public <T> NodeOperation setProperty(final String propertyName, @Nullable final T value, final PropertyService.ValueFactory<T> factory) {
		return accept((nodeService, context) -> nodeService.setProperty(context, propertyName, value, factory));
	}
	public <T> NodeOperation setProperty(final String propertyName, final Locale locale, @Nullable final T value, final PropertyService.ValueFactory<T> factory) {
		return accept((nodeService, context) -> nodeService.setProperty(context, propertyName, locale, value, factory));
	}
	public <T> NodeOperation setPropertyOnlyIfMissing(final String propertyName, @Nullable final T value, final PropertyService.ValueFactory<T> factory) {
		return accept((nodeService, context) -> {
			if(!nodeService.hasProperty(context, propertyName)) {
				nodeService.setProperty(context, propertyName, value, factory);
			}
		});
	}
	public <T> NodeOperation setPropertyOnlyIfMissing(final String propertyName, final Locale locale, @Nullable final T value, final PropertyService.ValueFactory<T> factory) {
		return accept((nodeService, context) -> {
			if(!nodeService.hasProperty(context, propertyName, locale)) {
				nodeService.setProperty(context, propertyName, locale, value, factory);
			}
		});
	}

	public NodeOperation setClassProperty(final Class<?> clazz) {
		return setProperty("class", clazz.getName(), ValueConverter::toValue);
	}

	public NodeOperation setEnabledProperty(final boolean enabled) {
		return setProperty("enabled", enabled, ValueConverter::toValue);
	}

	public NodeOperation removeProperty(final String name) {
		return accept((nodeService, context) -> nodeService.removeProperty(context, name));
	}
	public NodeOperation removeProperty(final String name, final Locale locale) {
		return accept((nodeService, context) -> nodeService.removeProperty(context, name, locale));
	}

	public NodeOperation clearProperties() {
		return accept((nodeService, context) ->
				StreamSupport
						.stream(Spliterators.spliteratorUnknownSize(
								(Iterator< Property>)nodeService.getOrThrow(context::getProperties),
								Spliterator.ORDERED),
								false
						)
						.filter(JCR_MGNL_PROPERTY_HIDING_PREDICATE::evaluateTyped)
						.forEach(property ->
								nodeService.run(property::remove)
						)
		);
	}
}
