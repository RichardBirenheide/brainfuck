package org.birenheide.bf.ed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.birenheide.bf.core.BfActivator;
import org.birenheide.bf.ed.template.BfTemplateContext;
import org.birenheide.bf.ed.template.BfTemplateProposal;
import org.birenheide.bf.ed.template.ParametrizedTemplateTypeDescriptor;
import org.birenheide.bf.ui.BfImages;
import org.eclipse.jface.text.AbstractReusableInformationControlCreator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPartitioningException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

class BfTemplateCompletionProcessor extends TemplateCompletionProcessor {
	
	static List<String> parseParameters(String prefix) {
		List<String> parameters = new ArrayList<>(Arrays.asList(prefix.split(";")));
		
		/*
		 * Filter out empty parameters and convert values prefixed with '!' to negative values.
		 * Assumes that all parameters are numbers.
		 */
		parameters = parameters.stream()
			.filter(param -> !"".equals(param.trim()))
			.map(param -> param.trim().startsWith("!") ? "-" + param.trim().substring(1) : param)
			.collect(Collectors.toList());
		return parameters;
	}
	
	private final IInformationControlCreator informationControlCreator = new InformationControlCreator();
	
	@Override
	protected Template[] getTemplates(String contextTypeId) {
		return BfActivator.getDefault().getTemplateStore().getTemplates(contextTypeId);
	}

	@Override
	protected TemplateContextType getContextType(ITextViewer viewer,
			IRegion region) {
		try {
			String prefix = viewer.getDocument().get(region.getOffset(), region.getLength());
			int parameterCount = parseParameters(prefix).size();
			TemplateContextType type = ParametrizedTemplateTypeDescriptor.findTemplateType(parameterCount);
			if (type != null) {
				return type;
			}
		} 
		catch (BadLocationException ex) {
			BfActivator.getDefault().logError("Context type could not be evaluated", ex);
		}
		return ParametrizedTemplateTypeDescriptor.NoParameters.templateType;
	}

	@Override
	protected Image getImage(Template template) {
		return BfActivator.getDefault().getImageRegistry().get(BfImages.ICON_BF);
	}

	@Override
	protected String extractPrefix(ITextViewer viewer, int offset) {
		try {
			if (offset == 0) {
				return "";
			}
			if (!(viewer.getDocument() instanceof IDocumentExtension3)) {
				return super.extractPrefix(viewer, offset);
			}
			IDocumentExtension3 document3 = (IDocumentExtension3) viewer.getDocument();
			ITypedRegion previousRegion = document3.getPartition(EditorConstants.BF_PARTITIONING, offset - 1, false);
			if (EditorConstants.PARTITION_TYPE_TEMPLATE_PARAMETERS.equals(previousRegion.getType())) {
				return viewer.getDocument().get(previousRegion.getOffset(), previousRegion.getLength());
			}
		} 
		catch (BadLocationException | BadPartitioningException ex) {
			BfActivator.getDefault().logError("Prefix for Template could not be computed", ex);
		}
		return super.extractPrefix(viewer, offset);
	}

	@Override
	protected int getRelevance(Template template, String prefix) {
		return super.getRelevance(template, prefix);
	}

	@Override
	protected TemplateContext createContext(ITextViewer viewer, IRegion region) {
		TemplateContext context = null;
		TemplateContextType contextType= getContextType(viewer, region);
		if (contextType != null) {
			IDocument document= viewer.getDocument();
			context =  new BfTemplateContext(contextType, document, region.getOffset(), region.getLength());
		}
		if (context == null) {
			return null;
		}
		try {
			String prefix = viewer.getDocument().get(region.getOffset(), region.getLength());
			int i = 0;
			for (String param : parseParameters(prefix)) {
				context.setVariable("x" + (i++), param);
			}
		} 
		catch (BadLocationException ex) {
			BfActivator.getDefault().logError("Prefix for Template could not be computed", ex);
		}
		return context;
	}

	@Override
	protected ICompletionProposal createProposal(Template template,
			TemplateContext context, IRegion region, int relevance) {
		TemplateProposal proposal = new BfTemplateProposal(template, context, region, getImage(template), relevance);
		proposal.setInformationControlCreator(informationControlCreator);
		return proposal;
	}
	

	/**
	 * @author Richard Birenheide
	 *
	 */
	private static class InformationControlCreator extends AbstractReusableInformationControlCreator {

		@Override
		protected IInformationControl doCreateInformationControl(Shell parent) {
			return new DefaultInformationControl(parent, "Press 'F2' for focus");
		}
		
	}
}