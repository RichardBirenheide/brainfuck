package org.birenheide.bf.debug.core;

import java.util.ArrayList;
import java.util.List;

import org.birenheide.bf.BfActivator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.IMemoryBlockManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IThread;

public class BfDebugTarget extends BfDebugElement implements IDebugTarget {

	private final ILaunch launch;
	private final BfProcess process;
	private final String name;
	private final BfThread thread;
	private final ResourceListener fileListener;
	private final List<BfBreakpoint> installedBreakpoints = new ArrayList<>(2);
	
	public BfDebugTarget(ILaunch launch, String label, BfProcess process) {
		super(null);
		this.launch = launch;
		this.process = process;
		this.name = label;
		this.thread = new BfThread(this, "Brainfuck Thread");
		this.process.getProcessListener().addEventSourceElement(this.thread);
		ResourceListener listener = null;
		try {
			IFile file = this.getFile();
			listener = new ResourceListener(file);
			ResourcesPlugin.getWorkspace().addResourceChangeListener(listener, IResourceChangeEvent.POST_CHANGE);
		} 
		catch (CoreException ex) {
			listener = null;
			BfActivator.getDefault().logError("Associated File could not be retrieved", ex);
		}
		this.fileListener = listener;
		this.installDeferredBreakpoints();
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
	}

	@Override
	public boolean canDisconnect() {
		return false;
	}

	@Override
	public void disconnect() throws DebugException {
	}

	@Override
	public boolean isDisconnected() {
		return this.isTerminated();
	}

	@Override
	public void breakpointAdded(IBreakpoint breakpoint) {
		if (this.isTerminated()) {
			return;
		}
		try {
			if (breakpoint instanceof BfWatchpoint) {
				BfWatchpoint wp = (BfWatchpoint) breakpoint;
				if (wp.isEnabled() && DebugPlugin.getDefault().getBreakpointManager().isEnabled()) {
					this.process.getInterpreter().addWatchpoint(wp);
					return;
				}
			}
			BfBreakpoint bp = this.getValidBreakpoint(breakpoint);
			if (bp == null) {
				return;
			}
			int location = bp.getCharStart();
			if (bp.isEnabled() && DebugPlugin.getDefault().getBreakpointManager().isEnabled()) {
				this.process.getInterpreter().addBreakpoint(location);
				this.addInstalledBreakpoint(bp);
			}
		}
		catch (CoreException ex) {
			BfActivator.getDefault().logError("Breakpoint could not be added", ex);
		}
	}

