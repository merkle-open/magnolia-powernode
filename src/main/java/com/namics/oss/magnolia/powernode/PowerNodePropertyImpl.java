package com.namics.oss.magnolia.powernode;

import com.namics.oss.magnolia.powernode.exceptions.PowerNodeException;
import info.magnolia.jcr.iterator.FilteringPropertyIterator;
import info.magnolia.jcr.predicate.AbstractPredicate;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.wrapper.I18nNodeWrapper;
import info.magnolia.link.LinkException;
import info.magnolia.link.LinkUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.commons.predicate.Predicate;
import org.apache.jackrabbit.commons.predicate.Predicates;
import org.apache.jackrabbit.value.ValueFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.jcr.*;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

class PowerNodePropertyImpl {

	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final PowerNodeService powerNodeService;
	private final LocalizedNameProvider localizedNameProvider;

	PowerNodePropertyImpl(PowerNodeService powerNodeService, LocalizedNameProvider localizedNameProvider) {
		this.powerNodeService = powerNodeService;
		this.localizedNameProvider = localizedNameProvider;
	}

	/**
	 * @see PowerNode#hasProperty(String) (String)
	 */
	@SuppressWarnings("unused")
	private boolean hasProperty(@Nonnull Node node, String name) {
		try {
			if (StringUtils.isBlank(name)) {
				LOG.trace("Property name on 'hasProperty' is empty (node: '{}')", node.getPath());
				return false;
			}
			return node.hasProperty(name);
		} catch (RepositoryException e) {
			LOG.error("Could not check if node has property "+name, e);
		}
		return false;
	}

	/**
	 * @see PowerNode#getPropertyMap()
	 */
	@SuppressWarnings("unused")
	private Map<String, Object> getPropertyMap(@Nonnull Node node) {
		return getPropertyMap(node, Predicate.TRUE);
	}

	/**
	 * @see PowerNode#getPropertyMap()
	 */
	@SuppressWarnings("unused")
	private <T> Map<String, T> getPropertyMap(@Nonnull Node node, Class<T> type) {
		Predicate typePredicate = createTypePredicate(type);
		Map<String, Object> propertyMap = getPropertyMap(node, typePredicate);
		return (Map<String, T>) propertyMap;
	}

	/**
	 * @see PowerNode#getPropertyMap()
	 */
	@SuppressWarnings("unused")
	private Map<String, Object> getPropertyMap(@Nonnull Node node, Predicate predicate) {
		try {
			Map<String, Object> propertyMap = new HashMap<>();

			Predicate combinedPredicate = Predicates.and(predicate, notSystemPropertyPredicate);
			PropertyIterator properties = new FilteringPropertyIterator(node.getProperties(), wrapPredicate(combinedPredicate));

			while (properties.hasNext()) {
				Property property = properties.nextProperty();
				if (property.isMultiple()) {
					Value[] valueArray = property.getValues();
					List<Value> values = Arrays.asList(valueArray);
					List<Object> valueList = values.stream()
							.map(this::getValueObject)
							.collect(Collectors.toList());
					propertyMap.put(property.getName(), valueList);
				} else {
					propertyMap.put(property.getName(), getValueObject(property.getValue()));
				}
			}
			return Map.copyOf(propertyMap);
		} catch (RepositoryException e) {
			LOG.error("Could not collect node properties", e);
		}
		return Map.of();
	}

	/**
	 * @see PowerNode#getPropertyOptional(String)
	 */
	private Optional<Property> getPropertyOptional(@Nonnull Node node, String name) {
		String propName = StringUtils.defaultString(name);
		try {
			if (node.hasProperty(propName)) {
				return Optional.of(node.getProperty(propName));
			}
		} catch (RepositoryException e) {
			LOG.error("Could not retrieve property "+propName, e);
		}
		return Optional.empty();
	}

	/**
	 * @see PowerNode#getPropertyValue(String, Class)
	 */
	@SuppressWarnings({"unused"})
	private <T> Optional<T> getPropertyValue(@Nonnull Node node, String name, Class<? extends T> type) {
		RepoBiFunction<Node, String, T> extractor = getPropertyExtractorForType(type);

		if (extractor == null) {
			LOG.warn("Type '{}' is not supported", type);
			return Optional.empty();
		}

		return Optional.ofNullable(getValueFromProp(node, name, null, extractor));
	}

