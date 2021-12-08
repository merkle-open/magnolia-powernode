package com.namics.oss.magnolia.powernode;

import com.google.common.collect.ObjectArrays;
import com.namics.oss.magnolia.powernode.exceptions.PowerNodeException;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.objectfactory.Components;
import info.magnolia.repository.RepositoryManager;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.util.ISO9075;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.jcr.*;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Service which creates the dynamic proxy for PowerNodes
 * and provides some useful node operations.
 */
@Service
public class PowerNodeService {

	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String GET_WRAPPED = "getWrappedNode";
	private static final String ILLEGAL_CHARS_REGEX = "([" + Pattern.quote("%/:[]*'\"|\t\r\n") + "])+";

	private final PowerNodeImpl powerNodeImpl;
	private final PowerNodePropertyImpl powerNodePropertyImpl;
	private final RepositoryManager repositoryManager;

	public PowerNodeService(DefaultLanguageHelper defaultLanguageHelper) {
		this.powerNodeImpl = new PowerNodeImpl(this);
		this.powerNodePropertyImpl = new PowerNodePropertyImpl(this, defaultLanguageHelper);
		this.repositoryManager = Components.getComponent(RepositoryManager.class);
	}

	public PowerNodeService() {
		this(new DefaultLanguageHelper());
	}

	/**
	 * Converts the given node into a {@link PowerNode}!
	 * <p>
	 * The original node can be retrieved with the "getWrappedNode" method.
	 *
	 * @param node the node which should be a PowerNode!
	 * @return a POWERNODE!!!
	 */
	public PowerNode convertToPowerNode(Node node) {
		if (node instanceof PowerNode) {
			return (PowerNode) node;
		}
		return (PowerNode) Proxy.newProxyInstance(
				Node.class.getClassLoader(),
				new Class[]{PowerNode.class},
				(proxy, method, methodArgs) -> {
					try {
						// special method to get the wrapped node
						if (GET_WRAPPED.equals(method.getName())) {
							return node;
						}

						ExecutableMethod<Object> executableMethod = getPowerNodeMethod(method, powerNodeImpl)
								.or(() -> getPowerNodeMethod(method, powerNodePropertyImpl))
								.orElse(getJcrNodeMethod(method));

						Object result = executableMethod.execute(node, methodArgs);

						// if the result is a node convert to PowerNode
						if (!(result instanceof PowerNode) && result instanceof Node) {
							result = this.convertToPowerNode((Node) result);
						}
						return result;
					} catch (NoSuchMethodException e) {
						throw PowerNodeException.wrap(e, PowerNodeException.Type.REFLECTION);
					} catch (Exception e) {
						if (e.getCause() instanceof RepositoryException) {
							throw PowerNodeException.wrap(e, PowerNodeException.Type.JCR_REPOSITORY);
						}
						throw PowerNodeException.wrap(e, PowerNodeException.Type.DEFAULT);
					}
				});
	}

	private ExecutableMethod<Object> getJcrNodeMethod(Method method) {
		return (node, methodArgs) -> {
			Method nodeMethod = node.getClass().getMethod(method.getName(), method.getParameterTypes());
			return nodeMethod.invoke(node, methodArgs);
		};
	}

	private Optional<ExecutableMethod<Object>> getPowerNodeMethod(Method method, Object impl) {
		Class<?>[] types = ObjectUtils.defaultIfNull(method.getParameterTypes(), new Class<?>[]{});
		Class<?>[] typesWithNode = ObjectArrays.concat(Node.class, types);

		return Arrays.stream(impl.getClass().getDeclaredMethods())
				.filter(m -> method.getName().equals(m.getName())
						&& Arrays.equals(typesWithNode, m.getParameterTypes())
				)
				.findFirst()
				.map(m -> {
					m.setAccessible(true);
					return m;
				})
				.map(m ->
						(node, methodArgs) -> invokePowerNodeHelper(node, m, methodArgs, impl)
				);
	}

