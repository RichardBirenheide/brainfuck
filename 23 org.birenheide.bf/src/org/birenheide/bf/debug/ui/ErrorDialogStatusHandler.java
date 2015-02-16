package org.birenheide.bf.debug.ui;

import static org.birenheide.bf.BfActivator.BUNDLE_SYMBOLIC_NAME;

import org.birenheide.bf.BfActivator;
import org.birenheide.bf.BfPreferenceInitializer;
import org.birenheide.bf.debug.core.BfLaunchConfigurationDelegate;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.widgets.Display;

public class ErrorDialogStatusHandler implements IStatusHandler {

	@Override
	public Object handleStatus(IStatus status, Object source)
			throws CoreException {
		if (status.getCode() == BfLaunchConfigurationDelegate.FILE_ERROR_STATUS_CODE) {
			if (!(source instanceof IFile)) {
				throw new CoreException(new Status(IStatus.ERROR, BUNDLE_SYMBOLIC_NAME, "Source is no file"));
			}
			return this.handleFileError((IFile) source);
		}
		
		return null;
	}
	
	private Boolean handleFileError(IFile file) {
		String launchAlways = BfActivator.getDefault().getPreferenceStore().getString(BfPreferenceInitializer.CONTINUE_LAUNCH_WITH_FILE_ERRORS);
		if (MessageDialogWithToggle.ALWAYS.equals(launchAlways)) {
			return true;
		}
		String message = "Errors in File: " + file.getName() + "\n\nProceed with launch?";
		
		MessageDialogWithToggle dialog = new MessageDialogWithToggle (
				Display.getCurrent().getActiveShell(), 
				"Errors in File", 
				null, 
				message, 
				MessageDialog.QUESTION, new String[]{IDialogConstants.PROCEED_LABEL, IDialogConstants.CANCEL_LABEL}, 
				0,
				"Always launch with file errors",
				false);
		
		int result = dialog.open();
		if (result == IDialogConstants.PROCEED_ID) {
			if (dialog.getToggleState()) {
				BfActivator.getDefault().getPreferenceStore().setValue(BfPreferenceInitializer.CONTINUE_LAUNCH_WITH_FILE_ERRORS, MessageDialogWithToggle.ALWAYS);
			}
			return true;
		}
		else {
			return false;
		}
	}
}
