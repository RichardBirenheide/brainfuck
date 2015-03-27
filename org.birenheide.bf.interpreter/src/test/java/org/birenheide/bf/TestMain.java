package org.birenheide.bf;

import java.util.List;
import java.util.Scanner;


public class TestMain {
	
//	private static final String HELLO_WORLD = "++++++++++[>+++++++>++++++++++>+++>+<<<<-]>++.>+.+++++++..+++.>++.<<+++++++++++++++.>.+++.------.--------.>+.>. // >>>>>>+";
	private static final String NESTED_HELLO_WORLD = "++++++++[>++++[>++>+++>+++>+<<<<-]>+>+>->>+[<]<-]>>.>---.+++++++..+++.>>.<-.<.+++.------.--------.>>+.>++.";
	
	private static final String TEST = ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>++++++++++++++++++++++++++++++";

	public static void main(String[] args) throws Exception {
		BrainfuckInterpreter interpreter = new BrainfuckInterpreter(NESTED_HELLO_WORLD.toCharArray(), System.out, System.err, System.in);
		interpreter.run();

		System.out.println();
		interpreter = new BrainfuckInterpreter(TEST.toCharArray(), System.out, System.err, System.in);
		TestListener l = new TestListener();
		interpreter.addListener(l);
		interpreter.addBreakpoint(115);
		interpreter.addWatchpoint(new Main.SimpleWatchpoint(88, (byte) 2));
		interpreter.addWatchpoint(new Main.SimpleWatchpoint(88, (byte) 2){

			@Override
			public boolean suspendOnAccess() {
				return true;
			}

			@Override
			public boolean suspendOnModification() {
				return false;
			}
		});
		Thread i = new Thread(interpreter);
		i.start();
		while (!l.finished) {
			if (l.suspended) {
				@SuppressWarnings("resource")
				Scanner s = new Scanner(System.in);
				String command = s.nextLine();
				if ("s".equals(command)) {
					interpreter.step();
				}
				else {
					interpreter.resume();
				}
			}
			Thread.sleep(10);
		}
	}

	private static class TestListener implements InterpreterListener {
		
		private boolean finished = false;
		private boolean suspended = false;

		@Override
		public void instructionPointerChanged(InterpreterState state) {
		}

		@Override
		public void dataPointerChanged(InterpreterState state) {
		}

		@Override
		public void dataResized(InterpreterState state) {
		}

		@Override
		public void dataContentChanged(InterpreterState state) {
//			System.err.println(Arrays.toString(state.data()));
		}

		@Override
		public void interpreterSuspended(InterpreterState state, List<EventReason> eventReasons) {
			System.err.println("Suspended\n" + state);
			this.suspended = true;
		}

		@Override
		public void interpreterResumed(InterpreterState state) {
			this.suspended = false;
		}

		
		@Override
		public void interpreterStarted(InterpreterState state) {
			System.err.println("Started");
			
		}

		@Override
		public void interpreterFinished(InterpreterState state, List<EventReason> reasons) {
			System.err.println("Finished: " + reasons);
			this.finished = true;
		}
		
	}
}
