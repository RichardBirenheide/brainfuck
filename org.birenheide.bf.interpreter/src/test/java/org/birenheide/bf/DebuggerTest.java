package org.birenheide.bf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DebuggerTest extends InterpreterTestCase {

	private static final String LONG_LOOP = "->->-[<[<[-]->-]->-]";
	
	@Test(timeout=10000)
	public void testBreakpoint() throws Exception {
		BrainfuckInterpreter interpreter = new BrainfuckInterpreter(LONG_LOOP.toCharArray(), null, null, null);
		
		interpreter.addBreakpoint(0);
		interpreter.addBreakpoint(19);
		
		TestListener listener = this.runInterpreter(interpreter);
		
		try {
			while (!listener.isStarted()) {
				Thread.sleep(1);
			}
			while (!listener.isSuspended()) {
				Thread.sleep(1);
			}
			InterpreterState state = listener.getSuspendState();
			assertEquals(0, state.instructionPointer());
			interpreter.resume();
			while (listener.isSuspended()) {
				Thread.sleep(1);
			}
			while (!listener.isSuspended()) {
				Thread.sleep(1);
			}
			state = listener.getSuspendState();
			assertEquals(19, state.instructionPointer());
			assertEquals(2, state.dataPointer());
			assertEquals(254, state.dataSnapShot(2, 3)[0] & 0xFF);
			assertEquals(1, listener.getSuspendReasons().size());
			assertEquals(EventReason.BreakPoint, listener.getSuspendReasons().get(0));
		}
		finally {
			interpreter.terminate();
		}
	}
	
	@Test(timeout=10000)
	public void testValueWatchpoint() throws Exception {
		BrainfuckInterpreter interpreter = new BrainfuckInterpreter(LONG_LOOP.toCharArray(), null, null, null);
		
		interpreter.addWatchpoint(new TestWatchpoint(2, (byte) 253, false, false));
		
		TestListener listener = this.runInterpreter(interpreter);
		
		try {
			while (!listener.isStarted()) {
				Thread.sleep(1);
			}
			while (!listener.isSuspended()) {
				Thread.sleep(1);
			}
			InterpreterState state = listener.getSuspendState();
			assertEquals(19, state.instructionPointer());
			assertEquals(2, state.dataPointer());
			assertEquals(253, state.dataSnapShot(2, 3)[0] & 0xFF);
			assertEquals(1, listener.getSuspendReasons().size());
			assertEquals(EventReason.WatchPoint, listener.getSuspendReasons().get(0));
		}
		finally {
			interpreter.terminate();
		}
	}
	
	@Test(timeout=10000)
	public void testModificationWatchpoint() throws Exception {
		BrainfuckInterpreter interpreter = new BrainfuckInterpreter(LONG_LOOP.toCharArray(), null, null, null);
		
		MemoryWatchpoint wp = new TestWatchpoint(2, (byte) 2, true, false);
		interpreter.addWatchpoint(wp);
		
		TestListener listener = this.runInterpreter(interpreter);
		
		try {
			while (!listener.isStarted()) {
				Thread.sleep(1);
			}
			while (!listener.isSuspended()) {
				Thread.sleep(1);
			}
			InterpreterState state = listener.getSuspendState();
			assertEquals(5, state.instructionPointer());
			assertEquals(2, state.dataPointer());
			assertEquals(255, state.dataSnapShot(2, 3)[0] & 0xFF);
			interpreter.removeWatchpoint(wp);
			interpreter.resume();
			while (listener.isSuspended()) {
				Thread.sleep(1);
			}
			interpreter.addWatchpoint(wp);
			while (!listener.isSuspended()) {
				Thread.sleep(1);
			}
			state = listener.getSuspendState();
			assertEquals(19, state.instructionPointer());
			assertEquals(2, state.dataPointer());
			assertTrue(254 >= (state.dataSnapShot(2, 3)[0] & 0xFF));
			assertEquals(1, listener.getSuspendReasons().size());
			assertEquals(EventReason.WatchPoint, listener.getSuspendReasons().get(0));
		}
		finally {
			interpreter.terminate();
		}
	}
	
	@Test(timeout=10000)
	public void testAccessWatchpoint() throws Exception {
		BrainfuckInterpreter interpreter = new BrainfuckInterpreter(LONG_LOOP.toCharArray(), null, null, null);
		
		MemoryWatchpoint wp = new TestWatchpoint(2, (byte) 2, false, true);
		interpreter.addWatchpoint(wp);
		
		TestListener listener = this.runInterpreter(interpreter);
		
		try {
			while (!listener.isStarted()) {
				Thread.sleep(1);
			}
			while (!listener.isSuspended()) {
				Thread.sleep(1);
			}
			InterpreterState state = listener.getSuspendState();
			assertEquals(4, state.instructionPointer());
			assertEquals(2, state.dataPointer());
			assertEquals(0, state.dataSnapShot(2, 3)[0] & 0xFF);
			
			interpreter.removeWatchpoint(wp);
			interpreter.resume();
			while (listener.isSuspended()) {
				Thread.sleep(1);
			}
			interpreter.addWatchpoint(wp);
			while (!listener.isSuspended()) {
				Thread.sleep(1);
			}
			state = listener.getSuspendState();
			assertEquals(18, state.instructionPointer());
			assertEquals(2, state.dataPointer());
			assertTrue(255 >= (state.dataSnapShot(2, 3)[0] & 0xFF));
			assertEquals(1, listener.getSuspendReasons().size());
			assertEquals(EventReason.WatchPoint, listener.getSuspendReasons().get(0));
		}
		finally {
			interpreter.terminate();
		}
	}
	
	@Test(timeout=10000)
	public void testSuspend() throws Exception {
		BrainfuckInterpreter interpreter = new BrainfuckInterpreter(LONG_LOOP.toCharArray(), null, null, null);
		TestListener listener = this.runInterpreter(interpreter);
		try {
			while (!listener.isStarted()) {
				Thread.sleep(1);
			}
			Thread.sleep(5);
			interpreter.suspend();
			while (!listener.isSuspended()) {
				Thread.sleep(1);
			}
			assertEquals(1, listener.getSuspendReasons().size());
			assertEquals(EventReason.ClientRequest, listener.getSuspendReasons().get(0));
		}
		finally {
			interpreter.terminate();
		}
	}
	
	/**
	 * @author Richard Birenheide
	 *
	 */
	private static class TestWatchpoint implements MemoryWatchpoint {
		
		private final int location;
		private final byte value;
		private final boolean suspendOnModification;
		private final boolean suspendOnAccess;
		
		TestWatchpoint(int location, byte value, boolean suspendOnModification, boolean suspendOnAccess) {
			this.location = location;
			this.value = value;
			this.suspendOnModification = suspendOnModification;
			this.suspendOnAccess = suspendOnAccess;
		}

		@Override
		public int getLocation() {
			return this.location;
		}

		@Override
		public byte getValue() {
			return this.value;
		}

		@Override
		public boolean suspendOnAccess() {
			return this.suspendOnAccess;
		}

		@Override
		public boolean suspendOnModification() {
			return this.suspendOnModification;
		}
	}
}
