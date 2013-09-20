package org.birenheide.bf;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Properties;

public class Main {
	
	private static final String USAGE = "Usage:\n"
			+ "java org.birenheide.bf.Main [in=<filename>] [out=<filename>] [dbg=<filename>] <filename>\n"
			+ "in: name of a file to read from in the brainfuck program; will read from command line if omitted\n"
			+ "out: name of a file to write to in the brainfuck program; will write to command line if omitted\n"
			+ "dbg: name of a file containing debug information; no debugging, if omitted. See debug.properties for usage\n"
			+ "filename: mandatory file containing the brainfuck program"; 
	
	private static final String BREAK_POINT_PROPERTY = "breakpoints";
	private static final String WATCH_POINT_PROPERTY = "watchpoints";
	
	private static final char STEP = 49;
	private static final char RESUME = 50;

	/**
	 * Called with arguments: 
	 * org.birenheide.bf.Main&nbsp;[in=&lt;filename&gt;]&nbsp;[out=&lt;filename&gt;]&nbsp;[dbg=&lt;filename&gt;]&nbsp;&lt;filename&gt;
	 * <ul>
	 * <li>in:  name of a file to read from in the brainfuck program; will read from command line if omitted</li>
	 * <li>out: name of a file to write to in the brainfuck program; will write to command line if omitted</li>
	 * <li>dbg: name of a file containing debug information; no debugging, if omitted. See <code>debug.properties</code> for usage</li>
	 * <li>filename: mandatory file containing the brainfuck program</li>
	 * </ul>
	 * @param args the arguments. 
	 */
	public static void main(String[] args) throws Exception {
		if (args.length < 1) {
			sayUsage();
			return;
		}
		String brainfuckFilename = null;
		InputStream input = System.in;
		PrintStream output = System.out;
		String debugFilename = null;
		for (String arg : args) {
			if (arg.startsWith("in=")) {
				String inputFilename = arg.substring("in=".length());
				Path inputPath = Paths.get(inputFilename);
				if (Files.exists(inputPath)) {
					input = Files.newInputStream(inputPath, StandardOpenOption.READ);
				}
			}
			else if (arg.startsWith("out=")) {
				String outputFilename = arg.substring("out=".length());
				output = new PrintStream(outputFilename, "UTF-8");
			}
			else if (arg.startsWith("dbg=")) {
				debugFilename = arg.substring("dbg=".length());
			}
			else {
				brainfuckFilename = arg;
			}
		}
		if (brainfuckFilename == null) {
			sayUsage();
			return;
		}
		Path filePath = Paths.get(brainfuckFilename);
		if (!Files.exists(filePath)) {
			sayUsage();
			return;
		}
		String source = new String(Files.readAllBytes(filePath), "UTF-8");
		BrainfuckInterpreter interpreter = new BrainfuckInterpreter(source.toCharArray(), output, input);
		if (debugFilename != null) {
			Properties props = new Properties();
			props.load(new FileInputStream(debugFilename));
			instrumentInterpreter(interpreter, props);
			Debugger debugger = new Debugger(interpreter);
			Thread t = new Thread(debugger, "Debugger");
			t.setDaemon(true);
			t.start();
			interpreter.addListener(debugger);
		}
		interpreter.run();
	}
	
	private static void instrumentInterpreter(Debuggable interpreter, Properties props) {
		String breakpoints = props.getProperty(BREAK_POINT_PROPERTY);
		if (breakpoints != null) {
			for (String bp : breakpoints.split(";")) {
				int breakpoint = Integer.parseInt(bp.trim());
				interpreter.addBreakpoint(breakpoint);
			}
		}
		String watchpoints = props.getProperty(WATCH_POINT_PROPERTY);
		if (watchpoints != null) {
			for (String wp : watchpoints.split(";")) {
				String[] watchpoint = wp.split(",");
				int loc = Integer.parseInt(watchpoint[0].trim());
				byte val = 0;
				String value = watchpoint[1].trim();
				if (value.startsWith("0x")) {
					val = (byte) Integer.parseInt(value.substring("0x".length()), 16);
				}
				else {
					val = (byte) Integer.parseInt(value);
				}
				interpreter.addWatchpoint(loc, val);
			}
		}
	}
	
	private static void sayUsage() {
		System.out.println(USAGE);
	}
	
	private static class Debugger implements InterpreterListener, Runnable {
		private final Debuggable debuggable;
		
		private boolean finished = false;
		private boolean suspended = false;
		
		Debugger(Debuggable debuggable) {
			this.debuggable = debuggable;
		}
		
		@Override
		public void run() {
			try {
				while (!this.finished) {
					if (this.suspended) {
						char c = (char) System.in.read();
						switch (c) {
							case STEP: 
								this.debuggable.step();
								break;
							case RESUME:
								this.debuggable.resume();
								break;
						}
					}
					Thread.sleep(10);
				}
			}
			catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}

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
		public void interpreterSuspended(InterpreterState state, List<EventReason> reasons) {
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
		public void interpreterFinished(InterpreterState state) {
			System.err.println("Finished");
			this.finished = true;
		}
		
	}
	

}
