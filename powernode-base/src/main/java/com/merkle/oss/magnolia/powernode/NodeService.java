package com.merkle.oss.magnolia.powernode;

import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.jcr.util.NodeNameHelper;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.wrapper.I18nNodeWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.jcr.*;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class NodeService {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private final LocalizedNameProvider localizedNameProvider;
	private final NodeNameHelper nodeNameHelper;
	private final JcrSessionProvider jcrSessionProvider;
	private final PropertyService propertyService;

	@Inject
	public NodeService(
			final LocalizedNameProvider localizedNameProvider,
			final NodeNameHelper nodeNameHelper,
			final JcrSessionProvider jcrSessionProvider,
			final PropertyService propertyService
	) {
		this.localizedNameProvider = localizedNameProvider;
		this.nodeNameHelper = nodeNameHelper;
		this.jcrSessionProvider = jcrSessionProvider;
		this.propertyService = propertyService;
	}

	public void run(final RepositoryRunnable runnable) {
		getOrThrow(() -> {
			runnable.run();
			return null;
		});
	}

	public <T> T getOrThrow(final RepositoryProvider<T> provider) {
		try {
			return provider.get();
		} catch (RepositoryException e) {
			throw new RuntimeRepositoryException(e.getMessage(), e);
		}
	}

	public <T> Optional<T> get(final RepositoryProvider<T> provider) {
		try {
			return Optional.ofNullable(provider.get());
		} catch (RepositoryException e) {
			LOG.error("Failed to apply node function!", e);
			return Optional.empty();
		}
	}

	public interface RepositoryProvider<T> {
		T get() throws RepositoryException;
	}

	public interface RepositoryRunnable {
		void run() throws RepositoryException;
	}

	public Optional<Session> getSession(final String workspace) {
		return get(() -> jcrSessionProvider.getSession(workspace));
	}

	public Optional<Session> getSystemSession(final String workspace) {
		return get(() -> jcrSessionProvider.getSystemSession(workspace));
	}

	public Optional<Node> getByIdentifier(final String workspace, final String identifier) {
		return getSession(workspace).flatMap(session -> getByIdentifier(session, identifier));
	}

	public Optional<Node> getByIdentifier(final Session session, final String identifier) {
		return get(() -> {
			try {
				return Optional.of(session.getNodeByIdentifier(identifier));
			} catch (ItemNotFoundException e) {
				return Optional.<Node>empty();
			}
		}).flatMap(Function.identity());
	}

	public Optional<Node> getByPath(final String workspace, final String path) {
		return getSession(workspace).flatMap(session -> getByPath(session, path));
	}

	public Optional<Node> getByPath(final Session session, final String path) {
		return get(() -> {
			try {
				return Optional.of(session.getNode(path));
			} catch (PathNotFoundException e) {
				return Optional.<Node>empty();
			}
		}).flatMap(Function.identity());
	}

	public void rename(final Node node, final String newName) {
		run(() -> {
			if (!Objects.equals(node.getName(), newName)) {
				final String validatedName = nodeNameHelper.getValidatedName(newName);
				final String destinationPath = node.getParent().getPath() + "/" + validatedName;

				@Nullable
				final Node nextSibling = getNextSibling(node).orElse(null);
				node.getSession().move(node.getPath(), destinationPath);

				if(nextSibling != null) {
					// Restore order of nodes (https://experienceleaguecommunities.adobe.com/t5/adobe-experience-manager/change-node-name-without-move-method/m-p/299414)
					node.getParent().orderBefore(validatedName, nextSibling.getName());
				}
				NodeTypes.LastModified.update(node);
			}
		});
	}

	private Optional<Node> getNextSibling(final Node node) {
		final String nodeName = getOrThrow(node::getName);
		return getParent(node).stream().flatMap(this::streamChildren).dropWhile(sibling -> Objects.equals(getOrThrow(sibling::getName), nodeName)).findFirst();
	}

	public void move(final Node node, final Node newParent) {
		if (!getOrThrow(() -> node.getParent().isSame(newParent))) {
			run(() -> {
				final String destinationPath = newParent.getPath() + "/" + node.getName();
				node.getSession().move(node.getPath(), destinationPath);
				NodeTypes.LastModified.update(node);
				NodeTypes.LastModified.update(newParent);
			});
		}
	}

	/**
     * Gets child by localizing all relative path names.<br>
     * e.g. /node1/child_de/grandchild_de
	 */
	public Optional<Node> getChild(final Node node, final String relativePath, final Locale locale) {
		final String[] relativePaths = relativePath.split("/", 2);
		return localizeNode(node, relativePaths[0], locale, localizedChildNodeName -> getChild(node, localizedChildNodeName)).flatMap(child ->
				relativePaths.length > 1 ? getChild(child, relativePaths[1], locale) : Optional.of(child)
		);
	}

	public Optional<Node> getChild(final Node node, final String relativePath) {
		return get(() -> {
			try {
				return Optional.of(node.getNode(relativePath));
			} catch (PathNotFoundException e) {
				return Optional.<Node>empty();
			}
		}).flatMap(Function.identity());
	}

	public Stream<Node> streamChildren(final Node node) {
		return streamChildren(node, ignored -> true);
	}

	public Stream<Node> streamChildren(final Node node, final Predicate<Node> predicate) {
		return get(node::getNodes).stream()
				.map(nodeIterator -> (Iterator<Node>)nodeIterator)
				.flatMap(nodeIterator ->
					StreamSupport.stream(Spliterators.spliteratorUnknownSize(nodeIterator, Spliterator.ORDERED), false)
				)
				.filter(predicate);
	}

	public Optional<Node> getParent(final Node node) {
		return get(() -> {
			try {
				return Optional.of(node.getParent());
			} catch (ItemNotFoundException e) {
				return Optional.<Node>empty();
			}
		}).flatMap(Function.identity());
	}

	public Optional<Node> getAncestor(final Node node, final Predicate<Node> predicate) {
		return getParent(node).flatMap(parent ->
				Optional.of(parent).filter(predicate).or(() -> getAncestor(parent, predicate))
		);
	}

	public boolean hasProperty(final Node node, final String propertyName) {
		return getOrThrow(() -> node.hasProperty(propertyName));
	}
	public boolean hasProperty(final Node node, final String propertyName, final Locale locale) {
		return localizeProperty(node, propertyName, locale, localizedPropertyName -> hasProperty(node, localizedPropertyName));
	}

	public <T> Optional<T> getProperty(final Node node, final String propertyName, final PropertyService.PropertyProvider<T> provider) {
		return get(() -> propertyService.getProperty(node, propertyName, provider)).flatMap(Function.identity());
	}
	public <T> Optional<T> getProperty(final Node node, final String propertyName, final Locale locale, final PropertyService.PropertyProvider<T> provider) {
		return localizeProperty(node, propertyName, locale, localizedPropertyName -> getProperty(node, localizedPropertyName, provider));
	}
	public <T> Stream<T> streamMultiProperty(final Node node, final String propertyName, final PropertyService.PropertyProvider<T> provider) {
		return get(() -> propertyService.streamMultiProperty(node, propertyName, provider)).stream().flatMap(Function.identity());
	}
	public <T> Stream<T> streamMultiProperty(final Node node, final String propertyName, final Locale locale, final PropertyService.PropertyProvider<T> provider) {
		return localizeProperty(node, propertyName, locale, localizedPropertyName -> streamMultiProperty(node, localizedPropertyName, provider));
	}


	public <T> Property setProperty(final Node node, final String propertyName, @Nullable final T value, final PropertyService.ValueFactory<T> factory) {
		return getOrThrow(() -> propertyService.setProperty(node, propertyName, value, factory));
	}
	public <T> Property setProperty(final Node node, final String propertyName, final Locale locale, @Nullable final T value, final PropertyService.ValueFactory<T> factory) {
		return localizeProperty(node, propertyName, locale, localizedPropertyName -> setProperty(node, localizedPropertyName, value, factory));
	}
	public <T> Property setMultiProperty(final Node node, final String propertyName, final Iterable<T> values, final PropertyService.ValueFactory<T> factory) {
		return getOrThrow(() -> propertyService.setMultiProperty(node, propertyName, values, factory));
	}
	public <T> Property setMultiProperty(final Node node, final String propertyName, final Locale locale, final Iterable<T> values, final PropertyService.ValueFactory<T> factory) {
		return localizeProperty(node, propertyName, locale, localizedPropertyName -> setMultiProperty(node, localizedPropertyName, values, factory));
	}


	public Optional<Property> removeProperty(final Node node, final String propertyName) {
		return get(() -> propertyService.removeProperty(node, propertyName)).flatMap(Function.identity());
	}
	public Optional<Property> removeProperty(final Node node, final String propertyName, final Locale locale) {
		return localizeProperty(node, propertyName, locale, localizedPropertyName -> removeProperty(node, localizedPropertyName));
	}

	private <T> T localizeProperty(final Node node, final String propertyName, final Locale locale, final Function<String, T> provider) {
		if (!NodeUtil.isWrappedWith(node, I18nNodeWrapper.class)) {
			return provider.apply(localizedNameProvider.getLocalizedPropertyName(node, propertyName, locale));
		}
		return provider.apply(propertyName);
	}
	private <T> T localizeNode(final Node node, final String nodeName, final Locale locale, final Function<String, T> provider) {
		return provider.apply(localizedNameProvider.getLocalizedNodeName(node, nodeName, locale));
	}
}