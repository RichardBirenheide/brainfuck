package org.birenheide.bf.debug.ui;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.birenheide.bf.debug.core.BfBreakpoint;
import org.birenheide.bf.debug.core.BfWatchpoint;
import org.eclipse.debug.ui.IDetailPane;
import org.eclipse.debug.ui.IDetailPaneFactory;
import org.eclipse.jface.viewers.IStructuredSelection;

public class BfBreakpointDetailFactory implements IDetailPaneFactory {
	

	@Override
	public Set<String> getDetailPaneTypes(IStructuredSelection selection) {
		if (selection.getFirstElement() instanceof BfWatchpoint) {
			return new TreeSet<>(Arrays.asList(BfWatchpointDetailPane.ID));
		}
		else if (selection.getFirstElement() instanceof BfBreakpoint) {
			return new TreeSet<>(Arrays.asList(BfBreakpointDetailPane.ID));
		}
		return Collections.emptySet();
	}

	@Override
	public String getDefaultDetailPane(IStructuredSelection selection) {
		if (selection.getFirstElement() instanceof BfWatchpoint) {
			return BfWatchpointDetailPane.ID;
		}
		else if (selection.getFirstElement() instanceof BfBreakpoint) {
			return BfBreakpointDetailPane.ID;
		}
		return null;
	}

	@Override
	public IDetailPane createDetailPane(String paneID) {
		if (paneID.equals(BfWatchpointDetailPane.ID)) {
			return new BfWatchpointDetailPane();
		}
		else if (paneID.equals(BfBreakpointDetailPane.ID)) {
			return new BfBreakpointDetailPane();
		}
		return null;
	}

	@Override
	public String getDetailPaneName(String paneID) {
		if (paneID.equals(BfWatchpointDetailPane.ID)) {
			return new BfWatchpointDetailPane().getName();
		}
		else if (paneID.equals(BfBreakpointDetailPane.ID)) {
			return new BfBreakpointDetailPane().getName();
		}
		return null;
	}

	@Override
	public String getDetailPaneDescription(String paneID) {
		return null;
	}

}
