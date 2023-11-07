package com.merkle.oss.magnolia.powernode.generator;

import com.merkle.oss.magnolia.powernode.AbstractPowerNodeService;
import com.merkle.oss.magnolia.powernode.NodeService;
import com.squareup.javapoet.*;

import javax.inject.Inject;
import javax.lang.model.element.Modifier;
import java.util.List;

public class PowerNodeServiceClassGenerator implements ClassGenerator {
	public static final ClassName CLASS_NAME = ClassName.get(AbstractPowerNodeService.class.getPackageName(), "PowerNodeService");

	@Override
	public JavaFile generate(final AnnotationSpec... additionalClassAnnotations) {
		final MethodSpec constructor = MethodSpec.constructorBuilder()
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(Inject.class)
				.addParameter(NodeService.class, "nodeService", Modifier.FINAL)
				.addParameter(PowerNodeDecoratorClassGenerator.CLASS_NAME, "decorator", Modifier.FINAL)
				.addStatement("super(nodeService, decorator)")
				.build();

		final TypeSpec type = TypeSpec.classBuilder(CLASS_NAME)
				.addAnnotations(List.of(additionalClassAnnotations))
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.superclass(ParameterizedTypeName.get(ClassName.get(AbstractPowerNodeService.class), PowerNodeClassGenerator.CLASS_NAME))
				.addMethod(constructor)
				.build();

		return JavaFile.builder(CLASS_NAME.packageName(), type)
				.skipJavaLangImports(true)
				.indent("\t")
				.build();
	}
}
