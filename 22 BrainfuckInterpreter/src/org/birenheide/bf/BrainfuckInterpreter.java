package org.birenheide.bf;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BrainfuckInterpreter implements Runnable, Debuggable {
	
	public static final char[] RESERVED_CHARS = {'<', '>', '+', '-', '.', ',', '[', ']'};
	private static final int MIN_SIZE = 10;
	
	private final List<InterpreterListener> listeners = new ArrayList<>(1);
	private final List<Integer> breakpoints = new ArrayList<>(1);
	private final Map<Integer, List<Byte>> watchpoints = new HashMap<>();
	private final char[] program;
	private final PrintStream out;
	private final InputStream in;
	private final InterpreterState state = new StateWrapper();
	private volatile boolean suspend = false;
	private List<EventReason> suspendRequestReasons = Collections.synchronizedList(new ArrayList<EventReason>(3));
	private Thread interpreterThread = null;
//	private volatile boolean stepping = false;
	
	private final Object suspendLock = new Object();
//	private final Object steppingLock = new Object();
	
	public BrainfuckInterpreter(char[] program, PrintStream out, InputStream in) {
		this.program = program;
		this.out = out;
		this.in = in;
	}
	
	private int instructionPointer = 0;
	private int dataPointer = 0;
	private byte[] data = new byte[MIN_SIZE];
	
	public void run() {
		try {
			this.interpreterThread = Thread.currentThread();
			this.notifyStarted();
			while (instructionPointer < program.length) {
				{//Debugger section
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
							return; //Thread has been interrupted
						}
						this.notifyResumed();
					}
					if (Thread.interrupted()) {
						return;
					}
				} //End debugger section
				
				char instruction = program[instructionPointer];
				switch (instruction) {
					case '>': dataPointer++;
							ensureDataCapacity();
							notifyDataPointerChanged();
							break;
					case '<': {
							dataPointer--;
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
						out.print((char) c);
						break;
					}
					case ',': {
						try {
							int b = in.read();
							if (b > -1) {
								data[dataPointer] = (byte) in.read();
								notifyDataChanged();
							}
						} 
						catch (IOException ex) {
							throw new InterpreterException("Reading a byte failed", ex);
						}
						break;
					}
					case '[': {
						if (data[dataPointer] == 0) {
							int openingBrackets = 1;
							int startBracketPosition = instructionPointer;
							instructionPointer++;
							while (openingBrackets != 0) {
								if (program[instructionPointer] == '[') {
									openingBrackets++;
								}
								if (program[instructionPointer] == ']') {
									openingBrackets--;
								}
								instructionPointer++;
								if (instructionPointer >= program.length) {
									throw new InterpreterException("No matching closing bracket at: " + startBracketPosition);
								}
							}
						}
						break;	
					}
					case ']': {
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
									throw new InterpreterException("Non matching closing bracket at: " + startBracketPosition);
								}
							}
						}
						break;
					}
				}
				instructionPointer++;
				notifyInstructionPointerChanged();
			}
		}
		finally {
			this.notifyFinished();
			this.interpreterThread = null;
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
	public boolean addWatchpoint(int cell, byte value) {
		List<Byte> values = null;
		if (this.watchpoints.containsKey(cell)) {
			values = this.watchpoints.get(cell);
		}
		else {
			values = new ArrayList<>(1);
			this.watchpoints.put(cell, values);
		}
		if (values.contains(value)) {
			return false;
		}
		values.add(value);
		return true;
	}

	@Override
	public boolean removeWatchpoint(int cell, byte value) {
		if (!this.watchpoints.containsKey(cell)) {
			return false;
		}
		List<Byte> values = this.watchpoints.get(cell);
		if (!values.contains(value)) {
			return false;
		}
		values.remove(Byte.valueOf(value));
		if (values.isEmpty()) {
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

	private void notifyInstructionPointerChanged() {
		for (InterpreterListener listener : this.listeners) {
			listener.instructionPointerChanged(this.state);
		}
	}
	
	private void notifyDataPointerChanged() {
		for (InterpreterListener listener : this.listeners) {
			listener.dataPointerChanged(this.state);
		}
	}
	
	private void notifyDataChanged() {
		if (this.watchpoints.containsKey(this.dataPointer)) {
			List<Byte> values = this.watchpoints.get(this.dataPointer);
			if (values.contains(this.data[this.dataPointer])) {
				this.addSuspendRequestReason(EventReason.WatchPoint);
				this.suspend = true;
			}
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
	
	private void notifyFinished() {
		for (InterpreterListener listener : this.listeners) {
			listener.interpreterFinished(this.state);
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
