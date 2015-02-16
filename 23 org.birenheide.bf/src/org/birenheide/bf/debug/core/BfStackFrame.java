package org.birenheide.bf.debug.core;

import org.birenheide.bf.BfActivator;
import org.birenheide.bf.InterpreterState;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;

public class BfStackFrame extends BfDebugElement implements IStackFrame {
	
	private final String name;
	private final BfThread ownerThread;

	public BfStackFrame(BfDebugTarget target, BfThread thread, String name) {
		super(target);
		try {
			name = target.getLaunch().getLaunchConfiguration().getAttribute(BfLaunchConfigurationDelegate.FILE_ATTR, name);
		} 
		catch (CoreException e) {
			BfActivator.getDefault().logError("Configuration could not be read", e);
		}
		this.name = name;
		this.ownerThread = thread;
	}

	@Override
	public boolean canStepInto() {
		return false;
	}

	@Override
	public boolean canStepOver() {
		return this.isSuspended();
	}

	@Override
	public boolean canStepReturn() {
		return false;
	}

	@Override
	public boolean isStepping() {
		return this.ownerThread.isStepping();
	}

	@Override
	public void stepInto() throws DebugException {
	}

	@Override
	public void stepOver() throws DebugException {
		this.ownerThread.stepOver();
	}

	@Override
	public void stepReturn() throws DebugException {
	}

	@Override
	public boolean canResume() {
		return this.ownerThread.canResume();
	}

	@Override
	public boolean canSuspend() {
		return this.ownerThread.canSuspend();
	}

	@Override
	public boolean isSuspended() {
		return this.ownerThread.isSuspended();
	}

	@Override
	public void resume() throws DebugException {
		this.ownerThread.resume();
	}

	@Override
	public void suspend() throws DebugException {
		this.ownerThread.suspend();
	}

	@Override
	public boolean canTerminate() {
		return getDebugTarget().canTerminate();
	}

	@Override
	public boolean isTerminated() {
		return getDebugTarget().isTerminated();
	}

	@Override
	public void terminate() throws DebugException {
		getDebugTarget().terminate();
	}

	@Override
	public IThread getThread() {
		return this.ownerThread;
	}

	@Override
	public IVariable[] getVariables() throws DebugException {
		return new IVariable[0];
	}

	@Override
	public boolean hasVariables() throws DebugException {
		return false;
	}

	@Override
	public int getLineNumber() throws DebugException {
		return -1;
	}

	@Override
	public int getCharStart() throws DebugException {
		return getDebugTarget().getProcess().getProcessListener().getInstructionPointer();
	}
	
	public int getMemoryPointer() {
		return getDebugTarget().getProcess().getProcessListener().getSuspendedState().dataPointer();
	}

	@Override
	public int getCharEnd() throws DebugException {
		return getDebugTarget().getProcess().getProcessListener().getInstructionPointer() + 1;
	}

	@Override
	public String getName() throws DebugException {
		InterpreterState state = this.getDebugTarget().getProcess().getProcessListener().getSuspendedState();
		int datapointer = -1;
		if (state != null) {
			datapointer = state.dataPointer();
		}
		String dataPointerLabel = "";
		if (datapointer > -1) {
			dataPointerLabel = "; mp=0x" + Integer.toHexString(datapointer).toUpperCase();
		}
		String label = this.name + " at: ip=" + this.getCharStart() + dataPointerLabel;
		return label;
	}

	@Override
	public IRegisterGroup[] getRegisterGroups() throws DebugException {
		return this.ownerThread.getRegisters();
	}

	@Override
	public boolean hasRegisterGroups() throws DebugException {
		return this.ownerThread.isSuspended();
	}
}
