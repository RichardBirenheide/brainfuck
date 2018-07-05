package org.birenheide.bf.ed;

import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;

public class EditorCloseListener implements IPartListener2 {

	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partClosed(IWorkbenchPartReference partRef) {
		if (partRef.getId().equals(EditorConstants.EDITOR_ID)) {
			IWorkbenchPart part = partRef.getPart(false);
			if (part instanceof BfEditor) {//The editor is dirty: changes will be discarded, revalidate from saved resource
				BfEditor editor = (BfEditor) part;
				if (editor.isDirty()) {
					editor.validateFromResource();
				}
			}
		}
	}

	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partOpened(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partHidden(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partVisible(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) {
	}
}