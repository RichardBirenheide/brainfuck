package org.birenheide.bf;

import org.birenheide.bf.ed.BfEditor;
import org.birenheide.bf.ed.template.BfTemplateType;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.editors.text.EditorsUI;

public class BfPreferenceInitializer extends AbstractPreferenceInitializer {

	public static final String TEMPLATE_KEY = BfTemplateType.TYPE_ID;
	public static final String CONTINUE_LAUNCH_WITH_FILE_ERRORS = "continueLaunchWithFileErrors";
	
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = BfActivator.getDefault().getPreferenceStore();
		store.setDefault(TEMPLATE_KEY, "");
		store.setDefault(CONTINUE_LAUNCH_WITH_FILE_ERRORS, MessageDialogWithToggle.PROMPT);
		
		IPreferenceStore editorStore = EditorsUI.getPreferenceStore();
		PreferenceConverter.setDefault(editorStore, BfEditor.EDITOR_MATCHING_BRACKETS_COLOR_PREF, getSystemColorRGB(SWT.COLOR_DARK_GRAY));
		PreferenceConverter.setDefault(editorStore, BfEditor.EDITOR_KEY_CHAR_COLOR_PREF, getSystemColorRGB(SWT.COLOR_DARK_MAGENTA));
		PreferenceConverter.setDefault(editorStore, BfEditor.EDITOR_OTHER_CHAR_COLOR_PREF, getSystemColorRGB(SWT.COLOR_DARK_GRAY));
		PreferenceConverter.setDefault(editorStore, BfEditor.EDITOR_COMMENT_CHAR_COLOR_PREF, getSystemColorRGB(SWT.COLOR_DARK_GREEN));
		PreferenceConverter.setDefault(editorStore, BfEditor.EDITOR_TEMPLATE_PARAMS_COLOR_PREF, getSystemColorRGB(SWT.COLOR_BLUE));
		editorStore.setDefault(BfEditor.EDITOR_MATCHING_BRACKETS_PREF, true);
		editorStore.setDefault(BfEditor.EDITOR_CLOSE_BRACKET, true);
	}
	
	private RGB getSystemColorRGB(int id) {
		return Display.getDefault().getSystemColor(id).getRGB();
	}
	
	
}
