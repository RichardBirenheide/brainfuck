package org.birenheide.bf.debug.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.birenheide.bf.debug.DbgActivator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IMemoryBlockManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;

public class BfThread extends BfDebugElement implements IThread {
	
	private final String name;
	private final IRegisterGroup[] registers;
	private BfStackFrame stackFrame= null;
	private volatile boolean isStepping = false;
	private IBreakpoint[] suspendedBreakpoints = new IBreakpoint[0];

	public BfThread(BfDebugTarget target, String name) {
		super(target);
		this.name = name;
		this.registers = new IRegisterGroup[] {new BfRegisterGroup(target)};
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
		this.suspendedBreakpoints = new IBreakpoint[0];
		this.fireResumeEvent(DebugEvent.CLIENT_REQUEST);
		this.getDebugTarget().fireResumeEvent(DebugEvent.CLIENT_REQUEST);
		this.getDebugTarget().getProcess().getProcessListener().removeEventSourceElement(stackFrame);
		this.stackFrame = null;
		getDebugTarget().getProcess().getInterpreter().resume();
	}

	@Override
	public void suspend() throws DebugException {
		getDebugTarget().getProcess().getInterpreter().suspend();
		
//		this.getDebugTarget().getProcess().getProcessListener().addEventSourceElement(this.stackFrame);
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
		this.isStepping = true;
		getDebugTarget().getProcess().getInterpreter().step();
	}
	
	

	@Override
	public void fireSuspendEvent(int detail) {
		this.stackFrame = new BfStackFrame(getDebugTarget(), this, "Brainfuck Stack Frame");
		if (detail == DebugEvent.STEP_END) {
			this.isStepping = false;
			this.fireChangeEvent(DebugEvent.CONTENT | DebugEvent.STATE);
		}
		else if (detail == DebugEvent.BREAKPOINT) {
			try {
			IBreakpointManager bpManager = DebugPlugin.getDefault().getBreakpointManager();
				int location = this.getDebugTarget().getProcess().getProcessListener().getInstructionPointer();
				List<IBreakpoint> breakpoints = new ArrayList<>();
				for (IBreakpoint bp : bpManager.getBreakpoints(getModelIdentifier())) {
					if (bp instanceof  BfBreakpoint && ((BfBreakpoint) bp).getCharStart() == location) {
						breakpoints.add(bp);
					}
				}
				this.suspendedBreakpoints = breakpoints.toArray(new IBreakpoint[breakpoints.size()]);
//				this.fireChangeEvent(DebugEvent.CONTENT | DebugEvent.STATE);
			}
			catch (CoreException ex) {
				DbgActivator.getDefault().logError("Breakpoints cannot be found", ex);
			}
		}
		try {
			IMemoryBlockManager mbManager = DebugPlugin.getDefault().getMemoryBlockManager();
			List<IMemoryBlock> memoryBlocks = Arrays.asList(mbManager.getMemoryBlocks(getDebugTarget()));
			boolean found = false;
			int length = this.getDebugTarget().getProcess().getProcessListener().getSuspendedState().getDataSize();
			for (IMemoryBlock block : memoryBlocks) {
				if (block.getStartAddress() == 0 && block.getLength() == length) {
					found = true;
					break;
				}
			}
			if (!found) {
				BfMemoryBlock mb = (BfMemoryBlock) this.getDebugTarget().getMemoryBlock(0, length);
				mb.setUserCreated(false);
				mb.fireCreationEvent();		
				mbManager.addMemoryBlocks(new IMemoryBlock[]{mb});
			}
		} 
		catch (DebugException e) {
			DbgActivator.getDefault().logError("Memory block could not be created", e);
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
	
	IRegisterGroup[] getRegisters() {
		return this.registers;
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
		return this.suspendedBreakpoints;
	}

}
