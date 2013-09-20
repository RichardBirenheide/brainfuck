package org.birenheide.bf.debug.ui;

import org.birenheide.bf.ed.BrainfuckEditor;
import org.eclipse.core.resources.IFile;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.ui.IDebugEditorPresentation;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.part.FileEditorInput;

public class BfDebugModelPresentation implements IDebugModelPresentation, IDebugEditorPresentation {

	@Override
	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public IEditorInput getEditorInput(Object element) {
		if (element instanceof IFile) {
			return new FileEditorInput((IFile) element);
		}
		return null;
	}

	@Override
	public String getEditorId(IEditorInput input, Object element) {
		if (element instanceof IFile) {
			if (((IFile) element).getFileExtension().equals("bf")) {
				return BrainfuckEditor.EDITOR_ID;
			}
		}
		return EditorsUI.DEFAULT_TEXT_EDITOR_ID;
	}

	@Override
	public void setAttribute(String attribute, Object value) {
		// TODO Auto-generated method stub

	}

	@Override
	public Image getImage(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getText(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void computeDetail(IValue value, IValueDetailListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean addAnnotations(IEditorPart editorPart, IStackFrame frame) {
		/*
		 * The color of the default annotation can be set with 
		 * "Window->Preferences->General->Editors->Text Editors->Annotations" 
		 * at Debug Call Stack.
		 */
		if (editorPart instanceof BrainfuckEditor) {
			BrainfuckEditor editor = (BrainfuckEditor) editorPart;
		}
		return false;
	}

	@Override
	public void removeAnnotations(IEditorPart editorPart, IThread thread) {
		// TODO Auto-generated method stub
		
	}



}
