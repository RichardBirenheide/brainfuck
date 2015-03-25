package org.birenheide.bf;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import org.junit.Test;

public class CopyTest extends InterpreterTestCase {
	
	private static final String COPY_SOURCE = "CopyInputToOutput.bf";
	private static final String INPUT = "testInput.txt";

	@Test(timeout=10000)
	public void copy() throws Exception {
		String source = this.readResource(COPY_SOURCE);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(bos);
		InputStream is = this.getClass().getClassLoader().getResourceAsStream(INPUT);
		BrainfuckInterpreter interpreter = new BrainfuckInterpreter(source.toCharArray(), out, System.err, is);
		interpreter.run();
		String expected = this.readResource(INPUT);
		String actual = new String(bos.toByteArray(), 0, bos.toByteArray().length - 1, "UTF-8");
		assertEquals(expected, actual);
	}

}
