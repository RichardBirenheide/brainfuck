package org.birenheide.bf;

import java.util.Map;
import java.util.TreeMap;

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
	
	public static final Map<Integer, RGB> FALLBACK_COLORS = new TreeMap<Integer, RGB>();
	static {
		FALLBACK_COLORS.put(SWT.COLOR_DARK_GRAY, new RGB(128, 128, 128));
		FALLBACK_COLORS.put(SWT.COLOR_DARK_MAGENTA, new RGB(128, 0, 128));
		FALLBACK_COLORS.put(SWT.COLOR_DARK_GREEN, new RGB(0, 128, 0));
		FALLBACK_COLORS.put(SWT.COLOR_BLUE, new RGB(0, 0, 255));
	}
	public static final RGB UNKNOWN_COLOR = new RGB(255, 0, 0);
	
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
		if (Display.getCurrent() != null) {
			return Display.getCurrent().getSystemColor(id).getRGB();
		}
		else if (FALLBACK_COLORS.containsKey(id)) {
			return FALLBACK_COLORS.get(id);
		}
		else {
			return UNKNOWN_COLOR;
		}
	}
}
