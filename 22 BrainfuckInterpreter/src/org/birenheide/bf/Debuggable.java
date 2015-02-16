package org.birenheide.bf;

public interface Debuggable {

	public boolean addBreakpoint(int location);
	public boolean removeBreakpoint(int location);
	public boolean addWatchpoint(MemoryWatchpoint watchpoint);
	public boolean removeWatchpoint(MemoryWatchpoint watchpoint);
	public void step();
	public void resume();
	public void suspend();
	public void terminate();
	public void addListener(InterpreterListener listener);
	public void removeListener(InterpreterListener listener);
	public void replaceProgam(char[] newProgram);
}
