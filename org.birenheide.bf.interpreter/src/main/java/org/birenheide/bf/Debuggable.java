package org.birenheide.bf;

/**
 * Represents the debuggable part of the interpreter.
 * @author Richard Birenheide
 *
 */
public interface Debuggable extends Runnable {

	/**
	 * Adds a breakpoint.
	 * @param location the zero-based instruction pointer location to add the breakpoint for.
	 * @return <code>true</code> if the breakpoint has been added.
	 */
	public boolean addBreakpoint(int location);
	/**
	 * Removes a breakpoint.
	 * @param location the zero-based instruction pointer location to remove the breakpoint from.
	 * @return <code>true</code> if the breakpoint has been removed.
	 */
	public boolean removeBreakpoint(int location);
	/**
	 * Add a watchpoint.<br>
	 * Multiple watchpoints for the same data location can be added to 
	 * the debuggable to enable watching for different values.
	 * @param watchpoint the watchpoint to add.
	 * @return <code>true</code> if the watchpoint has been added.
	 */
	public boolean addWatchpoint(MemoryWatchpoint watchpoint);
	/**
	 * Remove a watchpoint.<br>
	 * The watchpoint will be removed according its {@link #equals(Object)} method.
	 * @param watchpoint the watchpoint to remove.
	 * @return <code>true</code> if the watchpoint was removed.
	 */
	public boolean removeWatchpoint(MemoryWatchpoint watchpoint);
	/**
	 * Execute the next instruction.<br>
	 * Has no effect when called on a non-suspended {@link Debuggable}.
	 */
	public void step();
	/**
	 * Resume execution.<br>
	 * Has no effect when called on a non-suspended {@link Debuggable}.
	 */
	public void resume();
	/**
	 * Suspend execution.<br>
	 * Has no effect when called on a suspended {@link Debuggable}.
	 */
	public void suspend();
	/**
	 * Terminate execution.<br>
	 */
	public void terminate();
	/**
	 * Adds a listener.
	 * @param listener the listener to add.
	 */
	public void addListener(InterpreterListener listener);
	/**
	 * Removes the listener given.<br>
	 * Has no effect if the listener is not present.
	 * @param listener the listener to remove.
	 */
	public void removeListener(InterpreterListener listener);
	/**
	 * Replaces the currently running program.<br>
	 * Intended for hot code replacement. Data pointer and instruction pointer
	 * remain unaffected for this operation. This may lead to different behavior
	 * when the replaced program is run anew compared to the replacement run.
	 * @param newProgram the new program to execute.
	 */
	public void replaceProgam(char[] newProgram);
}
