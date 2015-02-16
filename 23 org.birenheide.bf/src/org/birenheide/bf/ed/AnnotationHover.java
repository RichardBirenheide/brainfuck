package org.birenheide.bf.ed;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.source.Annotation;

public interface AnnotationHover {

	public String getHoverText(Annotation annotation, ITextViewer textViewer, IRegion hoverRegion);
}
