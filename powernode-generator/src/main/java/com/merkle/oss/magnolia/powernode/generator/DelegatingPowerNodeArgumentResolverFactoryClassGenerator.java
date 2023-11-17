package com.merkle.oss.magnolia.powernode.generator;

import com.merkle.oss.magnolia.powernode.DelegatingPowerNodeArgumentResolver.AbstractDelegatingPowerNodeArgumentResolverFactory;
import com.squareup.javapoet.*;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.lang.model.element.Modifier;
import java.util.List;

public class DelegatingPowerNodeArgumentResolverFactoryClassGenerator implements ClassGenerator {
	public static final ClassName CLASS_NAME = ClassName.get(AbstractDelegatingPowerNodeArgumentResolverFactory.class.getPackageName(), "DelegatingPowerNodeArgumentResolverFactory");

	@Override
	public JavaFile generate(final AnnotationSpec... additionalClassAnnotations) {
		final MethodSpec constructor = MethodSpec.constructorBuilder()
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(Inject.class)
				.addParameter(PowerNodeDecoratorClassGenerator.CLASS_NAME, "decorator", Modifier.FINAL)
				.addStatement("super(decorator, $T.class)", PowerNodeClassGenerator.CLASS_NAME)
				.build();

		final TypeSpec type = TypeSpec.classBuilder(CLASS_NAME)
				.addAnnotations(List.of(additionalClassAnnotations))
				.addAnnotation(Component.class)
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.superclass(ParameterizedTypeName.get(ClassName.get(AbstractDelegatingPowerNodeArgumentResolverFactory.class), PowerNodeClassGenerator.CLASS_NAME))
				.addMethod(constructor)
				.build();

		return JavaFile.builder(CLASS_NAME.packageName(), type)
				.skipJavaLangImports(true)
				.indent("\t")
				.build();
	}
}
