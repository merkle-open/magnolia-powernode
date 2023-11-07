package com.merkle.oss.magnolia.powernode;

import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.RepositoryException;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class PropertyServiceTest {
	private PropertyService propertyService;
	private MockNode node;

	@BeforeEach
	void setUp() {
		propertyService = new PropertyService(valueFactory -> new ValueConverter(valueFactory, ZoneId::systemDefault));
		node = new MockNode(new MockSession("someWorkspace"));
	}

	@Test
	void property() throws RepositoryException {
		propertyService.setProperty(node, "someKey", "someValue", ValueConverter::toValue);
		assertEquals(
				Optional.of("someValue"),
				propertyService.getProperty(node, "someKey", ValueConverter::getString)
		);
		propertyService.removeProperty(node, "someKey");
		assertTrue(propertyService.getProperty(node, "someKey", ValueConverter::getString).isEmpty());
	}

	@Test
	void setAndStreamMultiProperty() throws RepositoryException {
		propertyService.setMultiProperty(node, "someKey", List.of("someValue", "someOtherValue"), ValueConverter::toValue);
		assertEquals(
				List.of("someValue", "someOtherValue"),
				propertyService.streamMultiProperty(node, "someKey", ValueConverter::getString).collect(Collectors.toList())
		);
		propertyService.removeProperty(node, "someKey");
		assertEquals(0, propertyService.streamMultiProperty(node, "someKey", ValueConverter::getString).count());

	}
}