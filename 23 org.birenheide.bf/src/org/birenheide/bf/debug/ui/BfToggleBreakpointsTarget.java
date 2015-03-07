package org.birenheide.bf.debug.ui;

import org.birenheide.bf.BrainfuckInterpreter;
import org.birenheide.bf.debug.core.BfBreakpoint;
import org.birenheide.bf.debug.core.BfDebugTarget;
import org.birenheide.bf.debug.core.BfWatchpoint;
import org.birenheide.bf.ed.BfEditor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;

public class BfToggleBreakpointsTarget implements IToggleBreakpointsTarget {

	@Override
	public void toggleLineBreakpoints(IWorkbenchPart part, ISelection selection)
			throws CoreException {
		//TODO Handle breakpoints at deleted regions at end of document correctly.
		if (part instanceof BfEditor) {
			BfEditor editor = (BfEditor) part;
			ITextSelection sel = (ITextSelection) selection;
			IFile file = ((IFileEditorInput) editor.getEditorInput()).getFile();
			int location = sel.getOffset();
			IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
			int line = -1;
			try {
				line = document.getLineOfOffset(location);
				while (location < document.getLength()) {
					if (document.getLineOfOffset(location) != line) {
						return;
					}
					char c = document.getChar(location);
//					System.out.println(new String(new char[]{c}));
					if (BrainfuckInterpreter.isReservedChar(c)) {
						break;
					}
					location++;
				}
				line = document.getLineOfOffset(location);
			} 
			catch (BadLocationException e) {
				return;
			}
			
			if (location >= 0 && location <= document.getLength()) {
				IBreakpointManager bpm = DebugPlugin.getDefault().getBreakpointManager();
				boolean deleted = false;
				for (IBreakpoint bp : bpm.getBreakpoints(BfDebugTarget.MODEL_IDENTIFIER)) {
					if (bp instanceof BfBreakpoint) {
						BfBreakpoint breakpoint = (BfBreakpoint) bp;
//						System.out.println(breakpoint + ": " + location);
						if (breakpoint.getMarker().getResource().equals(file) && breakpoint.getCharStart() >= location) {
							breakpoint.delete();
							deleted = true;
						}
					}
				}
				if (!deleted) {
					
					BfBreakpoint bp = new BfBreakpoint(file, location, line);
					DebugPlugin.getDefault().getBreakpointManager().addBreakpoint(bp);
				}
			}
		}
	}

	@Override
	public boolean canToggleLineBreakpoints(IWorkbenchPart part,
			ISelection selection) {
		return part instanceof BfEditor;
	}

	@Override
	public void toggleMethodBreakpoints(IWorkbenchPart part,
			ISelection selection) throws CoreException {
	}

	@Override
	public boolean canToggleMethodBreakpoints(IWorkbenchPart part,
			ISelection selection) {
		return false;
	}

