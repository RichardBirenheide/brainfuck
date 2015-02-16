package org.birenheide.bf.debug.core;

import org.birenheide.bf.BfActivator;
import org.birenheide.bf.MemoryWatchpoint;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.Breakpoint;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IWatchpoint;

public class BfWatchpoint extends Breakpoint implements IWatchpoint, IBfBreakpoint, MemoryWatchpoint {
	
	public static final String MARKER_TYPE = BfActivator.BUNDLE_SYMBOLIC_NAME + ".brainfuckWatchpointMarker";
	public static final String ID = BfActivator.BUNDLE_SYMBOLIC_NAME + ".brainfuckWatchpoint";
	
	private static final String MEMORY_LOC_ATTRIBUTE = "MemoryLocation";
	private static final String MEMORY_VAL_ATTRIBUTE = "MemoryValue";
	private static final String SUSPEND_ON_ACCESS = "SuspendOnAccess";
	private static final String SUSPEND_ON_MODIFICATION = "SuspendOnModification";

	private static String generateMessageText(BfWatchpoint wp) {
		String valueString = "value: ";
		if (wp.suspendOnModification()) {
			valueString += "any";
		}
		else {
			valueString += "0x" + Integer.toHexString(wp.getValue() & 0xFF).toUpperCase();
		}
		String accessString = "";
		if (wp.suspendOnAccess()) {
			accessString = "; access";
		}
		return "Brainfuck Watchpoint [mp: 0x" + Integer.toHexString(wp.getLocation()).toUpperCase() + "; " + valueString + accessString + "]";
	}
	
	public BfWatchpoint() {
	}
	
	public BfWatchpoint(int memoryLoc, byte value, boolean suspendOnAccess, boolean suspendOnModification) throws CoreException {
		IMarker wpMarker = ResourcesPlugin.getWorkspace().getRoot().createMarker(MARKER_TYPE);
		wpMarker.setAttribute(MEMORY_LOC_ATTRIBUTE, memoryLoc);
		wpMarker.setAttribute(MEMORY_VAL_ATTRIBUTE, (int) value);
		wpMarker.setAttribute(SUSPEND_ON_ACCESS, suspendOnAccess);
		wpMarker.setAttribute(SUSPEND_ON_MODIFICATION, suspendOnModification);
		wpMarker.setAttribute(IBreakpoint.ID, BfDebugElement.MODEL_IDENTIFIER);
		this.setMarker(wpMarker);
		wpMarker.setAttribute(IMarker.MESSAGE, generateMessageText(this));
		this.setPersisted(true);
		this.setEnabled(true);
	}

	@Override
	public String getModelIdentifier() {
		return BfDebugTarget.MODEL_IDENTIFIER;
	}

	@Override
	public boolean isAccess() throws CoreException {
		return this.getMarker().getAttribute(SUSPEND_ON_ACCESS, false);
	}

	@Override
	public void setAccess(boolean access) throws CoreException {
		this.getMarker().setAttribute(SUSPEND_ON_ACCESS, access);
		this.getMarker().setAttribute(IMarker.MESSAGE, generateMessageText(this));
	}

	@Override
	public boolean isModification() throws CoreException {
		return this.getMarker().getAttribute(SUSPEND_ON_MODIFICATION, false);
	}

	@Override
	public void setModification(boolean modification) throws CoreException {
		this.getMarker().setAttribute(SUSPEND_ON_MODIFICATION, modification);
		this.getMarker().setAttribute(IMarker.MESSAGE, generateMessageText(this));
	}

	@Override
	public boolean supportsAccess() {
		return true;
	}

	@Override
	public boolean supportsModification() {
		return true;
	}

	@Override
	public boolean suspendOnAccess() {
		return this.getMarker().getAttribute(SUSPEND_ON_ACCESS, false);
	}

	@Override
	public boolean suspendOnModification() {
		return this.getMarker().getAttribute(SUSPEND_ON_MODIFICATION, false);
	}

	@Override
	public int getLocation() {
		return this.getMarker().getAttribute(MEMORY_LOC_ATTRIBUTE, -1);
	}
	
	public byte getValue() {
		int value = this.getMarker().getAttribute(MEMORY_VAL_ATTRIBUTE, -1);
		return (byte) value;
	}
	
	public String getMessage() {
		return this.getMarker().getAttribute(IMarker.MESSAGE, "Failed");
	}

	@Override
	public String toString() {
		return generateMessageText(this);
	}
}