	private Object invokePowerNodeHelper(Node node, Method powerNodeHelperMethod, Object[] methodArgs, Object impl) throws ReflectiveOperationException {
		methodArgs = ObjectUtils.defaultIfNull(methodArgs, new Object[]{});
		Object[] methodArgsWithNode = ObjectArrays.concat(node, methodArgs);
		return powerNodeHelperMethod.invoke(impl, methodArgsWithNode);
	}

	public Optional<PowerNode> getNodeByUuid(String uuid, Session session) {
		if (isSessionInvalid(session)) {
			return Optional.empty();
		}
		if (!isValidUuid(uuid)) {
			LOG.error("UUID '{}' is not a valid UUID.", uuid);
			return Optional.empty();
		}
		try {
			return Optional.of(convertToPowerNode(session.getNodeByIdentifier(uuid)));
		} catch (RepositoryException e) {
			var workspaceName = session.getWorkspace().getName();
			LOG.error("Could not get node with uuid '{}' from workspace '{}'", uuid, workspaceName);
			LOG.debug("Could not get node with uuid '{}' from workspace '{}'", uuid, workspaceName, e);
		}
		return Optional.empty();
	}

	public Optional<PowerNode> getNodeByPath(String path, Session session) {
		if (isSessionInvalid(session)) {
			return Optional.empty();
		}
		if (StringUtils.isBlank(path)) {
			LOG.error("Path ('{}') not specified.", path);
			return Optional.empty();
		}
		try {
			return Optional.of(convertToPowerNode(session.getNode(path)));
		} catch (PathNotFoundException e) {
			var workspaceName = session.getWorkspace().getName();
			LOG.error("Node path '{}' not found in workspace '{}'", path, workspaceName);
			LOG.debug("Node path '{}' not found in workspace '{}'", path, workspaceName, e);
		} catch (RepositoryException e) {
			var workspaceName = session.getWorkspace().getName();
			LOG.error("Could not get node with path '{}' from workspace '{}'", path, workspaceName);
			LOG.debug("Could not get node with path '{}' from workspace '{}'", path, workspaceName, e);
		}
		return Optional.empty();
	}

	private boolean isSessionInvalid(Session session) {
		if (session == null || !session.isLive()) {
			LOG.error("JCR Session must not be null and has to be live");
			return true;
		}
		return false;
	}

	public Optional<PowerNode> getWorkspaceRootNode(Session session) {
		if (isSessionInvalid(session)) {
			return Optional.empty();
		}
		try {
			return Optional.of(convertToPowerNode(session.getRootNode()));
		} catch (RepositoryException e) {
			var workspaceName = session.getWorkspace().getName();
			LOG.error("Could not get root node from workspace '{}'", workspaceName);
			LOG.debug("Could not get root node from workspace '{}'", workspaceName, e);
		}
		return Optional.empty();
	}

	public boolean nodeExists(String path, Session session) {
		if (isSessionInvalid(session)) {
			return false;
		}
		if (StringUtils.isBlank(path)) {
			LOG.error("Path ('{}') not specified.", path);
			return false;
		}
		try {
			return session.nodeExists(path);
		} catch (RepositoryException e) {
			var workspaceName = session.getWorkspace().getName();
			LOG.error("Could not get node with path '{}' from workspace '{}'", path, workspaceName);
			LOG.debug("Could not get node with path '{}' from workspace '{}'", path, workspaceName, e);
		}
		return false;
	}

	/**
	 * There is an edge case which required encoding of node names
	 * in xpath queries:
	 * The chars '(' and ')' are valid for node names but not valid
	 * in xpath queries.
	 * Encode the node name to avoid issues in this case.
	 *
	 * @param nodeName node name to encode
	 * @return ISO9075 encoded node name
	 */
	public String encodeNodeNameForXPathQueries(String nodeName) {
		return ISO9075.encodePath(nodeName);
	}

	public String createValidNodeName(String nodeName, String replacement) {
		return nodeName.replaceAll(ILLEGAL_CHARS_REGEX, replacement);
	}

