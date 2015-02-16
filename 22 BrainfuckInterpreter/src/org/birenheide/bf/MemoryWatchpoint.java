package org.birenheide.bf;

public interface MemoryWatchpoint {

	/**
	 * Location of the memory pointer for this watchpoint.
	 * @return the location.
	 */
	public int getLocation();
	/**
	 * The value for this watchpoint on which to suspend. Meaningless if
	 * {@link #suspendOnModification()} returns <code>true</code>.
	 * @return the suspend value.
	 */
	public byte getValue();
	/**
	 * If <code>true</code> the interpreter will suspend when the memory pointer is
	 * changed to the location of this watchpoint.
	 * @return <code>true</code> if the interpreter should suspend.
	 */
	public boolean suspendOnAccess();
	/**
	 * If <code>true</code> the interpreter will suspend on each increment or
	 * decrement of the memory location of this watchpoint.
	 * @return <code>true</code> for modification suspend.
	 */
	public boolean suspendOnModification();
}
