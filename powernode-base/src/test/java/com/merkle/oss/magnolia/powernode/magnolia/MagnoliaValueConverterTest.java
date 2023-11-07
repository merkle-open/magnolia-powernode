package com.merkle.oss.magnolia.powernode.magnolia;

import com.merkle.oss.magnolia.powernode.ValueConverter;
import info.magnolia.link.LinkUtil;
import org.apache.jackrabbit.commons.SimpleValueFactory;
import org.apache.jackrabbit.value.StringValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import javax.jcr.RepositoryException;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;

class MagnoliaValueConverterTest {
	private ValueConverter valueConverter;

	@BeforeEach
	void setUp() {
		valueConverter = new MagnoliaValueConverter(new SimpleValueFactory(), ZoneId::systemDefault);
	}

	@Test
	void toStringValue() {
		try (MockedStatic<LinkUtil> linkUtil = mockStatic(LinkUtil.class)) {
			final String stringWithUUIDs = "someString with uuidPattern";
			final String stringWithLinks = "someString with link";
			linkUtil.when(() -> LinkUtil.convertAbsoluteLinksToUUIDs(stringWithLinks)).thenReturn(stringWithUUIDs);
			assertEquals(
					Optional.of(new StringValue(stringWithUUIDs)),
					valueConverter.toValue(stringWithLinks)
			);
		}
	}

	@Test
	void getString() throws RepositoryException {
		try (MockedStatic<LinkUtil> linkUtil = mockStatic(LinkUtil.class)) {
			final String stringWithUUIDs = "someString with uuidPattern";
			final String stringWithLinks = "someString with link";
			linkUtil.when(() -> LinkUtil.convertLinksFromUUIDPattern(stringWithUUIDs)).thenReturn(stringWithLinks);
			assertEquals(
					Optional.of(stringWithLinks),
					valueConverter.getString(new StringValue(stringWithUUIDs))
			);
		}
	}
}