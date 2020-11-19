package com.namics.oss.magnolia.powernode;

import com.namics.oss.magnolia.powernode.configuration.PowerNodeConfiguration;
import info.magnolia.objectfactory.Components;
import info.magnolia.rendering.context.RenderingContext;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.inject.Inject;
import javax.jcr.Node;

/**
 * Resolves arguments with type {@link PowerNode}.
 * <p>
 * - Import {@link PowerNodeConfiguration} in BlossomServletConfiguration
 * - Register PowerNodeArgumentResolver Bean as custom Resolver in the
 * handlerAdapter Bean in BlossomServletConfiguration
 */
public class PowerNodeArgumentResolver implements HandlerMethodArgumentResolver {

	@Inject
	private PowerNodeService powerNodeService;

	@Override
	public boolean supportsParameter(MethodParameter methodParameter) {
		return parameterIsPowerNode(methodParameter);
	}

	@Override
	public Object resolveArgument(MethodParameter methodParameter,
	                              ModelAndViewContainer modelAndViewContainer,
	                              NativeWebRequest nativeWebRequest,
	                              WebDataBinderFactory webDataBinderFactory) {

		if (parameterIsPowerNode(methodParameter)) {
			RenderingContext renderingContext = Components.getComponent(RenderingContext.class);
			Node currentNode = renderingContext.getCurrentContent();
			if (currentNode == null) {
				currentNode = renderingContext.getMainContent();
			}
			return powerNodeService.convertToPowerNode(currentNode);
		}

		return null;
	}

	private boolean parameterIsPowerNode(MethodParameter methodParameter) {
		return methodParameter.getParameterType().isAssignableFrom(PowerNode.class);
	}
}
