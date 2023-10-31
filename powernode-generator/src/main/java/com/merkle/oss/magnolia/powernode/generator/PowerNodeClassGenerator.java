package com.merkle.oss.magnolia.powernode.generator;

import com.merkle.oss.magnolia.powernode.AbstractPowerNode;
import com.merkle.oss.magnolia.powernode.NodeService;
import com.merkle.oss.magnolia.powernode.PowerNodeIterator;
import com.squareup.javapoet.*;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.lang.model.element.Modifier;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PowerNodeClassGenerator implements ClassGenerator {
	public static final ClassName CLASS_NAME = ClassName.get(AbstractPowerNode.class.getPackageName(), "PowerNode");
	private final ParameterizedTypeName ITERATOR_CLASS_NAME = ParameterizedTypeName.get(ClassName.get(PowerNodeIterator.class), CLASS_NAME);
	private static final Set<String> IGNORED_METHOD_NAMES = Set.of(
			"getWrappedNode",
			"deepUnwrap"
	);

	@Override
	public JavaFile generate(final AnnotationSpec... additionalClassAnnotations) {
		final MethodSpec constructor = MethodSpec.constructorBuilder()
				.addModifiers(Modifier.PUBLIC)
				.addParameter(NodeService.class, "nodeService", Modifier.FINAL)
				.addParameter(Node.class, "node", Modifier.FINAL)
				.addParameter(PowerNodeDecoratorClassGenerator.CLASS_NAME, "decorator", Modifier.FINAL)
				.addStatement("super(nodeService, node, decorator)")
				.build();

		final TypeSpec type = TypeSpec.classBuilder(CLASS_NAME)
				.addAnnotations(List.of(additionalClassAnnotations))
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.superclass(ParameterizedTypeName.get(ClassName.get(AbstractPowerNode.class), CLASS_NAME))
				.addMethod(constructor)
				.addMethods(getMethods())
				.build();

		return JavaFile.builder(CLASS_NAME.packageName(), type)
				.skipJavaLangImports(true)
				.indent("\t")
				.build();
	}

	private List<MethodSpec> getMethods() {
		return getMethodsToOverride()
				.map(this::getMethod)
				.collect(Collectors.toList());
	}

	private MethodSpec getMethod(final Method method) {
		final List<ParameterSpec> parameters = convert(method.getParameters());
		final TypeName returnType = mapReturnTypeNodeToPowerNode(method.getReturnType());

		return MethodSpec
				.methodBuilder(method.getName())
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(Override.class)
				.addParameters(parameters)
				.addExceptions(toTypeName(removeRepositoryExceptions(method.getExceptionTypes())))
				.returns(returnType)
				.addStatement(getStatement(method.getName(), parameters, returnType))
				.build();
	}

	private CodeBlock getStatement(final String methodName, final List<ParameterSpec> parameters, final TypeName returnType) {
		final String commaSeparatedParameter = parameters.stream().map(p -> p.name).collect(Collectors.joining(", "));
		if(TypeName.VOID.equals(returnType)) {
			return CodeBlock.of("run(wrappedNode -> wrappedNode.$L($L))", methodName, commaSeparatedParameter);
		}
		final CodeBlock getOrThrowDelegate = CodeBlock.of("getOrThrow(wrappedNode -> wrappedNode.$L($L))", methodName, commaSeparatedParameter);
		if(CLASS_NAME.equals(returnType)) {
			return CodeBlock.of("return getContentDecorator().wrapNode($L)", getOrThrowDelegate.toString());
		}
		if(ITERATOR_CLASS_NAME.equals(returnType)) {
			return CodeBlock.of("return getContentDecorator().wrapNodeIterator($L)", getOrThrowDelegate.toString());
		}
		return CodeBlock.of("return $L", getOrThrowDelegate.toString());
	}

	private List<ParameterSpec> convert(final Parameter[] parameters) {
		return Arrays.stream(parameters)
				.map(this::convert)
				.collect(Collectors.toList());
	}

	private ParameterSpec convert(final Parameter parameter) {
		return ParameterSpec.builder(parameter.getType(), parameter.getName(), Modifier.FINAL).build();
	}

	private List<TypeName> toTypeName(final Class<?>[] types) {
		return Arrays.stream(types)
				.map(TypeName::get)
				.collect(Collectors.toList());
	}

	private Stream<Method> getMethodsToOverride() {
		return Stream
				.concat(
						getMethodsThrowingRepositoryException(),
						getMethodsReturningNode()
				)
				.filter(method -> java.lang.reflect.Modifier.isPublic(method.getModifiers()))
				.filter(method -> !IGNORED_METHOD_NAMES.contains(method.getName()))
				.distinct();
	}

	private Stream<Method> getMethodsReturningNode() {
		return Arrays
				.stream(AbstractPowerNode.class.getSuperclass().getMethods())
				.filter(method -> Node.class.equals(method.getReturnType()));
	}

	private TypeName mapReturnTypeNodeToPowerNode(final Class<?> returnType) {
		if(Node.class.equals(returnType)) {
			return CLASS_NAME;
		}
		if(NodeIterator.class.equals(returnType)) {
			return ITERATOR_CLASS_NAME;
		}
		return TypeName.get(returnType);
	}

	private Stream<Method> getMethodsThrowingRepositoryException() {
		return Arrays
				.stream(AbstractPowerNode.class.getSuperclass().getMethods())
				.filter(method -> Set.of(method.getExceptionTypes()).contains(RepositoryException.class));
	}

	private Class<?>[] removeRepositoryExceptions(final Class<?>[] exceptionTypes) {
		return Arrays
				.stream(exceptionTypes)
				.filter(Predicate.not(RepositoryException.class::isAssignableFrom))
				.toArray(Class[]::new);
	}
}
