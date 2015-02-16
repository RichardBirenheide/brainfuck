package org.birenheide.bf.debug.ui;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

public class WatchpointPropertyPage extends PropertyPage implements
		IWorkbenchPropertyPage {

	public WatchpointPropertyPage() {
		this.setDescription("Watchpoint DEscription");
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite area = new Composite(parent, SWT.NONE);
		area.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).create());
		
		Label lbl = new Label(area, SWT.NONE);
		lbl.setText("Test");
		this.setTitle("Test");
		return area;
	}
}
