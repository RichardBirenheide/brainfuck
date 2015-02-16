package org.birenheide.bf;

import java.util.List;

public interface InterpreterListener {
	
	public void instructionPointerChanged(InterpreterState state);
	public void dataPointerChanged(InterpreterState state);
	public void dataResized(InterpreterState state);
	public void dataContentChanged(InterpreterState state);
	public void interpreterSuspended(InterpreterState state, List<EventReason> eventReasons);
	public void interpreterResumed(InterpreterState state);
	public void interpreterFinished(InterpreterState state);
	public void interpreterStarted(InterpreterState state);

}
