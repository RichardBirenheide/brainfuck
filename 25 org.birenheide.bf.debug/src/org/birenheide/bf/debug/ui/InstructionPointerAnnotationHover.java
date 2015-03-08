package org.birenheide.bf.debug.ui;

import org.birenheide.bf.debug.DbgActivator;
import org.birenheide.bf.debug.core.BfStackFrame;
import org.birenheide.bf.ed.AnnotationHover;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.source.Annotation;

public class InstructionPointerAnnotationHover implements AnnotationHover {

	@Override
	public String getHoverText(Annotation annotation, ITextViewer textViewer, IRegion hoverRegion) {
		if (!BfDebugModelPresentation.INSTRUCTION_POINTER_ANNOTATION_TYPE.equals(annotation.getType())) {
			return null;
		}
		IAdaptable adaptable = DebugUITools.getDebugContext();
		if (adaptable instanceof BfStackFrame) {
			BfStackFrame stackFrame = (BfStackFrame) adaptable;
			try {
				int instructionPointer = stackFrame.getCharStart();
				String text = "Instruction Pointer: [<b>" + instructionPointer + "</b>]";
				int memoryPointer = stackFrame.getMemoryPointer();
				IMemoryBlock memoryBlock = stackFrame.getDebugTarget().getMemoryBlock(memoryPointer, 1);
				byte value = memoryBlock.getBytes()[0];
				text = text + "<br>Memory Value: [<b>0x" + Integer.toHexString(memoryPointer).toUpperCase() + "</b>]=<b>0x" + Integer.toHexString((value & 0xFF)) + "</b>";
				return text;
			} 
			catch (DebugException ex) {
				DbgActivator.getDefault().logError("Memory Block could not be evaluated", ex);
			}
		}
		return null;
	}
}
