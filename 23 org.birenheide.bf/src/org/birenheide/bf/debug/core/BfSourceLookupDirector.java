package org.birenheide.bf.debug.core;

import org.birenheide.bf.BfActivator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupParticipant;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;

public class BfSourceLookupDirector extends AbstractSourceLookupDirector
		implements IPersistableSourceLocator {
	
	public static final String SOURCE_LOCATOR_ID = "org.birenheide.bf.sourceLocator"; 

	@Override
	public void initializeParticipants() {
		try {
			ILaunchConfiguration config = this.getLaunchConfiguration();
			if (config != null) {
				String fileName = config.getAttribute(BfLaunchConfigurationDelegate.FILE_ATTR, (String) null);
				this.addParticipants(new ISourceLookupParticipant[] {new BfSourceLocatorParticipant(fileName)});
			}
			else {
				BfActivator.getDefault().logWarning("No LaunchConfiguration in: " + BfSourceLookupDirector.class, null);
			}
		} 
		catch (CoreException e) {
			BfActivator.getDefault().logError("Launch configuration could not be read", e);
		}
	}
	
	private static class BfSourceLocatorParticipant extends AbstractSourceLookupParticipant {
		
		private final String sourceName;
		
		BfSourceLocatorParticipant(String fileName) {
			this.sourceName = fileName;
		}

		@Override
		public String getSourceName(Object object) throws CoreException {
			return this.sourceName;
		}
		
	}

}
