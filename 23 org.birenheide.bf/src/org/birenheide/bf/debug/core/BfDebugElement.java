package org.birenheide.bf.debug.core;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.DebugElement;

public class BfDebugElement extends DebugElement {
	
	public static final String MODEL_IDENTIFIER = "org.birenheide.bf.debugModelPresentation";

	public BfDebugElement(BfDebugTarget target) {
		super(target);
	}

	@Override
	public String getModelIdentifier() {
		return MODEL_IDENTIFIER;
	}

	@Override
	public BfDebugTarget getDebugTarget() {
		return (BfDebugTarget) super.getDebugTarget();
	}

	public IProject getProject() throws CoreException {
		String projectName = this.getLaunch().getLaunchConfiguration().getAttribute(BfLaunchConfigurationDelegate.PROJECT_ATTR, (String)null);
		if (projectName != null) {
			return ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		}
		return null;
	}
	
	public IFile getFile() throws CoreException {
		IProject project = this.getProject();
		if (project != null) {
			String fileName = this.getLaunch().getLaunchConfiguration().getAttribute(BfLaunchConfigurationDelegate.FILE_ATTR, (String)null);
			if (fileName != null) {
				return project.getFile(fileName);
			}
		}
		return null;
	}
}
