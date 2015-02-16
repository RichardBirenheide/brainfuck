package org.birenheide.bf.ui;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.birenheide.bf.BfActivator;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

public class BfMainPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	public BfMainPreferencePage() {
	}

	public BfMainPreferencePage(String title) {
		super(title);
	}

	public BfMainPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	@Override
	public void init(IWorkbench workbench) {
//		this.setDescription("No content yet, consult the subpages");
	}

	@Override
	protected Control createContents(Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), HelpContext.PREFERENCES_ID);
		Composite area = new Composite(parent, SWT.NONE);
		area.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).create());
		area.setLayout(GridLayoutFactory.swtDefaults().numColumns(1).create());
		
		Group group = new Group(area, SWT.NONE);
		group.setLayoutData(GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).span(1, 1).grab(true, false).create());
		group.setLayout(new GridLayout(2, false));
		group.setText("Brainfuck Interpreter");
		
		Label label = new Label(group, SWT.NONE);
		label.setLayoutData(GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).span(1, 1).create());
		label.setText("Extract Brainfuck Interpreter Jar File:");
		
		Button button = new Button(group, SWT.PUSH);
		button.setLayoutData(GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).grab(true, false).span(1, 1).create());
		button.setText("Save As...");
		button.addSelectionListener(new JarSaver());

		return area;
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return BfActivator.getDefault().getPreferenceStore();
	}

	@Override
	protected void performApply() {
		// TODO Auto-generated method stub
		super.performApply();
	}

	@Override
	protected void performDefaults() {
		// TODO Auto-generated method stub
		super.performDefaults();
	}
	
	private static class JarSaver extends SelectionAdapter {

		@Override
		public void widgetSelected(SelectionEvent e) {
			Shell parentShell = e.display.getActiveShell();
			FileDialog fd = new FileDialog(parentShell, SWT.SAVE);
			fd.setFilterExtensions(new String[]{"*.jar", "*.*"});
			fd.setFilterIndex(0);
			fd.setFileName("BrainfuckInterpreter.jar");
			fd.setOverwrite(true);
			String selectedFile = fd.open();
			if (selectedFile == null) {
				return;
			}
			java.nio.file.Path savePath = Paths.get(selectedFile);
			try {
				InputStream jarStream = FileLocator.openStream(BfActivator.getDefault().getBundle(), new Path("/lib/interpreter.jar"), false);
				Files.copy(jarStream, savePath, StandardCopyOption.REPLACE_EXISTING);
				jarStream.close();
				MessageDialog.openInformation(parentShell, "Saved", "'" + savePath.getFileName() + "' saved successfully");
			} 
			catch (IOException ex) {
				BfActivator.getDefault().logError("Interpreter Jar File could not be saved", ex);
				MessageDialog.openError(parentShell, "File not saved", "Error on saving '" + selectedFile + "'");
			}
		}
	}
	
}
