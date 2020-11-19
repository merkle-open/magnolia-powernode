package com.namics.oss.magnolia.powernode;

import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class PowerNodeModule implements ModuleLifecycle {

	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	public void start(ModuleLifecycleContext moduleLifecycleContext) {
		LOG.debug("Starting PowerNode Module");
	}

	@Override
	public void stop(ModuleLifecycleContext moduleLifecycleContext) {
		LOG.debug("Stopping PowerNode Module");
	}
}
