package org.birenheide.bf.debug.core;

import org.birenheide.bf.debug.DbgActivator;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

	public static final String CONTINUE_LAUNCH_WITH_FILE_ERRORS = "continueLaunchWithFileErrors";
	public static final String ENABLE_HOT_CODE_REPLACEMENT = "enableHotCodeReplacement";

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = DbgActivator.getDefault().getPreferenceStore();
		store.setDefault(CONTINUE_LAUNCH_WITH_FILE_ERRORS, MessageDialogWithToggle.PROMPT);
		store.setDefault(ENABLE_HOT_CODE_REPLACEMENT, true);
	}
}
