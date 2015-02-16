package org.birenheide.bf.ed;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ContextInformationValidator;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

class BfKeywordCompletionProposal implements IContentAssistProcessor {
	
	private static final String[][] PROPOSAL_TEXTS = new String[][] {
			{">", "> - Increase Memory Pointer"},
			{"<", "< - Decrease Memory Pointer"},
			{"+", "+ - Increment Memory Value"},
			{"-", "- - Decrement Memory Value"},
			{"[", "[ - Jump Forward on Zero"},
			{"]", "] - Jump Backward on Non-Zero"},
			{".", ". - Write Memory Value to Output"},
			{",", ", - Read Memory Value from Input"}
	};
	
	@Override
	public ICompletionProposal[] computeCompletionProposals(
			ITextViewer viewer, int offset) {
		List<ICompletionProposal> proposals = new ArrayList<>();
		for (String[] texts : PROPOSAL_TEXTS) {
			String replacement = texts[0];
			String text = texts[1];
			CompletionProposal proposal = 
					new CompletionProposal(replacement, 
										   offset, 
										   replacement.length(), 
										   replacement.length(),
										   null,
										   text,
										   new ContextInformation(replacement, text),
										   null);
			proposals.add(proposal);
		}
		return proposals.toArray(new ICompletionProposal[proposals.size()]);
	}

	@Override
	public IContextInformation[] computeContextInformation(
			ITextViewer viewer, int offset) {
		return null;
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return null;
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		return new ContextInformationValidator(this);
	}
}