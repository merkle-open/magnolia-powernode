package com.merkle.oss.magnolia.powernode.configuration;

import com.merkle.oss.magnolia.powernode.NodeService;
import info.magnolia.objectfactory.Components;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Configuration
@ComponentScan(
		basePackages = {
				"com.merkle.oss.magnolia.powernode"
		},
		includeFilters = {
				@ComponentScan.Filter(Service.class)
		}
)
public class BasePowerNodeConfiguration {

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public NodeService nodeService() {
		return Components.getComponent(NodeService.class);
	}
}
