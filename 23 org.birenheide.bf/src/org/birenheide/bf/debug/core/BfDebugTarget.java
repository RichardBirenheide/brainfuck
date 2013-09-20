package org.birenheide.bf.debug.core;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IThread;

public class BfDebugTarget extends BfDebugElement implements IDebugTarget {

	private final ILaunch launch;
	private final BfProcess process;
	private final String name;
	private final BfThread thread;
	
	public BfDebugTarget(ILaunch launch, String label, BfProcess process) {
		super(null);
		this.launch = launch;
		this.process = process;
		this.name = label;
		this.thread = new BfThread(this, "Brainfuck Thread");
		this.process.getProcessListener().addEventSourceElement(this.thread);
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
		// TODO Auto-generated method stub
		
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

	@Override
	public void terminate() throws DebugException {
		this.process.terminate();
	}

	@Override
	public boolean supportsStorageRetrieval() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IMemoryBlock getMemoryBlock(long startAddress, long length)
			throws DebugException {
		// TODO Auto-generated method stub
		return null;
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
}
