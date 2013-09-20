package org.birenheide.bf.debug.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.birenheide.bf.BfActivator;
import org.birenheide.bf.BrainfuckInterpreter;
import org.birenheide.bf.EventReason;
import org.birenheide.bf.InterpreterListener;
import org.birenheide.bf.InterpreterState;
import org.birenheide.bf.debug.ui.BfMainTab;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.DebugElement;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;

public class BfProcess implements IProcess {
	
	private final Map<String, String> attributes = new TreeMap<>();
	private final String label;
	private final ILaunch launch;
	private final BrainfuckInterpreter interpreter;
	private final ProcessListener listener = new ProcessListener();
	private final IStreamsProxy proxy;

	public BfProcess(ILaunch launch, String label, Map<String, String> attrs) throws CoreException {
		this.label = label;
		this.launch = launch;
		if (attrs != null) {
			this.attributes.putAll(attrs);
		}
		
		BfStreamsProxy proxy = new BfStreamsProxy(true, false);
		final BfOutputStream outStream = proxy.getOutputStream();
		PrintStream out = new PrintStream(outStream);
		this.proxy = proxy;
		
		ILaunchConfiguration config = launch.getLaunchConfiguration();
		String projectName = config.getAttribute(BfMainTab.PROJECT_ATTR, "");
		String fileName = config.getAttribute(BfMainTab.FILE_ATTR, "");
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if (project == null) {
			throw new CoreException(new Status(IStatus.ERROR, BfActivator.BUNDLE_SYMBOLIC_NAME, "Project does not exist: " + projectName));
		}
		IFile file = project.getFile(fileName);
		if (!file.exists()) {
			throw new CoreException(new Status(IStatus.ERROR, BfActivator.BUNDLE_SYMBOLIC_NAME, "File does not exist: " + file.toString()));
		}
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
			throw new CoreException(new Status(IStatus.ERROR, BfActivator.BUNDLE_SYMBOLIC_NAME, e.getMessage(), e));
		}
		String code = new String(bos.toByteArray(), Charset.forName("UTF-8"));
		this.interpreter = new BrainfuckInterpreter(code.toCharArray(), out, System.in);
		this.interpreter.addListener(listener);
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
				interpreter.run();
			}
		}, "Brainfuck");
		t.setDaemon(true);
		t.start();
	}

	BrainfuckInterpreter getInterpreter() {
		return this.interpreter;
	}
	
	ProcessListener getProcessListener() {
		return this.listener;
	}
	
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
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
		private volatile int suspendedInstructionPointer = -1;
		private List<EventReason> lastSuspendedReasons = Collections.emptyList();
		
		void addEventSourceElement(DebugElement element) {
			if (!this.debugElements.contains(element)) {
				this.debugElements.add(element);
			}
		}
		
		void removeEventSourceElement(DebugElement element) {
			this.debugElements.remove(element);
		}

		int getInstructionPointer() {
			if (this.isSuspended) {
				return this.suspendedInstructionPointer;
			}
			else {
				return -1;
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
			this.suspendedInstructionPointer = state.instructionPointer();
			this.lastSuspendedReasons = eventReasons;
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
		
		List<EventReason> lastSuspendedEventReasons() {
			return this.lastSuspendedReasons;
		}

		boolean isSuspended() {
			return this.isSuspended;
		}
		
		@Override
		public void interpreterResumed(InterpreterState state) {
			this.isSuspended = false;
		}

		@Override
		public void interpreterFinished(InterpreterState state) {
			this.isStopped = true;
			this.isSuspended = false;
			DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[]{new DebugEvent(BfProcess.this, DebugEvent.TERMINATE)});
		}
		
		boolean isTerminated() {
			return this.isStopped;
		}

		@Override
		public void interpreterStarted(InterpreterState state) {
			DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[]{new DebugEvent(BfProcess.this, DebugEvent.CHANGE)});
		}
	}
	
	private static class BfStreamsProxy implements IStreamsProxy {
		
		private final BfStreamMonitor output;
		private final IStreamMonitor error;
		
		BfStreamsProxy(boolean createOutputStream, boolean createInputStream) {
			if (createOutputStream) {
				this.output = new BfStreamMonitor();
			}
			else {
				this.output = null;
			}
			this.error = null;
		}
		
		BfOutputStream getOutputStream() {
			return this.output.getStream();
		}

		@Override
		public IStreamMonitor getErrorStreamMonitor() {
			//TODO connect interpreter exception handling here
			return this.error; //Brainfuck does not support Error Stream
		}

		@Override
		public IStreamMonitor getOutputStreamMonitor() {
			return this.output;
		}

		@Override
		public void write(String input) throws IOException {
			// TODO Auto-generated method stub
			
		}

	}
	
	private static class BfStreamMonitor implements IStreamMonitor {
		
		private final BfOutputStream stream;
		
		BfStreamMonitor() {
			this.stream = new BfOutputStream(this);
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
	
	private static class BfOutputStream extends ByteArrayOutputStream {

		private static long MAX_WAIT_TIME = 5000;
		
		private final IStreamMonitor monitor;
		private final List<IStreamListener> listeners = new ArrayList<IStreamListener>(1);
		private final Object lock = new Object();
		private String content = null;
		
		BfOutputStream(IStreamMonitor monitor) {
			this.monitor = monitor;
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
				return this.toString();
			}
		}
		
		public synchronized void addListener(IStreamListener listener) {
			if (!this.listeners.contains(listener)) {
				this.listeners.add(listener);
			}
			this.content = this.toString();
			synchronized (this.lock) {
				this.lock.notify();
			}
		}
		
		public synchronized void removeListener(IStreamListener listener) {
			this.listeners.remove(listener);
		}

		@Override
		public synchronized void write(int b) {
			int oldLength = this.toString().length();
			super.write(b);
			String appended = this.toString().substring(oldLength);
//			System.out.println(oldLength + ":" + this.toString() + ":" + appended + ":" + this.listeners.size());
			for (IStreamListener l : this.listeners) {
				l.streamAppended(appended, monitor);
			}
		}

		@Override
		public synchronized void write(byte[] b, int off, int len) {
			int oldLength = this.toString().length();
			super.write(b, off, len);
			String appended = this.toString().substring(oldLength);
//			System.out.println(oldLength + ":" + this.toString() + ":" + appended + ":" + this.listeners.size());
			for (IStreamListener l : this.listeners) {
				l.streamAppended(appended, monitor);
			}
		}
		
	}

}
