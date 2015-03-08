package org.birenheide.bf;

import org.birenheide.bf.core.BfActivator;
import org.birenheide.bf.ed.EditorConstants;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.editors.text.EditorsUI;

public class BfPreferenceInitializer extends AbstractPreferenceInitializer {

	public static final String TEMPLATE_KEY = "org.birenheide.bf.brainfuck";
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = BfActivator.getDefault().getPreferenceStore();
		store.setDefault(TEMPLATE_KEY, "");
		
		IPreferenceStore editorStore = EditorsUI.getPreferenceStore();
		editorStore.setDefault(EditorConstants.PREF_EDITOR_MATCHING_BRACKETS, true);
		editorStore.setDefault(EditorConstants.PREF_EDITOR_MATCHING_BRACKETS_SHOW_CARET, false);
		editorStore.setDefault(EditorConstants.PREF_EDITOR_MATCHING_BRACKETS_SHOW_ENCLOSING, false);
		PreferenceConverter.setDefault(editorStore, EditorConstants.PREF_EDITOR_MATCHING_BRACKETS_COLOR, getSystemColorRGB(SWT.COLOR_DARK_GRAY));
		
		PreferenceConverter.setDefault(editorStore, EditorConstants.PREF_EDITOR_KEY_CHAR_COLOR, getSystemColorRGB(SWT.COLOR_DARK_MAGENTA));
		PreferenceConverter.setDefault(editorStore, EditorConstants.PREF_EDITOR_OTHER_CHAR_COLOR, getSystemColorRGB(SWT.COLOR_DARK_GRAY));
		PreferenceConverter.setDefault(editorStore, EditorConstants.PREF_EDITOR_COMMENT_CHAR_COLOR, getSystemColorRGB(SWT.COLOR_DARK_GREEN));
		PreferenceConverter.setDefault(editorStore, EditorConstants.PREF_EDITOR_TEMPLATE_PARAMS_COLOR, getSystemColorRGB(SWT.COLOR_BLUE));
		
		editorStore.setDefault(EditorConstants.PREF_EDITOR_CLOSE_BRACKET, true);
	}
	
	private RGB getSystemColorRGB(int id) {
		return Display.getDefault().getSystemColor(id).getRGB();
	}
	
	
}
