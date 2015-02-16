package org.birenheide.bf.debug.ui;

import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.texteditor.IAnnotationImageProvider;

public class InstructionPointerAnnotationImageProvider implements IAnnotationImageProvider {

	@Override
	public Image getManagedImage(Annotation annotation) {
		if (BfDebugModelPresentation.INSTRUCTION_POINTER_ANNOTATION_TYPE.equals(annotation.getType())) {
			return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_INSTRUCTION_POINTER_TOP);
		}
		return null;
	}

	@Override
	public String getImageDescriptorId(Annotation annotation) {
		return null;
	}

	@Override
	public ImageDescriptor getImageDescriptor(String imageDescriptorId) {
		return null;
	}

}
