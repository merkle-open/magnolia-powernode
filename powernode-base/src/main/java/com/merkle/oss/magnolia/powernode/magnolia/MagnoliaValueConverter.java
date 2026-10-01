package com.merkle.oss.magnolia.powernode.magnolia;

import info.magnolia.link.LinkUtil;

import java.lang.invoke.MethodHandles;
import java.time.ZoneId;
import java.util.Optional;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.merkle.oss.magnolia.powernode.ValueConverter;

import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class MagnoliaValueConverter extends ValueConverter {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public MagnoliaValueConverter(final ValueFactory factory, final Provider<ZoneId> zoneIdProvider) {
		super(factory, zoneIdProvider);
	}

	public Optional<Value> toValue(@Nullable final String value) {
		return Optional.ofNullable(value).map(this::convertAbsoluteLinksToIdentifiers).flatMap(super::toValue);
	}
	protected String convertAbsoluteLinksToIdentifiers(final String value) {
		try {
			return LinkUtil.convertAbsoluteLinksToUUIDs(value);
		} catch (Exception e) {
			LOG.debug("Failed to convert absolute links to identifiers for '{}', resolving original value...", value, e);
            return value;
        }
	}

	public Optional<String> getString(final Value value) throws RepositoryException {
		return super.getString(value).map(this::convertLinksFromIdentifierPattern);
	}
	protected String convertLinksFromIdentifierPattern(final String value) {
		try {
			return LinkUtil.convertLinksFromUUIDPattern(value);
		} catch (Exception e) {
			LOG.debug("Failed to convert links from identifier pattern for '{}', resolving original value...", value, e);
			return value;
		}
	}

	public static class Factory implements ValueConverter.Factory {
		private final Provider<ZoneId> zoneIdProvider;

		@Inject
		public Factory(final Provider<ZoneId> zoneIdProvider) {
			this.zoneIdProvider = zoneIdProvider;
		}

		@Override
		public MagnoliaValueConverter create(final ValueFactory valueFactory) {
			return new MagnoliaValueConverter(valueFactory, zoneIdProvider);
		}
	}
}