	@Override
	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
		if (this.isTerminated()) {
			return;
		}
		try {
			if (breakpoint instanceof BfWatchpoint) {
				BfWatchpoint wp = (BfWatchpoint) breakpoint;
				this.process.getInterpreter().removeWatchpoint(wp);
			}
			BfBreakpoint bp = this.getValidBreakpoint(breakpoint);
			if (bp == null) {
				return;
			}
			int location = bp.getCharStart();
			this.process.getInterpreter().removeBreakpoint(location);
			this.removeInstalledBreakpoint(bp);
		}
		catch (CoreException ex) {
			BfActivator.getDefault().logError("Breakpoint could not be added", ex);
		}
	}

	@Override
	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
		if (delta == null) {//Ignore non marker events which come from BfBreakpoint.setInstalled
			return;
		}
		if (this.isTerminated()) {
			return;
		}
		try {
			if (breakpoint instanceof BfWatchpoint) {
				BfWatchpoint wp = (BfWatchpoint) breakpoint;
				if (wp.isEnabled() && DebugPlugin.getDefault().getBreakpointManager().isEnabled()) {
					this.process.getInterpreter().addWatchpoint(wp);
				}
				else {
					this.process.getInterpreter().removeWatchpoint(wp);
				}
				return;
			}
			BfBreakpoint bp = this.getValidBreakpoint(breakpoint);
			if (bp == null) {
				return;
			}
			int location = bp.getCharStart();
			if (bp.isEnabled() && DebugPlugin.getDefault().getBreakpointManager().isEnabled()) {
				this.process.getInterpreter().addBreakpoint(location);
				this.addInstalledBreakpoint(bp);
			}
			else {
				this.process.getInterpreter().removeBreakpoint(location);
				this.removeInstalledBreakpoint(bp);
			}
		}
		catch (CoreException ex) {
			BfActivator.getDefault().logError("Breakpoint could not be added", ex);
		}
		
	}
	
	private BfBreakpoint getValidBreakpoint(IBreakpoint bp) throws CoreException {
		if (!(bp instanceof BfBreakpoint)) {
			return null;
		}
		BfBreakpoint breakpoint = (BfBreakpoint) bp;
		if (bp.getMarker().getResource().equals(getFile())) {
			return breakpoint; 
		}
		return null;
	}
	
	private void addInstalledBreakpoint(BfBreakpoint breakpoint) {
		breakpoint.setInstalled(true, this);
		if (!this.installedBreakpoints.contains(breakpoint)) {
			this.installedBreakpoints.add(breakpoint);
		}
	}
	
	private void removeInstalledBreakpoint(BfBreakpoint breakpoint) {
		breakpoint.setInstalled(false, this);
		this.installedBreakpoints.remove(breakpoint);
	}
	
	private void installDeferredBreakpoints() {
		for (IBreakpoint bp : DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(getModelIdentifier())) {
			this.breakpointAdded(bp);
		}
	}

	@Override
	public boolean canResume() {
		return this.thread.canResume();
	}

	@Override
	public boolean canSuspend() {
		return this.thread.canSuspend();
	}

	@Override
	public boolean isSuspended() {
		return this.process.getProcessListener().isSuspended();
	}

	@Override
	public void resume() throws DebugException {
		this.thread.resume();
	}

	@Override
	public void suspend() throws DebugException {
		this.thread.suspend();
	}

	@Override
	public boolean canTerminate() {
		return !this.process.isTerminated();
	}

	@Override
	public boolean isTerminated() {
		return this.process.isTerminated();
	}

	/**
	 * Retrieves whether the executed code represents the file contents.
	 * @return <code>true</code> if the file contents was uploaded to the interpreter.
	 */
	public boolean isInSync() {
		if (this.fileListener != null) {
			return !this.fileListener.isOutOfSync();
		}
		return false;
	}
	
	public boolean hasCodeBeenReplaced() {
		if (this.fileListener != null) {
			return this.fileListener.hasChanged();
		}
		else {
			return false;
		}
	}
	
	@Override
	public void terminate() throws DebugException {
		this.process.terminate();
	}

	@Override
	public boolean supportsStorageRetrieval() {
		return this.process.getProcessListener().getSuspendedState() != null;
	}

	@Override
	public IMemoryBlock getMemoryBlock(long startAddress, long length)
			throws DebugException {
		if (this.process.getProcessListener().getSuspendedState() != null) {
			return new BfMemoryBlock(this, this.process.getProcessListener().getSuspendedState(), startAddress, length);
		}
		else {
			return null;
		}
	}

	@Override
	public BfProcess getProcess() {
		return this.process;
	}

	@Override
	public IThread[] getThreads() throws DebugException {
		return new IThread[] {this.thread};
	}

	@Override
	public boolean hasThreads() throws DebugException {
		return !this.isTerminated();
	}

	@Override
	public String getName() throws DebugException {
		return this.name;
	}

	@Override
	public boolean supportsBreakpoint(IBreakpoint breakpoint) {
		try {
			if (breakpoint instanceof BfWatchpoint) {
				return true;
			}
			else if (breakpoint instanceof BfBreakpoint) {
				return breakpoint.getMarker().getResource().equals(this.getFile());
			}
		}
		catch (CoreException ex) {
			BfActivator.getDefault().logError("Breakpoint support failed", ex);
		}
		return false;
	}

	@Override
	public ILaunch getLaunch() {
		return this.launch;
	}

	@Override
	public BfDebugTarget getDebugTarget() {
		return this;
	}

	@Override
	public void fireTerminateEvent() {
		IMemoryBlockManager mbManager = DebugPlugin.getDefault().getMemoryBlockManager();
		mbManager.removeMemoryBlocks(mbManager.getMemoryBlocks(this));
		if (this.fileListener != null) {
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(fileListener);
		}
		for (BfBreakpoint bp : this.installedBreakpoints) {
			bp.setInstalled(false, this);
		}
		this.installedBreakpoints.clear();
		super.fireTerminateEvent();
	}
	
	private class ResourceListener implements IResourceChangeListener {
		
		private final IFile target;
		private boolean changed = false;
		private boolean outOfsync = false;
		
		ResourceListener(IFile file) {
			this.target = file;
		}

		@Override
		public void resourceChanged(IResourceChangeEvent event) {
			IResourceDelta fileDelta = event.getDelta().findMember(this.target.getFullPath());
			if (fileDelta != null) {
				boolean contentChanged = ((fileDelta.getFlags() & IResourceDelta.CONTENT) != 0);
				if (fileDelta.getKind() == IResourceDelta.CHANGED && contentChanged) {
					if (this.changed == false) {
						this.changed = true;
						BfDebugTarget.this.fireChangeEvent(DebugEvent.CONTENT);
					}
					try {
						char[] newProgram = BfDebugTarget.this.getProcess().getContentsAsString(this.target).toCharArray();
						BfDebugTarget.this.getProcess().getInterpreter().replaceProgam(newProgram);
						this.outOfsync = false;
					} 
					catch (CoreException ex) {
						BfActivator.getDefault().logError("File could not be replaced", ex);
						this.outOfsync = true;
					}
				}
			}
		}
		
		boolean hasChanged() {
			return this.changed;
		}
		
		boolean isOutOfSync() {
			return this.outOfsync;
		}
	}
}
