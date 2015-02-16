package org.birenheide.bf.debug.ui;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.birenheide.bf.BfActivator;
import org.birenheide.bf.debug.core.BfBreakpoint;
import org.birenheide.bf.debug.core.BfDebugTarget;
import org.birenheide.bf.debug.core.BfStackFrame;
import org.birenheide.bf.debug.core.BfWatchpoint;
import org.birenheide.bf.ed.BfEditor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugEditorPresentation;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IInstructionPointerPresentation;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.part.FileEditorInput;

public class BfDebugModelPresentation implements 
										IDebugModelPresentation, 
										IDebugEditorPresentation,
										IInstructionPointerPresentation {
	
	public static final String INSTRUCTION_POINTER_ANNOTATION_TYPE = "org.birenheide.bf.debug.currentIP";
	
	private final Map<ImageDescriptor, Image> overlayedImages = new HashMap<>();
	private final List<Image> disposeImages = new ArrayList<>(5);
	
	public BfDebugModelPresentation() {
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
		System.out.println("Label listener added: " + listener);
	}

	@Override
	public void dispose() {
		this.overlayedImages.clear();
		for (Image baseImage : this.disposeImages) {
			baseImage.dispose();
		}
		this.disposeImages.clear();
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
//		System.out.println(element + ":" + property);
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {

	}

	@Override
	public IEditorInput getEditorInput(Object element) {
		if (element instanceof IFile) {
			return new FileEditorInput((IFile) element);
		}
		else if (element instanceof BfBreakpoint) {
			return new FileEditorInput((IFile)((BfBreakpoint) element).getMarker().getResource());
		}
		return null;
	}

	@Override
	public String getEditorId(IEditorInput input, Object element) {
		if (element instanceof IFile) {
			if (((IFile) element).getFileExtension().equals("bf")) {
				return BfEditor.EDITOR_ID;
			}
		}
		else if (element instanceof BfBreakpoint) {
			return BfEditor.EDITOR_ID;
		}
		return EditorsUI.DEFAULT_TEXT_EDITOR_ID;
	}

	@Override
	public void setAttribute(String attribute, Object value) {
		System.out.println(attribute + ":" + value);
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof BfDebugTarget) {
			BfDebugTarget target = (BfDebugTarget) element;
			if ((target.hasCodeBeenReplaced() || !target.isInSync()) && !target.isTerminated()) {
				return this.getDebugTargetWarningImage(target);
			}
		}
		else if (element instanceof BfBreakpoint) {
			BfBreakpoint breakPoint = (BfBreakpoint) element;
			if (breakPoint.isInstalled()) {
				return this.getInstalledBreakpointImage(breakPoint);
			}
		}
		return null;
	}

	@Override
	public String getText(Object element) {
//		System.out.println(element);
		if (element instanceof BfBreakpoint) {
			BfBreakpoint bp = (BfBreakpoint) element;
			try {
				String result = bp.getMarker().getResource().getName() + " [ip: " + bp.getCharStart() + "]";
				return result;
			} 
			catch (CoreException e) {
				BfActivator.getDefault().logError("Marker Resource", e);
			}
		}
		else if (element instanceof BfWatchpoint) {
			BfWatchpoint wp = (BfWatchpoint) element;
			return wp.getMessage();
		}
		else if (element instanceof BfDebugTarget) {
			BfDebugTarget target = (BfDebugTarget) element;
			if (target.isTerminated()) {
				return null; //Default text
			}
			try {
				String text = target.getName();
				if (!target.isInSync()) {
					return text + " (out of sync)";
				}
				else if (target.hasCodeBeenReplaced()) {
					return text + " (code replaced)";
				}
			} 
			catch (DebugException ex) {
				BfActivator.getDefault().logError("Name could not be retrieved", ex);
			}
		}
		return null;
	}

	@Override
	public void computeDetail(IValue value, IValueDetailListener listener) {
		
	}

	@Override
	public boolean addAnnotations(IEditorPart editorPart, IStackFrame frame) {
		/*
		 * The color of the default annotation can be set with 
		 * "Window->Preferences->General->Editors->Text Editors->Annotations" 
		 * at Debug Call Stack.
		 */
		if (editorPart instanceof BfEditor) {
			@SuppressWarnings("unused")
			BfEditor editor = (BfEditor) editorPart;
		}
		return false;
	}

	@Override
	public void removeAnnotations(IEditorPart editorPart, IThread thread) {
	}

	@Override
	public Annotation getInstructionPointerAnnotation(IEditorPart editorPart,
			IStackFrame frame) {
		return null;
	}

	@Override
	public String getInstructionPointerAnnotationType(IEditorPart editorPart,
			IStackFrame frame) {
//		if (editorPart instanceof BfEditor) {
//			BfEditor editor = (BfEditor) editorPart;
//			editor.get
//		}
		/*
		 * Because of bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=459664
		 * this annotation MUST define an own IAnnotationImageProvider.
		 */
		return INSTRUCTION_POINTER_ANNOTATION_TYPE;
	}

	@Override
	public Image getInstructionPointerImage(IEditorPart editorPart,
			IStackFrame frame) {
		return null;
	}

	@Override
	public String getInstructionPointerText(IEditorPart editorPart,
			IStackFrame frame) {
//		AnnotationPreference preference = EditorsUI.getAnnotationPreferenceLookup().getAnnotationPreference(INSTRUCTION_POINTER_ANNOTATION_TYPE);
//		String text = preference.getPreferenceLabel();
		String text = "Instruction Pointer";
		if (frame instanceof BfStackFrame) {
			try {
				int location = ((BfStackFrame) frame).getCharStart();
				text = text + " [" + location + "]";
			} 
			catch (DebugException ex) {
				BfActivator.getDefault().logError("Instruction Pointer location could not be revealed", ex);
			}
		}
		return text;
	}
	
	private Image getInstalledBreakpointImage(BfBreakpoint breakpoint) {
		ImageDescriptor defaultImageDescriptor = DebugUITools.getDefaultImageDescriptor(breakpoint);
		if (this.overlayedImages.containsKey(defaultImageDescriptor)) {
			return this.overlayedImages.get(defaultImageDescriptor);
		}
		try {
			ImageDescriptor overlay = ImageDescriptor.createFromURL(new URL("platform:/plugin/org.eclipse.ui.ide/icons/full/obj16/header_complete.png"));
			Image defaultImage = defaultImageDescriptor.createImage();
			this.disposeImages.add(defaultImage);
			ImageDescriptor overlayedDescriptor = new DecorationOverlayIcon(defaultImage, overlay, IDecoration.BOTTOM_LEFT);
			Image overlayedImage = overlayedDescriptor.createImage();
			this.disposeImages.add(overlayedImage);
			this.overlayedImages.put(defaultImageDescriptor, overlayedImage);
			return overlayedImage;
		} 
		catch (MalformedURLException ex) {
			BfActivator.getDefault().logError("URL malformed", ex);
			return null;
		}
	}
	
	private Image getDebugTargetWarningImage(BfDebugTarget debugTarget) {
		ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
		ImageDescriptor warningOverlay = sharedImages.getImageDescriptor(ISharedImages.IMG_DEC_FIELD_WARNING);
		ImageDescriptor targetDescriptor = DebugUITools.getDefaultImageDescriptor(debugTarget);
		if (this.overlayedImages.containsKey(targetDescriptor)) {
			return this.overlayedImages.get(targetDescriptor);
		}
		Image targetImage = targetDescriptor.createImage();
		this.disposeImages.add(targetImage);
		ImageDescriptor all = new DecorationOverlayIcon(targetImage, warningOverlay, IDecoration.TOP_LEFT);
		Image overlayedImage = all.createImage();
		this.disposeImages.add(overlayedImage);
		this.overlayedImages.put(targetDescriptor, overlayedImage);
		return overlayedImage;
	}
}
