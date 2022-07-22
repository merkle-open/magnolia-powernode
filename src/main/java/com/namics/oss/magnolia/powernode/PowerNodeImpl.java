package com.namics.oss.magnolia.powernode;

import com.namics.oss.magnolia.powernode.exceptions.PowerNodeException;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.wrapper.DelegateNodeWrapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.commons.iterator.FilteringNodeIterator;
import org.apache.jackrabbit.commons.predicate.NodeTypePredicate;
import org.apache.jackrabbit.commons.predicate.Predicate;
import org.apache.jackrabbit.commons.predicate.Predicates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.jcr.*;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Implementation of the methods defined on the {@link PowerNode} interface.
 * The dynamic proxy defined in {@link PowerNodeService} delegates the calls
 * to this class.
 * Methods not found on this class are delegated to the underlying {@link Node}
 * implementation.
 * <p>
 * Please note:
 * This class should not be used outside the context of the dynamic proxy,
 * this is why the class is package private and all methods are private.
 *
 * <p>
 * This class does not implement the {@link PowerNode} interface, since the
 * dynamic proxy, created in {@link PowerNodeService} has to inject the current
 * node as parameter, thus the signatures are not the same as defined in the
 * interface.
 */
class PowerNodeImpl {

	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final String PATH_SEPARATOR = "/";

	private final PowerNodeService powerNodeService;
	private final LocalizedNameProvider localizedNameProvider;

	PowerNodeImpl(PowerNodeService powerNodeService, LocalizedNameProvider localizedNameProvider) {
		this.powerNodeService = powerNodeService;
		this.localizedNameProvider = localizedNameProvider;
	}

	/**
	 * Override of the default JCR Node implementation, in order to
	 * add the magnolia created and modified properties.
	 *
	 * @param node    current node
	 * @param relPath node to add
	 * @return the newly added node
	 * @throws RepositoryException ...
	 */
	@SuppressWarnings("unused")
	private Node addNode(@Nonnull Node node, String relPath) throws RepositoryException {
		Node newNode = node.addNode(relPath);
		powerNodeService.setMgnlCreated(node);
		return powerNodeService.convertToPowerNode(newNode);
	}

	/**
	 * Override of the default JCR Node implementation, in order to
	 * add the magnolia created and modified properties.
	 *
	 * @param node     current node
	 * @param relPath  node to add
	 * @param nodeType type of the node to be created
	 * @return the newly added node
	 * @throws RepositoryException ...
	 */
	@SuppressWarnings("unused")
	private Node addNode(@Nonnull Node node, String relPath, String nodeType) throws RepositoryException {
		Node newNode = node.addNode(relPath, nodeType);
		powerNodeService.setMgnlCreated(node);
		return powerNodeService.convertToPowerNode(newNode);
	}

	/**
	 * Override of the default JCR Node implementation, in order to
	 * compare the unwrapped versions of the node.
	 *
	 * @param node  current node
	 * @param other node to compare
	 * @return true if nodes are the same, false otherwise
	 * @throws RepositoryException ...
	 */
	@SuppressWarnings("unused")
	private boolean isSame(@Nonnull Node node, Item other) throws RepositoryException {
		return unwrap(node).isSame(unwrap((Node) other));
	}

	/**
	 * @see PowerNode#getTemplate()
	 */
	@SuppressWarnings("unused")
	private String getTemplate(@Nonnull Node node) throws RepositoryException {
		if (node.hasProperty(NodeTypes.Renderable.TEMPLATE)) {
			Property property = node.getProperty(NodeTypes.Renderable.TEMPLATE);
			return property.getString();
		}
		LOG.debug("Node has no '{}' property.", NodeTypes.Renderable.TEMPLATE);
		return StringUtils.EMPTY;
	}

	/**
	 * @see PowerNode#rename(String)
	 */
	@SuppressWarnings("unused")
	private void rename(@Nonnull Node node, String newName) throws RepositoryException {
		if (StringUtils.isNotEmpty(newName)) {
			if (node.getName().equals(newName)) {
				LOG.info("No rename needed: new name equals current name: '{}'", newName);
				return;
			}
			String newNameValid = powerNodeService.createValidNodeName(newName, "-");
			Node parent = node.getParent();
			String newPath = parent.getPath() + PATH_SEPARATOR + newNameValid;
			Optional<PowerNode> siblingAfter = getSiblingAfter(node);

			node.getSession().move(node.getPath(), newPath);

			if (siblingAfter.isPresent()) {
				parent.orderBefore(newName, siblingAfter.get().getName());
			}
			powerNodeService.updateMgnlModified(node);
		}
	}

