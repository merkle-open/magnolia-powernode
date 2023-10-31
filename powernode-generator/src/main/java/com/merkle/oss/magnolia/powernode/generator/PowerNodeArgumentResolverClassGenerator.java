package com.merkle.oss.magnolia.powernode.generator;

import com.merkle.oss.magnolia.powernode.AbstractPowerNodeArgumentResolver;
import com.squareup.javapoet.*;
import info.magnolia.rendering.context.RenderingContext;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.lang.model.element.Modifier;
import java.util.List;

public class PowerNodeArgumentResolverClassGenerator implements ClassGenerator {
	public static final ClassName CLASS_NAME = ClassName.get(AbstractPowerNodeArgumentResolver.class.getPackageName(), "PowerNodeArgumentResolver");

	@Override
	public JavaFile generate(final AnnotationSpec... additionalClassAnnotations) {

		final MethodSpec constructor = MethodSpec.constructorBuilder()
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(Inject.class)
				.addParameter(ParameterizedTypeName.get(ClassName.get(Provider.class), ClassName.get(RenderingContext.class)), "renderingContextProvider", Modifier.FINAL)
				.addParameter(PowerNodeDecoratorClassGenerator.CLASS_NAME, "decorator", Modifier.FINAL)
				.addStatement("super(renderingContextProvider, decorator, $T.class)", PowerNodeClassGenerator.CLASS_NAME)
				.build();

		final TypeSpec type = TypeSpec.classBuilder(CLASS_NAME)
				.addAnnotations(List.of(additionalClassAnnotations))
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.superclass(ParameterizedTypeName.get(ClassName.get(AbstractPowerNodeArgumentResolver.class), PowerNodeClassGenerator.CLASS_NAME))
				.addMethod(constructor)
				.build();

		return JavaFile.builder(CLASS_NAME.packageName(), type)
				.skipJavaLangImports(true)
				.indent("\t")
				.build();
	}
}
