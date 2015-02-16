package org.birenheide.bf.debug.ui;

import org.birenheide.bf.BfActivator;
import org.birenheide.bf.BfPreferenceInitializer;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class DebugPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {


	private Button alwaysLaunchOnError;
	
	public DebugPreferencePage() {
		super();
	}

	public DebugPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	public DebugPreferencePage(String title) {
		super(title);
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite area = new Composite(parent, SWT.NONE);
		area.setLayoutData(GridDataFactory.fillDefaults().create());
		area.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());
		
		Label placeholder = new Label(area, SWT.NONE);
		placeholder.setLayoutData(GridDataFactory.swtDefaults().span(2, 1).create());
		placeholder.setText(" ");
		
		this.alwaysLaunchOnError = new Button(area, SWT.CHECK);
		this.alwaysLaunchOnError.setLayoutData(GridDataFactory.swtDefaults().span(2, 1).create());
		this.alwaysLaunchOnError.setText("Always launch files with errors");
		
		this.initializeValues();
		
		return area;
	}

	@Override
	protected void performDefaults() {
		this.alwaysLaunchOnError.setSelection(this.getPreferenceStore().getDefaultString(BfPreferenceInitializer.CONTINUE_LAUNCH_WITH_FILE_ERRORS).equals(MessageDialogWithToggle.ALWAYS));
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		if (this.alwaysLaunchOnError.getSelection()) {
			this.getPreferenceStore().setValue(BfPreferenceInitializer.CONTINUE_LAUNCH_WITH_FILE_ERRORS, MessageDialogWithToggle.ALWAYS);
		}
		else {
			this.getPreferenceStore().setValue(BfPreferenceInitializer.CONTINUE_LAUNCH_WITH_FILE_ERRORS, MessageDialogWithToggle.PROMPT);
		}
		return super.performOk();
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return BfActivator.getDefault().getPreferenceStore();
	}

	private void initializeValues() {
		this.alwaysLaunchOnError.setSelection(this.getPreferenceStore().getString(BfPreferenceInitializer.CONTINUE_LAUNCH_WITH_FILE_ERRORS).equals(MessageDialogWithToggle.ALWAYS));
	}
	
}
