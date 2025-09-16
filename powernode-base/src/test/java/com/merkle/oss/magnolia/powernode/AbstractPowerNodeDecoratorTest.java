package com.merkle.oss.magnolia.powernode;

import static org.junit.jupiter.api.Assertions.*;

import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.wrapper.I18nNodeWrapper;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockSession;

import java.time.ZoneId;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.merkle.oss.magnolia.powernode.mock.LocalizedNameProviderMock;

class AbstractPowerNodeDecoratorTest {
    private PowerNodeDecorator powerNodeDecorator;

    @BeforeEach
    void setUp() {
        ComponentsTestUtil.setImplementation(I18nContentSupport.class, DefaultI18nContentSupport.class);
        this.powerNodeDecorator = new PowerNodeDecorator();
    }

    @Test
    void wrap_unwrapI18nNodeWrapper() {
        final MockNode mockNode = new MockNode(new MockSession("testing"));
        final I18nNodeWrapper i18nNodeWrapper = new I18nNodeWrapper(mockNode);
        final PowerNode node = powerNodeDecorator.wrapNode(i18nNodeWrapper);
        assertFalse(NodeUtil.isWrappedWith(node, I18nNodeWrapper.class));
    }

    @Test
    void wrap_alreadyWrapped() throws RepositoryException {
        final MockNode mockNode = new MockNode(new MockSession("testing"));
        final PowerNode node = powerNodeDecorator.wrapNode(new I18nNodeWrapper(powerNodeDecorator.wrapNode(mockNode)));
        assertTrue(NodeUtil.isWrappedWith(node, PowerNode.class));
        assertFalse(NodeUtil.isWrappedWith(NodeUtil.unwrap(node), PowerNode.class));
    }

    private static class PowerNode extends AbstractPowerNode<PowerNode> {
        private PowerNode(final Node node) {
            super(
                    null,
                    node,
                    new PowerNodeDecorator()
            );
        }
    }
    private static class PowerNodeDecorator extends AbstractPowerNodeDecorator<PowerNode> {
        @Override
        protected PowerNode wrapNodeInternal(final Node node) {
            return new PowerNode(node);
        }
        @Override
        protected Node unwrapNodeInternal(final Node node) {
            return NodeUtil.deepUnwrap(node, PowerNode.class);
        }
    };
}
