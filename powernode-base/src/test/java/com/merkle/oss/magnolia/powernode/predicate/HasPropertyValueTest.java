package com.merkle.oss.magnolia.powernode.predicate;

import com.merkle.oss.magnolia.powernode.mock.MockPowerNode;
import com.merkle.oss.magnolia.powernode.ValueConverter;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HasPropertyValueTest {

	@Test
	void  test() {
		final MockPowerNode node = new MockPowerNode("name");
		assertFalse(new HasPropertyValue<MockPowerNode, String>("someKey", ValueConverter::getString, "someValue").test(node));
		node.setProperty("someKey", "someValue", ValueConverter::toValue);
		node.setProperty("someKey", Locale.ENGLISH, "someValue", ValueConverter::toValue);
		assertTrue(new HasPropertyValue<MockPowerNode, String>("someKey", ValueConverter::getString, "someValue").test(node));
		assertFalse(new HasPropertyValue<MockPowerNode, String>("someKey", ValueConverter::getString, "someOtherValue").test(node));
		assertFalse(new HasPropertyValue<MockPowerNode, String>("someOtherKey", ValueConverter::getString, "someValue").test(node));
		assertTrue(new HasPropertyValue<MockPowerNode, String>("someKey", Locale.ENGLISH, ValueConverter::getString, "someValue").test(node));
		assertFalse(new HasPropertyValue<MockPowerNode, String>("someOtherKey", Locale.ENGLISH, ValueConverter::getString, "someValue").test(node));
		assertFalse(new HasPropertyValue<MockPowerNode, String>("someKey", Locale.GERMAN, ValueConverter::getString, "someValue").test(node));
		assertFalse(new HasPropertyValue<MockPowerNode, String>("someKey", Locale.ENGLISH, ValueConverter::getString, "someOtherValue").test(node));
	}
}