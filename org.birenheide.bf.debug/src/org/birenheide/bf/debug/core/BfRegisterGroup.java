package org.birenheide.bf.debug.core;

import org.birenheide.bf.debug.core.BfRegister.IpRegister;
import org.birenheide.bf.debug.core.BfRegister.MpRegister;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IRegister;
import org.eclipse.debug.core.model.IRegisterGroup;

class BfRegisterGroup extends BfDebugElement implements IRegisterGroup {
	
	private final IRegister[] registers;

	public BfRegisterGroup(BfDebugTarget target) {
		super(target);
		IRegister instruction = new IpRegister(getDebugTarget(), this);
		IRegister memory = new MpRegister(getDebugTarget(), this);
		this.registers = new IRegister[]{instruction, memory};
	}

	@Override
	public String getName() throws DebugException {
		return "Pointers";
	}

	@Override
	public IRegister[] getRegisters() throws DebugException {
		return this.registers;
	}

	@Override
	public boolean hasRegisters() throws DebugException {
		return true;
	}
}