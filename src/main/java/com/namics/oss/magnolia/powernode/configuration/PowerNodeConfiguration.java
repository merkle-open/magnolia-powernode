package com.namics.oss.magnolia.powernode.configuration;

import com.namics.oss.magnolia.powernode.PowerNodeArgumentResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

@Configuration
@ComponentScan(
		basePackages = {
				"com.namics.oss.magnolia.powernode"
		},
		includeFilters = {
				@ComponentScan.Filter(Service.class)
		}
)
public class PowerNodeConfiguration {

	@Bean
	public PowerNodeArgumentResolver powerNodeArgumentResolver() {
		return new PowerNodeArgumentResolver();
	}

}
