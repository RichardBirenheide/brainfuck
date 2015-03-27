package org.birenheide.bf;

import java.util.List;

/**
 * A listener which will be notified on interpreter events.
 * Implementations should avoid long running operations as the methods
 * are called in the interpreters thread.
 * @author Richard Birenheide
 *
 */
public interface InterpreterListener {
	
	/**
	 * Called whenever the interpreter instruction pointer changes.<br>
	 * On conditional jumps this method is only called when the final instruction pointer
	 * index has been evaluated. The method is called before the instruction at the pointer
	 * is executed.
	 * @param state the current state. Only valid during the lifetime of this call.
	 */
	public void instructionPointerChanged(InterpreterState state);
	/**
	 * Called when the data pointer changes.
	 * @param state the current state. Only valid during the lifetime of this call.
	 */
	public void dataPointerChanged(InterpreterState state);
	/**
	 * Called when the size of the internal memory increases.
	 * The size of the internal memory is dynamically increased. This method is called
	 * whenever this takes place. 
	 * @param state the current state. Only valid during the lifetime of this call.
	 */
	public void dataResized(InterpreterState state);
	/**
	 * Called whenever the value of a data cell is changed.
	 * @param state the current state. Only valid during the lifetime of this call.
	 */
	public void dataContentChanged(InterpreterState state);
	/**
	 * Called when the interpreter suspends. 
	 * @param state the current state. Only valid during the lifetime of this call.
	 * @param eventReasons list of reasons leading to suspension.
	 */
	public void interpreterSuspended(InterpreterState state, List<EventReason> eventReasons);
	/**
	 * Called when the interpreter resumes. This can only happen by calling {@link Debuggable#resume()}.
	 * @param state the current state. Only valid during the lifetime of this call.
	 */
	public void interpreterResumed(InterpreterState state);
	/**
	 * Called when the interpreter finishes either when the program is finished or
	 * by a call to {@link Debuggable#terminate()}.
	 * @param state the current state. Only valid during the lifetime of this call.
	 * @param eventReasons list of reasons leading to termination.
	 */
	public void interpreterFinished(InterpreterState state, List<EventReason> eventReasons);
	/**
	 * Called when interpreter starts. Is called before any instruction is executed.
	 * @param state the current state. Only valid during the lifetime of this call.
	 */
	public void interpreterStarted(InterpreterState state);
}
