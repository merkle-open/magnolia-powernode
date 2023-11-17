package com.merkle.oss.magnolia.powernode.generator;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;

import javax.annotation.processing.Generated;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class SourceGenerator {
	private final Set<ClassGenerator> classGenerators = Set.of(
			new PowerNodeClassGenerator(),
			new PowerNodeDecoratorClassGenerator(),
			new DelegatingPowerNodeArgumentResolverFactoryClassGenerator(),
			new PowerNodeConfigurationClassGenerator(),
			new PowerNodeServiceClassGenerator()
	);

	public void generateSources(final Path path) throws IOException {
		write(createSources(), path);
	}

	private void write(final Collection<JavaFile> files, final Path path) throws IOException {
		for (JavaFile file : files) {
			file.writeTo(path);
		}
	}

	private Set<JavaFile> createSources() {
		final AnnotationSpec generated = AnnotationSpec.builder(Generated.class)
				.addMember("value", "$S", this.getClass().getName())
				.addMember("date", "$S", LocalDateTime.now().toString())
				.build();
		return classGenerators.stream()
				.map(classGenerator ->
						classGenerator.generate(generated)
				)
				.collect(Collectors.toSet());
	}

	public static void main(final String[] args) throws IOException {
		final Path outputDirectory = Path.of(args[0]);
		new SourceGenerator().generateSources(outputDirectory);
	}
}
