package org.birenheide.bf;

import java.util.List;

/**
 * @author Richard Birenheide
 *
 */
class TestListener implements InterpreterListener {

	private List<EventReason> terminateReasons = null;
	private List<EventReason> suspendReasons = null;
	private InterpreterState suspendedState = null;
	private volatile boolean isStarted = false;
	private volatile boolean isSuspended = false;
	
	@Override
	public void instructionPointerChanged(InterpreterState state) {
	}

	@Override
	public void dataPointerChanged(InterpreterState state) {
	}

	@Override
	public void dataResized(InterpreterState state) {
	}

	@Override
	public void dataContentChanged(InterpreterState state) {
	}

	@Override
	public void interpreterSuspended(InterpreterState state,
			List<EventReason> eventReasons) {
		this.suspendedState = state;
		this.suspendReasons = eventReasons;
		this.isSuspended = true;
	}

	@Override
	public void interpreterResumed(InterpreterState state) {
		this.isSuspended = false;
	}

	@Override
	public void interpreterFinished(InterpreterState state,
			List<EventReason> eventReasons) {
		this.terminateReasons = eventReasons;
	}

	@Override
	public void interpreterStarted(InterpreterState state) {
		this.isStarted = true;
	}
	
	boolean isStarted() {
		return this.isStarted;
	}
	
	boolean isSuspended() {
		return this.isSuspended;
	}
	
	List<EventReason> getTerminateReasons() {
		return this.terminateReasons;
	}
	
	List<EventReason> getSuspendReasons() {
		return this.suspendReasons;
	}
	
	InterpreterState getSuspendState() {
		return this.suspendedState;
	}
}