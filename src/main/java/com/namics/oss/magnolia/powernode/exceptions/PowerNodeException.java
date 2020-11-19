package com.namics.oss.magnolia.powernode.exceptions;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.helpers.MessageFormatter;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class PowerNodeException extends RuntimeException {

	private static final long serialVersionUID = -229172473220960435L;

	private static final String EMPTY_MESSAGE = "EMPTY MESSAGE";
	private static final String TYPE = "Type";


	private final transient Map<String, Object> data = new HashMap<>();

	private Type type;

	/**
	 * Default constructor for AppBuilderException.
	 *
	 * @param message Custom exceptions message
	 * @param params  Message parameters
	 */
	public PowerNodeException(Type type, String message, String... params) {
		super(MessageFormatter.arrayFormat(message, params).getMessage());
		this.type = type;
	}

	private PowerNodeException(Throwable e, Type type) {
		super(e.getMessage(), e);
		this.type = type;
	}

	/**
	 * @param exception the existing exceptions
	 * @return AppBuilderException wrapped around the original exceptions with default error code
	 */
	public static PowerNodeException wrap(Throwable exception, Type type) {
		if (exception instanceof PowerNodeException) {
			PowerNodeException systemException = (PowerNodeException) exception;
			if (type != null && type != systemException.getType()) {
				return new PowerNodeException(exception, type);
			}
			return systemException;
		} else {
			return new PowerNodeException(exception, type);
		}
	}

	/**
	 * @param exception the existing exceptions
	 * @return SystemException wrapped around the original exceptions with default error code
	 */
	public static PowerNodeException wrap(Throwable exception) {
		return wrap(exception, Type.DEFAULT);
	}

	public Type getType() {
		return type;
	}

	/**
	 * @param key   of the value to add to exceptions
	 * @param value to add to exceptions
	 * @return The exceptions itself
	 */
	public PowerNodeException set(final String key, final Object value) {
		data.put(key, value);
		return this;
	}

	@Override
	public String getMessage() {
		final StringBuilder message = new StringBuilder(StringUtils.defaultString(super.getMessage(), EMPTY_MESSAGE));
		message.append(" (" + TYPE + ": ").append(type.toString()).append(")");
		if (!data.isEmpty()) {
			message.append(System.lineSeparator());
			message.append(data.entrySet()
					.stream()
					.map(entry -> entry.getKey() + ": " + entry.getValue())
					.collect(Collectors.joining(", ")));
		}
		return message.toString();
	}

	public enum Type {
		DEFAULT,
		REFLECTION,
		JCR_REPOSITORY
	}
}
