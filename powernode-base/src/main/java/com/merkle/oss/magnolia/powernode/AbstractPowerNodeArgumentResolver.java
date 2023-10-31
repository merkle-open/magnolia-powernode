package com.merkle.oss.magnolia.powernode;

import com.merkle.oss.magnolia.powernode.configuration.BasePowerNodeConfiguration;
import info.magnolia.rendering.context.RenderingContext;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.inject.Provider;

/**
 * Resolves arguments with type {@link AbstractPowerNode}.
 * <p>
 * - Import {@link BasePowerNodeConfiguration} in BlossomServletConfiguration
 * - Register PowerNodeArgumentResolver Bean as custom Resolver in the
 * handlerAdapter Bean in BlossomServletConfiguration
 */
public class AbstractPowerNodeArgumentResolver<N extends AbstractPowerNode<N>> implements HandlerMethodArgumentResolver {
	private final Provider<RenderingContext> renderingContextProvider;
	private final AbstractPowerNodeDecorator<N> decorator;
	private final Class<N> powerNodeClass;

	public AbstractPowerNodeArgumentResolver(
			final Provider<RenderingContext> renderingContextProvider,
			final AbstractPowerNodeDecorator<N> decorator,
			final Class<N> powerNodeClass
	) {
		this.renderingContextProvider = renderingContextProvider;
		this.decorator = decorator;
		this.powerNodeClass = powerNodeClass;
	}

	@Override
	public boolean supportsParameter(final MethodParameter methodParameter) {
		return parameterIsPowerNode(methodParameter);
	}

	@Override
	public Object resolveArgument(
			final MethodParameter methodParameter,
			final ModelAndViewContainer modelAndViewContainer,
			final NativeWebRequest nativeWebRequest,
			final WebDataBinderFactory webDataBinderFactory
	) {
		if (parameterIsPowerNode(methodParameter)) {
			final RenderingContext renderingContext = renderingContextProvider.get();
			if(renderingContext.getCurrentContent() != null) {
				return decorator.wrapNode(renderingContext.getCurrentContent());
			}
			return decorator.wrapNode(renderingContext.getMainContent());
		}
		return null;
	}

	private boolean parameterIsPowerNode(final MethodParameter methodParameter) {
		return methodParameter.getParameterType().isAssignableFrom(powerNodeClass);
	}
}
