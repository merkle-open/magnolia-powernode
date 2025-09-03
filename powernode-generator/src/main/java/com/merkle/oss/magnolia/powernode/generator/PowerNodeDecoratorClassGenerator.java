package com.merkle.oss.magnolia.powernode.generator;

import info.magnolia.jcr.util.NodeUtil;

import java.util.List;

import jakarta.inject.Inject;
import javax.jcr.Node;
import javax.lang.model.element.Modifier;

import com.merkle.oss.magnolia.powernode.AbstractPowerNodeDecorator;
import com.merkle.oss.magnolia.powernode.NodeService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

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

		final MethodSpec unwrapNode = MethodSpec.methodBuilder("unwrapNodeInternal")
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC)
				.addParameter(ParameterSpec.builder(Node.class, "node", Modifier.FINAL).build())
				.returns(ClassName.get(Node.class))
				.addStatement("return deepUnwrap(node, PowerNode.class)", PowerNodeClassGenerator.CLASS_NAME)
				.build();

		final TypeSpec type = TypeSpec.classBuilder(CLASS_NAME)
				.addAnnotations(List.of(additionalClassAnnotations))
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.superclass(ParameterizedTypeName.get(ClassName.get(AbstractPowerNodeDecorator.class), PowerNodeClassGenerator.CLASS_NAME))
				.addField(NodeService.class, "nodeService", Modifier.PRIVATE, Modifier.FINAL)
				.addMethod(constructor)
				.addMethod(wrapNode)
				.addMethod(unwrapNode)
				.build();

		return JavaFile.builder(CLASS_NAME.packageName(), type)
				.addStaticImport(ClassName.get(NodeUtil.class), "deepUnwrap")
				.skipJavaLangImports(true)
				.indent("\t")
				.build();
	}
}
