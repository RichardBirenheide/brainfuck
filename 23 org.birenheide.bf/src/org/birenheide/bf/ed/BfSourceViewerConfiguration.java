package org.birenheide.bf.ed;

import static org.birenheide.bf.BfActivator.BUNDLE_SYMBOLIC_NAME;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.birenheide.bf.BfActivator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.AbstractReusableInformationControlCreator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPartitioningException;
import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextHoverExtension2;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.information.IInformationPresenter;
import org.eclipse.jface.text.information.InformationPresenter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.DefaultAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.AnnotationPreferenceLookup;

class BfSourceViewerConfiguration extends TextSourceViewerConfiguration {
	
	private static final String UNCHANGED_QUICKDIFF_ANNOTATION = "org.eclipse.ui.workbench.texteditor.quickdiffUnchanged";
	
	BfSourceViewerConfiguration(IPreferenceStore editorPreferences) {
		super(editorPreferences);
	}

	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		
		PresentationReconciler reconciler = new PresentationReconciler();
		reconciler.setDocumentPartitioning(this.getConfiguredDocumentPartitioning(sourceViewer));
		
		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(new BfCodeScanner(this.fPreferenceStore));
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
		
		dr = new DefaultDamagerRepairer(new BfCodeScanner(fPreferenceStore));
		reconciler.setDamager(dr, EditorConstants.PARTITION_TYPE_BRAINFUCK_CODE);
		reconciler.setRepairer(dr, EditorConstants.PARTITION_TYPE_BRAINFUCK_CODE);
		
		dr = new DefaultDamagerRepairer(new SingleColorScanner(fPreferenceStore, EditorConstants.PREF_EDITOR_COMMENT_CHAR_COLOR));
		reconciler.setDamager(dr, EditorConstants.PARTITION_TYPE_MULTILINE_COMMENT);
		reconciler.setRepairer(dr, EditorConstants.PARTITION_TYPE_MULTILINE_COMMENT);
		
		dr = new DefaultDamagerRepairer(new SingleColorScanner(fPreferenceStore, EditorConstants.PREF_EDITOR_TEMPLATE_PARAMS_COLOR));
		reconciler.setDamager(dr, EditorConstants.PARTITION_TYPE_TEMPLATE_PARAMETERS);
		reconciler.setRepairer(dr, EditorConstants.PARTITION_TYPE_TEMPLATE_PARAMETERS);
		
