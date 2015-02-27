package org.birenheide.bf.debug.ui;

import org.birenheide.bf.BfActivator;
import org.birenheide.bf.debug.core.PreferenceInitializer;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class DebugPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {


	private Button alwaysLaunchOnError;
	private Button enableHotCodeReplacement;
	
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
		
		Point newParagraph = new Point(0, this.convertHeightInCharsToPixels(1));
		
		this.alwaysLaunchOnError = new Button(area, SWT.CHECK);
		this.alwaysLaunchOnError.setLayoutData(GridDataFactory.swtDefaults().indent(newParagraph).span(2, 1).create());
		this.alwaysLaunchOnError.setText("Always launch files with errors");
		
		this.enableHotCodeReplacement = new Button(area, SWT.CHECK);
		this.enableHotCodeReplacement.setLayoutData(GridDataFactory.swtDefaults().indent(newParagraph).span(2, 1).create());
		this.enableHotCodeReplacement.setText("Enable hot code replacement");
		
		this.initializeValues();
		
		return area;
	}

	@Override
	protected void performDefaults() {
		this.alwaysLaunchOnError.setSelection(this.getPreferenceStore().getDefaultString(PreferenceInitializer.CONTINUE_LAUNCH_WITH_FILE_ERRORS).equals(MessageDialogWithToggle.ALWAYS));
		this.enableHotCodeReplacement.setSelection(this.getPreferenceStore().getDefaultBoolean(PreferenceInitializer.ENABLE_HOT_CODE_REPLACEMENT));
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		if (this.alwaysLaunchOnError.getSelection()) {
			this.getPreferenceStore().setValue(PreferenceInitializer.CONTINUE_LAUNCH_WITH_FILE_ERRORS, MessageDialogWithToggle.ALWAYS);
		}
		else {
			this.getPreferenceStore().setValue(PreferenceInitializer.CONTINUE_LAUNCH_WITH_FILE_ERRORS, MessageDialogWithToggle.PROMPT);
		}
		this.getPreferenceStore().setValue(PreferenceInitializer.ENABLE_HOT_CODE_REPLACEMENT, this.enableHotCodeReplacement.getSelection());
		return super.performOk();
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return BfActivator.getDefault().getPreferenceStore();
	}

	private void initializeValues() {
		this.alwaysLaunchOnError.setSelection(this.getPreferenceStore().getString(PreferenceInitializer.CONTINUE_LAUNCH_WITH_FILE_ERRORS).equals(MessageDialogWithToggle.ALWAYS));
		this.enableHotCodeReplacement.setSelection(this.getPreferenceStore().getBoolean(PreferenceInitializer.ENABLE_HOT_CODE_REPLACEMENT));
	}
	
}
