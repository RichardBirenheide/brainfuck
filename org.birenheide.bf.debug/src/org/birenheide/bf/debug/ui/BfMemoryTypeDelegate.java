package org.birenheide.bf.debug.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.ui.memory.IMemoryRendering;
import org.eclipse.debug.ui.memory.IMemoryRenderingTypeDelegate;

public class BfMemoryTypeDelegate implements IMemoryRenderingTypeDelegate {


	@Override
	public IMemoryRendering createRendering(String id) throws CoreException {
		return new BfMemoryRendering(id);
	}

}
