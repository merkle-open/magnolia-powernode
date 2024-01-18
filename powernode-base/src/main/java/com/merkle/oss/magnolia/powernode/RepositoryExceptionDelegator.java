package com.merkle.oss.magnolia.powernode;

import info.magnolia.jcr.RuntimeRepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.ItemNotFoundException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.lang.invoke.MethodHandles;
import java.util.Optional;

public class RepositoryExceptionDelegator {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public void run(final NodeService.RepositoryRunnable runnable) {
		getOrThrow(() -> {
			runnable.run();
			return null;
		});
	}

	public <T> T getOrThrow(final NodeService.RepositoryProvider<T> provider) {
		try {
			return provider.get();
		} catch (RepositoryException e) {
			throw new RuntimeRepositoryException(e.getMessage(), e);
		}
	}

	public <T> Optional<T> get(final NodeService.RepositoryProvider<T> provider) {
		try {
			return Optional.ofNullable(provider.get());
		} catch (PathNotFoundException | ItemNotFoundException e) {
			return Optional.empty();
		} catch (RuntimeRepositoryException e) {
			return get(() -> {throw (RepositoryException)e.getCause();});
		} catch (RepositoryException e) {
			LOG.error("Failed to apply node function!", e);
			return Optional.empty();
		}
	}

	public interface RepositoryProvider<T> {
		T get() throws RepositoryException;
	}

	public interface RepositoryRunnable {
		void run() throws RepositoryException;
	}
}
