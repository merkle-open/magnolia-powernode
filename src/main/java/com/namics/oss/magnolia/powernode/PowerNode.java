package com.namics.oss.magnolia.powernode;

import org.apache.jackrabbit.commons.predicate.Predicate;

import javax.jcr.*;
import javax.jcr.lock.Lock;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

/**
 * Interface used to create a proxy via PowerNodeService.
 * <p>
 * Method calls are delegated to the PowerNodeImpl or PowerNodePropertyImpl
 * implementation.
 * When delegating to PowerNodeImpl or PowerNodePropertyImpl
 * the node is added as the first method argument.
 * <p>
 * Methods not found on PowerNodeImpl or PowerNodePropertyImpl are delegated to
 * the current javax.jcr.Node implementation.
 * <p>
 * This allows to override javax.jcr.Node methods, with
 * custom implementations, given that the overriding method
 * has exactly the same signature.
 */
public interface PowerNode extends Node {

	//////////////////////////////////////////////////////////////////////////////////
	// PowerNode methods, implemented in PowerNodeImpl or PowerNodePropertyImpl
	//////////////////////////////////////////////////////////////////////////////////

	/// Node related methods (should replace NodeUtil) ///

	/**
	 * Implementation: {@link PowerNodeImpl#getTemplate(Node)}
	 *
	 * @return template name, or an empty String if no template is available
	 */
	String getTemplate();

	/**
	 * Implementation: {@link PowerNodeImpl#rename(Node, String)}
	 */
	void rename(String newName);

	/**
	 * Implementation:  {@link PowerNodeImpl#getWorkspaceName(Node)}
	 *
	 * @return Name of the nodes workspace
	 */
	String getWorkspaceName();

	/**
	 * Implementation:  {@link PowerNodeImpl#getWorkspace(Node)}
	 *
	 * @return Workspace of the node
	 */
	Workspace getWorkspace();

	/**
	 * Implementation:  {@link PowerNodeImpl#createPath(Node, String, String)}
	 *
	 * @param relPath  path to create
	 * @param nodeType node typo to use for the new nodes
	 * @return optional of the newly created node, last child of the path,
	 * if the path is empty, an optional of the current node is returned,
	 * if the nodeType is not specified and empty optional is returned
	 */
	Optional<PowerNode> createPath(String relPath, String nodeType);

	/**
	 * Implementation:  {@link PowerNodeImpl#getOrCreateNode(Node, String, String)}
	 *
	 * @param relPath  path to create
	 * @param nodeType node typo to use for the new nodes
	 * @return optional of the newly created node, or the currently existing node,
	 * if the nodeType is not specified and empty optional is returned
	 */
	Optional<PowerNode> getOrCreateNode(String relPath, String nodeType);

	/**
	 * Implementation:  {@link PowerNodeImpl#getNodeByName(Node, String)}
	 *
	 * @param relPath name or relative path to node
	 * @return optional of the node
	 */
	Optional<PowerNode> getNodeByName(String relPath);

	/**
	 * Implementation:  {@link PowerNodeImpl#getNodeByName(Node, String)}
	 *
	 * @param relPath name or relative path to node
	 * @return optional of the node
	 */
	Optional<PowerNode> getNodeByName(String relPath, Locale locale);

	/**
	 * Implementation:  {@link PowerNodeImpl#overwriteOrCreateNode(Node, String, String)}
	 *
	 * @param relPath  path to create or override
	 * @param nodeType node typo to use for the new nodes
	 * @return optional of the newly created node
	 */
	Optional<PowerNode> overwriteOrCreateNode(String relPath, String nodeType);

	/**
	 * Implementation:  {@link PowerNodeImpl#getAncestors(Node)}
	 *
	 * @return a list of all ancestor nodes, to the top most node excluding "/" (workspace-root)
	 * Index 0 is the closest node, highest index is the root node.
	 * If the node has no ancestors (site-root), an empty list will be returned
	 */
	List<PowerNode> getAncestors();