		return reconciler;
	}

	@Override
	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
		DefaultAnnotationHover annHover = new DefaultAnnotationHover() {

			@Override
			protected boolean isIncluded(Annotation annotation) {
				if (UNCHANGED_QUICKDIFF_ANNOTATION.equals(annotation.getType())) {
					return false;
				}
				return super.isIncluded(annotation);
			}

			@Override
			public String getHoverInfo(ISourceViewer sourceViewer,
					int lineNumber) {
				String raw = super.getHoverInfo(sourceViewer, lineNumber);
				String result = raw != null ? raw.replace("\r\n", "<br>").replace("\r", "<br>").replace("\n", "<br>") : null;
				return result;
			}
		};
		return annHover;
	}

	@Override
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[]{
				IDocument.DEFAULT_CONTENT_TYPE, 
				EditorConstants.PARTITION_TYPE_BRAINFUCK_CODE, 
				EditorConstants.PARTITION_TYPE_MULTILINE_COMMENT, 
				EditorConstants.PARTITION_TYPE_TEMPLATE_PARAMETERS};
	}

	@Override
	public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer) {
		return  EditorConstants.BF_PARTITIONING;
	}

	@Override
	public IAutoEditStrategy[] getAutoEditStrategies(
			ISourceViewer sourceViewer, String contentType) {
		IAutoEditStrategy closeBrackets = new CloseBracketStrategy();
		return new IAutoEditStrategy[]{new DefaultIndentLineAutoEditStrategy(), closeBrackets};
	}

	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		ContentAssistant assistant = new ContentAssistant();
		assistant.setDocumentPartitioning(this.getConfiguredDocumentPartitioning(sourceViewer));
		CompletionProposalToggler toggler = new CompletionProposalToggler();
		assistant.setContentAssistProcessor(toggler, IDocument.DEFAULT_CONTENT_TYPE);
		assistant.setContentAssistProcessor(toggler, EditorConstants.PARTITION_TYPE_BRAINFUCK_CODE);
		assistant.addCompletionListener(toggler);		
		assistant.setStatusLineVisible(true);
		assistant.setRepeatedInvocationMode(true);
		assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
		assistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
		assistant.setAutoActivationDelay(500);
		assistant.enableAutoActivation(false);
		
		assistant.setInformationControlCreator(new AbstractReusableInformationControlCreator() {
			
			@Override
			protected IInformationControl doCreateInformationControl(Shell parent) {
				return new DefaultInformationControl(parent);
			}
		});
		assistant.setContentAssistProcessor(null, EditorConstants.PARTITION_TYPE_MULTILINE_COMMENT);
		return assistant;
	}
	
	@Override
	public ITextHover getTextHover(ISourceViewer sourceViewer,
			String contentType) {
		if (EditorConstants.PARTITION_TYPE_TEMPLATE_PARAMETERS.equals(contentType)) {
			return new ParametersTextHover(sourceViewer);
		}
		else {
			return new CodeTextHover(sourceViewer);
		}
	}
	
	@Override
	public IInformationPresenter getInformationPresenter(
			ISourceViewer sourceViewer) {
		return new InformationPresenter(getInformationControlCreator(sourceViewer));
	}
	
	@Override
	public IInformationControlCreator getInformationControlCreator(
			ISourceViewer sourceViewer) {
		return new InformationControlCreator();
	}


	/**
	 * @author Richard Birenheide
	 *
	 */
	private final class CloseBracketStrategy implements IAutoEditStrategy {
		
		@Override
		public void customizeDocumentCommand(IDocument document, DocumentCommand command) {
			if (command.getCommandCount() == 1 
					&& command.length == 0 
					&& command.text.equals("[")
					&& BfSourceViewerConfiguration.this.fPreferenceStore.getBoolean(EditorConstants.PREF_EDITOR_CLOSE_BRACKET)) {
				
				command.text = command.text + "]";
				command.caretOffset = command.offset + 1;
				command.shiftsCaret = false;
			}
		}
	}
	
	/**
	 * @author Richard Birenheide
	 *
	 */
	private final class CodeTextHover extends PlatformObject implements ITextHover, ITextHoverExtension, ITextHoverExtension2 {
		
		private static final String ANNOTATION_HOVER_ID = "annotationHover";
		private static final String CLASS_ATTRIBUTE = "class";
		private static final String ANNOTATION_TYPE_ATTRIBUTE = "annotationType";
		
		private final ISourceViewer sourceViewer;
		private Map<String, AnnotationHover> hoverContributions = new TreeMap<String, AnnotationHover>();
		
		CodeTextHover(ISourceViewer sourceViewer) {
			this.sourceViewer = sourceViewer;
			IExtensionRegistry registry = RegistryFactory.getRegistry();
			
			for (IConfigurationElement extension : registry.getConfigurationElementsFor(BUNDLE_SYMBOLIC_NAME, ANNOTATION_HOVER_ID)) {
				if (extension.isValid()) {
					String annotationType = extension.getAttribute(ANNOTATION_TYPE_ATTRIBUTE);
					try {
						AnnotationHover hover = (AnnotationHover) extension.createExecutableExtension(CLASS_ATTRIBUTE);
						if (annotationType != null && hover != null) {
							this.hoverContributions.put(annotationType, hover);
						}
					} 
					catch (CoreException | ClassCastException ex) {
						BfActivator.getDefault().logError("AnnotationHover coud not be created", ex);
					}
				}
			}
		}

		@Override
		public Object getHoverInfo2(ITextViewer textViewer, IRegion hoverRegion) {
			IAnnotationModel model = sourceViewer.getAnnotationModel();
			@SuppressWarnings("unchecked")
			Iterator<Annotation> i = model.getAnnotationIterator();
			Annotation provideText = null;
			AnnotationPreferenceLookup preferenceLookup = EditorsUI.getAnnotationPreferenceLookup();
			int annotationLayer = -1;
			while (i.hasNext()) {
				Annotation ann = i.next();
				
				Position p = model.getPosition(ann);
				if (p == null || !p.overlapsWith(hoverRegion.getOffset(), hoverRegion.getLength())) {
					continue;
				}
				if (UNCHANGED_QUICKDIFF_ANNOTATION.equals(ann.getType())) {
					continue; //Ignore unchanged line notification
				}
				if (provideText == null) {
					provideText = ann;
				}
				AnnotationPreference preference = preferenceLookup.getAnnotationPreference(ann);
				if (preference != null && preference.getPresentationLayer() > annotationLayer) {
					provideText = ann;
					annotationLayer = preference.getPresentationLayer();
				}
			}
			String text = null;
			if (provideText != null) {
				if (this.hoverContributions.containsKey(provideText.getType())) {
					text = this.hoverContributions.get(provideText.getType()).getHoverText(provideText, textViewer, hoverRegion);
				}
				else {
					text = "<b>" +  provideText.getText() + "</b>";
				}
			}
			try {
				if (text == null) {
					IDocument document = textViewer.getDocument();
					if (document instanceof IDocumentExtension3) {
						IDocumentExtension3 ext3 = (IDocumentExtension3) document;
						ITypedRegion partition = ext3.getPartition(EditorConstants.BF_PARTITIONING, hoverRegion.getOffset(), false);
						if (EditorConstants.PARTITION_TYPE_BRAINFUCK_CODE.equals(partition.getType())) {
							text = "Offset: [<b>" + hoverRegion.getOffset() + "</b>]";
						}
					}
				}
			}
			catch (BadLocationException | BadPartitioningException ex) {
				BfActivator.getDefault().logError("hoverRegion partitioning could not be evaluated", ex);
			}
			return text;
		}

		@Override
		public IInformationControlCreator getHoverControlCreator() {
			IInformationControlCreator creator = BfSourceViewerConfiguration.this.getInformationControlCreator(this.sourceViewer);
			if (creator instanceof InformationControlCreator) {
				((InformationControlCreator) creator).setSize(100, 100);
			}
			return creator;
		}

		@Override
		public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
			return null;
		}

		@Override
		public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
			return new Region(offset, 1);
		}
	}
	
	/**
	 * @author Richard Birenheide
	 *
	 */
	private final class ParametersTextHover extends PlatformObject implements ITextHover, ITextHoverExtension, ITextHoverExtension2 {

		private final ISourceViewer sourceViewer;
		
		ParametersTextHover(ISourceViewer sourceViewer) {
			this.sourceViewer = sourceViewer;
		}
		
		@Override
		public Object getHoverInfo2(ITextViewer textViewer, IRegion hoverRegion) {
			if (!(hoverRegion instanceof ITypedRegion) && !((ITypedRegion) hoverRegion).getType().equals(EditorConstants.PARTITION_TYPE_TEMPLATE_PARAMETERS)) {
				return null;
			}
			try {
				String hoveredText = textViewer.getDocument().get(hoverRegion.getOffset(), hoverRegion.getLength());
				List<String> parameters = BfTemplateCompletionProcessor.parseParameters(hoveredText);
				String hoverText = "";
				int i = 0;
				for (String param : parameters) {
					hoverText += "x" + i + " = " + param + "<br>";
					i++;
				}
				if (!hoverText.isEmpty()) {
					return "<b>Template parameters:</b><br>" + hoverText.substring(0, hoverText.length() - 4);
				}
				return null;
			} 
			catch (BadLocationException ex) {
				BfActivator.getDefault().logError("Hover could not be calculated", ex);
			}
			return null;
		}

		@Override
		public IInformationControlCreator getHoverControlCreator() {
			IInformationControlCreator creator = BfSourceViewerConfiguration.this.getInformationControlCreator(this.sourceViewer);
			if (creator instanceof InformationControlCreator) {
				((InformationControlCreator) creator).setSize(100, 100);
			}
			return creator;
		}

		@Override
		public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
			return null;
		}

		@Override
		public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
			IRegion region = new Region(offset, 0);
			try {
				IDocument doc = textViewer.getDocument();
				
				if (doc instanceof IDocumentExtension3) {
					IDocumentExtension3 ext3 = (IDocumentExtension3) doc;
					region = ext3.getPartition(EditorConstants.BF_PARTITIONING, offset, true);
				}
				else {
					region = doc.getPartition(offset);
				}
			}
			catch (BadPartitioningException | BadLocationException ex) {
				BfActivator.getDefault().logError("Partitioning Problem", ex);
			}
			return region;
		}
	}
	
	/**
	 * @author Richard Birenheide
	 *
	 */
	private static class InformationControlCreator extends AbstractReusableInformationControlCreator {

		private int width = -1;
		private int height = -1;
		
		@Override
		protected IInformationControl doCreateInformationControl(Shell parent) {
			DefaultInformationControl infoControl = new DefaultInformationControl(parent, "Press 'F2' for focus") {

				@Override
				public Point computeSizeHint() {
					if (width != -1 && height != -1) {
						Point suggested = super.computeSizeHint();
//						System.out.println(suggested);
						int width = suggested.x + 50;
						int height = Math.min(suggested.y + 10, 100);
						return new Point(width, height);
					}
					return super.computeSizeHint();
				}
			};
			return infoControl;
		}
		
		void setSize(int width, int height) {
			this.width = width;
			this.height = height;
		}
	}
}
