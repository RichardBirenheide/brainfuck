package org.birenheide.bf;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public abstract class AbstractBfActivator extends AbstractUIPlugin {

	public AbstractBfActivator() {
		super();
	}

	public void logError(String message, Throwable cause) {
	
	    this.log(message, cause, IStatus.ERROR);
	}

	public void logInfo(String message, Throwable cause) {
	
	    this.log(message, cause, IStatus.INFO);
	}

	public void logWarning(String message, Throwable cause) {
	
	    this.log(message, cause, IStatus.WARNING);
	}

	private void log(String message, Throwable cause, int severity) {
	
	    String pluginId = this.getBundle() != null ? this.getBundle().getSymbolicName() : getDefaultPluginId();
	    if (message == null) {
	        message = cause != null ? cause.getMessage() : "";
	    }
	    if (cause != null) {
	        this.getLog().log(new Status(severity, pluginId, message, cause));
	        if (this.isDebugging()) {
	            System.err.println("Message: " + message);
	            cause.printStackTrace();
	        }
	    }
	    else {
	        this.getLog().log(new Status(severity, pluginId, message));
	        if (this.isDebugging()) {
	            System.err.println("Message: " + message);
	        }
	    }
	}
	
	protected abstract String getDefaultPluginId();

}