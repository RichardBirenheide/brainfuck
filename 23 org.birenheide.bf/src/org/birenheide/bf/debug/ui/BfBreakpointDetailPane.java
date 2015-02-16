package org.birenheide.bf.debug.ui;

import org.birenheide.bf.BfActivator;
import org.birenheide.bf.debug.core.BfBreakpoint;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.ui.IDetailPane;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPartSite;

public class BfBreakpointDetailPane implements IDetailPane {
	
	static final String ID = BfBreakpointDetailPane.class.getCanonicalName();
	
	private Composite pane = null;
	private Label resourceLocation = null;
	private Text breakpointLocation = null;

	@Override
	public void init(IWorkbenchPartSite partSite) {
	}

	@Override
	public Control createControl(Composite parent) {
		pane = new Composite(parent, SWT.NONE);
		pane.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).create());
		pane.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		pane.setLayout(GridLayoutFactory.swtDefaults().numColumns(2).create());
		
		PixelConverter converter = new PixelConverter(parent);
		
		Label resourceLabel = new Label(pane, SWT.NONE);
		resourceLabel.setText("Resource:");
		resourceLabel.setLayoutData(GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER).grab(false, false).span(1, 1).create());
		
		resourceLocation = new Label(pane, SWT.NONE);
		resourceLocation.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(1, 1).create());
		
		Label locationLabel = new Label(pane, SWT.NONE);
		locationLabel.setText("Suspend Location:");
		locationLabel.setLayoutData(GridDataFactory.copyData((GridData) resourceLabel.getLayoutData()));
		
		breakpointLocation = new Text(pane, SWT.BORDER);
		breakpointLocation.setEditable(false);
		breakpointLocation.setLayoutData(GridDataFactory.
				swtDefaults().
				align(SWT.LEFT, SWT.CENTER).
				grab(false, false).
				span(1, 1).
				hint(converter.convertWidthInCharsToPixels(10), SWT.DEFAULT).
				create());
		
		
		return pane;
	}

	@Override
	public void dispose() {
		pane.dispose();
	}

	@Override
	public void display(IStructuredSelection selection) {
		if (selection == null || selection.isEmpty() || !(selection.getFirstElement() instanceof BfBreakpoint)) {
			resourceLocation.setText("");
			breakpointLocation.setText("");
			return;
		}
		BfBreakpoint bp = (BfBreakpoint) selection.getFirstElement();
		IResource resource = bp.getMarker().getResource();
		String fileLocationString = resource.getProjectRelativePath().toString() + " in Project: " + resource.getProject().getName();
		
		this.resourceLocation.setText(fileLocationString);
		try {
			this.breakpointLocation.setText("" + bp.getCharStart());
		}
		catch (CoreException ex) {
			BfActivator.getDefault().logError("Setting Breakpoint location failed", ex);
		}
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
		// TODO Auto-generated method stub
		return "Brainfuck Breakpoint Detail";
	}

	@Override
	public String getDescription() {
		return null;
	}

}
