package org.birenheide.bf.ed;

import static org.birenheide.bf.BfActivator.BUNDLE_SYMBOLIC_NAME;

import java.util.Iterator;
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
import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
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
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.information.IInformationPresenter;
import org.eclipse.jface.text.information.InformationPresenter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.DefaultAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
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
		
		DefaultDamagerRepairer dr = new WholeDamagerRepairer(new BfCodeScanner(this.fPreferenceStore));
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
		
		dr = new WholeDamagerRepairer(new BfCodeScanner(fPreferenceStore));
		reconciler.setDamager(dr, BfPartitionScanner.BRAINFUCK_CODE);
		reconciler.setRepairer(dr, BfPartitionScanner.BRAINFUCK_CODE);

		dr = new DefaultDamagerRepairer(new BfCodeScanner(fPreferenceStore));
		reconciler.setDamager(dr, BfPartitionScanner.NON_BRAINFUCK_CHARS);
		reconciler.setRepairer(dr, BfPartitionScanner.NON_BRAINFUCK_CHARS);
		
		dr = new WholeDamagerRepairer(new BfCommentScanner());
		reconciler.setDamager(dr, BfPartitionScanner.MULTILINE_COMMENT);
		reconciler.setRepairer(dr, BfPartitionScanner.MULTILINE_COMMENT);
		
		return reconciler;
	}

	@Override
	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
		IAnnotationHover annHover = new DefaultAnnotationHover() {

			@Override
			protected boolean isIncluded(Annotation annotation) {
				if (UNCHANGED_QUICKDIFF_ANNOTATION.equals(annotation.getType())) {
					return false;
				}
				return super.isIncluded(annotation);
			}
			
		};
		return annHover;
	}

	@Override
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[]{
				IDocument.DEFAULT_CONTENT_TYPE, 
				BfPartitionScanner.BRAINFUCK_CODE, 
				BfPartitionScanner.MULTILINE_COMMENT, 
				BfPartitionScanner.NON_BRAINFUCK_CHARS};
	}

	@Override
	public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer) {
//		return  BfDocSetupParticipant.BF_PARTITIONING;
		return super.getConfiguredDocumentPartitioning(sourceViewer);
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
		assistant.setContentAssistProcessor(toggler, BfPartitionScanner.BRAINFUCK_CODE);
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
		assistant.setContentAssistProcessor(null, BfPartitionScanner.MULTILINE_COMMENT);
		return assistant;
	}
	
	@Override
	public ITextHover getTextHover(ISourceViewer sourceViewer,
			String contentType) {
		return new BfTextHover(sourceViewer);
	}
	
	@Override
	public IInformationPresenter getInformationPresenter(
			ISourceViewer sourceViewer) {
		return new InformationPresenter(getInformationControlCreator(sourceViewer));
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
					&& BfSourceViewerConfiguration.this.fPreferenceStore.getBoolean(BfEditor.EDITOR_CLOSE_BRACKET)) {
				
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
	private final class BfTextHover extends PlatformObject implements ITextHover, ITextHoverExtension, ITextHoverExtension2 {
		
		private static final String ANNOTATION_HOVER_ID = "annotationHover";
		private static final String CLASS_ATTRIBUTE = "class";
		private static final String ANNOTATION_TYPE_ATTRIBUTE = "annotationType";
		
		private final ISourceViewer sourceViewer;
		private Map<String, AnnotationHover> hoverContributions = new TreeMap<String, AnnotationHover>();
		
		BfTextHover(ISourceViewer sourceViewer) {
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
					text = provideText.getText();
				}
			}
			return text;
		}

		@Override
		public IInformationControlCreator getHoverControlCreator() {
			return BfSourceViewerConfiguration.this.getInformationControlCreator(this.sourceViewer);
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
	private static class WholeDamagerRepairer extends DefaultDamagerRepairer {

		public WholeDamagerRepairer(ITokenScanner scanner) {
			super(scanner);
		}

		@Override
		public IRegion getDamageRegion(ITypedRegion partition, DocumentEvent e,
				boolean documentPartitioningChanged) {
			return partition;
		}
	}
	
	/**
	 * @author Richard Birenheide
	 *
	 */
	private static class BfCommentScanner extends BufferedRuleBasedScanner {

		public BfCommentScanner() {
			this.setDefaultReturnToken(new Token(new TextAttribute(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GREEN))));
		}
	}
}