	/**
	 * Implementation:  {@link PowerNodeImpl#getAncestorByLevel(Node, int)}
	 *
	 * @return the ancestor of the given node which is found at the given level (top down)
	 */
	Optional<PowerNode> getAncestorByLevel(int level);

	/**
	 * Implementation:  {@link PowerNodeImpl#isDescendantOf(Node, Node)}
	 *
	 * @param ancestor possible ancestor of the node
	 * @return true if the node is a descendant of the ancestor, false otherwise.
	 */
	boolean isDescendantOf(Node ancestor);

	/**
	 * Implementation:  {@link PowerNodeImpl#getChildNodes(Node)}
	 *
	 * @return all child nodes.
	 */
	List<PowerNode> getChildNodes();

	/**
	 * Implementation:  {@link PowerNodeImpl#getChildNodes(Node, String)}
	 *
	 * @param nodeType filter for child nodes with the given node type
	 * @return all child nodes with matching node type,
	 * an empty list if the node type is not specified
	 */
	List<PowerNode> getChildNodes(String nodeType);

	/**
	 * Implementation:  {@link PowerNodeImpl#getChildNodes(Node, Predicate)}
	 *
	 * @param predicate filter for child nodes with the given predicate
	 * @return all child nodes with matching predicate,
	 * an empty list if the node type is not specified
	 */
	List<PowerNode> getChildNodes(Predicate predicate);

	/**
	 * Implementation:  {@link PowerNodeImpl#getChildNodesRecursive(Node, String)}
	 *
	 * @param nodeType filter for child nodes with the given node type
	 * @return all child nodes, recursively, with matching node type,
	 * an empty list if the node type is not specified
	 */
	List<PowerNode> getChildNodesRecursive(String nodeType);

	/**
	 * Implementation:  {@link PowerNodeImpl#getChildNodesRecursive(Node, Predicate)}
	 *
	 * @param predicate filter for child nodes with the given predicate
	 * @return all child nodes, recursively, with matching predicate,
	 * an empty list if the node type is not specified
	 */
	List<PowerNode> getChildNodesRecursive(Predicate predicate);

	/**
	 * Implementation:  {@link PowerNodeImpl#getSiblings(Node)}
	 *
	 * @return all siblings, this excludes the current node.
	 */
	List<PowerNode> getSiblings();

	/**
	 * Implementation:  {@link PowerNodeImpl#getSiblingAfter(Node)}
	 *
	 * @return the 'next' node
	 */
	Optional<PowerNode> getSiblingAfter();

	/**
	 * Implementation:  {@link PowerNodeImpl#getSibling(Node, String)}
	 *
	 * @param name name of the sibling to be found
	 * @return optional of the sibling with the given name, an empty
	 * optional if the sibling could not be found
	 */
	Optional<PowerNode> getSibling(String name);

	/**
	 * Implementation:  {@link PowerNodeImpl#unwrap(Node)}
	 *
	 * @return the node without any {@link info.magnolia.jcr.wrapper.DelegateNodeWrapper} or
	 * {@link PowerNode} wrappings
	 */
	Node unwrap();

	/**
	 * Implementation:  {@link PowerNodeImpl#sessionSave(Node)}
	 */
	void sessionSave();

	/**
	 * Implementation:  {@link PowerNodeImpl#sessionLogout(Node)}
	 */
	void sessionLogout();

	/// Property related methods (should replace PropertyUtil) ///

	/**
	 * Implementation: {@link PowerNodePropertyImpl#getPropertyOptional(Node, String)}
	 */
	Optional<Property> getPropertyOptional(String name);

	/**
	 * Implementation: {@link PowerNodePropertyImpl#getPropertyValue(Node, String, Class)}
	 *
	 * @return an optional of the property, an empty optional if property
	 * could not be retrieved
	 */
	<T> Optional<T> getPropertyValue(String name, Class<? extends T> type);

