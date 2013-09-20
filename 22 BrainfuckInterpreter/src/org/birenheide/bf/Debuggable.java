package org.birenheide.bf;

public interface Debuggable {

	public boolean addBreakpoint(int location);
	public boolean removeBreakpoint(int location);
	public boolean addWatchpoint(int cell, byte value);
	public boolean removeWatchpoint(int cell, byte value);
	public void step();
	public void resume();
	public void suspend();
	public void terminate();
	public void addListener(InterpreterListener listener);
	public void removeListener(InterpreterListener listener);
}
