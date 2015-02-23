package org.birenheide.bf.ed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.birenheide.bf.BfActivator;
import org.birenheide.bf.ed.template.ParametrizedTemplateTypeDescriptor;
import org.birenheide.bf.ui.BfImages;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPartitioningException;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.swt.graphics.Image;

class BfTemplateCompletionProcessor extends TemplateCompletionProcessor {
	
	static List<String> parseParameters(String prefix) {
		List<String> parameters = new ArrayList<>(Arrays.asList(prefix.split(";")));
		for (Iterator<String> i = parameters.iterator(); i.hasNext();) {
			String param = i.next();
			if ("".equals(param.trim())) {
				i.remove();
			}
		}
		return parameters;
	}
	
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
			ITypedRegion previousRegion = document3.getPartition(BfDocSetupParticipant.BF_PARTITIONING, offset - 1, false);
			if (BfPartitionScanner.TEMPLATE_PARAMETERS.equals(previousRegion.getType())) {
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
		TemplateContext context = super.createContext(viewer, region);
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
}