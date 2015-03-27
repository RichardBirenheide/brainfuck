package org.birenheide.bf.debug.core;

import static org.birenheide.bf.debug.core.BfLaunchConfigurationDelegate.AUTO_FLUSH_ATTR;
import static org.birenheide.bf.debug.core.BfLaunchConfigurationDelegate.FILE_ATTR;
import static org.birenheide.bf.debug.core.BfLaunchConfigurationDelegate.INPUT_FILE_ATTR;
import static org.birenheide.bf.debug.core.BfLaunchConfigurationDelegate.OUTPUT_FILE_ATTR;
import static org.birenheide.bf.debug.core.BfLaunchConfigurationDelegate.PROJECT_ATTR;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.birenheide.bf.BrainfuckInterpreter;
import org.birenheide.bf.Debuggable;
import org.birenheide.bf.EventReason;
import org.birenheide.bf.InterpreterException;
import org.birenheide.bf.InterpreterListener;
import org.birenheide.bf.InterpreterState;
import org.birenheide.bf.debug.DbgActivator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.DebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;

public class BfProcess implements IProcess {
	
	private static final String FALLBACK_CHARSET = "UTF-8";
	
	private final Map<String, String> attributes = new TreeMap<>();
	private final String label;
	private final ILaunch launch;
	private final Debuggable interpreter;
	private final ProcessListener listener = new ProcessListener();
	private final IStreamsProxy proxy;
	private final BfOutputStream outStream;
	private final BfOutputStream errorStream;
	private final PrintStream interpreterPrintStream;
	private final PrintStream interpreterErrorStream;
	private final InputStream interpreterInputStream;
	private final IFile possibleOutputFile;

