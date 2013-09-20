package org.birenheide.bf.debug.ui;

import org.birenheide.bf.BfActivator;
import org.birenheide.bf.debug.core.BfProcessFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;

public class BfLaunchShortcut implements ILaunchShortcut {

	@Override
	public void launch(ISelection selection, String mode) {
		if (selection instanceof IStructuredSelection) {
			Object o = ((IStructuredSelection) selection).getFirstElement();
			if (o instanceof IFile) {
				IFile file = (IFile) o;
				this.launch(file, mode);
			}
		}
	}

	@Override
	public void launch(IEditorPart editor, String mode) {
		if (editor.getEditorInput() instanceof IFileEditorInput) {
			IFile file = ((IFileEditorInput) editor.getEditorInput()).getFile();
			this.launch(file, mode);
		}

	}
	
	private void launch(IFile file, String mode) {
		if (!PlatformUI.getWorkbench().saveAllEditors(true)) {
			return;
		}
		ILaunchConfigurationType bfType = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType("org.birenheide.bf.launchConfigurationType");
		try {
			ILaunchConfiguration[] configs = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations(bfType);
			ILaunchConfiguration matchingConfig = null;
			for (ILaunchConfiguration config : configs) {
				String confFile = config.getAttribute(BfMainTab.FILE_ATTR, "");
				String project = config.getAttribute(BfMainTab.PROJECT_ATTR, "");
//				System.out.println(project + "; " + confFile);
				if (file.getProject().getName().equals(project) && file.getProjectRelativePath().toString().equals(confFile)) {
					System.out.println("Config found: " + config.getName());
					matchingConfig = config;
					break;
				}
			}
			if (matchingConfig == null) {
				String name = file.getName();
				if (name.contains(".")) {
					name = name.substring(0, name.lastIndexOf('.'));
				}
				name = DebugPlugin.getDefault().getLaunchManager().generateLaunchConfigurationName(name);
				ILaunchConfigurationWorkingCopy lc = bfType.newInstance(null, name);
				lc.setAttribute(BfMainTab.FILE_ATTR, file.getProjectRelativePath().toString());
				lc.setAttribute(BfMainTab.PROJECT_ATTR, file.getProject().getName());
				lc.setAttribute(DebugPlugin.ATTR_PROCESS_FACTORY_ID, BfProcessFactory.FACTORY_ID);
				lc.doSave();
				matchingConfig = lc;
			}
			matchingConfig.launch(mode, null, false, true);
		} 
		catch (CoreException e) {
			BfActivator.getDefault().logError("Launch failed", e);
		}
		
	}

}
