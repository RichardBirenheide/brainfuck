package org.birenheide.bf.debug.ui;

import org.birenheide.bf.debug.DbgActivator;
import org.birenheide.bf.debug.core.BfMemoryBlock;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IMemoryBlockListener;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.memory.IMemoryRendering;
import org.eclipse.debug.ui.memory.IMemoryRenderingContainer;
import org.eclipse.debug.ui.memory.IMemoryRenderingSite;
import org.eclipse.debug.ui.memory.IMemoryRenderingType;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.WorkbenchJob;

public class BfUIListenerContributor {
	
	private final IMemoryBlockListener memoryBlockListener = new IMemoryBlockListener() {
		
		@Override
		public void memoryBlocksRemoved(IMemoryBlock[] memory) {
		}
		
		@Override
		public void memoryBlocksAdded(final IMemoryBlock[] memory) {
			
			WorkbenchJob job = new WorkbenchJob("Update Memory View") {
				private static final long RESCHEDULE_TIME = 200;
				private static final int MAX_ATTEMPTS = 5;
				private volatile int attempts = 0;
				
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					if (page == null) {
						return Status.OK_STATUS;
					}
					try {
						IMemoryRenderingSite memoryView = null;
						for (IViewReference ref : page.getViewReferences()) {
							if (ref.getId().equals(IDebugUIConstants.ID_MEMORY_VIEW)) {
								memoryView = (IMemoryRenderingSite) ref.getView(true);
								break;
							}
						}
						if (memoryView == null) {
							if (attempts < MAX_ATTEMPTS) { //Memory view may still not been opened, try 5 times
								attempts++;
								this.schedule(RESCHEDULE_TIME);
							}
							else {
								attempts = 0;
							}
							return Status.OK_STATUS;
						}
						
						for (IMemoryBlock mb : memory) {
							if (mb instanceof BfMemoryBlock && !((BfMemoryBlock) mb).isUserCreated()) {
								IMemoryRenderingType renderingType = DebugUITools.getMemoryRenderingManager().getPrimaryRenderingType(mb);
								IMemoryRendering rendering = renderingType.createRendering();
								IMemoryRenderingContainer container = memoryView.getContainer(IDebugUIConstants.PLUGIN_ID + ".MemoryView.RenderingViewPane.1");
								rendering.init(container, mb);
								container.addMemoryRendering(rendering);
							}
						}
					}
					catch (CoreException e) {
						DbgActivator.getDefault().logError("Updating Memory View failed", e);
					}
					this.attempts = 0;
					return Status.OK_STATUS;
				}
			};
			job.setUser(false);
			job.schedule();
		}
	};
	
	public void addListeners() {
		DebugPlugin.getDefault().getMemoryBlockManager().addListener(this.memoryBlockListener);

	}
	
	public void removeListeners() {
		DebugPlugin.getDefault().getMemoryBlockManager().removeListener(this.memoryBlockListener);
	}

}