	public BfProcess(ILaunch launch, String label, Map<String, String> attrs) throws CoreException {
		this.label = label;
		this.launch = launch;
		if (attrs != null) {
			this.attributes.putAll(attrs);
		}
		
		String inputFilename = attrs.get(INPUT_FILE_ATTR);
		String outputFilename = attrs.get(OUTPUT_FILE_ATTR);
		boolean autoFlush = Boolean.parseBoolean(attrs.get(AUTO_FLUSH_ATTR));
		String encoding = attrs.get(DebugPlugin.ATTR_CONSOLE_ENCODING);
		
		BfStreamsProxy proxy;
		try {
			proxy = new BfStreamsProxy(outputFilename == null, inputFilename == null, encoding);
		} 
		catch (IOException ex) {
			throw new CoreException(new Status(IStatus.ERROR, DbgActivator.PLUGIN_ID, "Process could not be created", ex));
		}
		
		
		if (inputFilename != null) {
			try {
				this.interpreterInputStream = Files.newInputStream(Paths.get(inputFilename));
			} 
			catch (IOException ex) {
				throw new CoreException(new Status(IStatus.ERROR, DbgActivator.PLUGIN_ID, "Process could not be created", ex));
			}
		}
		else {
			this.interpreterInputStream = proxy.getInputStream();
		}
		
		this.outStream = proxy.getOutputStream();
		this.errorStream = proxy.getErrorStream();
		this.outStream.setAutoFlush(autoFlush);
		if (outputFilename != null) {
			IPath path = Path.fromOSString(outputFilename);
			this.possibleOutputFile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
			try {
				OutputStream os = Files.newOutputStream(Paths.get(outputFilename), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
				this.interpreterPrintStream = new PrintStream(os, true, encoding);
			} 
			catch (IOException ex) {
				throw new CoreException(new Status(IStatus.ERROR, DbgActivator.PLUGIN_ID, "Process could not be created", ex));
			}
		}
		else {
			this.possibleOutputFile = null;
			try {
				this.interpreterPrintStream = new PrintStream(this.outStream, true, encoding);
			} 
			catch (UnsupportedEncodingException ex) {
				throw new CoreException(new Status(IStatus.ERROR, DbgActivator.PLUGIN_ID, "Process could not be created", ex));
			}
		}
		try {
			this.interpreterErrorStream = new PrintStream(this.errorStream, true, encoding);
		} 
		catch (UnsupportedEncodingException ex) {
			throw new CoreException(new Status(IStatus.ERROR, DbgActivator.PLUGIN_ID, "Process could not be created", ex));
		}
		
		this.proxy = proxy;
		
		String projectName = attrs.get(PROJECT_ATTR);
		String fileName = attrs.get(FILE_ATTR);
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if (project == null) {
			throw new CoreException(new Status(IStatus.ERROR, DbgActivator.PLUGIN_ID, "Project does not exist: " + projectName));
		}
		IFile file = project.getFile(fileName);
		if (!file.exists()) {
			throw new CoreException(new Status(IStatus.ERROR, DbgActivator.PLUGIN_ID, "File does not exist: " + file.toString()));
		}
		String code = getContentsAsString(file);
		this.interpreter = new BrainfuckInterpreter(code.toCharArray(), this.interpreterPrintStream, this.interpreterErrorStream, this.interpreterInputStream);
		this.interpreter.addListener(listener);
//		/*
//		 * The launch framework has issues in the order of processing content obtained
//		 * via IStreamMonitor.getContent() and content provided to the listener when
//		 * there is already content when the listener is added. Therefore it is safer
//		 * to wait until the first listener is added before starting the interpreter.
//		 */
//		Thread t = new Thread(new Runnable() {
//			@Override
//			public void run() {
//				if (outStream != null) {
//					outStream.waitForListenerAdded();
//				}
//				interpreter.run();
//			}
//		}, "Brainfuck");
//		t.setDaemon(true);
//		t.start();
	}

	void startInterpreter() {
		/*
		 * The launch framework has issues in the order of processing content obtained
		 * via IStreamMonitor.getContent() and content provided to the listener when
		 * there is already content when the listener is added. Therefore it is safer
		 * to wait until the first listener is added before starting the interpreter.
		 */
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				if (outStream != null) {
					outStream.waitForListenerAdded();
				}
				if (errorStream != null) {
					errorStream.waitForListenerAdded();
				}
				try {
					if (interpreter instanceof Runnable) {
						((Runnable) interpreter).run();
					}
				}
				catch (InterpreterException ex) {
					if (ex.getCause() instanceof InterruptedIOException) {
						//Do nothing probably terminated in input wait
					}
					else {
						DbgActivator.getDefault().logError("Interpreter terminated unexpectedly", ex);
					}
				}
				finally {
					try {
						interpreterInputStream.close();
					} 
					catch (IOException ex) {
						DbgActivator.getDefault().logError("Interpreter InputStream could not be closed", ex);
					}
					interpreterPrintStream.flush();
					interpreterPrintStream.close();
					interpreterErrorStream.close();
					if (possibleOutputFile != null) {
						try {
							possibleOutputFile.getParent().refreshLocal(IResource.DEPTH_ONE, null);
						} 
						catch (CoreException ex) {
							DbgActivator.getDefault().logError("Workspace output file could not be refreshed", ex);
						}
					}
				}
			}
		}, "Brainfuck");
		t.setDaemon(true);
		t.start();
	}

	Debuggable getInterpreter() {
		return this.interpreter;
	}
	
	ProcessListener getProcessListener() {
		return this.listener;
	}

	/**
	 * @param file
	 * @return
	 * @throws CoreException
	 */
	String getContentsAsString(IFile file) throws CoreException {
		InputStream in = file.getContents();
		byte[] buffer = new byte[1024];
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		int len = -1;
		try {
			while ((len = in.read(buffer)) != -1) {
				bos.write(buffer, 0, len);
			}
		} 
		catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, DbgActivator.PLUGIN_ID, e.getMessage(), e));
		}
		String code;
		try {
			code = new String(bos.toByteArray(), file.getCharset());
		} 
		catch (UnsupportedEncodingException ex) {
			DbgActivator.getDefault().logError(file + " has unsupported charset", ex);
			code = new String(bos.toByteArray(), Charset.forName(FALLBACK_CHARSET));
		}
		return code;
	}
	
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		if (adapter.equals(ConsoleStreamFlusher.class)) {
			return new ConsoleStreamFlusher() {
				
				@Override
				public void flush() {
					if (BfProcess.this.outStream != null) {
						try {
							BfProcess.this.outStream.flush();
						} 
						catch (IOException ex) {
							DbgActivator.getDefault().logError("Stream could not be flushed", ex);
						}
					}
				}
				
				@Override
				public boolean canFlush() {
					return BfProcess.this.outStream != null && !BfProcess.this.outStream.isAutoFlush();
				}
			};
		}
		return null;
	}

	@Override
	public boolean canTerminate() {
		return !this.listener.isStopped;
	}

	@Override
	public boolean isTerminated() {
		return this.listener.isStopped;
		
	}

	@Override
	public void terminate() throws DebugException {
		this.interpreter.terminate();
	}

	@Override
	public String getLabel() {
		return this.label;
	}

	@Override
	public ILaunch getLaunch() {
		return this.launch;
	}

	@Override
	public IStreamsProxy getStreamsProxy() {
		return this.proxy;
	}

	@Override
	public void setAttribute(String key, String value) {
		this.attributes.put(key, value);
	}

	@Override
	public String getAttribute(String key) {
		return attributes.get(key);
	}

	@Override
	public int getExitValue() throws DebugException {
		return 0;
	}
	
	class ProcessListener implements InterpreterListener {
		
		private volatile boolean isStopped = false;
		private volatile boolean isSuspended = false;
		private List<DebugElement> debugElements = Collections.synchronizedList(new ArrayList<DebugElement>(2));
		private volatile InterpreterState suspendedState = null;
		
		void addEventSourceElement(DebugElement element) {
			if (!this.debugElements.contains(element)) {
				this.debugElements.add(element);
			}
		}
		
		void removeEventSourceElement(DebugElement element) {
			this.debugElements.remove(element);
		}

		int getInstructionPointer() {
			if (this.isSuspended && this.suspendedState != null) {
				return this.suspendedState.instructionPointer();
			}
			else {
				return -1;
			}
		}
		
		InterpreterState getSuspendedState() {
			if (this.isSuspended) {
				return this.suspendedState;
			}
			else {
				return null;
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
		}

		@Override
		public void interpreterSuspended(InterpreterState state, List<EventReason> eventReasons) {
			this.suspendedState = state;
			this.isSuspended = true;
			synchronized (this.debugElements) {
				for (DebugElement element : this.debugElements) {
					if (eventReasons.contains(EventReason.BreakPoint) || eventReasons.contains(EventReason.WatchPoint)) {
						element.fireSuspendEvent(DebugEvent.BREAKPOINT);
					}
					if (eventReasons.contains(EventReason.ClientRequest)) {
						element.fireSuspendEvent(DebugEvent.CLIENT_REQUEST);
					}
					if (eventReasons.contains(EventReason.StepEnd)) {
						element.fireSuspendEvent(DebugEvent.STEP_END);
					}
				}
			}
		}

		boolean isSuspended() {
			return this.isSuspended;
		}
		
		@Override
		public void interpreterResumed(InterpreterState state) {
			this.isSuspended = false;
		}

		@Override
		public void interpreterFinished(InterpreterState state, List<EventReason> reasons) {
			this.isStopped = true;
			this.isSuspended = false;
			IDebugTarget target = null;
			if (DebugPlugin.getDefault() != null) {
				synchronized (this.debugElements) {
					for (DebugElement element : this.debugElements) {
						element.fireTerminateEvent();
						target = element.getDebugTarget();
					}
				}
				DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[]{new DebugEvent(BfProcess.this, DebugEvent.TERMINATE)});
				if (target != null && target instanceof BfDebugTarget) {
					((BfDebugTarget) target).fireTerminateEvent();
				}
			}
		}
		
		boolean isTerminated() {
			return this.isStopped;
		}

		@Override
		public void interpreterStarted(InterpreterState state) {
			DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[]{new DebugEvent(BfProcess.this, DebugEvent.CHANGE)});
		}
	}
	
	/**
	 * @author Richard Birenheide
	 *
	 */
	private static class BfStreamsProxy implements IStreamsProxy {
		
		private final BfStreamMonitor output;
		private final BfStreamMonitor error;
		private final PrintStream pipedPrintStream;
		private final InputStream pipedInputStream;
		
		
		BfStreamsProxy(boolean createOutputStream, boolean createInputStream, String encoding) throws IOException {
			if (createOutputStream) {
				this.output = new BfStreamMonitor(encoding);
			}
			else {
				this.output = null;
			}
			this.error = new BfStreamMonitor(encoding);
			
			if (createInputStream) {
				PipedInputStream is = new PipedInputStream(1);
				PipedOutputStream os = new PipedOutputStream(is);
				this.pipedPrintStream = new PrintStream(os, true, encoding);
				this.pipedInputStream = is;
			}
			else {
				this.pipedInputStream = null;
				this.pipedPrintStream = null;
			}
		}
		
		BfOutputStream getOutputStream() {
			return this.output!= null ? this.output.getStream() : null;
		}
		
		BfOutputStream getErrorStream() {
			return this.error.getStream();
		}
		
		InputStream getInputStream() {
			return this.pipedInputStream;
		}

		@Override
		public IStreamMonitor getErrorStreamMonitor() {
			return this.error;
		}

		@Override
		public IStreamMonitor getOutputStreamMonitor() {
			return this.output;
		}

		@Override
		public void write(String input) throws IOException {
			if (this.pipedPrintStream != null) {
				this.pipedPrintStream.print(input);
				this.pipedPrintStream.flush();
			}
		}
	}
	
	/**
	 * @author Richard Birenheide
	 *
	 */
	private static class BfStreamMonitor implements IStreamMonitor {
		
		private final BfOutputStream stream;
		
		BfStreamMonitor(String encoding) {
			this.stream = new BfOutputStream(this, encoding);
		}
		
		BfOutputStream getStream() {
			return this.stream;
		}

		@Override
		public void addListener(IStreamListener listener) {
			this.stream.addListener(listener);
		}

		@Override
		public String getContents() {
			synchronized (this.stream) {
				String content = this.stream.getContent();
//				System.out.println("Content: " + content);
				return content;
			}
		}

		@Override
		public void removeListener(IStreamListener listener) {
			this.stream.removeListener(listener);
		}
		
	}
	
	/**
	 * @author Richard Birenheide
	 *
	 */
	private static class BfOutputStream extends ByteArrayOutputStream {

		private static long MAX_WAIT_TIME = 5000;
		
		private final IStreamMonitor monitor;
		private final List<IStreamListener> listeners = new ArrayList<IStreamListener>(1);
		private final Object lock = new Object();
		private final String encoding;
		private boolean autoFlush;
		private String content = null;
		private int flushLength = 0;
		
		BfOutputStream(IStreamMonitor monitor, String encoding) {
			this.monitor = monitor;
			this.encoding = encoding;
		}
		
		void waitForListenerAdded() {
			synchronized (this) {
				if (!this.listeners.isEmpty()) {
					return;
				}
			}
			synchronized(this.lock) {
				try {
					this.lock.wait(MAX_WAIT_TIME);
				} 
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		synchronized String getContent() {
			if (content != null) {
				return content;
			}
			else {
				try {
					return this.toString(encoding);
				} 
				catch (UnsupportedEncodingException ex) {
					return this.toString();
				}
			}
		}
		
		void setAutoFlush(boolean autoFlush) {
			this.autoFlush = autoFlush;
		}
		
		boolean isAutoFlush() {
			return this.autoFlush;
		}
		
		public synchronized void addListener(IStreamListener listener) {
			if (!this.listeners.contains(listener)) {
				this.listeners.add(listener);
			}
			try {
				this.content = this.toString(encoding);
			} 
			catch (UnsupportedEncodingException ex) {
				this.content = this.toString();
			}
			synchronized (this.lock) {
				this.lock.notify();
			}
		}
		
		public synchronized void removeListener(IStreamListener listener) {
			this.listeners.remove(listener);
		}

		@Override
		public synchronized void write(int b) {

			super.write(b);
			if (this.autoFlush) {
				try {
					this.flush();
				} 
				catch (IOException ex) {
					DbgActivator.getDefault().logError("Stream could not be autoflushed", ex);
				}
			}
		}

		@Override
		public synchronized void write(byte[] b, int off, int len) {
			super.write(b, off, len);
			if (this.autoFlush) {
				try {
					this.flush();
				} 
				catch (IOException ex) {
					DbgActivator.getDefault().logError("Stream could not be autoflushed", ex);
				}
			}
		}

		@Override
		public synchronized void flush() throws IOException {
			super.flush();
			String cont;
			try {
				cont = this.toString(encoding);
				
			} 
			catch (UnsupportedEncodingException ex) {
				DbgActivator.getDefault().logError("Encoding problem", ex);
				cont = this.toString();
			}
			String appended = cont.substring(flushLength);
			flushLength = cont.length();
			if (this.content != null) {
				this.reset();
				flushLength = 0;
			}
			
//			System.out.println(oldLength + ":" + this.toString() + ":" + appended + ":" + this.listeners.size());
			for (IStreamListener l : this.listeners) {
				l.streamAppended(appended, monitor);
			}
		}
	}
}
