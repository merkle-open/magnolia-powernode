package com.merkle.oss.magnolia.powernode.generator;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;

public interface ClassGenerator {
	JavaFile generate(AnnotationSpec... additionalClassAnnotations);
}
