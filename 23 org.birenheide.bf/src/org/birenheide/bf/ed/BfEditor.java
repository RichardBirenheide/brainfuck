package org.birenheide.bf.ed;

import static org.birenheide.bf.BfActivator.BF_PROBLEM_MARKER_ID;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.birenheide.bf.BfActivator;
import org.birenheide.bf.ui.BfEditorPreferencePage;
import org.birenheide.bf.ui.BfTemplatePreferencePage;
import org.birenheide.bf.ui.HelpContext;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.ui.actions.ToggleBreakpointAction;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.source.DefaultCharacterPairMatcher;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.preferences.ViewPreferencesAction;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

public class BfEditor extends TextEditor {

	public final static String EDITOR_MATCHING_BRACKETS_PREF = BfActivator.BUNDLE_SYMBOLIC_NAME + ".matchingBrackets";
	public final static String EDITOR_MATCHING_BRACKETS_COLOR_PREF= BfActivator.BUNDLE_SYMBOLIC_NAME + ".matchingBracketsColor";
	public final static String EDITOR_CLOSE_BRACKET= BfActivator.BUNDLE_SYMBOLIC_NAME + ".closeBracket";
	public final static String EDITOR_KEY_CHAR_COLOR_PREF = BfActivator.BUNDLE_SYMBOLIC_NAME + ".keyCharColor";
	public final static String EDITOR_OTHER_CHAR_COLOR_PREF = BfActivator.BUNDLE_SYMBOLIC_NAME + ".otherCharColor";
	
	public final static String EDITOR_ID = "org.birenheide.bf.BrainfuckEditor";
	
	/**
	 * File extension for Brainfuck files: {@value}.
	 */
	public static final String BF_FILE_EXTENSION = "bf";
	
	private BfContentValidator validator = new BfContentValidator();
//	private BracketCloser bracketCloser = new BracketCloser();
	
	@Override
	protected void initializeEditor() {
		super.initializeEditor();
		setSourceViewerConfiguration(new BfSourceViewerConfiguration(this.getPreferenceStore()));
		this.setHelpContextId(HelpContext.EDITOR_ID);
	}
	
	@Override
	protected void configureSourceViewerDecorationSupport(SourceViewerDecorationSupport support) {
		super.configureSourceViewerDecorationSupport(support);
		
		DefaultCharacterPairMatcher matcher = new DefaultCharacterPairMatcher(new char[]{'[', ']'}, IDocumentExtension3.DEFAULT_PARTITIONING, true);
		support.setCharacterPairMatcher(matcher);
		support.setMatchingCharacterPainterPreferenceKeys(EDITOR_MATCHING_BRACKETS_PREF, EDITOR_MATCHING_BRACKETS_COLOR_PREF);
	}

	@Override
	protected void doSetInput(IEditorInput input) throws CoreException {
		if (this.getDocumentProvider() != null) {
			this.getDocumentProvider().getDocument(getEditorInput()).removeDocumentListener(validator);
		}
		super.doSetInput(input);
		IDocument document = this.getDocumentProvider().getDocument(input);
		try {
			this.validator.validate(document);
		} 
		catch (BadLocationException e) {
			throw new CoreException(new Status(IStatus.ERROR, BfActivator.BUNDLE_SYMBOLIC_NAME, e.getMessage(), e));
		}
		document.addDocumentListener(validator);
		
		IDocumentPartitioner partitioner = new FastPartitioner(new BfPartitionScanner(), BfPartitionScanner.BRAINFUCK_PARTITION_TYPES);
		partitioner.connect(document);
		document.setDocumentPartitioner(partitioner);
//		document.addDocumentListener(bracketCloser);
	}

	
	@Override
	protected void createActions() {
		super.createActions();
		this.setAction(
				ITextEditorActionConstants.RULER_DOUBLE_CLICK, 
				new ToggleBreakpointAction(this, null, this.getVerticalRuler()));
		this.setAction(ITextEditorActionConstants.RULER_PREFERENCES, new OpenBfEditorPreferenceDialog());
		this.setAction(ITextEditorActionConstants.CONTEXT_PREFERENCES, new OpenBfEditorPreferenceDialog());
	}


