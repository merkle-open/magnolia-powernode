package com.merkle.oss.magnolia.powernode;

import info.magnolia.jcr.RuntimeRepositoryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.RepositoryException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RepositoryExceptionDelegatorTest {
	private RepositoryExceptionDelegator repositoryExceptionDelegator;

	@BeforeEach
	void setUp() {
		repositoryExceptionDelegator = new RepositoryExceptionDelegator();
	}

	@Test
	void run() {
		final RuntimeRepositoryException exception = assertThrows(RuntimeRepositoryException.class, () -> repositoryExceptionDelegator.run(() -> {
			throw new RepositoryException("some repo exception");
		}));
		assertEquals("some repo exception", exception.getMessage());
	}

	@Test
	void getOrThrow() {
		final RuntimeRepositoryException exception = assertThrows(RuntimeRepositoryException.class, () -> repositoryExceptionDelegator.getOrThrow(() -> {
			throw new RepositoryException("some repo exception");
		}));
		assertEquals("some repo exception", exception.getMessage());

		assertEquals(42, repositoryExceptionDelegator.getOrThrow(() -> 42));
	}

	@Test
	void get() {
		assertTrue(repositoryExceptionDelegator.get(() -> {
			throw new RepositoryException("some repo exception");
		}).isEmpty());

		assertEquals(Optional.of(42), repositoryExceptionDelegator.get(() -> 42));
	}
}