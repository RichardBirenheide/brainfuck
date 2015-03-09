package org.birenheide.bf.debug.ui;

import org.birenheide.bf.debug.core.BfWatchpoint;
import org.eclipse.debug.ui.IDetailPane;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPartSite;

public class BfWatchpointDetailPane implements IDetailPane {
	
	static final String ID = BfWatchpointDetailPane.class.getCanonicalName();
	
	private Composite pane = null;
	private Text memoryLocation = null;
	private Text memoryValue = null;
	private Button suspendOnAccess = null;

	@Override
	public void init(IWorkbenchPartSite partSite) {
	}

	@Override
	public Control createControl(Composite parent) {
		pane = new Composite(parent, SWT.NONE);
		pane.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).create());
		pane.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		pane.setLayout(GridLayoutFactory.swtDefaults().numColumns(4).create());
		
		PixelConverter converter = new PixelConverter(parent);
		
		Label locationLabel = new Label(pane, SWT.NONE);
		locationLabel.setText("Memory Location (mp):");
		locationLabel.setLayoutData(GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER).grab(false, false).span(1, 1).create());
		
		memoryLocation = new Text(pane, SWT.BORDER);
		memoryLocation.setEditable(false);
		memoryLocation.setLayoutData(GridDataFactory.
				swtDefaults().
				align(SWT.LEFT, SWT.CENTER).
				grab(false, false).
				span(1, 1).
				hint(converter.convertWidthInCharsToPixels(10), SWT.DEFAULT).
				create());
		
		Label suspendLabel = new Label(pane, SWT.NONE);
		suspendLabel.setLayoutData(GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER).grab(false, false).span(1, 1).create());
		suspendLabel.setText("Suspend on Access:");
		
		suspendOnAccess = new Button(pane, SWT.CHECK);
		suspendOnAccess.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(false, false).span(1, 1).create());
		suspendOnAccess.setText("");
		suspendOnAccess.setEnabled(false);
		
		Label valueLabel = new Label(pane, SWT.NONE);
		valueLabel.setText("Suspend Value:");
		valueLabel.setLayoutData(GridDataFactory.copyData((GridData) locationLabel.getLayoutData()));
		
		memoryValue = new Text(pane, SWT.BORDER);
		memoryValue.setEditable(false);
		memoryValue.setLayoutData(GridDataFactory.copyData((GridData) memoryLocation.getLayoutData()));
		
		return pane;
	}

	@Override
	public void dispose() {
		pane.dispose();
	}

	@Override
	public void display(IStructuredSelection selection) {
		if (selection == null || selection.isEmpty() || !(selection.getFirstElement() instanceof BfWatchpoint)) {
			memoryLocation.setText("");
			memoryValue.setText("");
			return;
		}
		BfWatchpoint wp = (BfWatchpoint) selection.getFirstElement();
		this.memoryLocation.setText("0x" + Integer.toHexString(wp.getLocation()).toUpperCase());
		String memValue = "any";
		if (!wp.suspendOnModification()) {
			memValue = "0x" + Integer.toHexString(wp.getValue() & 0xFF).toUpperCase();
		}
		this.memoryValue.setText(memValue);
		this.suspendOnAccess.setSelection(wp.suspendOnAccess());
	}

	@Override
	public boolean setFocus() {
		return false;
	}

	@Override
	public String getID() {
		return ID;
	}

	@Override
	public String getName() {
		return "Brainfuck Watchpoint Detail";
	}

	@Override
	public String getDescription() {
		return "Test";
	}

}
