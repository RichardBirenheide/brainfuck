package org.birenheide.bf.debug.ui;

import org.birenheide.bf.BfActivator;
import org.birenheide.bf.debug.core.BfBreakpoint;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.texteditor.IMarkerUpdater;

public class BreakpointMarkerUpdater implements IMarkerUpdater {
	
	private static final String[] ATTRIBUTES = new String[]{IMarker.MESSAGE};
	
	@Override
	public String getMarkerType() {
		return BfBreakpoint.MARKER_TYPE;
	}

	@Override
	public String[] getAttribute() {
		return ATTRIBUTES;
	}

	@Override
	public boolean updateMarker(IMarker marker, IDocument document,	Position position) {
		try {
			IBreakpoint bp = DebugPlugin.getDefault().getBreakpointManager().getBreakpoint(marker);
			if (bp instanceof BfBreakpoint) {
				String newMessage = BfBreakpoint.generateMessageText((BfBreakpoint) bp);
				marker.setAttribute(IMarker.MESSAGE, newMessage);
			}
		} 
		catch (CoreException e) {
			BfActivator.getDefault().logError("Update Marker failed", e);
		}
		return true;
	}

}
