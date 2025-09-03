package com.merkle.oss.magnolia.powernode;

import java.util.Optional;

import javax.jcr.Node;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import jakarta.annotation.Nullable;
import jakarta.inject.Inject;

/**
 * Resolves arguments with type {@link PowerNode} by resolving type {@link Node} and wrapping it.
 */
public class DelegatingPowerNodeArgumentResolver implements HandlerMethodArgumentResolver {
	private final PowerNodeDecorator decorator;
	private final HandlerMethodArgumentResolver delegate;

	public DelegatingPowerNodeArgumentResolver(
			final PowerNodeDecorator decorator,
			final HandlerMethodArgumentResolver delegate
	) {
		this.decorator = decorator;
		this.delegate = delegate;
	}

	@Override
	public boolean supportsParameter(final MethodParameter methodParameter) {
		return nodeMethodParam(methodParameter)
				.map(delegate::supportsParameter)
				.orElseGet(() -> delegate.supportsParameter(methodParameter));
	}

	@Override
	public Object resolveArgument(
			final MethodParameter methodParameter,
			final ModelAndViewContainer modelAndViewContainer,
			final NativeWebRequest nativeWebRequest,
			final WebDataBinderFactory webDataBinderFactory
	) throws Exception {
		@Nullable
		final MethodParameter nodeMethodParameter = nodeMethodParam(methodParameter).orElse(null);
		if(nodeMethodParameter != null) {
			return decorator.wrapNode((Node)delegate.resolveArgument(nodeMethodParameter, modelAndViewContainer, nativeWebRequest, webDataBinderFactory));
		}
		return delegate.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest, webDataBinderFactory);
	}

	private Optional<MethodParameter> nodeMethodParam(final MethodParameter methodParameter) {
		if(methodParameter.getParameterType().isAssignableFrom(PowerNode.class)) {
			return Optional.of(
					new MethodParameter(methodParameter) {
						@Override
						public Class<?> getParameterType() {
							return Node.class;
						}
					}
			);
		}
		return Optional.empty();
	}

	@Component
	public static class Factory {
		private final PowerNodeDecorator decorator;

		@Inject
		public Factory(final PowerNodeDecorator decorator) {
			this.decorator = decorator;
		}

		public DelegatingPowerNodeArgumentResolver create(final HandlerMethodArgumentResolver delegate) {
			return new DelegatingPowerNodeArgumentResolver(decorator, delegate);
		}
	}
}
