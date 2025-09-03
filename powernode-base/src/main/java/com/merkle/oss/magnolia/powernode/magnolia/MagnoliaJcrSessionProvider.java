package com.merkle.oss.magnolia.powernode.magnolia;

import com.merkle.oss.magnolia.powernode.JcrSessionProvider;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

public class MagnoliaJcrSessionProvider implements JcrSessionProvider {
	private final Provider<SystemContext> systemContextProvider;

	@Inject
	public MagnoliaJcrSessionProvider(final Provider<SystemContext> systemContextProvider) {
		this.systemContextProvider = systemContextProvider;
	}

	@Override
	public Session getSession(final String workspace) throws RepositoryException {
		return MgnlContext.getJCRSession(workspace);
	}

	@Override
	public Session getSystemSession(final String workspace) throws RepositoryException {
		return systemContextProvider.get().getJCRSession(workspace);
	}
}
