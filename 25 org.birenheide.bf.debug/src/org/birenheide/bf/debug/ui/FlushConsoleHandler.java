package org.birenheide.bf.debug.ui;

import org.birenheide.bf.debug.core.ConsoleStreamFlusher;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.console.IConsoleView;

public class FlushConsoleHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		ConsoleStreamFlusher flusher = this.getFlusher(event.getApplicationContext());
		if (flusher != null && flusher.canFlush()) {
			flusher.flush();
		}
		return null;
	}

	@Override
	public void setEnabled(Object evaluationContext) {
		ConsoleStreamFlusher flusher = this.getFlusher(evaluationContext);
		if (flusher != null) {
			this.setBaseEnabled(flusher.canFlush());
		}
		else {
			this.setBaseEnabled(false);
		}
	}
	
	private ConsoleStreamFlusher getFlusher(Object context) {
		if (context instanceof IEvaluationContext) {
			IEvaluationContext evaluationContext = (IEvaluationContext) context;
			Object o = evaluationContext.getVariable(ISources.ACTIVE_PART_NAME);
			if (!(o instanceof IWorkbenchPart)) {
				return null;
			}
			IWorkbenchPart part = (IWorkbenchPart) o;
			if (part instanceof IConsoleView && ((IConsoleView) part).getConsole() instanceof IConsole) {
				IConsole activeConsole = (IConsole) ((IConsoleView) part).getConsole();
				IProcess process = activeConsole.getProcess();
				return (ConsoleStreamFlusher) process.getAdapter(ConsoleStreamFlusher.class);
			}
		}
		return null;
	}

}
