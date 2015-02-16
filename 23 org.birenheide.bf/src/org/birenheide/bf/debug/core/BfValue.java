package org.birenheide.bf.debug.core;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;

class BfValue extends BfDebugElement implements IValue {
	
	private final int value;
	private final boolean showHex;

	public BfValue(BfDebugTarget target, int value, boolean showHex) {
		super(target);
		this.value = value;
		this.showHex = showHex;
	}

	@Override
	public String getReferenceTypeName() throws DebugException {
		return "int";
	}

	@Override
	public String getValueString() throws DebugException {
		if (showHex) {
			return "0x" + Integer.toHexString(value);
		}
		return Integer.toString(value);
	}

	@Override
	public boolean isAllocated() throws DebugException {
		return true;
	}

	@Override
	public IVariable[] getVariables() throws DebugException {
		return new IVariable[0];
	}

	@Override
	public boolean hasVariables() throws DebugException {
		return false;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + value;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BfValue other = (BfValue) obj;
		if (value != other.value)
			return false;
		return true;
	}
}