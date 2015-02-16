package org.birenheide.bf.ui.wizard;

import java.io.IOException;
import java.io.InputStream;

import org.birenheide.bf.BfActivator;
import org.birenheide.bf.ed.BfEditor;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

public class NewBfFileCreationWizardPage extends WizardNewFileCreationPage {
	
	private static final String TEMPLATE_PATH = "templates/newBrainfuckFileContent.bf";
	private static final String PAGE_NAME = "Create Brainfuck File";

	public NewBfFileCreationWizardPage(IStructuredSelection selection) {
		super(PAGE_NAME, selection);
		this.setFileExtension(BfEditor.BF_FILE_EXTENSION);
		this.setTitle(PAGE_NAME);
//		this.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor("IMG_WIZBAN_NEW_WIZ"));
		this.setImageDescriptor(JFaceResources.getImageRegistry().getDescriptor(TitleAreaDialog.DLG_IMG_TITLE_BANNER));
//		this.setImageDescriptor(null);
	}

	@Override
	protected InputStream getInitialContents() {
		try {
			return FileLocator.openStream(BfActivator.getDefault().getBundle(), new Path(TEMPLATE_PATH), false);
		} 
		catch (IOException ex) {
			BfActivator.getDefault().logError("Template for new file could not be read", ex);
		}
		return null;
	}

	@Override
	protected String getNewFileLabel() {
		return super.getNewFileLabel();
	}
}
