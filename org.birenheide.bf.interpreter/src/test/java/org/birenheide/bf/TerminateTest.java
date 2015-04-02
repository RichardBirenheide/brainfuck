package org.birenheide.bf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;

import org.junit.Test;

public class TerminateTest extends InterpreterTestCase {

	private static final String LONG_LOOP = "->->-[<[<[-]->-]->-]";
	private static final String INPUT_INTERRUPT = ",";
	private static final String SHORT_LOOP = "-[-]";
	private static final String INVALID_MEMORY_ACCESS = "<";
	private static final String INVALID_BRACKET_01 = "[";
	private static final String INVALID_BRACKET_02 = "[]+]";
	
	@Test(timeout=10000)
	public void testSuspendedTermination() throws Exception {
		BrainfuckInterpreter interpreter = new BrainfuckInterpreter(LONG_LOOP.toCharArray(), null, null, null);
		TestListener listener = this.runInterpreter(interpreter);
		while (!listener.isStarted()) {
			Thread.sleep(10);
		}
		interpreter.suspend();
		while (!listener.isSuspended()) {
			Thread.sleep(10);
		}
		interpreter.terminate();
		while (listener.getTerminateReasons() == null) {
			Thread.sleep(10);
		}
		assertEquals(1, listener.getTerminateReasons().size());
		assertEquals(EventReason.ClientRequest, listener.getTerminateReasons().get(0));
	}
	
	@Test(timeout=10000)
	public void testLongLoop() throws Exception {
		BrainfuckInterpreter interpreter = new BrainfuckInterpreter(LONG_LOOP.toCharArray(), null, null, null);
		TestListener listener = this.runInterpreter(interpreter);
		while (!listener.isStarted()) {
			Thread.sleep(10);
		}
		interpreter.terminate();
		while (listener.getTerminateReasons() == null) {
			Thread.sleep(10);
		}
		assertEquals(1, listener.getTerminateReasons().size());
		assertEquals(EventReason.ClientRequest, listener.getTerminateReasons().get(0));
	}

	@Test(timeout=10000)
	public void testInputInterrupt() throws Exception {
		InputStream is = new InputStream() {
			
			@Override
			public int read() throws IOException {
				try {
					Thread.sleep(10000);
				} 
				catch (InterruptedException ex) {
					throw new InterruptedIOException();
				}
				return -1;
			}
		};
		BrainfuckInterpreter interpreter = new BrainfuckInterpreter(INPUT_INTERRUPT.toCharArray(), null, null, is);
		TestListener listener = this.runInterpreter(interpreter);
		while (!listener.isStarted()) {
			Thread.sleep(10);
		}
		interpreter.terminate();
		while (listener.getTerminateReasons() == null) {
			Thread.sleep(10);
		}
		assertEquals(1, listener.getTerminateReasons().size());
		assertEquals(EventReason.ClientRequest, listener.getTerminateReasons().get(0));
	}
	
	@Test(timeout=10000)
	public void testShortLoop() throws Exception {
		BrainfuckInterpreter interpreter = new BrainfuckInterpreter(SHORT_LOOP.toCharArray(), null, null, null);
		TestListener listener = new TestListener();
		interpreter.addListener(listener);
		interpreter.run();
		assertEquals(1, listener.getTerminateReasons().size());
		assertEquals(EventReason.Finished, listener.getTerminateReasons().get(0));
	}
	
	@Test(timeout=10000)
	public void testInvalidMemoryAccess() throws Exception {
		BrainfuckInterpreter interpreter = new BrainfuckInterpreter(INVALID_MEMORY_ACCESS.toCharArray(), null, null, null);
		TestListener listener = new TestListener();
		interpreter.addListener(listener);
		boolean exc = false;
		try {
			interpreter.run();
		}
		catch (InterpreterException ex) {
			exc = true;
		}
		assertTrue(exc);
		assertEquals(1, listener.getTerminateReasons().size());
		assertEquals(EventReason.Failed, listener.getTerminateReasons().get(0));
	}
	
	@Test(timeout=10000)
	public void testInvalidBracket01() throws Exception {
		BrainfuckInterpreter interpreter = new BrainfuckInterpreter(INVALID_BRACKET_01.toCharArray(), null, null, null);
		TestListener listener = new TestListener();
		interpreter.addListener(listener);
		boolean exc = false;
		try {
			interpreter.run();
		}
		catch (InterpreterException ex) {
			exc = true;
		}
		assertTrue(exc);
		assertEquals(1, listener.getTerminateReasons().size());
		assertEquals(EventReason.Failed, listener.getTerminateReasons().get(0));
	}
	
	@Test(timeout=10000)
	public void testInvalidBracket02() throws Exception {
		BrainfuckInterpreter interpreter = new BrainfuckInterpreter(INVALID_BRACKET_02.toCharArray(), null, null, null);
		TestListener listener = new TestListener();
		interpreter.addListener(listener);
		boolean exc = false;
		try {
			interpreter.run();
		}
		catch (InterpreterException ex) {
			exc = true;
		}
		assertTrue(exc);
		assertEquals(1, listener.getTerminateReasons().size());
		assertEquals(EventReason.Failed, listener.getTerminateReasons().get(0));
	}
}
