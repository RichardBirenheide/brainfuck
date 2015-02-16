package org.birenheide.bf.debug.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourcePathComputerDelegate;
import org.eclipse.debug.core.sourcelookup.containers.ProjectSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.WorkspaceSourceContainer;

public class BfSourcePathComputer implements ISourcePathComputerDelegate {
	
	public static final String SOURCE_PATH_COMPUTER_ID = "org.birenheide.bf.sourcePathComputer";

	@Override
	public ISourceContainer[] computeSourceContainers(ILaunchConfiguration configuration, IProgressMonitor monitor)
			throws CoreException {
		String projectName = configuration.getAttribute(BfLaunchConfigurationDelegate.PROJECT_ATTR, "");
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if (project != null) {
			return new ISourceContainer[]{new ProjectSourceContainer(project, false)};
		}
		else {
			return new ISourceContainer[]{new WorkspaceSourceContainer()};
		}
	}

}