	@Override
	public void toggleWatchpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
		if (part instanceof BfEditor) {
			LocationDialog dialog = new LocationDialog(((BfEditor) part).getEditorSite().getShell());
			if (dialog.open() == 0) {
				int location = dialog.getMemoryLocation();
				int value = dialog.getSuspendValue();
				if (location == -1 || value == -1) {
					return;
				}
				byte b = (byte) value;
				BfWatchpoint bw = new BfWatchpoint(location, b, dialog.isAccess(), dialog.isModification());
				DebugPlugin.getDefault().getBreakpointManager().addBreakpoint(bw);
			}
		}
	}

	@Override
	public boolean canToggleWatchpoints(IWorkbenchPart part,
			ISelection selection) {
		return part instanceof BfEditor;
	}
	
	/**
	 * @author Richard Birenheide
	 *
	 */
	private static class LocationDialog extends MessageDialog {
		
		private static final String MESSAGE_TEXT = "Watchpoint Location and Value (0x.. for Hex Value)";
		
		private Text location = null;
		private Text value = null;
		private Button access = null;
		private Button modification = null;
		
		private int inputValue = -1;
		private int inputLocation = -1;
		private boolean isAccess = false;
		private boolean isModification = false;

		LocationDialog(Shell parentShell) {
			super(parentShell, 
				  "Watchpoint", 
				  null, 
				  MESSAGE_TEXT,
				  MessageDialog.NONE, 
				  new String[]{"OK", "Cancel"}, 
				  0);
		}

		@Override
		protected Control createCustomArea(Composite parent) {
			Composite area = new Composite(parent, SWT.NONE);
			area.setLayout(GridLayoutFactory.swtDefaults().numColumns(2).create());
			area.setLayoutData(GridDataFactory.fillDefaults().span(1, 1).grab(true, true).create());
			
			Label locationLabel = new Label(area, SWT.NONE);
			locationLabel.setText("Memory Location:");
			locationLabel.setLayoutData(GridDataFactory.swtDefaults().span(1, 1).create());
			
			location = new Text(area, SWT.BORDER);
			location.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(1, 1).create());
			location.addModifyListener(new LocationValidator());
			
			Label valueLabel = new Label(area, SWT.NONE);
			valueLabel.setText("Suspend Value:");
			valueLabel.setLayoutData(GridDataFactory.swtDefaults().span(1, 1).create());
			
			value = new Text(area, SWT.BORDER);
			value.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(1, 1).create());
			value.addModifyListener(new ValueValidator());
			
			Label accessLabel = new Label(area, SWT.NONE);
			accessLabel.setLayoutData(GridDataFactory.swtDefaults().span(1, 1).create());
			accessLabel.setText("Suspend on Access:");
			
			access = new Button(area, SWT.CHECK);
			access.setLayoutData(GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER).grab(false, false).span(1, 1).create());
			access.setText(" ");
			access.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					isAccess = access.getSelection();
				}
			});
			
			Label modifcationLabel = new Label(area, SWT.NONE);
			modifcationLabel.setLayoutData(GridDataFactory.swtDefaults().span(1, 1).create());
			modifcationLabel.setText("Suspend on Modification:");
			
			modification = new Button(area, SWT.CHECK);
			modification.setLayoutData(GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER).grab(false, false).span(1, 1).create());
			modification.setText(" ");
			modification.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					isModification = modification.getSelection();
				}
			});

			return area;
		}
		
		int getMemoryLocation() {
			return this.inputLocation;
		}
		
		int getSuspendValue() {
			return this.inputValue;
		}
		
		boolean isAccess() {
			return this.isAccess;
		}
		
		boolean isModification() {
			return this.isModification;
		}

		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			super.createButtonsForButtonBar(parent);
			this.setValueValid(false, -1);
			this.setLocationValid(false, -1);
		}

		private void setValid(Text control, boolean isValid) {
			if (isValid) {
				control.setForeground(control.getDisplay().getSystemColor(SWT.COLOR_BLACK));
			}
			else {
				control.setForeground(control.getDisplay().getSystemColor(SWT.COLOR_RED));
			}
			this.getButton(0).setEnabled(this.inputLocation >= 0 && this.inputValue >= 0 );
		}
		
		private void setLocationValid(boolean isValid, int loc) {
			if (isValid) {
				this.inputLocation = loc;
			}
			else {
				this.inputLocation = -1;
			}
			setValid(location, isValid);
		}
		
		private void setValueValid(boolean isValid, int value) {
			if (isValid) {
				this.inputValue = value;
			}
			else {
				this.inputValue = -1;
			}
			setValid(this.value, isValid);
		}
		
		/**
		 * @author Richard Birenheide
		 *
		 */
		private abstract class Validator {
			
			int parseValue(String text) {
				text = text.trim();
				try {
					if (text.startsWith("0x")) {
						return Integer.parseInt(text.substring(2), 16);
					}
					else {
						return Integer.parseInt(text);
					}
				}
				catch (NumberFormatException ex) {
					return -1;
				}
			}
		}
		
		/**
		 * @author Richard Birenheide
		 *
		 */
		private class LocationValidator extends Validator implements ModifyListener {

			@Override
			public void modifyText(ModifyEvent e) {
				int value = this.parseValue(location.getText());
				setLocationValid(value >= 0, value);
			}
		}
		
		/**
		 * @author Richard Birenheide
		 *
		 */
		private class ValueValidator extends Validator implements ModifyListener {

			@Override
			public void modifyText(ModifyEvent e) {
				int val = this.parseValue(value.getText());
				setValueValid(val >= 0 && val <= 255, val);
			}
		}
		
	} //class LocationDialog
}
