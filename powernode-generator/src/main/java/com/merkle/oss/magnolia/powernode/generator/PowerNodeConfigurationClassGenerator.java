package com.merkle.oss.magnolia.powernode.generator;

import com.merkle.oss.magnolia.powernode.configuration.BasePowerNodeConfiguration;
import com.squareup.javapoet.*;
import info.magnolia.objectfactory.Components;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PowerNodeConfigurationClassGenerator implements ClassGenerator {
	public static final ClassName CLASS_NAME = ClassName.get(BasePowerNodeConfiguration.class.getPackageName(), "PowerNodeConfiguration");
	private static final Set<ClassName> GUICE_DELEGATE_BEANS = Set.of(
			PowerNodeDecoratorClassGenerator.CLASS_NAME,
			PowerNodeArgumentResolverClassGenerator.CLASS_NAME,
			PowerNodeServiceClassGenerator.CLASS_NAME
	);

	@Override
	public JavaFile generate(final AnnotationSpec... additionalClassAnnotations) {
		final TypeSpec type = TypeSpec.classBuilder(CLASS_NAME)
				.addAnnotation(Configuration.class)
				.addAnnotation(AnnotationSpec.builder(Import.class).addMember("value", "{$T.class}", BasePowerNodeConfiguration.class).build())
				.addAnnotations(List.of(additionalClassAnnotations))
				.addModifiers(Modifier.PUBLIC)
				.addMethods(GUICE_DELEGATE_BEANS.stream().map(this::guiceBean).collect(Collectors.toList()))
				.build();
		return JavaFile.builder(CLASS_NAME.packageName(), type)
				.skipJavaLangImports(true)
				.indent("\t")
				.build();
	}

	private MethodSpec guiceBean(final ClassName className) {
		return MethodSpec.methodBuilder(className.simpleName()+"_binding")
				.addAnnotation(Bean.class)
				.addAnnotation(AnnotationSpec.builder(Scope.class).addMember("value", "$S", ConfigurableBeanFactory.SCOPE_PROTOTYPE).build())
				.addModifiers(Modifier.PUBLIC)
				.returns(className)
				.addStatement("return $T.getComponent($T.class)", Components.class, className)
				.build();
	}
}
