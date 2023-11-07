package com.merkle.oss.magnolia.powernode.generator;

import com.merkle.oss.magnolia.powernode.AbstractPowerNodeDecorator;
import com.merkle.oss.magnolia.powernode.NodeService;
import com.squareup.javapoet.*;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.jcr.Node;
import javax.lang.model.element.Modifier;
import java.util.List;

public class PowerNodeDecoratorClassGenerator implements ClassGenerator {
	public static final ClassName CLASS_NAME = ClassName.get(AbstractPowerNodeDecorator.class.getPackageName(), "PowerNodeDecorator");

	@Override
	public JavaFile generate(final AnnotationSpec... additionalClassAnnotations) {
		final MethodSpec constructor = MethodSpec.constructorBuilder()
				.addAnnotation(Inject.class)
				.addModifiers(Modifier.PUBLIC)
				.addParameter(NodeService.class, "nodeService", Modifier.FINAL)
				.addStatement("this.nodeService = nodeService")
				.build();

		final MethodSpec wrapNode = MethodSpec.methodBuilder("wrapNodeInternal")
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC)
				.addParameter(ParameterSpec.builder(Node.class, "node", Modifier.FINAL).build())
				.returns(PowerNodeClassGenerator.CLASS_NAME)
				.addStatement("return new $T(nodeService, node, this)", PowerNodeClassGenerator.CLASS_NAME)
				.build();

		final TypeSpec type = TypeSpec.classBuilder(CLASS_NAME)
				.addAnnotations(List.of(additionalClassAnnotations))
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.superclass(ParameterizedTypeName.get(ClassName.get(AbstractPowerNodeDecorator.class), PowerNodeClassGenerator.CLASS_NAME))
				.addField(NodeService.class, "nodeService", Modifier.PRIVATE, Modifier.FINAL)
				.addMethod(constructor)
				.addMethod(wrapNode)
				.build();

		return JavaFile.builder(CLASS_NAME.packageName(), type)
				.skipJavaLangImports(true)
				.indent("\t")
				.build();
	}
}
