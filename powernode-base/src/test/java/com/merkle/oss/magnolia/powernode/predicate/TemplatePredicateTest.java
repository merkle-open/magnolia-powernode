package com.merkle.oss.magnolia.powernode.predicate;

import com.merkle.oss.magnolia.powernode.AbstractPowerNode;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class TemplatePredicateTest {

	@Test
	<N extends AbstractPowerNode<N>> void  test() {
		N node = (N)mock(AbstractPowerNode.class);
		doReturn(Optional.of("someTemplateId")).when(node).getTemplate();
		assertTrue(new TemplatePredicate<N>("someTemplateId").test(node));
		assertFalse(new TemplatePredicate<N>("someOtherTemplateId").test(node));
	}
}