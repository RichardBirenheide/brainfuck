package org.birenheide.bf.ed.template;

import org.birenheide.bf.BfActivator;
import org.birenheide.bf.ui.HelpContext;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.window.Window;
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
		this.setDescription("Brainfuck Templates");
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

	@Override
	protected Template editTemplate(Template template, boolean edit,
			boolean isNameModifiable) {
		EditTemplateDialog dialog= new EditTemplateDialog(getShell(), template, edit, isNameModifiable, getContextTypeRegistry());
		dialog.setTitle("Edit Brainfuck Template");
		if (dialog.open() == Window.OK) {
			return dialog.getTemplate();
		}
		return null;
	}
}