	/**
	 * @see PowerNode#getWorkspaceName()
	 */
	@SuppressWarnings("unused")
	private String getWorkspaceName(@Nonnull Node node) throws RepositoryException {
		return node.getSession().getWorkspace().getName();
	}

	/**
	 * @see PowerNode#getWorkspace()
	 */
	@SuppressWarnings("unused")
	private Workspace getWorkspace(@Nonnull Node node) throws RepositoryException {
		return node.getSession().getWorkspace();
	}

	/**
	 * @see PowerNode#createPath(String, String)
	 */
	@SuppressWarnings("unused")
	private Optional<PowerNode> createPath(@Nonnull Node node, String relPath, String nodeType) throws RepositoryException {
		String currentPath = StringUtils.removeStart(relPath, PATH_SEPARATOR);
		if (StringUtils.isBlank(currentPath)) {
			LOG.info("Path is empty, no path to create.");
			return Optional.of(powerNodeService.convertToPowerNode(node));
		}
		if (StringUtils.isBlank(nodeType)) {
			LOG.warn("NodeType is not specified, cannot create path '{}'", relPath);
			return Optional.empty();
		}
		Node currentParent = node;
		String[] names = currentPath.split(PATH_SEPARATOR);
		for (String name : names) {
			String validName = powerNodeService.createValidNodeName(name, "-");
			if (currentParent.hasNode(validName)) {
				currentParent = currentParent.getNode(validName);
			} else {
				Node newNode = currentParent.addNode(validName, nodeType);
				if (newNode.canAddMixin(JcrConstants.MIX_LOCKABLE)) {
					newNode.addMixin(JcrConstants.MIX_LOCKABLE);
				}
				currentParent = newNode;
			}
		}
		powerNodeService.setMgnlCreated(node);
		return Optional.of(powerNodeService.convertToPowerNode(currentParent));
	}

	/**
	 * @see PowerNode#getOrCreateNode(String, String)
	 */
	@SuppressWarnings("unused")
	private Optional<PowerNode> getOrCreateNode(@Nonnull Node node, String relPath, String nodeType) throws RepositoryException {
		if (StringUtils.isBlank(relPath)) {
			LOG.warn("Path is empty, cannot get or create Node.");
			return Optional.empty();
		}
		if (StringUtils.isBlank(nodeType)) {
			LOG.warn("NodeType is not specified, cannot create path '{}'", relPath);
			return Optional.empty();
		}
		if (node.hasNode(relPath)) {
			Node child = node.getNode(relPath);
			return Optional.of(powerNodeService.convertToPowerNode(child));
		}
		return createPath(node, relPath, nodeType);
	}

	/**
	 * @see PowerNode#getNodeByName(String)
	 */
	@SuppressWarnings("unused")
	private Optional<PowerNode> getNodeByName(@Nonnull Node node, String relPath) throws RepositoryException {
		if (StringUtils.isBlank(relPath)) {
			LOG.warn("Path is empty, cannot get Node.");
			return Optional.empty();
		}
		if (node.hasNode(relPath)) {
			Node child = node.getNode(relPath);
			return Optional.of(powerNodeService.convertToPowerNode(child));
		}
		return Optional.empty();
	}

	/**
	 * @see PowerNode#getNodeByName(String)
	 */
	@SuppressWarnings("unused")
	private Optional<PowerNode> getNodeByName(@Nonnull Node node, String relPath, Locale locale) throws RepositoryException {
		if (StringUtils.isBlank(relPath)) {
			LOG.warn("Path is empty, cannot get Node.");
			return Optional.empty();
		}
		final String localizedRelPath = localizedNameProvider.getLocalizedNodeName(node, relPath, locale);
		if (node.hasNode(localizedRelPath)) {
			Node child = node.getNode(localizedRelPath);
			return Optional.of(powerNodeService.convertToPowerNode(child));
		}
		return Optional.empty();
	}

	/**
	 * @see PowerNode#overwriteOrCreateNode(String, String)
	 */
	@SuppressWarnings("unused")
	private Optional<PowerNode> overwriteOrCreateNode(@Nonnull Node node, String relPath, String nodeType) throws RepositoryException {
		if (StringUtils.isBlank(relPath)) {
			LOG.warn("Path is empty, cannot get or create Node.");
			return Optional.empty();
		}
		if (StringUtils.isBlank(nodeType)) {
			LOG.warn("NodeType is not specified, cannot create path '{}'", relPath);
			return Optional.empty();
		}

		Optional<PowerNode> siblingAfter = Optional.empty();
		if (node.hasNode(relPath)) {
			Node subject = node.getNode(relPath);
			siblingAfter = getSiblingAfter(subject);
			subject.remove();
		}

		Optional<PowerNode> newNode = createPath(node, relPath, nodeType);

		if (siblingAfter.isPresent()) {
			node.orderBefore(relPath, siblingAfter.get().getName());
		}

		return newNode;
	}

