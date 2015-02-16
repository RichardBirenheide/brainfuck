package org.birenheide.bf.ui.wizard;

import org.birenheide.bf.BfActivator;
import org.birenheide.bf.ed.BfEditor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;

public class NewFileWizard extends Wizard implements INewWizard {
	
	private static final String WIZARD_TITLE = "New Brainfuck File";
	
	private IStructuredSelection selection = null;
	private IWorkbench workbench = null;
	private NewBfFileCreationWizardPage filePage = null;

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
		this.workbench = workbench;
		this.setWindowTitle(WIZARD_TITLE);
	}

	@Override
	public void addPages() {
		this.filePage = new NewBfFileCreationWizardPage(selection);
		this.addPage(filePage);
	}

	@Override
	public boolean performFinish() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IFile[] createdFile = new IFile[1];
		IWorkspaceRunnable fileCreator = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				createdFile[0] = filePage.createNewFile();
			}
		};
		try {
			workspace.run(fileCreator, null, IWorkspace.AVOID_UPDATE, null);
		}
		catch (CoreException ex) {
			BfActivator.getDefault().logError("New File creation failed", ex);
			return true;
		}
		if (createdFile[0] == null || !createdFile[0].exists()) {
			return true;
		}
		try {
			workbench.getActiveWorkbenchWindow().getActivePage().openEditor(new FileEditorInput(createdFile[0]), BfEditor.EDITOR_ID);
		} 
		catch (PartInitException ex) {
			BfActivator.getDefault().logError("Editor for new file could not be opened: " + createdFile[0], ex);
		}
		return true;
	}

}
