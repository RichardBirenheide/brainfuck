package org.birenheide.bf;

/**
 * Reason for a debugger event.
 * @author Richard Birenheide
 *
 */
public enum EventReason {
	/**
	 * A breakpoint was hit.
	 */
	BreakPoint,
	/**
	 * Stepping has finished.
	 */
	StepEnd,
	/**
	 * A watchpoint was hit.
	 */
	WatchPoint,
	/**
	 * The state of the interpreter has been changed by a call
	 * from outside.
	 */
	ClientRequest,
	/**
	 * The interpreter finished its execution normally.
	 */
	Finished,
	/**
	 * The interpreter finished its execution because of an exception.
	 */
	Failed
	
}
