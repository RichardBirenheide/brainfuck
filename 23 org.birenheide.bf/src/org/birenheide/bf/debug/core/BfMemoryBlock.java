package org.birenheide.bf.debug.core;

import org.birenheide.bf.InterpreterState;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlock;

public class BfMemoryBlock extends BfDebugElement implements IMemoryBlock {
	
	private final InterpreterState state;
	private final long start;
	private final long length;
	private boolean createdbyUser = true;
	
	BfMemoryBlock(BfDebugTarget parent, InterpreterState state, long start, long length) {
		super(parent);
		this.state = state;
		long st = start;
		long l = length;
		if (st >= state.getDataSize()) {
			st = state.getDataSize();
			l = 0;
		}
		else if (st + l > state.getDataSize()) {
			l = state.getDataSize() - st;
		}
		this.start = st;
		this.length = l;
	}
	
	public boolean isUserCreated() {
		return this.createdbyUser;
	}
	
	public void setUserCreated(boolean isUserCreated) {
		this.createdbyUser = isUserCreated;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.createdbyUser ? 1231 : 1237);
		result = prime * result + (int) (this.length ^ (this.length >>> 32));
		result = prime * result + (int) (this.start ^ (this.start >>> 32));
		result = prime * result + this.getDebugTarget().hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		BfMemoryBlock other = (BfMemoryBlock) obj;
		if (!this.getDebugTarget().equals(other.getDebugTarget())) {
			return false;
		}
		if (this.createdbyUser != other.createdbyUser) {
			return false;
		}
		if (this.length != other.length) {
			return false;
		}
		if (this.start != other.start) {
			return false;
		}
		return true;
	}

	@Override
	public long getStartAddress() {
		return this.start;
	}
	
	public int getMemoryPointer() {
		return this.getDebugTarget().getProcess().getProcessListener().getSuspendedState().dataPointer();
	}

	@Override
	public long getLength() {
		return this.length;
	}

	@Override
	public byte[] getBytes() throws DebugException {
		return this.state.dataSnapShot((int) this.start, (int) (this.start + this.length));
	}

	@Override
	public boolean supportsValueModification() {
		return false;
	}

	@Override
	public void setValue(long offset, byte[] bytes) throws DebugException {
	}

	
	
}