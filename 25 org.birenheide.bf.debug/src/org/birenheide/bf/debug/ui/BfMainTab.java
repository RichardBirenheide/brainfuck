package org.birenheide.bf.debug.ui;

import static org.birenheide.bf.debug.core.BfLaunchConfigurationDelegate.AUTO_FLUSH_ATTR;
import static org.birenheide.bf.debug.core.BfLaunchConfigurationDelegate.FILE_ATTR;
import static org.birenheide.bf.debug.core.BfLaunchConfigurationDelegate.INPUT_FILE_ATTR;
import static org.birenheide.bf.debug.core.BfLaunchConfigurationDelegate.OUTPUT_FILE_ATTR;
import static org.birenheide.bf.debug.core.BfLaunchConfigurationDelegate.PROJECT_ATTR;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.birenheide.bf.debug.DbgActivator;
import org.birenheide.bf.debug.core.BfProcessFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class BfMainTab extends AbstractLaunchConfigurationTab {
	
	private static final String NAME = "File";
	
	private static final String AUTO_FLUSH_TOOL_TIP = "Enables flushing after each character written. "
			+ "Will be considered only in debug mode.";
	
	private Text project = null;
	private Text file = null;
	
	private Text inputFile = null;
	private Button inputDefault = null;
	private Button inputOther = null;
	private Button browseInputFile = null;
	
	private Text outputFile = null;
	private Button outputDefault = null;
	private Button outputOther = null;
	private Button browseOutputFile = null;
	private Button outputAutoFlush = null;
	

	@Override
	public void createControl(Composite parent) {
		Composite area = new Composite(parent, SWT.NONE);
		area.setLayoutData(GridDataFactory.fillDefaults().span(1, 1).grab(true, true).create());
		area.setLayout(GridLayoutFactory.swtDefaults().numColumns(1).create());
		
		Group projectGroup = new Group(area, SWT.NONE);
		projectGroup.setText("Project:");
		projectGroup.setLayout(GridLayoutFactory.swtDefaults().numColumns(2).create());
		projectGroup.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		
		this.project = new Text(projectGroup, SWT.BORDER);
		this.project.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(1, 1).create());
		this.project.addModifyListener(new TextListener());

		Button browseProjects = this.createPushButton(projectGroup, "Browse...", null);
		browseProjects.addSelectionListener(new ProjectBrowseListener());
		
		Group fileGroup = new Group(area, SWT.NONE);
		fileGroup.setText("File:");
		fileGroup.setLayout(GridLayoutFactory.copyLayout((GridLayout) projectGroup.getLayout()));
		fileGroup.setLayoutData(GridDataFactory.copyData((GridData) projectGroup.getLayoutData()));
		
		this.file = new Text(fileGroup, SWT.BORDER);
		this.file.setLayoutData(GridDataFactory.copyData(((GridData) this.project.getLayoutData())));
		this.file.addModifyListener(new TextListener());
		
		Button browseFiles = this.createPushButton(fileGroup, "Browse...", null);
		browseFiles.addSelectionListener(new BfFileBrowseListener());
		
		SelectionListener radioListener = new RadioButtonListener();
		{
			Group inputFileGroup = new Group(area, SWT.NONE);
			inputFileGroup.setText("Input File:");
			inputFileGroup.setLayout(GridLayoutFactory.createFrom((GridLayout) projectGroup.getLayout()).numColumns(3).create());
			inputFileGroup.setLayoutData(GridDataFactory.copyData((GridData) projectGroup.getLayoutData())); 
			
			this.inputDefault = new Button(inputFileGroup, SWT.RADIO);
			this.inputDefault.setLayoutData(GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).grab(false, false).span(1, 1).create());
			this.inputDefault.setText("Default:");
			
			Text inputDefaultText = new Text(inputFileGroup, SWT.BORDER);
			inputDefaultText.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(2,1).create());
			inputDefaultText.setText("System.in");
			inputDefaultText.setEditable(false);
			inputDefaultText.setEnabled(false);
			
			this.inputOther = new Button(inputFileGroup, SWT.RADIO);
			this.inputOther.setLayoutData(GridDataFactory.createFrom((GridData) this.inputDefault.getLayoutData()).span(1, 1).create());
			this.inputOther.setText("Other:");
			
			this.inputFile = new Text(inputFileGroup, SWT.BORDER);
			this.inputFile.setLayoutData(GridDataFactory.copyData(((GridData) inputDefaultText.getLayoutData())));
			this.inputFile.addModifyListener(new TextListener());
			
			this.browseInputFile = this.createPushButton(inputFileGroup, "Browse...", null);
			this.browseInputFile.setLayoutData(GridDataFactory.createFrom((GridData) this.browseInputFile.getLayoutData()).align(SWT.END, SWT.CENTER).grab(true, false).span(3, 1).create());
			this.browseInputFile.addSelectionListener(new FileBrowseListener(this.inputFile));
			
			
			this.inputDefault.addSelectionListener(radioListener);
			this.inputOther.addSelectionListener(radioListener);
		}
		
		{
			Group outputFileGroup = new Group(area, SWT.NONE);
			outputFileGroup.setText("Output File:");
			outputFileGroup.setLayout(GridLayoutFactory.createFrom((GridLayout) projectGroup.getLayout()).numColumns(3).create());
			outputFileGroup.setLayoutData(GridDataFactory.copyData((GridData) projectGroup.getLayoutData())); 
			
			this.outputDefault = new Button(outputFileGroup, SWT.RADIO);
			this.outputDefault.setLayoutData(GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).grab(false, false).span(1, 1).create());
			this.outputDefault.setText("Default:");
			
			Text outputDefaultText = new Text(outputFileGroup, SWT.BORDER);
			outputDefaultText.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(2,1).create());
			outputDefaultText.setText("System.out");
			outputDefaultText.setEditable(false);
			outputDefaultText.setEnabled(false);

			if (this.getLaunchConfigurationDialog().getMode().equals(ILaunchManager.DEBUG_MODE)) {
				outputDefaultText.setLayoutData(GridDataFactory.createFrom((GridData) outputDefaultText.getLayoutData()).span(1, 1).create());
				this.outputAutoFlush = new Button(outputFileGroup, SWT.CHECK);
				this.outputAutoFlush.setLayoutData(GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER).grab(false, false).span(1, 1).create());
				this.outputAutoFlush.setText("Auto Flush");
				this.outputAutoFlush.setToolTipText(AUTO_FLUSH_TOOL_TIP);
				this.outputAutoFlush.addSelectionListener(radioListener);
			}
			
			this.outputOther = new Button(outputFileGroup, SWT.RADIO);
			this.outputOther.setLayoutData(GridDataFactory.createFrom((GridData) this.outputDefault.getLayoutData()).span(1, 1).create());
			this.outputOther.setText("Other:");
			
			this.outputFile = new Text(outputFileGroup, SWT.BORDER);
			this.outputFile.setLayoutData(GridDataFactory.createFrom(((GridData) outputDefaultText.getLayoutData())).span(2, 1).create());
			this.outputFile.addModifyListener(new TextListener());
			
			this.browseOutputFile = this.createPushButton(outputFileGroup, "Browse...", null);
			this.browseOutputFile.setLayoutData(GridDataFactory.createFrom((GridData) this.browseOutputFile.getLayoutData()).align(SWT.END, SWT.CENTER).grab(true, false).span(3, 1).create());
			this.browseOutputFile.addSelectionListener(new FileBrowseListener(this.outputFile));
			
			this.outputDefault.addSelectionListener(radioListener);
			this.outputOther.addSelectionListener(radioListener);
		}
		
		this.setControl(area);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), getHelpContextId());
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		if (this.project != null) {
			this.project.setText("");
			this.file.setText("");
		}
		configuration.setAttribute(PROJECT_ATTR, (String) null);
		configuration.setAttribute(FILE_ATTR, (String) null);
		configuration.setAttribute(INPUT_FILE_ATTR, (String) null);
		configuration.setAttribute(OUTPUT_FILE_ATTR, (String) null);
		configuration.setAttribute(AUTO_FLUSH_ATTR, true);
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			this.project.setText(configuration.getAttribute(PROJECT_ATTR, ""));
			this.file.setText(configuration.getAttribute(FILE_ATTR, ""));
			
			String inputFileName = configuration.getAttribute(INPUT_FILE_ATTR, (String) null);
			if (inputFileName != null) {
				this.inputFile.setText(inputFileName);
				this.inputOther.setSelection(true);
			}
			else {
				this.inputFile.setText("");
				this.inputOther.setSelection(false);
			}
			this.inputFile.setEnabled(this.inputOther.getSelection());
			this.browseInputFile.setEnabled(this.inputOther.getSelection());
			this.inputDefault.setSelection(!this.inputOther.getSelection());
			
			String outputFileName = configuration.getAttribute(OUTPUT_FILE_ATTR, (String) null);
			if (outputFileName != null) {
				this.outputFile.setText(outputFileName);
				this.outputOther.setSelection(true);
			}
			else {
				this.outputFile.setText("");
				this.outputOther.setSelection(false);
			}
			this.outputFile.setEnabled(this.outputOther.getSelection());
			this.browseOutputFile.setEnabled(this.outputOther.getSelection());
			this.outputDefault.setSelection(!this.outputOther.getSelection());
			if (this.outputAutoFlush != null) {
				this.outputAutoFlush.setSelection(configuration.getAttribute(AUTO_FLUSH_ATTR, true));
				this.outputAutoFlush.setEnabled(this.outputDefault.getSelection());
			}
		}
		catch (CoreException ex) {
			DbgActivator.getDefault().logError("Configuration could not be initialized", ex);
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(PROJECT_ATTR, this.project.getText());
		configuration.setAttribute(FILE_ATTR, this.file.getText());
		if (this.inputDefault.getSelection()) {
			configuration.setAttribute(INPUT_FILE_ATTR, (String) null);
		}
		else {
			configuration.setAttribute(INPUT_FILE_ATTR, this.inputFile.getText());
		}
		if (this.outputDefault.getSelection()) {
			configuration.setAttribute(OUTPUT_FILE_ATTR, (String) null);
		}
		else {
			configuration.setAttribute(OUTPUT_FILE_ATTR, this.outputFile.getText());
		}
		if (this.outputAutoFlush != null) {
			configuration.setAttribute(AUTO_FLUSH_ATTR, this.outputAutoFlush.getSelection());
		}
		configuration.setAttribute(DebugPlugin.ATTR_PROCESS_FACTORY_ID, BfProcessFactory.FACTORY_ID);
	}
	
	@Override
	public boolean isValid(ILaunchConfiguration configuration) {
		this.setErrorMessage(null);
		try {
			String fileName = configuration.getAttribute(FILE_ATTR, (String) null);
			String projectName = configuration.getAttribute(PROJECT_ATTR, (String) null);
			if (fileName == null || projectName == null) {
				this.setErrorMessage("Project or File not specified");
				return false;
			}
			IProject project = null;
			try {
				project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			}
			catch (IllegalArgumentException ex) {}
			if (project == null || !project.exists()) {
				this.setErrorMessage("Project " + projectName + " does not exist");
				return false;
			}
			IFile file = project.getFile(fileName);
			if (file == null || !file.exists()) {
				this.setErrorMessage("File " + fileName + " does not exist in project " + projectName);
				return false;
			}
			String inputFileName = configuration.getAttribute(INPUT_FILE_ATTR, (String) null);
			if (inputFileName != null) {
				File inputFile = new File(inputFileName);
				if (!inputFile.exists()) {
					this.setErrorMessage("Input File " + inputFile + " does not exist");
					return false;
				}
			}
			String outputFileName = configuration.getAttribute(OUTPUT_FILE_ATTR, (String) null);
			if (outputFileName != null) {
				File outputFile = new File(outputFileName);
				if (outputFile.getParentFile() != null && !outputFile.getParentFile().exists()) {
					this.setErrorMessage("Output File Directory " + outputFile.getParent() + " does not exist");
					return false;
				}
				if (outputFileName.equals("") || outputFileName.endsWith("/") || outputFileName.endsWith("\\") || outputFile.isDirectory()) {
					this.setErrorMessage("Output File unspecified");
					return false;
				}
			}
			
		}
		catch (CoreException ex) {
			this.setErrorMessage(ex.getMessage());
			return false;
		}
		return true;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public Image getImage() {
		Image fileImage = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
		return fileImage;
	}
	
	/**
	 * @author Richard Birenheide
	 *
	 */
	private class TextListener implements ModifyListener {

		@Override
		public void modifyText(ModifyEvent e) {
			BfMainTab.this.setDirty(true);
			BfMainTab.this.getLaunchConfigurationDialog().updateButtons();
		}
	}
	
	
	private class ProjectBrowseListener extends SelectionAdapter {

		@Override
		public void widgetSelected(SelectionEvent e) {
			ListDialog dialog = new ListDialog(getShell());
			WorkbenchContentProvider contentProvider = new WorkbenchContentProvider();
			dialog.setContentProvider(contentProvider);
			dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
			dialog.setLabelProvider(new WorkbenchLabelProvider());
			dialog.setTitle("Select Project");
			dialog.setMessage("Select Project:");
			if (dialog.open() == Window.OK && dialog.getResult().length == 1) {
				IProject selectedProject = (IProject) dialog.getResult()[0];
				project.setText(selectedProject.getName());
			}
		}
	}
	
	private class BfFileBrowseListener extends SelectionAdapter {

		@Override
		public void widgetSelected(SelectionEvent e) {
			String projectName = project.getText();
			IProject selectedProject = null;
			try {
				selectedProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			}
			catch (IllegalArgumentException ex) {
				//Deliberately empty
			}
			WorkbenchContentProvider contentProvider = new WorkbenchContentProvider() {

				@Override
				public Object[] getChildren(Object element) {
					List<Object> result = new ArrayList<>();
					for (Object o : super.getChildren(element)) {
						if (o instanceof IFile && !((IFile) o).getFileExtension().equals("bf")) {
							continue;
						}
						result.add(o);
					}
					return result.toArray(new Object[result.size()]);
				}
			};
			ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(), new WorkbenchLabelProvider(), contentProvider);
			dialog.setAllowMultiple(false);
			dialog.setTitle("Select File");
			dialog.setMessage("Select File:");
			if (selectedProject != null) {
				String filename = file.getText();
				IFile previousFile = null;
				try {
					previousFile = selectedProject.getFile(filename);
				}
				catch (IllegalArgumentException ex){}
				if (previousFile != null) {
					dialog.setInitialSelection(previousFile);
				}
				else {
					dialog.setInitialSelection(selectedProject);
				}
			}
			dialog.setValidator(new ISelectionStatusValidator() {
				@Override
				public IStatus validate(Object[] selection) {
					if (selection.length != 1) {
						return new Status(IStatus.ERROR, DbgActivator.PLUGIN_ID, "Select one file");
					}
					if (!(selection[0] instanceof IFile)) {
						return new Status(IStatus.ERROR, DbgActivator.PLUGIN_ID, "Select a file");
					}
					return new Status(IStatus.OK, DbgActivator.PLUGIN_ID, "");
				}
			});
			
			dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
			if (dialog.open() == Window.OK && dialog.getFirstResult() != null) {
				IFile selectedFile = (IFile) dialog.getFirstResult();
				if (!selectedFile.getProject().equals(selectedProject)) {
					project.setText(selectedFile.getProject().getName());
				}
				file.setText(selectedFile.getProjectRelativePath().toString());
			}
		}
	}
	
	private class FileBrowseListener extends SelectionAdapter {
		
		private final Text fileField;
		
		FileBrowseListener(Text fileField) {
			this.fileField = fileField;
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			String projectName = project.getText();
			IProject selectedProject = null;
			try {
				selectedProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			}
			catch (IllegalArgumentException ex) {
				//Deliberately empty
			}
			String oldFileName = this.fileField.getText();
			FileDialog dialog = new FileDialog(e.display.getActiveShell(), SWT.OPEN);
			if (!oldFileName.equals("")) {
				Path oldFile = Paths.get(oldFileName);
				dialog.setFilterPath(oldFile.getParent().toString());
				dialog.setFileName(oldFile.getFileName().toString());
			}
			else if (selectedProject != null) {
				String projectFileName = selectedProject.getLocation().toOSString();
				dialog.setFilterPath(projectFileName);
			}
			String newFileName = dialog.open();
			if (newFileName != null) {
				this.fileField.setText(newFileName);
			}
		}
	}
	
	
	
	private class RadioButtonListener extends SelectionAdapter {

		@Override
		public void widgetSelected(SelectionEvent e) {
			inputFile.setEnabled(inputOther.getSelection());
			browseInputFile.setEnabled(inputOther.getSelection());
			
			outputFile.setEnabled(outputOther.getSelection());
			browseOutputFile.setEnabled(outputOther.getSelection());
			if (outputAutoFlush != null) {
				outputAutoFlush.setEnabled(outputDefault.getSelection());
			}
			
			BfMainTab.this.setDirty(true);
			BfMainTab.this.getLaunchConfigurationDialog().updateButtons();
		}
	}
}