	/**
	 * @see PowerNode#getAncestors()
	 */
	@SuppressWarnings("unused")
	private List<PowerNode> getAncestors(@Nonnull Node node) throws RepositoryException {
		int level = node.getDepth();
		List<PowerNode> ancestors = IntStream.range(1, level)
				.mapToObj(depth -> {
					try {
						return node.getAncestor(depth);
					} catch (RepositoryException e) {
						throw PowerNodeException.wrap(e, PowerNodeException.Type.JCR_REPOSITORY);
					}
				})
				.map(item -> (Node) item)
				.map(powerNodeService::convertToPowerNode)
				.collect(Collectors.toList());
		Collections.reverse(ancestors);
		return ancestors;
	}

	/**
	 * @see PowerNode#getAncestorByLevel(int)
	 */
	@SuppressWarnings("unused")
	private Optional<PowerNode> getAncestorByLevel(@Nonnull Node node, int level) throws RepositoryException {
		try {
			Item ancestor = node.getAncestor(level);
			if (ancestor instanceof Node) {
				return Optional.of(powerNodeService.convertToPowerNode((Node) ancestor));
			}
		} catch (ItemNotFoundException e) {
			LOG.info("Ancestor at level '{}' not found", level);
			LOG.debug("Ancestor at level '{}' not found", level, e);
		}
		return Optional.empty();
	}

	/**
	 * @see PowerNode#isDescendantOf(Node)
	 */
	@SuppressWarnings("unused")
	private boolean isDescendantOf(@Nonnull Node node, Node ancestor) throws RepositoryException {
		if (Objects.isNull(ancestor)) {
			LOG.warn("Ancestor node is not specified, cannot determine if node is descendant");
			return false;
		}
		List<PowerNode> ancestors = getAncestors(node);
		return ancestors.stream().anyMatch(a -> a.isSame(ancestor));
	}

	/**
	 * @see PowerNode#getChildNodes()
	 */
	@SuppressWarnings("unused")
	private List<PowerNode> getChildNodes(@Nonnull Node node) throws RepositoryException {
		FilteringNodeIterator noMetaNodesIterator = new FilteringNodeIterator(node.getNodes(), metaDataNodesPredicate);
		return powerNodeService.nodeIteratorToList(noMetaNodesIterator);
	}

	/**
	 * @see PowerNode#getChildNodes(String)
	 */
	@SuppressWarnings("unused")
	private List<PowerNode> getChildNodes(@Nonnull Node node, String nodeType) throws RepositoryException {
		if (StringUtils.isBlank(nodeType)) {
			LOG.warn("NodeType is not specified, cannot filter child nodes.");
			return Collections.emptyList();
		}
		NodeTypePredicate nodeTypePredicate = new NodeTypePredicate(nodeType, false);
		Predicate nodeTypeAndNoMetaPredicate = Predicates.and(nodeTypePredicate, metaDataNodesPredicate);

		FilteringNodeIterator filteringNodeIterator = new FilteringNodeIterator(node.getNodes(), nodeTypeAndNoMetaPredicate);
		return powerNodeService.nodeIteratorToList(filteringNodeIterator);
	}

	/**
	 * @see PowerNode#getChildNodes(Predicate)
	 */
	@SuppressWarnings("unused")
	private List<PowerNode> getChildNodes(@Nonnull Node node, Predicate predicate) throws RepositoryException {
		if (predicate == null) {
			LOG.warn("Predicate is not specified, cannot filter child nodes.");
			return Collections.emptyList();
		}
		Predicate nodeTypeAndNoMetaPredicate = Predicates.and(predicate, metaDataNodesPredicate);

		FilteringNodeIterator filteringNodeIterator = new FilteringNodeIterator(node.getNodes(), nodeTypeAndNoMetaPredicate);
		return powerNodeService.nodeIteratorToList(filteringNodeIterator);
	}

	/**
	 * @see PowerNode#getChildNodesRecursive(String)
	 */
	@SuppressWarnings("unused")
	private List<PowerNode> getChildNodesRecursive(@Nonnull Node node, String nodeType) throws RepositoryException {
		if (StringUtils.isBlank(nodeType)) {
			LOG.warn("NodeType is not specified, cannot filter child nodes.");
			return Collections.emptyList();
		}

		List<PowerNode> allNodes = new ArrayList<>();
		return recursiveChildHelper(allNodes, node, new NodeTypePredicate(nodeType, false));
	}

