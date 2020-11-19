package com.namics.oss.magnolia.powernode.setup;

import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.Task;
import info.magnolia.module.model.Version;

import java.util.LinkedList;
import java.util.List;


public class PowerNodeModuleVersionHandler extends DefaultModuleVersionHandler {

	private static final String SNAPSHOT_CLASSIFIER = "SNAPSHOT";

	private final List<Task> tasks;

	public PowerNodeModuleVersionHandler() {
		// add general tasks here
		this.tasks = List.of();
	}

	@Override
	protected final List<Task> getExtraInstallTasks(InstallContext installContext) { //when module node does not exist
		List<Task> installTasks = new LinkedList<>();
		installTasks.addAll(super.getExtraInstallTasks(installContext));
		installTasks.addAll(tasks);
		return installTasks;
	}

	@Override
	protected final List<Task> getDefaultUpdateTasks(Version forVersion) { //on every module update
		List<Task> installTasks = new LinkedList<>();
		installTasks.addAll(super.getDefaultUpdateTasks(forVersion));
		installTasks.addAll(tasks);
		return installTasks;
	}

	@Override
	protected final List<Task> getStartupTasks(InstallContext installContext) {
		List<Task> installTasks = new LinkedList<>();
		Version forVersion = getVersionFromInstallContext(installContext);
		if (isSnapshot(forVersion)) {
			installTasks.addAll(tasks);
		}
		return installTasks;
	}

	private static boolean isSnapshot(Version version) {
		return SNAPSHOT_CLASSIFIER.equalsIgnoreCase(version.getClassifier());
	}

	private static Version getVersionFromInstallContext(InstallContext installContext) {
		return installContext.getCurrentModuleDefinition().getVersion();
	}
}
