package com.merkle.oss.magnolia.powernode.magnolia;

import com.machinezoo.noexception.Exceptions;
import com.merkle.oss.magnolia.powernode.ValueConverter;
import info.magnolia.link.LinkUtil;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import java.time.ZoneId;
import java.util.Optional;

public class MagnoliaValueConverter extends ValueConverter {

	public MagnoliaValueConverter(final ValueFactory factory, final Provider<ZoneId> zoneIdProvider) {
		super(factory, zoneIdProvider);
	}

	public Optional<Value> toValue(@Nullable final String value) {
		return Optional.ofNullable(value).map(LinkUtil::convertAbsoluteLinksToUUIDs).flatMap(super::toValue);
	}

	public Optional<String> getString(final Value value) throws RepositoryException {
		return super.getString(value).map(string -> Exceptions.wrap().get(() -> LinkUtil.convertLinksFromUUIDPattern(string)));
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
