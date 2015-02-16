package org.birenheide.bf.ui;

import org.birenheide.bf.ed.BfEditor;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.editors.text.EditorsUI;

public class BfEditorPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {
	
	private static final String DUMMY = "dummy";
	private static final String LINK_TEXT = "Brainfuck editor preferences. "
			+ "See <a href=\"org.eclipse.ui.preferencePages.GeneralTextEditor\">'Text Editors'</a> "
			+ "for general text editor preferences and " 
			+ "<a href=\"org.eclipse.ui.editors.preferencePages.Annotations\">'Annotations'</a> " 
			+ "to have the exact "
			+ "<a href=\"org.eclipse.ui.editors.preferencePages.Annotations#Brainfuck Breakpoints\">breakpoint</a> "
			+ "point location and "
			+ "<a href=\"org.eclipse.ui.editors.preferencePages.Annotations#Brainfuck Instruction Pointer\">instruction</a> "
			+ "pointer location being displayed.";
	private static final String TITLE = "Brainfuck Editor"; 
	public static final String ID = "org.birenheide.bf.Editor";

	public BfEditorPreferencePage() {
		this(TITLE, FieldEditorPreferencePage.GRID);
	}

	public BfEditorPreferencePage(int style) {
		super(style);
	}

	public BfEditorPreferencePage(String title, int style) {
		super(title, style);
	}

	public BfEditorPreferencePage(String title, ImageDescriptor image, int style) {
		super(title, image, style);
	}

	@Override
	public void init(IWorkbench workbench) {
		this.setMessage(TITLE);
	}

	@Override
	protected void createFieldEditors() {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), HelpContext.PREFERENCES_EDITOR_ID);
		this.addField(new LinkFieldEditor(getFieldEditorParent()));
		this.addField(new EmptyFieldEditor(getFieldEditorParent()));
		this.addField(new BooleanFieldEditor(BfEditor.EDITOR_MATCHING_BRACKETS_PREF, "Highlight Matching Brackets", BooleanFieldEditor.SEPARATE_LABEL, getFieldEditorParent()));
		this.addField(new ColorFieldEditor(BfEditor.EDITOR_MATCHING_BRACKETS_COLOR_PREF, "Matching Brackets Color", getFieldEditorParent()));
		this.addField(new BooleanFieldEditor(BfEditor.EDITOR_CLOSE_BRACKET, "Close Brackets automatically", BooleanFieldEditor.SEPARATE_LABEL, getFieldEditorParent()));
		this.addField(new EmptyFieldEditor(getFieldEditorParent()));
		
		this.addField(new ColorFieldEditor(BfEditor.EDITOR_KEY_CHAR_COLOR_PREF, "Key Character Color", getFieldEditorParent()));
		this.addField(new ColorFieldEditor(BfEditor.EDITOR_OTHER_CHAR_COLOR_PREF, "Other Character Color", getFieldEditorParent()));
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return EditorsUI.getPreferenceStore();
	}
	
	private abstract static class NoPreferenceFieldEditor extends FieldEditor {
		
		NoPreferenceFieldEditor() {
			init(DUMMY, "");
		}
		
		@Override
		protected void doLoad() {
		}

		@Override
		protected void doLoadDefault() {
		}

		@Override
		protected void doStore() {
		}
		
		public void store() {
			//Do nothing
		}
	}
	
	private static class EmptyFieldEditor extends NoPreferenceFieldEditor {
		
		Label lbl = null;

		EmptyFieldEditor(Composite parent) {
			createControl(parent);
		}

		@Override
		protected void adjustForNumColumns(int numColumns) {
			((GridData) lbl.getLayoutData()).horizontalSpan = numColumns;
		}

		@Override
		protected void doFillIntoGrid(Composite parent, int numColumns) {
			lbl = new Label(parent, SWT.NONE);
			lbl.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).span(2, 1).create());
		}

		@Override
		public int getNumberOfControls() {
			return 1;
		}
	}
	
	private static class LinkFieldEditor extends NoPreferenceFieldEditor {

		private Link linkToOtherEditorPreferences = null;
		
		LinkFieldEditor(Composite parent) {
			createControl(parent);
		}
		
		@Override
		protected void adjustForNumColumns(int numColumns) {
			((GridData) linkToOtherEditorPreferences.getLayoutData()).horizontalSpan = numColumns;
			
		}

		@Override
		protected void doFillIntoGrid(Composite parent, int numColumns) {
			this.linkToOtherEditorPreferences = new Link(parent, SWT.NONE);
			this.linkToOtherEditorPreferences.setLayoutData(GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, false).span(numColumns, 1).hint(150, SWT.DEFAULT).create());
			this.linkToOtherEditorPreferences.setText(LINK_TEXT);
			this.linkToOtherEditorPreferences.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					String linkText = e.text;
					String additionalInfo = null;
					if (linkText.contains("#")) {
						String[] parts = linkText.split("#");
						linkText = parts[0];
						additionalInfo = parts[1];
					}
					PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(null, linkText, null, additionalInfo);
					dialog.open();
				}
				
			});
			
		}

		@Override
		public int getNumberOfControls() {
			return 1;
		}
		
	}

}
