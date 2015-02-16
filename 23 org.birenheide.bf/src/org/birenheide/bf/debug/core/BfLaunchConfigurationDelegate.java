package org.birenheide.bf.debug.core;

import static org.birenheide.bf.BfActivator.BUNDLE_SYMBOLIC_NAME;
import static org.eclipse.core.resources.IMarker.PROBLEM;
import static org.eclipse.core.resources.IMarker.SEVERITY_ERROR;
import static org.eclipse.core.resources.IResource.DEPTH_ZERO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;

import org.birenheide.bf.BfActivator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;

public class BfLaunchConfigurationDelegate extends
		LaunchConfigurationDelegate {

	public static final String PROJECT_ATTR = BfActivator.BUNDLE_SYMBOLIC_NAME + ".Project";
	public static final String FILE_ATTR = BfActivator.BUNDLE_SYMBOLIC_NAME + ".File";
	public static final String INPUT_FILE_ATTR = BfActivator.BUNDLE_SYMBOLIC_NAME + ".InputFile";
	public static final String OUTPUT_FILE_ATTR = BfActivator.BUNDLE_SYMBOLIC_NAME + ".OutputFile";
	public static final String AUTO_FLUSH_ATTR = BfActivator.BUNDLE_SYMBOLIC_NAME + ".AutoFlush";
	
	public static final int FILE_ERROR_STATUS_CODE = 1;

	@Override
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {

		String fileName = configuration.getAttribute(BfLaunchConfigurationDelegate.FILE_ATTR, (String) null);
		String projectName = configuration.getAttribute(BfLaunchConfigurationDelegate.PROJECT_ATTR, (String) null);
		if (fileName == null || projectName == null) {
			throw new CoreException(new Status(IStatus.ERROR, BfActivator.BUNDLE_SYMBOLIC_NAME, "Invalid Launch Configuration: " + configuration.getName()));
		}
		IProject project = null;
		try {
			project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		}
		catch (IllegalArgumentException ex) {}
		if (project == null || !project.exists()) {
			throw new CoreException(new Status(IStatus.ERROR, BfActivator.BUNDLE_SYMBOLIC_NAME, "Invalid Launch Configuration: " + configuration.getName()));
		}
		IFile file = null;
		try {
			file = project.getFile(fileName);
		}
		catch (IllegalArgumentException ex) {}
		if (file == null || !file.exists()) {
			throw new CoreException(new Status(IStatus.ERROR, BfActivator.BUNDLE_SYMBOLIC_NAME, "Invalid Launch Configuration: " + configuration.getName()));
		}
		String inputFilename = configuration.getAttribute(INPUT_FILE_ATTR, (String) null);
		String outputFilename = configuration.getAttribute(OUTPUT_FILE_ATTR, (String) null);
		String autoFlush = Boolean.toString(configuration.getAttribute(AUTO_FLUSH_ATTR, false));
		
		Map<String, String> attributes = new TreeMap<>();
		attributes.put(INPUT_FILE_ATTR, inputFilename);
		attributes.put(OUTPUT_FILE_ATTR, outputFilename);
		attributes.put(PROJECT_ATTR, projectName);
		attributes.put(FILE_ATTR, fileName);
		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
			attributes.put(AUTO_FLUSH_ATTR, autoFlush);
		}
		else {
			attributes.put(AUTO_FLUSH_ATTR, Boolean.toString(false));
		}
		String encoding = configuration.getAttribute(DebugPlugin.ATTR_CONSOLE_ENCODING, ResourcesPlugin.getEncoding());
		attributes.put(DebugPlugin.ATTR_CONSOLE_ENCODING, encoding);
		
		BfProcess process = (BfProcess) DebugPlugin.newProcess(launch, null, fileName, attributes);
		launch.addProcess(process);
		if (launch.getLaunchMode().equals(ILaunchManager.DEBUG_MODE)) {
			IDebugTarget target = new BfDebugTarget(launch, fileName, process);
			launch.addDebugTarget(target);
		}
		process.startInterpreter();
	}

	@Override
	public boolean preLaunchCheck(ILaunchConfiguration configuration,
			String mode, IProgressMonitor monitor) throws CoreException {
		String fileName = configuration.getAttribute(BfLaunchConfigurationDelegate.FILE_ATTR, (String) null);
		String projectName = configuration.getAttribute(BfLaunchConfigurationDelegate.PROJECT_ATTR, (String) null);
		if (fileName == null || projectName == null) {
			return false;
		}
		IProject project = null;
		try {
			project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		}
		catch (IllegalArgumentException ex) {}
		if (project == null || !project.exists()) {
			return false;
		}
		
		IFile file = project.getFile(fileName);
		if (file == null || !file.exists()) {
			return false;
		}
		int maxSeverity = file.findMaxProblemSeverity(PROBLEM, true, DEPTH_ZERO);
		if (maxSeverity >= SEVERITY_ERROR) {
			IStatusHandler prompter = DebugPlugin.getDefault().getStatusHandler(promptStatus);
			boolean proceed = (Boolean) prompter.handleStatus(getStatusForErrorCode(FILE_ERROR_STATUS_CODE), file);
			if (!proceed) {
				return false;
			}
		}
		
		String inputFilename = configuration.getAttribute(INPUT_FILE_ATTR, (String) null);
		if (inputFilename != null) {
			if (Files.notExists(Paths.get(inputFilename))) {
				return false;
			}
		}
		
		String outputFilename = configuration.getAttribute(OUTPUT_FILE_ATTR, (String) null);
		if (outputFilename != null) {
			Path outputFilePath = Paths.get(outputFilename);
			if (Files.notExists(outputFilePath)) {
				try {
					Files.createFile(outputFilePath);
				} 
				catch (IOException ex) {
					BfActivator.getDefault().logError("Output File cannot be created", ex);
					return false;
				}
			}
		}
		return super.preLaunchCheck(configuration, mode, monitor);
	}
	
	private IStatus getStatusForErrorCode(int errorCode) {
		return new Status(IStatus.INFO, BUNDLE_SYMBOLIC_NAME, errorCode, "", null);
	}
}
