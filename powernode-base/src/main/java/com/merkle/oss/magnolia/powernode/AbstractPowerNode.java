package com.merkle.oss.magnolia.powernode;

import info.magnolia.jcr.decoration.ContentDecoratorNodeWrapper;
import info.magnolia.jcr.util.NodeTypes;

import javax.annotation.Nullable;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class AbstractPowerNode<N extends AbstractPowerNode<N>> extends ContentDecoratorNodeWrapper<AbstractPowerNodeDecorator<N>> {
	private final NodeService nodeService;

	protected AbstractPowerNode(
			final NodeService nodeService,
			final Node node,
			final AbstractPowerNodeDecorator<N> decorator
	) {
		super(node, decorator);
		this.nodeService = nodeService;
	}

	public void run(final RepositoryConsumer consumer) {
		nodeService.run(() -> consumer.accept(getWrappedNode()));
	}

	public <T> T getOrThrow(final RepositoryFunction<T> function) {
		return nodeService.getOrThrow(() -> function.apply(getWrappedNode()));
	}

	public <T> Optional<T> get(final RepositoryFunction<T> provider) {
		return nodeService.get(() -> provider.apply(getWrappedNode()));
	}

	public interface RepositoryFunction<T> {
		T apply(Node node) throws RepositoryException;
	}

	public interface RepositoryConsumer {
		void accept(Node node) throws RepositoryException;
	}


	@Override
	protected N wrapNode(final Node node) {
		return getContentDecorator().wrapNode(node);
	}

	public Optional<String> getTemplate() {
		return getProperty(NodeTypes.Renderable.TEMPLATE, ValueConverter::getString);
	}

	public void rename(final String newName) {
		nodeService.rename(getWrappedNode(), newName);
	}

	public void move(final Node newParent) {
		nodeService.move(getWrappedNode(), newParent);
	}

	public N getOrAddChild(final String relPath, final String primaryNodeTypeName) {
		return wrapNode(nodeService.getOrAddChild(getWrappedNode(), primaryNodeTypeName, relPath));
	}
	/**
	 * Creates child by localizing all relative path names.<br>
	 * e.g. /node1/child_de/grandchild_de
	 */
	public N getOrAddChild(final String relPath, final String primaryNodeTypeName, final Locale locale) {
		return wrapNode(nodeService.getOrAddChild(getWrappedNode(), primaryNodeTypeName, relPath, locale));
	}

	public Optional<N> getChild(final String relativePath) {
		return nodeService.getChild(getWrappedNode(), relativePath).map(this::wrapNode);
	}

	/**
	 * Gets child by localizing all relative path names.<br>
	 * e.g. /node1/child_de/grandchild_de
	 */
	public Optional<N> getChild(final String relativePath, final Locale locale) {
		return nodeService.getChild(getWrappedNode(), relativePath, locale).map(this::wrapNode);
	}

	public Stream<N> streamChildren() {
		return nodeService.streamChildren(getWrappedNode()).map(this::wrapNode);
	}

	public Stream<N> streamChildren(final Predicate<N> predicate) {
		return nodeService.streamChildren(getWrappedNode(), node -> predicate.test(wrapNode(node))).map(this::wrapNode);
	}

	public Stream<N> streamChildrenRecursive() {
		return nodeService.streamChildrenRecursive(getWrappedNode()).map(this::wrapNode);
	}

	/**
	 * @param predicate has no impact on the traversed children (only filters)
	 */
	public Stream<N> streamChildrenRecursive(final Predicate<N> predicate) {
		return nodeService.streamChildrenRecursive(getWrappedNode(), node -> predicate.test(wrapNode(node))).map(this::wrapNode);
	}


	public Optional<N> getParentOptional() {
		return nodeService.getParent(getWrappedNode()).map(this::wrapNode);
	}

	public Optional<N> getAncestor(final Predicate<N> predicate) {
		return nodeService.getAncestor(getWrappedNode(), node -> predicate.test(wrapNode(node))).map(this::wrapNode);
	}

	//Override due to link uuid conversion (see ValueConverter)
	@Override
	public Property setProperty(final String propertyName, @Nullable final String value) {
		return setProperty(propertyName, value, ValueConverter::toValue);
	}
	@Override
	public Property setProperty(final String propertyName, final String[] values) {
		return setMultiProperty(propertyName, List.of(values), ValueConverter::toValue);
	}

	public boolean hasProperty(final String propertyName) {
		return nodeService.hasProperty(getWrappedNode(), propertyName);
	}
	public boolean hasProperty(final String propertyName, final Locale locale) {
		return nodeService.hasProperty(getWrappedNode(), propertyName, locale);
	}

	public <T> Property setProperty(final String propertyName, @Nullable final T value, final PropertyService.ValueFactory<T> factory) {
		return nodeService.setProperty(getWrappedNode(), propertyName, value, factory);
	}
	public <T> Property setProperty(final String propertyName, final Locale locale, @Nullable final T value, final PropertyService.ValueFactory<T> factory) {
		return nodeService.setProperty(getWrappedNode(), propertyName, locale, value, factory);
	}
	public <T> Property setMultiProperty(final String propertyName, final Iterable<T> values, final PropertyService.ValueFactory<T> factory) {
		return nodeService.setMultiProperty(getWrappedNode(), propertyName, values, factory);
	}
	public <T> Property setMultiProperty(final String propertyName, final Locale locale, final Iterable<T> values, final PropertyService.ValueFactory<T> factory) {
		return nodeService.setMultiProperty(getWrappedNode(), propertyName, locale, values, factory);
	}

	public <T> Optional<T> getProperty(final String propertyName, final PropertyService.PropertyProvider<T> provider) {
		return nodeService.getProperty(getWrappedNode(), propertyName, provider);
	}
	public <T> Optional<T> getProperty(final String propertyName, final Locale locale, final PropertyService.PropertyProvider<T> provider) {
		return nodeService.getProperty(getWrappedNode(), propertyName, locale, provider);
	}
	public <T> Stream<T> streamMultiProperty(final String propertyName, final PropertyService.PropertyProvider<T> provider) {
		return nodeService.streamMultiProperty(getWrappedNode(), propertyName, provider);
	}
	public <T> Stream<T> streamMultiProperty(final String propertyName, final Locale locale, final PropertyService.PropertyProvider<T> provider) {
		return nodeService.streamMultiProperty(getWrappedNode(), propertyName, locale, provider);
	}

	public Optional<Property> removeProperty(final String propertyName) {
		return nodeService.removeProperty(getWrappedNode(), propertyName);
	}
	public Optional<Property> removeProperty(final String propertyName, final Locale locale) {
		return nodeService.removeProperty(getWrappedNode(), propertyName, locale);
	}

	@Override
	public boolean equals(final Object other) {
		if (other instanceof AbstractPowerNode) {
			final AbstractPowerNode<?> otherPowerNode = (AbstractPowerNode<?>) other;
			return getOrThrow(wrappedNode ->
					Objects.equals(wrappedNode.getIdentifier(), otherPowerNode.getIdentifier()) &&
					Objects.equals(getWorkspaceName(), otherPowerNode.getWorkspaceName())
			);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getOrThrow(wrappedNode -> Objects.hash(
				wrappedNode.getIdentifier(),
				getWorkspaceName()
		));
	}

	private String getWorkspaceName() throws RepositoryException{
		return getWrappedNode().getSession().getWorkspace().getName();
	}
}