	/**
	 * @see PowerNode#getPropertyValue(String, Object)
	 */
	@SuppressWarnings({"unused"})
	private <T> T getPropertyValue(@Nonnull Node node, String name, T defaultValue) {
		Class<?> type = defaultValue.getClass();
		RepoBiFunction<Node, String, T> extractor = getPropertyExtractorForType(type);

		if (extractor == null) {
			LOG.warn("Type '{}' is not supported", type);
			return defaultValue;
		}

		return getValueFromProp(node, name, defaultValue, extractor);
	}

	/**
	 * @see PowerNode#getPropertyValueInherit(String, Class)
	 */
	@SuppressWarnings({"unused"})
	private <T> Optional<T> getPropertyValueInherit(@Nonnull Node node, String name, Class<? extends T> type) {
		RepoBiFunction<Node, String, T> extractor = getPropertyExtractorForType(type);

		if (extractor == null) {
			LOG.warn("Type '{}' is not supported", type);
			return Optional.empty();
		}

		return Optional.ofNullable(getInheritedValueFromProp(node, name, null, extractor));
	}

	/**
	 * @see PowerNode#getPropertyValueInherit(String, Object)
	 */
	@SuppressWarnings({"unused"})
	private <T> T getPropertyValueInherit(@Nonnull Node node, String name, T defaultValue) {
		Class<?> type = defaultValue.getClass();
		RepoBiFunction<Node, String, T> extractor = getPropertyExtractorForType(type);

		if (extractor == null) {
			LOG.warn("Type '{}' is not supported", type);
			return defaultValue;
		}

		return getInheritedValueFromProp(node, name, defaultValue, extractor);
	}

	/**
	 * @see PowerNode#getPropertyValueLocalized(String, Object, Locale)
	 */
	@SuppressWarnings({"unused"})
	private <T> Optional<T> getPropertyValueLocalized(@Nonnull Node node, String name, Locale locale, Class<? extends T> type) {
		RepoBiFunction<Node, String, T> extractor = getPropertyExtractorForType(type);

		if (extractor == null) {
			LOG.warn("Type '{}' is not supported", type);
			return Optional.empty();
		}

		return Optional.ofNullable(getLocalizedProperty(node, name, null, extractor, locale));
	}

	/**
	 * @see PowerNode#getPropertyValueLocalized(String, Object, Locale)
	 */
	@SuppressWarnings({"unused"})
	private <T> T getPropertyValueLocalized(@Nonnull Node node, String name, T defaultValue, Locale locale) {
		Class<?> type = defaultValue.getClass();
		RepoBiFunction<Node, String, T> extractor = getPropertyExtractorForType(type);

		if (extractor == null) {
			LOG.warn("Type '{}' is not supported", type);
			return defaultValue;
		}

		return getLocalizedProperty(node, name, defaultValue, extractor, locale);
	}

	/**
	 * @see PowerNode#getPropertyValueWithFallback(String, Class, String...)
	 */
	@SuppressWarnings({"unchecked", "unused"})
	private <T> Optional<T> getPropertyValueWithFallback(@Nonnull Node node, String name, Class<? extends T> type, String... fallbackNames) {
		Optional primary = getPropertyValue(node, name, type);
		if (primary.isPresent()) {
			return primary;
		}
		return Arrays.stream(fallbackNames)
				.filter(propName -> powerNodeService.convertToPowerNode(node).hasProperty(propName))
				.findFirst()
				.flatMap(propName -> getPropertyValue(node, propName, type));
	}

	/**
	 * @see PowerNode#getPropertyValueWithFallback(String, Object, String...)
	 */
	@SuppressWarnings({"unchecked", "unused"})
	private <T> T getPropertyValueWithFallback(@Nonnull Node node, String name, T defaultValue, String... fallbackNames) {
		Class<?> type = defaultValue.getClass();
		Optional primary = getPropertyValue(node, name, type);
		if (primary.isPresent()) {
			return (T) primary.get();
		}
		return Arrays.stream(fallbackNames)
				.filter(propName -> powerNodeService.convertToPowerNode(node).hasProperty(propName))
				.findFirst()
				.map(propName -> getPropertyValue(node, propName, defaultValue))
				.orElse(defaultValue);
	}

	/**
	 * @see PowerNode#getPropertyValueList(String, Class)
	 */
	@SuppressWarnings({"unchecked", "unused"})
	private <T> List<T> getPropertyValueList(@Nonnull Node node, String name, Class<? extends T> type) {
		Optional<Property> propertyOptional = getPropertyOptional(node, name);

		if (propertyOptional.isEmpty()) {
			return Collections.emptyList();
		}

		Property property = propertyOptional.get();
		RepoFunction extractor = getValueExtractorForType(type);
		try {
			if (property.isMultiple()) {
				Value[] valueArray = property.getValues();
				List<Value> values = Arrays.asList(valueArray);
				return values.stream()
						.map(extractValue(extractor, type))
						.collect(Collectors.toList());
			} else {
				return Collections.singletonList((T) extractor.apply(property.getValue()));
			}
		} catch (RepositoryException e) {
			LOG.error("Could not get value list from node '"+node+"', property '"+name+"', type '"+type+"'", e);
		}
		return Collections.emptyList();
	}

