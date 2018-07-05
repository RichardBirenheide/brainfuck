package org.birenheide.bf.debug.ui;

import org.birenheide.bf.ed.ActionContributor;
import org.birenheide.bf.ed.BfEditor;
import org.eclipse.debug.ui.actions.ToggleBreakpointAction;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

public class BfEditorActionContributor implements ActionContributor {

	@Override
	public void addActions(BfEditor editor) {
		editor.setAction(
				ITextEditorActionConstants.RULER_DOUBLE_CLICK, 
				new ToggleBreakpointAction(editor, null, editor.revealVerticalRuler()));
	}
}
