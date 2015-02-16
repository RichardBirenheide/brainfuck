package org.birenheide.bf.ed;

import org.birenheide.bf.BfActivator;
import org.birenheide.bf.BrainfuckInterpreter;
import org.birenheide.bf.ed.template.BfTemplateType;
import org.birenheide.bf.ui.BfImages;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.swt.graphics.Image;

class BfTemplateCompletionProcessor extends TemplateCompletionProcessor {
	
	@Override
	protected Template[] getTemplates(String contextTypeId) {
		return BfActivator.getDefault().getTemplateStore().getTemplates(contextTypeId);
	}

	@Override
	protected TemplateContextType getContextType(ITextViewer viewer,
			IRegion region) {
		return BfActivator.getDefault().getTemplateContextTypeRegistry().getContextType(BfTemplateType.TYPE_ID);
	}

	@Override
	protected Image getImage(Template template) {
		return BfActivator.getDefault().getImageRegistry().get(BfImages.ICON_BF);
	}

	@Override
	protected String extractPrefix(ITextViewer viewer, int offset) {
		try {
			IDocument document = viewer.getDocument();
			int keyWordStart = offset - 1;
			while (keyWordStart >= 0) {
				char c = document.getChar(keyWordStart);
				if (BrainfuckInterpreter.isReservedChar(c) || Character.isWhitespace(c)) {
					break;
				}
				else {
					keyWordStart--;
				}
			}
			keyWordStart++;
			String keyword = document.get(keyWordStart, offset - keyWordStart).trim();
			return keyword;
		} 
		catch (BadLocationException e) {
			BfActivator.getDefault().logError("Prefix for Template could not be computed", e);
		}
		return super.extractPrefix(viewer, offset);
	}

	@Override
	protected int getRelevance(Template template, String prefix) {
		// TODO Auto-generated method stub
		return super.getRelevance(template, prefix);
	}

	@Override
	protected TemplateContext createContext(ITextViewer viewer, IRegion region) {
		// TODO Auto-generated method stub
		return super.createContext(viewer, region);
	}
	
	
}