package com.merkle.oss.magnolia.powernode;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

public interface JcrSessionProvider {
	Session getSession(String workspace) throws RepositoryException;
	Session getSystemSession(String workspace) throws RepositoryException;
}
