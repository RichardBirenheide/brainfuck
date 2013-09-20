package org.birenheide.bf.debug.core;

import org.eclipse.debug.core.model.DebugElement;

public class BfDebugElement extends DebugElement {
	
	public static final String MODEL_IDENTIFIER = "org.birenheide.bf.debugModelPresentation";

	public BfDebugElement(BfDebugTarget target) {
		super(target);
	}

	@Override
	public String getModelIdentifier() {
		return MODEL_IDENTIFIER;
	}

	@Override
	public BfDebugTarget getDebugTarget() {
		return (BfDebugTarget) super.getDebugTarget();
	}

	
	
}
