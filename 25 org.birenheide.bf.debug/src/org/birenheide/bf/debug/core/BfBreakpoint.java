package org.birenheide.bf.debug.core;

import java.util.ArrayList;
import java.util.List;

import org.birenheide.bf.debug.DbgActivator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.LineBreakpoint;

public class BfBreakpoint extends LineBreakpoint implements IBfBreakpoint {
	
	public static final String MARKER_TYPE = DbgActivator.PLUGIN_ID + ".brainfuckBreakpointMarker";
	public static final String ID = DbgActivator.PLUGIN_ID + ".brainfuckBreakpoint";
	
	public static String generateMessageText(BfBreakpoint breakpoint) throws CoreException {
		int location = breakpoint.getCharStart();
		String state = "";
		if (!breakpoint.isEnabled()) {
			state = " disabled";
		}
		return "Breakpoint [ip: " + location + "]" + state;
	}

	private List<BfDebugTarget> installedTargets = new ArrayList<>();

	public BfBreakpoint(){} //Necessary for persistence!
	
	public BfBreakpoint(IFile file, int location, int line) throws CoreException {
		IMarker bpMarker = file.createMarker(BfBreakpoint.MARKER_TYPE);
		bpMarker.setAttribute(IMarker.CHAR_START, location);
		bpMarker.setAttribute(IMarker.CHAR_END, location + 1);
		bpMarker.setAttribute(IMarker.LINE_NUMBER, line);
		bpMarker.setAttribute(IMarker.TRANSIENT, false);
		bpMarker.setAttribute(IBreakpoint.ID, BfDebugElement.MODEL_IDENTIFIER);
		this.setMarker(bpMarker);
		this.setPersisted(true);
		this.setEnabled(true);
		bpMarker.setAttribute(IMarker.MESSAGE, generateMessageText(this));
	}
	
	@Override
	public String getModelIdentifier() {
		return BfDebugElement.MODEL_IDENTIFIER;
	}

	@Override
	public void setEnabled(boolean enabled) throws CoreException {
		super.setEnabled(enabled);
		this.getMarker().setAttribute(IMarker.MESSAGE, generateMessageText(this));
	}
	
	public boolean isInstalled() {
		return !this.installedTargets.isEmpty();
	}
	
	void setInstalled(boolean installed, BfDebugTarget target) {
		if (installed) {
			if (!this.installedTargets.contains(target)) {
				this.installedTargets.add(target);
				DebugPlugin.getDefault().getBreakpointManager().fireBreakpointChanged(this);
			}
		}
		else {
			if (this.installedTargets.remove(target)) {
				DebugPlugin.getDefault().getBreakpointManager().fireBreakpointChanged(this);
			}
		}
	}

	@Override
	public String toString() {
		
		try {
			return generateMessageText(this) + ": " + this.getMarker().getResource() + (this.isInstalled() ? ": installed":"");
		} 
		catch (CoreException ex) {
			return super.toString();
		}
	}
	
	
}