	/**
	 * Implementation: {@link PowerNodePropertyImpl#getPropertyValue(Node, String, Object)}
	 *
	 * @param <T> property type, supported: String, Boolean, Long, Double, BigDecimal, InputStream, Calendar, Date, LocalDate, LocalDateTime
	 * @return the property value if present, otherwise the default value
	 */
	<T> T getPropertyValue(String name, T defaultValue);

	/**
	 * Implementation: {@link PowerNodePropertyImpl#getPropertyValueInherit(Node, String, Class)}
	 *
	 * @param <T> property type, supported: String, Boolean, Long, Double, BigDecimal, InputStream, Calendar, Date, LocalDate, LocalDateTime
	 * @return an optional of the property, traverse up the node tree to
	 * find the property, or an empty optional if property
	 * could not be retrieved
	 */
	<T> Optional<T> getPropertyValueInherit(String name, Class<? extends T> type);

	/**
	 * Implementation: {@link PowerNodePropertyImpl#getPropertyValueInherit(Node, String, Object)}
	 *
	 * @param <T> property type, supported: String, Boolean, Long, Double, BigDecimal, InputStream, Calendar, Date, LocalDate, LocalDateTime
	 * @return the property value, traverse up the node tree to find the property,
	 * if the property could not be retrieved, return the default value
	 */
	<T> T getPropertyValueInherit(String name, T defaultValue);

	/**
	 * Implementation: {@link PowerNodePropertyImpl#getPropertyValueLocalized(Node, String, Locale, Class)}
	 *
	 * @param <T> property type, supported: String, Boolean, Long, Double, BigDecimal, InputStream, Calendar, Date, LocalDate, LocalDateTime
	 * @return an optional of the localized property, an empty optional if the desired
	 * localization or the property is not present
	 */
	<T> Optional<T> getPropertyValueLocalized(String name, Locale locale, Class<? extends T> type);

	/**
	 * Implementation: {@link PowerNodePropertyImpl#getPropertyValueLocalized(Node, String, Locale, Class)}
	 *
	 * @param <T> property type, supported: String, Boolean, Long, Double, BigDecimal, InputStream, Calendar, Date, LocalDate, LocalDateTime
	 * @return the localized property, the default value if the desired
	 * localization or the property is not present
	 */
	<T> T getPropertyValueLocalized(String name, T defaultValue, Locale locale);

	/**
	 * Implementation: {@link PowerNodePropertyImpl#getPropertyValueWithFallback(Node, String, Class, String...)}
	 *
	 * @param <T> property type, supported: String, Boolean, Long, Double, BigDecimal, InputStream, Calendar, Date, LocalDate, LocalDateTime
	 * @return an optional of the property if present, otherwise go through the fallback names and return the first
	 * property which is present, if none is present return an empty optional
	 */
	<T> Optional<T> getPropertyValueWithFallback(String name, Class<? extends T> type, String... fallbackNames);

	/**
	 * Implementation: {@link PowerNodePropertyImpl#getPropertyValueWithFallback(Node, String, Object, String...)}
	 *
	 * @param <T> property type, supported: String, Boolean, Long, Double, BigDecimal, InputStream, Calendar, Date, LocalDate, LocalDateTime
	 * @return the property if present, otherwise go through the fallback names and return the first
	 * property which is present, if none is present return the default value
	 */
	<T> T getPropertyValueWithFallback(String name, T defaultValue, String... fallbackNames);

	/**
	 * Implementation: {@link PowerNodePropertyImpl#getPropertyValueList(Node, String, Class)}
	 *
	 * @param <T> property type, supported: String, Boolean, Long, Double, BigDecimal, InputStream, Calendar, Date, LocalDate, LocalDateTime
	 * @return the property list, if the property is not a marked as 'multiple' (not a list-property),
	 * a list with one entry is returned, if the property is not present, an empty list is returned
	 */
	<T> List<T> getPropertyValueList(String name, Class<? extends T> type);

