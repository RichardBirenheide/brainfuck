package org.birenheide.bf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;


public class InterpreterTests extends InterpreterTestCase {

	private static final String SHORT_LOOP = "-[-?]";
	private static final String LONG_MEMORY = ">>>>>>>>>>>>";
	
	@Test(timeout=10000)
	public void invalidCharacterTest() throws Exception {
		BrainfuckInterpreter interpreter = new BrainfuckInterpreter(SHORT_LOOP.toCharArray(), null, null, null);
		try {
			interpreter.run();
		}
		catch (Exception ex) {
			fail(ex.getMessage());
		}
	}
	
	@Test(timeout=10000)
	public void memoryTest() throws Exception {
		BrainfuckInterpreter interpreter = new BrainfuckInterpreter(LONG_MEMORY.toCharArray(), null, null, null);
		interpreter.addBreakpoint(0);
		interpreter.addBreakpoint(11);
		
		TestListener listener = this.runInterpreter(interpreter);
		
		try {
			while (!listener.isStarted()) {
				Thread.sleep(1);
			}
			while (!listener.isSuspended()) {
				Thread.sleep(1);
			}
			InterpreterState state = listener.getSuspendState();
			int initialSize = state.getDataSize();
			
			interpreter.resume();
			while (listener.isSuspended());
			while (!listener.isSuspended());
			assertEquals(11, state.instructionPointer());
			state = listener.getSuspendState();
			assertEquals(2 * initialSize, state.getDataSize());
			assertTrue(state.toString().contains("" + 11));
		}
		finally {
			interpreter.terminate();
		}
	}
}
