package com.merkle.oss.magnolia.powernode.mock;

import info.magnolia.test.mock.jcr.MockWorkspace;

public class MockSession extends info.magnolia.test.mock.jcr.MockSession {
	public MockSession(final String name) {
		super(name);
		setRootNode(new MockNode(this));
	}
	public MockSession(final MockWorkspace workspace) {
		super(workspace);
		setRootNode(new MockNode(this));
	}
}
