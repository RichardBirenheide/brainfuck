package org.birenheide.bf.ed;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistEvent;
import org.eclipse.jface.text.contentassist.ICompletionListener;
import org.eclipse.jface.text.contentassist.ICompletionListenerExtension;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistantExtension2;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

class CompletionProposalToggler implements ICompletionListener, ICompletionListenerExtension, IContentAssistProcessor {
	
	private IContentAssistProcessor keyWordProcessor = new BfKeywordCompletionProposal();
	private IContentAssistProcessor templateProcessor = new BfTemplateCompletionProcessor();
	
	private IContentAssistProcessor activeProcessor = templateProcessor;
	private IContentAssistantExtension2 assistant = null;

	@Override
	public ICompletionProposal[] computeCompletionProposals(
			ITextViewer viewer, int offset) {
		ICompletionProposal[] result = this.activeProcessor.computeCompletionProposals(viewer, offset);
		if (this.activeProcessor == keyWordProcessor) {
			this.activeProcessor = templateProcessor;
			this.assistant.setStatusMessage("Press Ctrl-Space for Template Proposals");
		}
		else {
			this.activeProcessor = keyWordProcessor;
			this.assistant.setStatusMessage("Press Ctrl-Space for Keyword Proposals");
		}
		return result;
	}

	@Override
	public IContextInformation[] computeContextInformation(
			ITextViewer viewer, int offset) {
		return activeProcessor.computeContextInformation(viewer, offset);
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return new char[] {';'};
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		return new char[0];
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		return this.activeProcessor.getContextInformationValidator();
	}

	@Override
	public void assistSessionRestarted(ContentAssistEvent event) {
		this.activeProcessor = this.templateProcessor;
		this.assistant = (IContentAssistantExtension2) event.assistant;
		this.assistant.setStatusMessage("Press Ctrl-Space to show Keyword Proposals");
	}

	@Override
	public void assistSessionStarted(ContentAssistEvent event) {
		this.assistSessionRestarted(event);
	}

	@Override
	public void assistSessionEnded(ContentAssistEvent event) {
		this.assistSessionRestarted(event);
	}

	@Override
	public void selectionChanged(ICompletionProposal proposal,
			boolean smartToggle) {
	}
}