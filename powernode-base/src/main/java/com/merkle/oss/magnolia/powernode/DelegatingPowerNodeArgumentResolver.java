package com.merkle.oss.magnolia.powernode;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.annotation.Nullable;
import javax.jcr.Node;
import java.util.Optional;

/**
 * Resolves arguments with type {@link AbstractPowerNode} by resolving type {@link Node} and wrapping it.
 */
public class DelegatingPowerNodeArgumentResolver<N extends AbstractPowerNode<N>> implements HandlerMethodArgumentResolver {
	private final AbstractPowerNodeDecorator<N> decorator;
	private final Class<N> powerNodeClass;
	private final HandlerMethodArgumentResolver delegate;

	public DelegatingPowerNodeArgumentResolver(
			final AbstractPowerNodeDecorator<N> decorator,
			final Class<N> powerNodeClass,
			final HandlerMethodArgumentResolver delegate
	) {
		this.decorator = decorator;
		this.powerNodeClass = powerNodeClass;
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
		if(methodParameter.getParameterType().isAssignableFrom(powerNodeClass)) {
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

	public static abstract class AbstractDelegatingPowerNodeArgumentResolverFactory<N extends AbstractPowerNode<N>> {
		private final AbstractPowerNodeDecorator<N> decorator;
		private final Class<N> powerNodeClass;

		public AbstractDelegatingPowerNodeArgumentResolverFactory(
				final AbstractPowerNodeDecorator<N> decorator,
				final Class<N> powerNodeClass
		) {
			this.decorator = decorator;
			this.powerNodeClass = powerNodeClass;
		}

		public DelegatingPowerNodeArgumentResolver<N> create(final HandlerMethodArgumentResolver delegate) {
			return new DelegatingPowerNodeArgumentResolver<>(decorator, powerNodeClass, delegate);
		}
	}
}
