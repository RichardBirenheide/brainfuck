package org.birenheide.bf.debug.ui;

import java.lang.reflect.Field;
import java.math.BigInteger;

import org.birenheide.bf.BfActivator;
import org.birenheide.bf.debug.core.BfMemoryBlock;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.MemoryByte;
import org.eclipse.debug.ui.memory.AbstractTableRendering;
import org.eclipse.debug.ui.memory.MemoryRenderingElement;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public class BfMemoryRendering extends AbstractTableRendering {

	public BfMemoryRendering(String renderingId) {
		super(renderingId);
	}

	@Override
	protected IColorProvider getColorProviderAdapter() {
		return new IColorProvider() {
			
			@Override
			public Color getForeground(Object element) {
				Color result = null;
				if (element instanceof MemoryRenderingElement) {
					MemoryRenderingElement mre = (MemoryRenderingElement) element;
					for (MemoryByte mb : mre.getBytes()) {
						if (mb.isHistoryKnown() && mb.isChanged()) {
							return (Display.getDefault().getSystemColor(SWT.COLOR_RED));
						}
					}
				}
				return result;
			}
			
			@Override
			public Color getBackground(Object element) {
				Color result = null;
				if (element instanceof MemoryRenderingElement) {
					MemoryRenderingElement mre = (MemoryRenderingElement) element;
					int memoryPointer = ((BfMemoryBlock)getMemoryBlock()).getMemoryPointer();
					int startAddress = mre.getAddress().intValue();
					if (memoryPointer >= startAddress && memoryPointer <= startAddress + mre.getBytes().length - 1) {
						result = Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
					}
				}
				return result;
			}
		};
	}

	@Override
	public void refresh() {
		super.refresh();
		this.revealMemoryPointer();
	}

	@Override
	public void becomesVisible() {
		super.becomesVisible();
		this.revealMemoryPointer();
	}

	@Override
	public String getString(String renderingTypeId, BigInteger address,
			MemoryByte[] data) {
		StringBuilder result = new StringBuilder();
		long maxAddress = this.getMemoryBlock().getStartAddress() + this.getMemoryBlock().getLength();
		int i = -1;
		for (MemoryByte mb : data) {
			i++;
			if (address.intValue() + i >= maxAddress) {
				result.append("??");
				continue;
			}
			int b = mb.getValue() & 0xFF;
			String val = Integer.toHexString(b).toUpperCase();
			String pre = "";
			if (val.length() == 1) {
				pre = "0";
			}
			result.append(pre).append(val);
		}
		return result.toString();
	}
	
	@Override
	public byte[] getBytes(String renderingTypeId, BigInteger address,
			MemoryByte[] currentValues, String newValue) {
		// TODO Needed when editing is allowed
		return null;
	}


	private void revealMemoryPointer() {
		int memoryPointer = ((BfMemoryBlock)getMemoryBlock()).getMemoryPointer();
		try {
			long maxLength = this.getMemoryBlock().getStartAddress() + this.getMemoryBlock().getLength();
			if (memoryPointer < maxLength) {
				this.goToAddress(BigInteger.valueOf(memoryPointer));
				for (Field f : this.getClass().getSuperclass().getDeclaredFields()) {
					if (f.getType().equals(TableCursor.class)) {
						f.setAccessible(true);
						TableCursor tc = (TableCursor) f.get(this);
						tc.setVisible(false);
					}
				}
			}
		} 
		catch (DebugException e) {
			BfActivator.getDefault().logError("Address: " + memoryPointer + " could not be shown", e);
		}
		catch (IllegalArgumentException | IllegalAccessException e) {
			BfActivator.getDefault().logError("Cursor cannot be set to invisible", e);
		}
	}

}