	/**
	 * @see PowerNode#removeProperty(String)
	 */
	@SuppressWarnings("unused")
	private void removeProperty(@Nonnull Node node, String name) throws RepositoryException {
		if (!node.hasProperty(name)) {
			return;
		}
		getPropertyOptional(node, name).ifPresent(property -> {
			try {
				property.remove();
			} catch (RepositoryException e) {
				LOG.error("Could not remove property '"+name+"' from node '"+node+"'", e);
				throw PowerNodeException.wrap(e, PowerNodeException.Type.JCR_REPOSITORY);
			}
		});
	}

	/**
	 * @see PowerNode#setPropertyValue(String, Object)
	 */
	@SuppressWarnings({"unchecked", "unused"})
	private <T> void setPropertyValue(@Nonnull Node node, String name, T value) throws RepositoryException {
		if (value instanceof Collection) {
			ArrayList<Value> values = new ArrayList<>();
			((Collection<T>) value).stream()
					.map(this::createValue)
					.filter(Objects::nonNull)
					.forEach(values::add);
			node.setProperty(name, values.toArray(new Value[0]));
		} else {
			Value val = createValue(value);
			node.setProperty(name, val);
		}
	}

	/**
	 * @see PowerNode#setPropertyValueIfNotPresent(String, Object)
	 */
	@SuppressWarnings("unused")
	private <T> void setPropertyValueIfNotPresent(@Nonnull Node node, String name, T value) throws RepositoryException {
		if (node.hasProperty(name)) {
			return;
		}
		setPropertyValue(node, name, value);
	}

	private <T> Value createValue(T value) {
		Class<?> type = value.getClass();
		Value val = null;
		ValueFactory valueFactory = ValueFactoryImpl.getInstance();
		try {
			if (String.class.equals(type)) {
				val = valueFactory.createValue((String) value);
			} else if (Calendar.class.isAssignableFrom(type)) {
				val = valueFactory.createValue((Calendar) value);
			} else if (Boolean.class.equals(type)) {
				val = valueFactory.createValue((Boolean) value);
			} else if (Long.class.equals(type)) {
				val = valueFactory.createValue((Long) value);
			} else if (Short.class.equals(type)) {
				val = valueFactory.createValue((Short) value);
			} else if (Integer.class.equals(type)) {
				val = valueFactory.createValue((Integer) value);
			} else if (Double.class.equals(type)) {
				val = valueFactory.createValue(((Double) value));
			} else if (Number.class.equals(type)) {
				val = valueFactory.createValue(((Number) value).doubleValue());
			} else if (BigDecimal.class.equals(type)) {
				val = valueFactory.createValue((BigDecimal) value);
			} else if (InputStream.class.isAssignableFrom(type)) {
				val = valueFactory.createValue(valueFactory.createBinary((InputStream) value));
			} else if (Date.class.equals(type)) {
				Calendar cal = Calendar.getInstance();
				cal.setTime((Date) value);
				val = valueFactory.createValue(cal);
			} else if (LocalDateTime.class.equals(type)) {
				LocalDateTime ldt = (LocalDateTime) value;
				GregorianCalendar cal = GregorianCalendar.from(ZonedDateTime.of(ldt, ZoneId.systemDefault()));
				val = valueFactory.createValue(cal);
			} else if (LocalDate.class.equals(type)) {
				LocalDate ld = (LocalDate) value;
				Date date = Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
				Calendar cal = Calendar.getInstance();
				cal.setTime(date);
				val = valueFactory.createValue(cal);
			} else if (Node.class.isAssignableFrom(type)) {
				val = valueFactory.createValue((Node) value);
			} else {
				LOG.warn("Can't create value from object '{}' with type '{}", value, type);
				throw new PowerNodeException(PowerNodeException.Type.JCR_REPOSITORY, "Value type not supported: '{}'", type.getName());
			}
		} catch (RepositoryException e) {
			LOG.error("Can't create value from object '"+value+"' with type '"+type+"'", e);
		}
		return val;
	}


