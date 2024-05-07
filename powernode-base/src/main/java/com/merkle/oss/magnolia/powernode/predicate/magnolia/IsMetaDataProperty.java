package com.merkle.oss.magnolia.powernode.predicate.magnolia;

import info.magnolia.jcr.util.NodeTypes;

import java.util.Set;
import java.util.function.Predicate;

import javax.jcr.Property;

import com.merkle.oss.magnolia.powernode.RepositoryExceptionDelegator;

public class IsMetaDataProperty extends RepositoryExceptionDelegator implements Predicate<Property> {
    private final Set<String> NAME_PREFIXES = Set.of(NodeTypes.JCR_PREFIX, NodeTypes.REP_PREFIX);
    private final Set<String> BLACKLISTED_NAMES = Set.of(
            NodeTypes.Created.NAME,
            NodeTypes.Created.CREATED_BY,
            NodeTypes.LastModified.NAME,
            NodeTypes.LastModified.LAST_MODIFIED_BY,
            NodeTypes.Activatable.NAME,
            NodeTypes.Activatable.LAST_ACTIVATED,
            NodeTypes.Activatable.LAST_ACTIVATED_BY,
            NodeTypes.Activatable.LAST_ACTIVATED_VERSION,
            NodeTypes.Activatable.LAST_ACTIVATED_VERSION_CREATED,
            NodeTypes.Activatable.ACTIVATION_STATUS
    );

    @Override
    public boolean test(final Property property) {
        return NAME_PREFIXES.stream().anyMatch(prefix -> {
            final String name = getOrThrow(property::getName);
            return name.startsWith(prefix) || BLACKLISTED_NAMES.contains(name);
        });
    }
}
