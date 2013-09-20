package org.birenheide.bf.debug.ui;

import org.birenheide.bf.BfActivator;
import org.birenheide.bf.debug.core.BfProcessFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class BfMainTab extends AbstractLaunchConfigurationTab {
	
	public static final String PROJECT_ATTR = "org.birenheide.bf.Project";
	public static final String FILE_ATTR = "org.birenheide.bf.File";
	
	private Text project = null;
	private Text file = null;
	
	private static final String NAME = "File";

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
		browseFiles.addSelectionListener(new FileBrowseListener());
		
		this.setControl(area);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		this.project.setText("");
		this.file.setText("");
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			this.project.setText(configuration.getAttribute(PROJECT_ATTR, ""));
			this.file.setText(configuration.getAttribute(FILE_ATTR, ""));
		}
		catch (CoreException ex) {
			BfActivator.getDefault().logError("Configuration could not be initialized", ex);
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(PROJECT_ATTR, this.project.getText());
		configuration.setAttribute(FILE_ATTR, this.file.getText());
		configuration.setAttribute(DebugPlugin.ATTR_PROCESS_FACTORY_ID, BfProcessFactory.FACTORY_ID);
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
		
	}
	
	private class FileBrowseListener extends SelectionAdapter {
		
	}
}