	private <T> T getInheritedValueFromProp(@Nonnull Node node, String name, T defaultValue, RepoBiFunction<Node, String, T> extractor) {
		String propName = StringUtils.defaultString(name);
		try {
			if (node.hasProperty(propName)) {
				return extractor.apply(node, name);
			} else {
				return Optional.of(node.getParent())
						.map(parent -> getInheritedValueFromProp(parent, name, defaultValue, extractor))
						.orElse(defaultValue);
			}
		} catch (RepositoryException e) {
			LOG.error("Can't read value '"+name+"' of the Node '"+node+"' will return default value", e);
		}
		return defaultValue;
	}

	private <T> T getLocalizedProperty(@Nonnull Node node, String name, T defaultValue, RepoBiFunction<Node, String, T> extractor, Locale locale) {
		if (StringUtils.isBlank(name)) {
			return defaultValue;
		}
		if (!NodeUtil.isWrappedWith(node, I18nNodeWrapper.class)) {
			return getValueFromProp(node, localizedNameProvider.getLocalizedPropertyName(node, name, locale), defaultValue, extractor);
		}
		return getValueFromProp(node, name, defaultValue, extractor);
	}

	@SuppressWarnings("unchecked")
	private <T> Function<Value, T> extractValue(RepoFunction extractor, Class<T> type) {
		return value -> {
			try {
				return type.cast(extractor.apply(value));
			} catch (RepositoryException | ClassCastException e) {
				LOG.error("Could not get value", e);
				throw PowerNodeException.wrap(e, PowerNodeException.Type.JCR_REPOSITORY);
			}
		};
	}

	private <T> T getValueFromProp(@Nonnull Node node, String name, T defaultValue, RepoBiFunction<Node, String, T> extractor) {
		String propName = StringUtils.defaultString(name);
		try {
			if (node.hasProperty(propName)) {
				return extractor.apply(node, name);
			}
		} catch (RepositoryException e) {
			LOG.error("Can't read value '"+name+"' of the Node '"+node+"' will return default value", e);
		}
		return defaultValue;
	}

	@SuppressWarnings("unchecked")
	private <T> RepoBiFunction<Node, String, T> getPropertyExtractorForType(Class type) {
		RepoBiFunction<Node, String, T> extractor = null;
		if (String.class.equals(type)) {
			extractor = (n, s) -> (T) convertUuidsToLink(n.getProperty(s).getString());
		} else if (Calendar.class.isAssignableFrom(type)) {
			extractor = (n, s) -> (T) n.getProperty(s).getDate();
		} else if (Boolean.class.equals(type)) {
			extractor = (n, s) -> (T) Boolean.valueOf(n.getProperty(s).getBoolean());
		} else if (Long.class.equals(type)) {
			extractor = (n, s) -> (T) Long.valueOf(n.getProperty(s).getLong());
		} else if (Double.class.equals(type)) {
			extractor = (n, s) -> (T) Double.valueOf(n.getProperty(s).getDouble());
		} else if (BigDecimal.class.equals(type)) {
			extractor = (n, s) -> (T) n.getProperty(s).getDecimal();
		} else if (InputStream.class.isAssignableFrom(type)) {
			extractor = (n, s) -> (T) n.getProperty(s).getBinary().getStream();
		} else if (Date.class.equals(type)) {
			extractor = (n, s) -> (T) n.getProperty(s).getDate().getTime();
		} else if (LocalDateTime.class.equals(type)) {
			extractor = (n, s) -> (T) n.getProperty(s).getDate()
					.getTime()
					.toInstant()
					.atZone(ZoneId.systemDefault())
					.toLocalDateTime();
		} else if (LocalDate.class.equals(type)) {
			extractor = (n, s) -> (T) n.getProperty(s).getDate()
					.getTime()
					.toInstant()
					.atZone(ZoneId.systemDefault())
					.toLocalDate();
		}
		return extractor;
	}

	@SuppressWarnings("unchecked")
	private <T> RepoFunction<Value, T> getValueExtractorForType(Class type) {
		RepoFunction<Value, T> extractor = null;
		if (String.class.equals(type)) {
			extractor = (v) -> (T) convertUuidsToLink(v.getString());
		} else if (Calendar.class.equals(type)) {
			extractor = (v) -> (T) v.getDate();
		} else if (Boolean.class.equals(type)) {
			extractor = (v) -> (T) Boolean.valueOf(v.getBoolean());
		} else if (Long.class.equals(type)) {
			extractor = (v) -> (T) Long.valueOf(v.getLong());
		} else if (Double.class.equals(type)) {
			extractor = (v) -> (T) Double.valueOf(v.getDouble());
		} else if (BigDecimal.class.equals(type)) {
			extractor = (v) -> (T) v.getDecimal();
		} else if (InputStream.class.equals(type)) {
			extractor = (v) -> (T) v.getBinary().getStream();
		} else if (Date.class.equals(type)) {
			extractor = (v) -> (T) v.getDate().getTime();
		} else if (LocalDateTime.class.equals(type)) {
			extractor = (v) -> (T) v.getDate()
					.getTime()
					.toInstant()
					.atZone(ZoneId.systemDefault())
					.toLocalDateTime();
		} else if (LocalDate.class.equals(type)) {
			extractor = (v) -> (T) v.getDate()
					.getTime()
					.toInstant()
					.atZone(ZoneId.systemDefault())
					.toLocalDate();
		}
		return extractor;
	}

