package com.merkle.oss.magnolia.powernode.predicate;

import java.util.Set;
import java.util.function.Predicate;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Workspace;

import com.merkle.oss.magnolia.powernode.RepositoryExceptionDelegator;

public class IsWorkspace<N extends Node> extends RepositoryExceptionDelegator implements Predicate<N> {
	private final Set<String> workspaces;

	public IsWorkspace(final String... workspaces) {
		this(Set.of(workspaces));
	}

	public IsWorkspace(final Set<String> workspaces) {
		this.workspaces = workspaces;
	}

	@Override
	public boolean test(final N node) {
		return get(node::getSession)
				.map(Session::getWorkspace)
				.map(Workspace::getName)
				.map(workspaces::contains)
				.orElse(false);
	}
}
