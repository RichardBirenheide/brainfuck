package org.birenheide.bf.debug.core;

import java.util.Map;

import org.birenheide.bf.BfActivator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.IProcessFactory;
import org.eclipse.debug.core.model.IProcess;

public class BfProcessFactory implements IProcessFactory {
	
	public static final String FACTORY_ID = "org.birenheide.bf.processFactory";

	@Override
	public IProcess newProcess(ILaunch launch, Process process, String label,
			Map<String, String> attributes) {
		try {
			return new BfProcess(launch, label, attributes);
		} 
		catch (CoreException e) {
			BfActivator.getDefault().logError("Process could not be started", e);
			return null;
		}
	}

}
