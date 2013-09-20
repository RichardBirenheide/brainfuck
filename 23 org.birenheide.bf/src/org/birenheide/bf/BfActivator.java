package org.birenheide.bf;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class BfActivator extends AbstractUIPlugin {
	
	public static final String BUNDLE_SYMBOLIC_NAME = "org.birenheide.bf";
	
	private static BfActivator instance = null;
	
	public static BfActivator getDefault() {
		return instance;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		instance = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		instance = null;
		super.stop(context);
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

        String pluginId = this.getBundle() != null ? this.getBundle().getSymbolicName() : BUNDLE_SYMBOLIC_NAME;
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
	
}
