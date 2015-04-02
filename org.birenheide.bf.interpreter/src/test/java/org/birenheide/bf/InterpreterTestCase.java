package org.birenheide.bf;

import java.io.InputStream;
import java.util.Scanner;


public class InterpreterTestCase {

	protected String readResource(String name) {
		InputStream is = this.getClass().getClassLoader().getResourceAsStream(name);
		Scanner sc = new Scanner(is, "UTF-8");
		sc.useDelimiter("\\A");
		try {
			if (sc.hasNext()) {
				return sc.next();
			}
			else {
				return null;
			}
		}
		finally {
			sc.close();
		}
	}

	protected TestListener runInterpreter(final BrainfuckInterpreter interpreter) {
		TestListener listener = new TestListener();
		interpreter.addListener(listener);
		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					interpreter.run();
				}
				catch (InterpreterException ex) {}
			}
		}, "Interpreter");
		t.setDaemon(true);
		t.start();
		return listener;
	}
}
