package org.birenheide.bf.ui;

import org.birenheide.bf.BfActivator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.templates.TemplatePreferencePage;

public class BfTemplatePreferencePage extends TemplatePreferencePage implements
		IWorkbenchPreferencePage {

	private static final String TITLE = "Brainfuck Templates";
	public static final String ID = "org.birenheide.bf.Templates";
	
	public BfTemplatePreferencePage() {
		this.setContextTypeRegistry(BfActivator.getDefault().getTemplateContextTypeRegistry());
		this.setTemplateStore(BfActivator.getDefault().getTemplateStore());
		this.setMessage(TITLE);
	}

	@Override
	protected Control createContents(Composite ancestor) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), HelpContext.PREFERENCES_TEMPLATES_ID);
		return super.createContents(ancestor);
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return BfActivator.getDefault().getPreferenceStore();
	}

	@Override
	protected boolean isShowFormatterSetting() {
		return false;
	}
}
