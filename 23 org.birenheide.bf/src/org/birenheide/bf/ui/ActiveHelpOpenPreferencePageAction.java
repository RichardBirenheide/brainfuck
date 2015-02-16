package org.birenheide.bf.ui;

import org.eclipse.help.ILiveHelpAction;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class ActiveHelpOpenPreferencePageAction implements ILiveHelpAction {
	
	private String link = null;
	private String additionalData = null;

	@Override
	public void run() {
		if (link == null) {
			return;
		}
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (window != null) {
					 Shell shell = window.getShell();
                     shell.setMinimized(false);
                     shell.forceActive();
                     PreferenceDialog prefsDialog = PreferencesUtil.createPreferenceDialogOn(shell, link, null, additionalData);
                   	 prefsDialog.open();
				}
			}
		});
	}

	@Override
	public void setInitializationString(String data) {
		if (data == null) {
			return;
		}
		
		if (data.contains("#")) {
			String[] parts = data.split("#");
			this.link = parts[0];
			this.additionalData = parts[1];
		}
		else {
			this.link = data;
		}
		
	}



}