	public boolean isValidNodeName(String nodeName) {
		if (StringUtils.isBlank(nodeName)) {
			return false;
		}
		String valid = createValidNodeName(nodeName, "-");
		return valid.equals(nodeName);
	}

	@SuppressWarnings("unchecked")
	public Stream<PowerNode> nodeIteratorToStream(NodeIterator iterator) {
		if (iterator == null) {
			return Stream.empty();
		}
		Iterable<Node> iterable = () -> iterator;
		return StreamSupport.stream(iterable.spliterator(), false)
				.map(this::convertToPowerNode);
	}

	public List<PowerNode> nodeIteratorToList(NodeIterator iterator) {
		return nodeIteratorToStream(iterator).collect(Collectors.toList());
	}

	public boolean workspaceExists(String workspaceName) {
		return repositoryManager.getWorkspaceNames().contains(workspaceName);
	}

	public Optional<Session> getSystemSession(String workspaceName) {
		try {
			return Optional.of(repositoryManager.getSystemSession(workspaceName));
		} catch (RepositoryException e) {
			LOG.error("Could not get system session for workspace {}", workspaceName);
			LOG.debug("Could not get system session for workspace {}", workspaceName, e);
		}
		return Optional.empty();
	}

	/**
	 * Gets a session via the magnolia context.
	 * <p>
	 * Make sure you are in the correct context where {@link MgnlContext} is
	 * available. If you are not in the correct context use getSystemSession instead.
	 *
	 * @param workspaceName to get session for
	 * @return optional of a session
	 */
	public Optional<Session> getMgnlContextSession(String workspaceName) {
		try {
			return Optional.of(MgnlContext.getJCRSession(workspaceName));
		} catch (RepositoryException e) {
			LOG.error("Could not get magnolia context session for workspace {}", workspaceName);
			LOG.debug("Could not get magnolia context session for workspace {}", workspaceName, e);
		}
		return Optional.empty();
	}

	/**
	 * Checks if the given string is a valid UUID.
	 *
	 * @param uuid String to check for validity
	 * @return true for valid UUID, false otherwise
	 */
	public boolean isValidUuid(String uuid) {
		if (StringUtils.isBlank(uuid)) {
			return false;
		}
		try {
			return UUID.fromString(uuid).toString().equals(uuid);
		} catch (IllegalArgumentException e) {
			LOG.trace("String '{}' is not a valid UUID", uuid, e);
			return false;
		}
	}

	/**
	 * Sets the "mgnl:created" and "mgnl:createdBy" properties.
	 * It also updates "mgnl:lastModified" and "mgnl:lastModifiedBy".
	 * <p>
	 * This method uses MgnlContext, in NodeTypes.Created.set(Node) to
	 * retrieve the user.
	 *
	 * @param node node to set properties on
	 */
	public void setMgnlCreated(@Nonnull Node node) {
		try {
			NodeTypes.Created.set(node);
		} catch (RepositoryException e) {
			LOG.error("Could not set properties '{}' and '{}'", NodeTypes.Created.NAME, NodeTypes.Created.CREATED_BY);
			LOG.debug("Could not set properties '{}' and '{}'", NodeTypes.Created.NAME, NodeTypes.Created.CREATED_BY, e);
		}
	}

	/**
	 * Sets the "mgnl:lastModified" and "mgnl:lastModifiedBy" properties.
	 * <p>
	 * This method uses MgnlContext, in NodeTypes.LastModified.update(Node) to
	 * retrieve the user.
	 *
	 * @param node node to set properties on
	 */
	public void updateMgnlModified(@Nonnull Node node) {
		try {
			NodeTypes.LastModified.update(node);
		} catch (RepositoryException e) {
			LOG.error("Could not set properties '{}' and '{}'", NodeTypes.LastModified.NAME, NodeTypes.LastModified.LAST_MODIFIED_BY);
			LOG.debug("Could not set properties '{}' and '{}'", NodeTypes.LastModified.NAME, NodeTypes.LastModified.LAST_MODIFIED_BY, e);
		}
	}

}
