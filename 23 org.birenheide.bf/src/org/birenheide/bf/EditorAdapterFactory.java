package org.birenheide.bf;
import org.birenheide.bf.debug.ui.BfToggleBreakpointsTarget;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;


public class EditorAdapterFactory implements IAdapterFactory {
	
	private static final Class<?>[] ADAPTER_TYPES = new Class<?>[]{IToggleBreakpointsTarget.class};

	@Override
	public Object getAdapter(Object adaptableObject, @SuppressWarnings("rawtypes") Class adapterType) {
		if (adapterType.equals(IToggleBreakpointsTarget.class)) {
			return new BfToggleBreakpointsTarget();
		}
		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return ADAPTER_TYPES;
	}

}