	private Object getValueObject(Value value) {
		try {
			switch (value.getType()) {
				case (PropertyType.DECIMAL):
					return value.getDecimal();
				case (PropertyType.DOUBLE):
					return value.getDouble();
				case (PropertyType.LONG):
					return value.getLong();
				case (PropertyType.BOOLEAN):
					return value.getBoolean();
				case (PropertyType.DATE):
					return value.getDate().getTime();
				case (PropertyType.URI):
					return new URI(value.getString());
				case (PropertyType.BINARY):
					return ValueFactoryImpl.getInstance().createBinary(value.getBinary().getStream());
				case (PropertyType.STRING):
				default:
					return value.getString();
			}
		} catch (RuntimeException | RepositoryException | URISyntaxException e) {
			LOG.error("Could not cast property", e);
		}
		return StringUtils.EMPTY;
	}

	private Class<?> getValueType(Value value) {
		try {
			switch (value.getType()) {
				case (PropertyType.DECIMAL):
					return BigDecimal.class;
				case (PropertyType.DOUBLE):
					return double.class;
				case (PropertyType.LONG):
					return long.class;
				case (PropertyType.BOOLEAN):
					return boolean.class;
				case (PropertyType.DATE):
					return Date.class;
				case (PropertyType.BINARY):
					return Binary.class;
				case (PropertyType.STRING):
				case (PropertyType.URI):
				default:
					return String.class;
			}
		} catch (RuntimeException e) {
			LOG.error("Could not find type of value", e);
		}
		return String.class;
	}

	//FIXME: LinkUtil.convertLinksFromUUIDPattern uses MgnlContext, consider rewriting
	private static String convertUuidsToLink(String stringValue) {
		try {
			return LinkUtil.convertLinksFromUUIDPattern(stringValue);
		} catch (LinkException e) {
			LOG.error("Cannot rewrite uuid links. return original text: "+ stringValue, e);
		}
		return stringValue;
	}

	/**
	 * Functional Interface which throws a RepositoryException (checked).
	 *
	 * @param <T> first parameter for the function
	 * @param <U> second parameter for the function
	 * @param <R> return type
	 */
	@FunctionalInterface
	private interface RepoBiFunction<T, U, R> {
		R apply(T t, U u) throws RepositoryException;
	}

	/**
	 * Functional Interface which throws a RepositoryException (checked).
	 *
	 * @param <T> parameter for the function
	 * @param <R> return type
	 */
	@FunctionalInterface
	private interface RepoFunction<T, R> {
		R apply(T t) throws RepositoryException;

	}

	/**
	 * Predicate needed to filter meta-data nodes such as "jcr:system".
	 * Nodes not starting with 'jcr:' match the predicate.
	 */
	private final AbstractPredicate<Property> notSystemPropertyPredicate = new AbstractPredicate<>() {
		@Override
		public boolean evaluateTyped(Property property) {
			try {
				return !StringUtils.startsWithAny(property.getName(), NodeTypes.JCR_PREFIX, NodeTypes.MGNL_PREFIX);
			} catch (RepositoryException e) {
				LOG.error("Could not retrieve property name.", e);
			}
			return false;
		}
	};

	private Predicate createTypePredicate(Class<?> type) {
		return property -> {
			if (property instanceof Property) {
				Property prop = (Property) property;
				try {
					if (prop.isMultiple() && type.isAssignableFrom(List.class)) {
						return true;
					}
					return getValueType(prop.getValue()).isAssignableFrom(type);
				} catch (RepositoryException e) {
					LOG.error("Could not get property value", e);
				}
			}
			return false;
		};
	}

	@Nonnull
	private AbstractPredicate<Property> wrapPredicate(Predicate combinedPredicate) {
		return new AbstractPredicate<>() {
			@Override
			public boolean evaluateTyped(Property property) {
				return combinedPredicate.evaluate(property);
			}
		};
	}
}
