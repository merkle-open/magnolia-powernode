package com.merkle.oss.magnolia.powernode.mock;

import com.merkle.oss.magnolia.powernode.*;
import info.magnolia.jcr.util.NodeNameHelper;

import javax.jcr.Node;
import java.time.ZoneId;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class MockPowerNode extends AbstractPowerNode<MockPowerNode> {

	public MockPowerNode(final String name) {
		this(name, "nt:base");
	}
	public MockPowerNode(final String name, final String primaryNodeType) {
		super(nodeService(), new MockNode(name, primaryNodeType), new MockPowerNodeDecorator());
	}
	private MockPowerNode(final NodeService nodeService, final Node node, final MockPowerNodeDecorator decorator) {
		super(nodeService, node, decorator);
	}

	public static class MockPowerNodeDecorator extends AbstractPowerNodeDecorator<MockPowerNode> {
		@Override
		protected MockPowerNode wrapNodeInternal(Node node) {
			return new MockPowerNode(nodeService(), node, this);
		}
	}

	private static NodeService nodeService() {
		final NodeNameHelper nodeNameHelper = mock(NodeNameHelper.class);
		doAnswer(invocationOnMock -> invocationOnMock.getArgument(0)).when(nodeNameHelper).getValidatedName(anyString());
		return new NodeService(
				new LocalizedNameProviderMock(),
				nodeNameHelper,
				new JcrSessionProviderMock(),
				new PropertyService(valueFactory -> new ValueConverter(valueFactory, ZoneId::systemDefault))
		);
	}
}
