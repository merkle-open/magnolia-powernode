package com.namics.oss.magnolia.powernode;

import javax.jcr.Node;

public interface ExecutableMethod<T> {
	T execute(Node node, Object[] methodArgs) throws Exception;
}