	/**
	 * @author Richard Birenheide
	 *
	 */
	private static final class OpenBfEditorPreferenceDialog extends
			ViewPreferencesAction {
		@Override
		public void openViewPreferencesDialog() {
			PreferenceDialog dialog = 
					PreferencesUtil.createPreferenceDialogOn(
							null, 
							BfEditorPreferencePage.ID, 
							new String[]{
									BfEditorPreferencePage.ID, 
									BfTemplatePreferencePage.ID, 
									"org.eclipse.ui.preferencePages.GeneralTextEditor"}, 
							null);
			dialog.open();
		}
	}


	/**
	 * @author Richard Birenheide
	 *
	 */
	private class BfContentValidator implements IDocumentListener {
		
		private boolean validate = false;

		@Override
		public void documentChanged(DocumentEvent event) {
			if (event.fText.length() > 0) {//Text inserted
				String insertedText = event.fText;
//				System.out.println("insert: >" + insertedText + "<");
				this.validate = this.validate || insertedText.contains("[") || insertedText.contains("]");
			}
			if (this.validate) {
				try {
					this.validate(event.fDocument);
				} 
				catch (BadLocationException e) {
					BfActivator.getDefault().logError("Document could not be read", e);
				} 
				catch (CoreException e) {
					BfActivator.getDefault().logError("Document could not be read", e);
				}
			}
		}

		@Override
		public void documentAboutToBeChanged(DocumentEvent event) {
			this.validate = false;
			if (event.fLength > 0) {//Deletion
				try {
					String deletedText = event.getDocument().get(event.fOffset, event.fLength);
					this.validate = deletedText.contains("[") || deletedText.contains("]");
//					System.out.println("delete: >" + deletedText + "<");
				} 
				catch (BadLocationException e) {
					BfActivator.getDefault().logError("Document could not be read", e);
				}
			}
		}
		
		void validate(IDocument document) throws BadLocationException, CoreException {
//			System.out.println("Validate");
			final Deque<Integer> openBrackets = new ArrayDeque<>();
			final List<Integer> invalidClosedBrackets = new ArrayList<>();
			for (int i = 0; i < document.getLength(); i++) {
				char c = document.getChar(i);
				if (c == '[') {
					openBrackets.push(i);
				}
				else if (c == ']') {
					if (openBrackets.isEmpty()) {
						invalidClosedBrackets.add(i);
					}
					else {
						openBrackets.pop();
					}
				}
			}
			final IFile resource = ((IFileEditorInput) getEditorInput()).getFile();
			
			WorkspaceJob wsj = new WorkspaceJob("Validate") {
				
				@Override
				public IStatus runInWorkspace(IProgressMonitor monitor)
						throws CoreException {
					resource.deleteMarkers(BF_PROBLEM_MARKER_ID, false, IResource.DEPTH_ZERO);
					for (int loc : openBrackets) {
						IMarker marker = resource.createMarker(BF_PROBLEM_MARKER_ID);
						marker.setAttribute(IMarker.CHAR_START, loc);
						marker.setAttribute(IMarker.CHAR_END, loc + 1);
						marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
						marker.setAttribute(IMarker.LOCATION, "Index: " + loc);
						marker.setAttribute(IMarker.MESSAGE, "Non-matching opening bracket");
					}
					for (int loc : invalidClosedBrackets) {
						IMarker marker = resource.createMarker(BF_PROBLEM_MARKER_ID);
						marker.setAttribute(IMarker.CHAR_START, loc);
						marker.setAttribute(IMarker.CHAR_END, loc + 1);
						marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
						marker.setAttribute(IMarker.LOCATION, "Index: " + loc);
						marker.setAttribute(IMarker.MESSAGE, "Non-matching closing bracket");
					}
					return Status.OK_STATUS;
				}
			};
			wsj.setRule(resource);
			wsj.schedule();
		}
		
	}
	
	
	@SuppressWarnings("unused")
	private class BracketCloser implements IDocumentListener {

		@Override
		public void documentAboutToBeChanged(DocumentEvent event) {
			if (event.fText.equals("[") && getPreferenceStore().getBoolean(EDITOR_CLOSE_BRACKET)) { //Exactly one character added
				int offset = event.fOffset;
				IDocument doc = event.fDocument;
				try {
					doc.replace(offset, 0, "]");
				} 
				catch (BadLocationException e) {
					BfActivator.getDefault().logError("Bracket could not be closed", e);
				}
			}
		}

		@Override
		public void documentChanged(DocumentEvent event) {
		}
		
	}
}