	/**
	 * Implementation: {@link PowerNodePropertyImpl#getPropertyMap(Node)}
	 *
	 * @return a map with all properties of the given node. If a property could not be cast, the value will will be
	 * an empty String.
	 */
	Map<String, Object> getPropertyMap();

	/**
	 * Implementation: {@link PowerNodePropertyImpl#getPropertyMap(Node, Class)}
	 *
	 * @return a map with all properties of the given type.
	 */
	<T> Map<String, T> getPropertyMap(Class<? extends T> type);

	/**
	 * Implementation: {@link PowerNodePropertyImpl#getPropertyMap(Node, Predicate)}
	 *
	 * @return a map with all properties of the given type.
	 */
	Map<String, Object> getPropertyMap(Predicate predicate);

	/**
	 * Implementation: {@link PowerNodePropertyImpl#removeProperty(Node, String)}
	 *
	 * @param name name of the property to be removed
	 */
	void removeProperty(String name);

	/**
	 * Implementation: {@link PowerNodePropertyImpl#hasProperty(Node, String)}
	 *
	 * @param name name of the property to be checked for
	 */
	boolean hasProperty(String relPath);

	/**
	 * Implementation: {@link PowerNodePropertyImpl#setPropertyValue(Node, String, Object)}
	 *
	 * @param name  name of the property to be set
	 * @param value value of the property
	 * @param <T>   property type, supported: String, Boolean, Long, Short, Integer, Double, Number, BigDecimal, InputStream, Calendar, Date, LocalDate, LocalDateTime, Node, List<T>
	 */
	<T> void setPropertyValue(String name, T value);

	/**
	 * Implementation: {@link PowerNodePropertyImpl#setPropertyValueIfNotPresent(Node, String, Object)}
	 *
	 * @param name  name of the property to be set
	 * @param value value of the property
	 * @param <T>   property type, supported: String, Boolean, Long, Short, Integer, Double, Number, BigDecimal, InputStream, Calendar, Date, LocalDate, LocalDateTime, Node, List<T>
	 */
	<T> void setPropertyValueIfNotPresent(String name, T value);

	// Object overrides

	/**
	 * Override equals with 'isSame' implementation.
	 * 'isSame' compares the node uuid and the workspace.
	 * <p>
	 * Implementation:  {@link PowerNodeImpl#equals(Node, Object)}
	 *
	 * @param other other object to compare for equality
	 * @return true if objects are equal, false otherwise
	 */
	@Override
	boolean equals(Object other);

	/**
	 * Override hashCode to match equality criteria from
	 * 'isSame' equals implementation.
	 * <p>
	 * Implementation:  {@link PowerNodeImpl#hashCode(Node)}
	 *
	 * @return true if objects are equal, false otherwise
	 */
	@Override
	int hashCode();

	/**
	 * This method is implemented in the dynamic proxy, since
	 * there is the only place where we have access to the
	 * converted Node.
	 * {@see com.namics.oss.magnolia.jcr.powernode.PowerNodeService#convertToPowerNode(javax.jcr.Node)}
	 *
	 * @return the Node which was converted to a PowerNode
	 */
	Node getWrappedNode();

	///////////////////////////////////////////////////////////////////////
	// Default Node methods, delegated to the current node implementation
	///////////////////////////////////////////////////////////////////////
	@Override
	PowerNode addNode(String relPath);

	@Override
	PowerNode addNode(String relPath, String primaryNodeTypeName);

	@Override
	void orderBefore(String srcChildRelPath, String destChildRelPath);

	@Override
	Property setProperty(String name, Value value);

	@Override
	Property setProperty(String name, Value value, int type);

	@Override
	Property setProperty(String name, Value[] values);

	@Override
	Property setProperty(String name, Value[] values, int type);

