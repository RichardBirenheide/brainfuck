package org.birenheide.bf.debug.core;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;

public class BfThread extends BfDebugElement implements IThread {
	
	private final String name;
	private BfStackFrame stackFrame= null;
	private volatile boolean isStepping = false;

	public BfThread(BfDebugTarget target, String name) {
		super(target);
		this.name = name;
	}

	@Override
	public boolean canResume() {
		return this.isSuspended();
	}

	@Override
	public boolean canSuspend() {
		return !this.isSuspended() && !this.isTerminated();
	}

	@Override
	public boolean isSuspended() {
		return getDebugTarget().getProcess().getProcessListener().isSuspended();
	}

	@Override
	public void resume() throws DebugException {
		getDebugTarget().getProcess().getInterpreter().resume();
		this.fireResumeEvent(DebugEvent.CLIENT_REQUEST);
		this.getDebugTarget().fireResumeEvent(DebugEvent.CLIENT_REQUEST);
		this.getDebugTarget().getProcess().getProcessListener().removeEventSourceElement(stackFrame);
		this.stackFrame = null;
	}

	@Override
	public void suspend() throws DebugException {
		getDebugTarget().getProcess().getInterpreter().suspend();
		this.stackFrame = new BfStackFrame(getDebugTarget(), this, "Brainfuck Stack Frame");
		this.getDebugTarget().getProcess().getProcessListener().addEventSourceElement(this.stackFrame);
//		this.fireSuspendEvent(DebugEvent.CLIENT_REQUEST);
		this.getDebugTarget().fireSuspendEvent(DebugEvent.CLIENT_REQUEST);
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
		return this.isStepping; 
	}

	@Override
	public void stepInto() throws DebugException {
	}

	@Override
	public void stepOver() throws DebugException {
		this.fireResumeEvent(DebugEvent.STEP_OVER);
		this.stackFrame.fireResumeEvent(DebugEvent.STEP_OVER);
		getDebugTarget().getProcess().getInterpreter().step();
		this.isStepping = true;
		//FIXME should listen for suspend event and subsequent coding
		//should be in the event handler
//		while (!this.isSuspended());
//		this.isStepping = false;
//		this.fireSuspendEvent(DebugEvent.STEP_END);
//		this.stackFrame.fireSuspendEvent(DebugEvent.STEP_END);
	}
	
	

	@Override
	public void fireSuspendEvent(int detail) {
		if (detail == DebugEvent.STEP_END) {
			this.isStepping = false;
			this.fireChangeEvent(DebugEvent.CONTENT | DebugEvent.STATE);
		}
		super.fireSuspendEvent(detail);
	}

	@Override
	public void stepReturn() throws DebugException {
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
	public IStackFrame[] getStackFrames() throws DebugException {
		if (this.stackFrame != null) {
			return new IStackFrame[] {this.stackFrame};
		}
		else {
			return new IStackFrame[] {};
		}
	}

	@Override
	public boolean hasStackFrames() throws DebugException {
		return this.stackFrame != null;
	}

	@Override
	public int getPriority() throws DebugException {
		return 0;
	}

	@Override
	public IStackFrame getTopStackFrame() throws DebugException {
		return this.stackFrame;
	}

	@Override
	public String getName() throws DebugException {
		String label = "";
		if (this.isSuspended()) {
			label = " (Suspended)";
		}
		else if (this.isStepping) {
			label = " (Stepping)";
		}
		else {
			label = " (Running)";
		}
		return this.name + label;
	}

	@Override
	public IBreakpoint[] getBreakpoints() {
		// TODO Auto-generated method stub
		return new IBreakpoint[0];
	}

}
