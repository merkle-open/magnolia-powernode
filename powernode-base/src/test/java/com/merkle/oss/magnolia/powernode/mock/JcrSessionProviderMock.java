package com.merkle.oss.magnolia.powernode.mock;

import com.merkle.oss.magnolia.powernode.JcrSessionProvider;

import javax.jcr.Session;
import java.util.HashMap;
import java.util.Map;

public class JcrSessionProviderMock implements JcrSessionProvider {
	private final Map<String, Session> mocks = new HashMap<>();
	private final Map<String, Session> systemMocks = new HashMap<>();

	public void mock(final Session session) {
		mocks.put(session.getWorkspace().getName(), session);
	}

	@Override
	public Session getSession(final String workspace) {
		if (mocks.containsKey(workspace)) {
			return mocks.get(workspace);
		}
		throw new IllegalStateException(workspace + " not mocked!");
	}

	public void mockSystem(final Session session) {
		systemMocks.put(session.getWorkspace().getName(), session);
	}

	@Override
	public Session getSystemSession(final String workspace) {
		if (systemMocks.containsKey(workspace)) {
			return systemMocks.get(workspace);
		}
		throw new IllegalStateException(workspace + " not mocked!");
	}
}
