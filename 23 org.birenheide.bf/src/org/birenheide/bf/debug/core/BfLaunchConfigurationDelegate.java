package org.birenheide.bf.debug.core;

import org.birenheide.bf.debug.ui.BfMainTab;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.IProcess;

public class BfLaunchConfigurationDelegate implements
		ILaunchConfigurationDelegate {

	@Override
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {

		String fileName = configuration.getAttribute(BfMainTab.FILE_ATTR, "No Filename");
		IProcess process = DebugPlugin.newProcess(launch, null, fileName);
		System.out.println(launch.getLaunchMode());
		launch.addProcess(process);
		if (launch.getLaunchMode().equals(ILaunchManager.DEBUG_MODE)) {
			IDebugTarget target = new BfDebugTarget(launch, fileName, (BfProcess) process);
			launch.addDebugTarget(target);
		}
	}
}
