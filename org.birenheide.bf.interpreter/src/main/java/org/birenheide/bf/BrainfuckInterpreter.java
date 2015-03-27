package org.birenheide.bf;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BrainfuckInterpreter implements Runnable, Debuggable {
	
	public static final char[] RESERVED_CHARS = {'<', '>', '+', '-', '.', ',', '[', ']'};
	public static final String[] RESERVED_WORDS = new String[RESERVED_CHARS.length];
	static {
		for (int i = 0; i < RESERVED_CHARS.length; i++) {
			RESERVED_WORDS[i] = new String(new char[]{RESERVED_CHARS[i]});
		}
	}
	public static final String DEFAULT_CHARSET = "UTF-8";
	private static final int MIN_SIZE = 10;
	
	public static boolean isReservedChar(char check) {
		for (char c : RESERVED_CHARS) {
			if (c == check) {
				return true;
			}
		}
		return false;
	}
	
	private final List<InterpreterListener> listeners = new ArrayList<>(1);
	private final List<Integer> breakpoints = new ArrayList<>(1);
	private final Map<Integer, List<MemoryWatchpoint>> watchpoints = new HashMap<>();
	private char[] program;
	private final PrintStream out;
	private final PrintStream err;
	private final InputStream in;
	private volatile InterpreterState state = null;
	private volatile boolean suspend = false;
	private List<EventReason> suspendRequestReasons = Collections.synchronizedList(new ArrayList<EventReason>(3));
	private Thread interpreterThread = null;
	
	private final Object suspendLock = new Object();
	private final Object programExchangeLock = new Object();
	
	private volatile int instructionPointer = 0;
	private volatile int dataPointer = 0;
	private volatile byte[] data = new byte[MIN_SIZE];
	
	public BrainfuckInterpreter(char[] program, PrintStream out, PrintStream err, InputStream in) {
		this.program = program;
		this.out = out;
		this.err = err;
		this.in = in;
	}
	
	public void run() {
		EventReason terminateReason = EventReason.Finished;
		try {
			this.interpreterThread = Thread.currentThread();
			this.notifyStarted();
			while (instructionPointer < program.length) {
				{//Debugger section
					this.state = new StateWrapper();
					if (this.breakpoints.contains(this.instructionPointer)) {
						this.suspend = true;
						this.addSuspendRequestReason(EventReason.BreakPoint);
					}
					if (this.suspend && this.isCurrentInstructionValid()) {
						try {
							this.notifySuspended(this.suspendRequestReasons);
							this.suspendRequestReasons.clear();
							synchronized (this.suspendLock) {
								this.suspendLock.wait();
							}
						} 
						catch (InterruptedException e) {
							terminateReason = EventReason.ClientRequest;
							return; //Thread has been interrupted
						}
						this.notifyResumed();
					}
					if (Thread.interrupted()) {
						terminateReason = EventReason.ClientRequest;
						return;
					}
				} //End debugger section
				
				char instruction;
				synchronized (this.programExchangeLock) {
					instruction = program[instructionPointer];
				}
				switch (instruction) {
					case '>': dataPointer++;
							ensureDataCapacity();
							notifyDataPointerChanged();
							break;
					case '<': {
							dataPointer--;
							if (dataPointer < 0) {
								throw new InterpreterException("Illegal Data Pointer (<0) at ip=" + instructionPointer);
							}
							notifyDataPointerChanged();
							break;
					}
					case '+': {
							data[dataPointer]++;
							notifyDataChanged();
							break;
					}
					case '-': {
							data[dataPointer]--;
							notifyDataChanged();
							break;
					}
					case '.': {
						int c = data[dataPointer] & 0xFF;
//						System.out.println(c);
						out.write(c);
						break;
					}
					case ',': {
						try {
							int b = in.read();
							if (b > -1) {
								data[dataPointer] = (byte) b;
//								System.out.println(b + ":" + (data[dataPointer] & 0xff));
								notifyDataChanged();
							}
						}
						catch (InterruptedIOException ex) {
							terminateReason = EventReason.ClientRequest; //Likely an interruption from outside
						}
						catch (IOException ex) {
							throw new InterpreterException("Reading a byte failed at ip=" + instructionPointer + "; mp=" + dataPointer, ex);
						}
						break;
					}
					case '[': {
						synchronized (this.programExchangeLock) {
							if (data[dataPointer] == 0) {
								int openingBrackets = 0;
								int startBracketPosition = instructionPointer;
								do {
									if (program[instructionPointer] == '[') {
										openingBrackets++;
									}
									if (program[instructionPointer] == ']') {
										openingBrackets--;
									}
									instructionPointer++;
									if (instructionPointer >= program.length && openingBrackets != 0) {
										throw new InterpreterException("No matching closing bracket at: " + startBracketPosition);
									}
								} while (openingBrackets != 0);
								instructionPointer--; //Instruction pointer is one too far after last bracket close.
							}
							break;
						}
					}
					case ']': {
						synchronized (this.programExchangeLock) {
							if (data[dataPointer] != 0) {
								int closingBrackets = 1;
								int startBracketPosition = instructionPointer;
								instructionPointer--;
								while (closingBrackets != 0) {
									if (program[instructionPointer] == ']') {
										closingBrackets++;
									}
									if (program[instructionPointer] == '[') {
										closingBrackets--;
									}
									instructionPointer--;
									if (instructionPointer < 0) {
										throw new InterpreterException("Non matching opening bracket at: " + startBracketPosition);
									}
								}
								instructionPointer++; //Put pointer right of the bracket
							}
							break;
						}
					}
				}
				instructionPointer++;
				notifyInstructionPointerChanged();
			}
		}
		catch (InterpreterException ex) {
			terminateReason = EventReason.Failed;
			if (out != null) {
				out.flush();
			}
			if (err != null) {
				err.println();
				err.println(ex.getMessage());
				err.flush();
			}
			throw ex;
		}
		finally {
			if (out != null) {
				out.flush();
			}
			this.notifyFinished(terminateReason);
			this.interpreterThread = null;
			this.state = null;
		}
	}
	
	@Override
	public void addListener(InterpreterListener listener) {
		if (listener == null) {
			return;
		}
		if (!this.listeners.contains(listener)) {
			this.listeners.add(listener);
		}
	}
	
	@Override
	public void removeListener(InterpreterListener listener) {
		if (listener == null) {
			return;
		}
		this.listeners.remove(listener);
	}
	
	@Override
	public boolean addBreakpoint(int location) {
		if (location < 0 || location >= this.program.length) {
			return false;
		}
		if (!this.breakpoints.contains(location)) {
			return this.breakpoints.add(location);
		}
		return false;
	}

	@Override
	public boolean removeBreakpoint(int location) {
		return this.breakpoints.remove(Integer.valueOf(location));
	}

	@Override
	public boolean addWatchpoint(MemoryWatchpoint watchpoint) {
		int cell = watchpoint.getLocation();
		List<MemoryWatchpoint> watchpoints = null;
		if (this.watchpoints.containsKey(cell)) {
			watchpoints = this.watchpoints.get(cell);
		}
		else {
			watchpoints = new ArrayList<>(1);
			this.watchpoints.put(cell, watchpoints);
		}
		if (watchpoints.contains(watchpoint)) {
			return false;
		}
		watchpoints.add(watchpoint);
		return true;
	}

	@Override
	public boolean removeWatchpoint(MemoryWatchpoint watchpoint) {
		int cell = watchpoint.getLocation();
		if (!this.watchpoints.containsKey(cell)) {
			return false;
		}
		List<MemoryWatchpoint> watchpoints = this.watchpoints.get(cell);
		if (!watchpoints.contains(watchpoint)) {
			return false;
		}
		watchpoints.remove(watchpoint);
		if (watchpoints.isEmpty()) {
			this.watchpoints.remove(cell);
		}
		return true;
	}
	
	private boolean isCurrentInstructionValid() {
		for (char c : RESERVED_CHARS) {
			if (c == this.program[this.instructionPointer]) {
				return true;
			}
		}
		return false;
	}
	
	private void addSuspendRequestReason(EventReason reason) {
		synchronized (this.suspendRequestReasons) {
			if (!this.suspendRequestReasons.contains(reason)) {
				this.suspendRequestReasons.add(reason);
			}
		}
	}

	@Override
	public void step() {
		this.addSuspendRequestReason(EventReason.StepEnd);
		this.suspend = true;
		synchronized(this.suspendLock) {
			this.suspendLock.notify();
		}
	}

	@Override
	public void resume() {
		this.suspend = false;
		synchronized (this.suspendLock) {
			this.suspendLock.notify();
		}
	}

	@Override
	public void suspend() {
		this.addSuspendRequestReason(EventReason.ClientRequest);
		this.suspend = true;
	}
	
	@Override
	public void terminate() {
		if (this.interpreterThread != null) {
			this.interpreterThread.interrupt();
		}
	}
	
	@Override
	public void replaceProgam(char[] newProgram) {
		synchronized (this.programExchangeLock) {
			this.program = newProgram;
		}
	}

	private void notifyInstructionPointerChanged() {
		for (InterpreterListener listener : this.listeners) {
			listener.instructionPointerChanged(this.state);
		}
	}
	
	private void notifyDataPointerChanged() {
		if (this.watchpoints.containsKey(this.dataPointer)) {
			List<MemoryWatchpoint> watchpoints = this.watchpoints.get(this.dataPointer);
			for (MemoryWatchpoint wp : watchpoints) {
				if (wp.suspendOnAccess()) {
					this.addSuspendRequestReason(EventReason.WatchPoint);
					this.suspend = true;
				}
			}
		}
		for (InterpreterListener listener : this.listeners) {
			listener.dataPointerChanged(this.state);
		}
	}
	
	private void notifyDataChanged() {
		if (this.watchpoints.containsKey(this.dataPointer)) {
			List<MemoryWatchpoint> watchpoints = this.watchpoints.get(this.dataPointer);
			for (MemoryWatchpoint wp : watchpoints) {
				if (wp.suspendOnModification()) {
					this.addSuspendRequestReason(EventReason.WatchPoint);
					this.suspend = true;
				}
				else if (wp.getValue() == this.data[this.dataPointer]) {
					this.addSuspendRequestReason(EventReason.WatchPoint);
					this.suspend = true;
				}
			}
//			if (values.contains(this.data[this.dataPointer])) {
//				this.addSuspendRequestReason(EventReason.WatchPoint);
//				this.suspend = true;
//			}
		}
		for (InterpreterListener listener : this.listeners) {
			listener.dataContentChanged(this.state);
		}
	}
	
	private void notifySuspended(List<EventReason> reasons) {
		for (InterpreterListener listener : this.listeners) {
			listener.interpreterSuspended(this.state, new ArrayList<>(reasons));
		}
	}
	
	private void notifyResumed() {
		for (InterpreterListener listener : this.listeners) {
			listener.interpreterResumed(this.state);
		}
	}
	
	private void notifyStarted() {
		for (InterpreterListener listener : this.listeners) {
			listener.interpreterStarted(state);
		}
	}
	
	private void notifyFinished(EventReason reason) {
		for (InterpreterListener listener : this.listeners) {
			listener.interpreterFinished(this.state, Arrays.asList(reason));
		}
	}
	
	private void ensureDataCapacity() {
		if (this.dataPointer < this.data.length) {
			return;
		}
		else {
			this.data = Arrays.copyOf(data, dataPointer * 2);
			for (InterpreterListener listener : this.listeners) {
				listener.dataResized(this.state);
			}
		}
	}
	
	/**
	 * @author Richard Birenheide
	 *
	 */
	private class StateWrapper implements InterpreterState {

		@Override
		public int instructionPointer() {
			return BrainfuckInterpreter.this.instructionPointer;
		}

		@Override
		public int dataPointer() {
			return BrainfuckInterpreter.this.dataPointer;
		}

		@Override
		public byte[] dataSnapShot(int start, int end) {
			if (end > BrainfuckInterpreter.this.data.length) {
				throw new IllegalArgumentException("end may not be larger than the data length");
			}
			if (start >= end) {
				throw new IllegalArgumentException("start must be smaller than end");
			}
			return Arrays.copyOfRange(BrainfuckInterpreter.this.data, start, end);
		}

		
		
		@Override
		public int getDataSize() {
			return BrainfuckInterpreter.this.data.length;
		}

		@Override
		public String toString() {
			StringBuilder result = new StringBuilder();
			result.append("Interpreter State at instruction: ").append(instructionPointer());
			result.append("; at data: ").append(dataPointer()).append("\n");
			
			int dp = dataPointer();
			int length = this.getDataSize();
			int start = Math.max(0, dp - 20);
			int end = Math.min(length, dp + 21);
			byte[] data = this.dataSnapShot(start, end);
			
			
			result.append("(").append(start).append(") ");
			result.append("[");
			if (start > 0) {
				result.append("..., ");
			}
			for (int i = 0; i < data.length; i++) {
				String element = Integer.toHexString((int) (data[i] & 0xFF));
				if (i + start == dp) {
					result.append(">>").append(element).append("<<, ");
				}
				else {
					result.append(element).append(", ");
				}
			}
			result.delete(result.length() - 2, result.length());
			if (end < length) {
				result.append(", ...");
			}
			result.append("] (").append(end - 1).append("/").append(length - 1).append(")");
			return result.toString();
		}
		
		
		
	}
}
