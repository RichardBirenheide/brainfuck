package org.birenheide.bf;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;

public class NestedHelloWorldTest extends InterpreterTestCase {
	
	private static final String NESTED_HELLO_WORLD = "NestedHelloWorld.bf";

	@Test(timeout=10000)
	public void runInterpreter() throws Exception {
		String source = this.readResource(NESTED_HELLO_WORLD);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(bos);
		BrainfuckInterpreter interpreter = new BrainfuckInterpreter(source.toCharArray(), out, System.err, System.in);
		interpreter.run();
		out.flush();
		String result = new String(bos.toByteArray(), "UTF-8");
		assertEquals("Hello World!\n", result);
	}
}