	@Override
	Property setProperty(String name, String[] values);

	@Override
	Property setProperty(String name, String[] values, int type);

	@Override
	Property setProperty(String name, String value);

	@Override
	Property setProperty(String name, String value, int type);

	@Override
	Property setProperty(String name, InputStream value);

	@Override
	Property setProperty(String name, Binary value);

	@Override
	Property setProperty(String name, boolean value);

	@Override
	Property setProperty(String name, double value);

	@Override
	Property setProperty(String name, BigDecimal value);

	@Override
	Property setProperty(String name, long value);

	@Override
	Property setProperty(String name, Calendar value);

	@Override
	Property setProperty(String name, Node value);

	@Override
	PowerNode getNode(String relPath);

	@Override
	NodeIterator getNodes();

	@Override
	NodeIterator getNodes(String namePattern);

	@Override
	NodeIterator getNodes(String[] nameGlobs);

	@Override
	Property getProperty(String relPath);

	@Override
	PropertyIterator getProperties();

	@Override
	PropertyIterator getProperties(String namePattern);

	@Override
	PropertyIterator getProperties(String[] nameGlobs);

	@Override
	Item getPrimaryItem();

	@Override
	String getUUID();

	@Override
	String getIdentifier();

	@Override
	int getIndex();

	@Override
	PropertyIterator getReferences();

	@Override
	PropertyIterator getReferences(String name);

	@Override
	PropertyIterator getWeakReferences();

	@Override
	PropertyIterator getWeakReferences(String name);

	@Override
	boolean hasNode(String relPath);

	@Override
	boolean hasNodes();

	@Override
	boolean hasProperties();

	@Override
	NodeType getPrimaryNodeType();

	@Override
	NodeType[] getMixinNodeTypes();

	@Override
	boolean isNodeType(String nodeTypeName);

	@Override
	void setPrimaryType(String nodeTypeName);

	@Override
	void addMixin(String mixinName);

	@Override
	void removeMixin(String mixinName);

	@Override
	boolean canAddMixin(String mixinName);

	@Override
	NodeDefinition getDefinition();

	@Override
	Version checkin();

	@Override
	void checkout();

	@Override
	void doneMerge(Version version);

	@Override
	void cancelMerge(Version version);

	@Override
	void update(String srcWorkspace);

	@Override
	NodeIterator merge(String srcWorkspace, boolean bestEffort);

	@Override
	String getCorrespondingNodePath(String workspaceName);

	@Override
	NodeIterator getSharedSet();

	@Override
	void removeSharedSet();

	@Override
	void removeShare();

	@Override
	boolean isCheckedOut();

	@Override
	void restore(String versionName, boolean removeExisting);

	@Override
	void restore(Version version, boolean removeExisting);

	@Override
	void restore(Version version, String relPath, boolean removeExisting);

	@Override
	void restoreByLabel(String versionLabel, boolean removeExisting);

	@Override
	VersionHistory getVersionHistory();

	@Override
	Version getBaseVersion();

	@Override
	Lock lock(boolean isDeep, boolean isSessionScoped);

	@Override
	Lock getLock();

	@Override
	void unlock();

	@Override
	boolean holdsLock();

	@Override
	boolean isLocked();

	@Override
	void followLifecycleTransition(String transition);

	@Override
	String[] getAllowedLifecycleTransistions();

	@Override
	String getPath();

	@Override
	String getName();

	@Override
	Item getAncestor(int depth);

	@Override
	PowerNode getParent();

	@Override
	int getDepth();

	@Override
	Session getSession();

	@Override
	boolean isNode();

	@Override
	boolean isNew();

	@Override
	boolean isModified();

	@Override
	boolean isSame(Item otherItem);

	@Override
	void accept(ItemVisitor visitor);

	@Override
	void save();

	@Override
	void refresh(boolean keepChanges);

	@Override
	void remove();

}

