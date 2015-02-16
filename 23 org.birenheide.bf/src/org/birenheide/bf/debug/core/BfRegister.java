package org.birenheide.bf.debug.core;

import org.birenheide.bf.InterpreterState;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IRegister;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IValue;

abstract class BfRegister extends BfDebugElement implements IRegister {

	private final BfRegisterGroup group;
	
	private int lastValue = -1;
	private InterpreterState lastState = null;
	private boolean valueChanged = false;
	
	BfRegister(BfDebugTarget target, BfRegisterGroup group) {
		super(target);
		this.group = group;
	}

	@Override
	public String getReferenceTypeName() throws DebugException {
		return "int";
	}

	@Override
	public void setValue(String expression) throws DebugException {
	}

	@Override
	public void setValue(IValue value) throws DebugException {
	}

	@Override
	public boolean supportsValueModification() {
		return false;
	}

	@Override
	public boolean verifyValue(String expression) throws DebugException {
		return false;
	}

	@Override
	public boolean verifyValue(IValue value) throws DebugException {
		return false;
	}

	@Override
	public IRegisterGroup getRegisterGroup() throws DebugException {
		return this.group;
	}
	
	@Override
	public boolean hasValueChanged() throws DebugException {
		InterpreterState currentState = this.getDebugTarget().getProcess().getProcessListener().getSuspendedState();
		int pointer = this.getIntValue();
		if (this.lastState == null) {
			this.lastState = currentState;
			this.lastValue = pointer;
			this.valueChanged = false;
			return false;
		}
		if (currentState != null) {
			if (!this.lastState.equals(currentState)) {
				this.valueChanged = this.lastValue != pointer;
				this.lastValue = pointer;
				this.lastState = currentState;
			}
		}
		return this.valueChanged;
	}
	
	@Override
	public IValue getValue() throws DebugException {
		InterpreterState lastState = this.getDebugTarget().getProcess().getProcessListener().getSuspendedState();
		int pointer = this.getIntValue();
		if (lastState != null && pointer > -1) {
			return new BfValue(this.getDebugTarget(), pointer, this.renderToHex());
		}
		return null;
	}
	
	abstract int getIntValue();
	abstract boolean renderToHex();
	
	/**
	 * @author Richard Birenheide
	 *
	 */
	static class IpRegister extends BfRegister {
		
		IpRegister(BfDebugTarget target, BfRegisterGroup group) {
			super(target, group);
		}

		@Override
		public String getName() throws DebugException {
			return "Instruction Pointer (ip)";
		}

		@Override
		int getIntValue() {
			InterpreterState state = this.getDebugTarget().getProcess().getProcessListener().getSuspendedState();
			if (state != null) {
				return state.instructionPointer();
			}
			return -1;
		}

		@Override
		boolean renderToHex() {
			return false;
		}

	
	}

	/**
	 * @author Richard Birenheide
	 *
	 */
	static class MpRegister extends BfRegister {
		
		MpRegister(BfDebugTarget target, BfRegisterGroup group) {
			super(target, group);
		}

		@Override
		public String getName() throws DebugException {
			return "Memory Pointer (mp)";
		}

		@Override
		int getIntValue() {
			InterpreterState state = this.getDebugTarget().getProcess().getProcessListener().getSuspendedState();
			if (state != null) {
				return state.dataPointer();
			}
			return -1;
		}

		@Override
		boolean renderToHex() {
			return true;
		}
	}
}