	/**
	 * @see PowerNode#getChildNodesRecursive(Predicate)
	 */
	@SuppressWarnings("unused")
	private List<PowerNode> getChildNodesRecursive(@Nonnull Node node, Predicate predicate) throws RepositoryException {
		if (predicate == null) {
			LOG.warn("Predicate is not specified, cannot filter child nodes.");
			return Collections.emptyList();
		}

		List<PowerNode> allNodes = new ArrayList<>();
		return recursiveChildHelper(allNodes, node, predicate);
	}

	/**
	 * Not present on the PowerNode interface, only for internal use (recursion)
	 */
	private List<PowerNode> recursiveChildHelper(List<PowerNode> collectedNodes, Node parent, Predicate predicate) throws RepositoryException {
		// get filtered sub nodes
		collectedNodes.addAll(getChildNodes(parent, predicate));

		// get all sub nodes regardless of filter
		List<PowerNode> childNodes = getChildNodes(parent);

		for (PowerNode child : childNodes) {
			recursiveChildHelper(collectedNodes, child, predicate);
		}
		return collectedNodes;
	}

	/**
	 * @see PowerNode#getSiblings()
	 */
	@SuppressWarnings("unused")
	private List<PowerNode> getSiblings(@Nonnull Node node) throws RepositoryException {
		Node parent = node.getParent();
		return getChildNodes(parent).stream()
				.filter(sibling -> !sibling.isSame(node))
				.collect(Collectors.toList());
	}

	/**
	 * @see PowerNode#getSiblingAfter()
	 */
	@SuppressWarnings("unused")
	private Optional<PowerNode> getSiblingAfter(@Nonnull Node node) throws RepositoryException {
		PowerNode current = powerNodeService.convertToPowerNode(node);
		Node parent = current.getParent();
		List<PowerNode> siblings = getChildNodes(parent);
		int siblingAfterIndex = siblings.indexOf(current) + 1;

		if (siblings.size() > siblingAfterIndex) {
			return Optional.of(siblings.get(siblingAfterIndex));
		}

		return Optional.empty();
	}

	/**
	 * @see PowerNode#getSibling(String)
	 */
	@SuppressWarnings("unused")
	private Optional<PowerNode> getSibling(@Nonnull Node node, String name) throws RepositoryException {
		if (StringUtils.isBlank(name)) {
			LOG.warn("Name is empty, cannot get sibling by name.");
			return Optional.empty();
		}
		Node parent = node.getParent();
		return getChildNodes(parent).stream()
				.filter(sibling -> sibling.getName().equals(name))
				.findFirst();
	}

	/**
	 * @see PowerNode#unwrap()
	 */
	@SuppressWarnings("unused")
	private Node unwrap(@Nonnull Node node) {
		Node unwrappedNode = node;
		while (unwrappedNode instanceof DelegateNodeWrapper || unwrappedNode instanceof PowerNode) {
			if (unwrappedNode instanceof PowerNode) {
				unwrappedNode = ((PowerNode) unwrappedNode).getWrappedNode();
			}
			if (unwrappedNode instanceof DelegateNodeWrapper) {
				unwrappedNode = ((DelegateNodeWrapper) unwrappedNode).getWrappedNode();
			}
		}
		return unwrappedNode;
	}

	/**
	 * @see PowerNode#sessionSave()
	 */
	@SuppressWarnings("unused")
	private void sessionSave(@Nonnull Node node) throws RepositoryException {
		node.getSession().save();
	}

	/**
	 * @see PowerNode#sessionLogout()
	 */
	@SuppressWarnings("unused")
	private void sessionLogout(@Nonnull Node node) throws RepositoryException {
		node.getSession().logout();
	}

	/**
	 * Predicate needed to filter meta-data nodes such as "jcr:system".
	 * Nodes not starting with 'jcr:' match the predicate.
	 */
	private org.apache.jackrabbit.commons.predicate.Predicate metaDataNodesPredicate =
			node -> {
				try {
					if (node instanceof Node) {
						return !((Node) node).getName().startsWith(NodeTypes.JCR_PREFIX);
					}
					return false;
				} catch (RepositoryException e) {
					LOG.error("Cannot get name from node.");
					LOG.debug("Cannot get name from node.", e);
					throw PowerNodeException.wrap(e, PowerNodeException.Type.JCR_REPOSITORY);
				}
			};

	/**
	 * @see PowerNode#equals(Object);
	 */
	@SuppressWarnings("unused")
	private boolean equals(@Nonnull Node node, Object other) throws RepositoryException {
		if (other instanceof Node) {
			return isSame(node, (Item) other);
		}
		return false;
	}

	/**
	 * @see PowerNode#hashCode();
	 */
	@SuppressWarnings("unused")
	private int hashCode(@Nonnull Node node) throws RepositoryException {
		String uuid = node.getIdentifier();
		String workspace = getWorkspaceName(node);
		return Objects.hash(uuid, workspace);
	}
}

