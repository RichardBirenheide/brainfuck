package org.birenheide.bf.ed.template;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.swt.graphics.Image;

public class BfTemplateProposal extends TemplateProposal {
	
	private static String escapeHTML(String raw) {
		String result = raw.replace("<", "&lt;").replace(">", "&gt;");
		result = result.replace("\r\n", "<br>").replace("\r", "<br>").replace("\n", "<br>");
		return result;
	}

	public BfTemplateProposal(Template template, TemplateContext context,
			IRegion region, Image image, int relevance) {
		super(template, context, region, image, relevance);
	}

	@Override
	public String getAdditionalProposalInfo() {
	    this.getContext().setReadOnly(true);
		TemplateBuffer templateBuffer;
		try {
			templateBuffer= this.getContext().evaluate(this.getTemplate());
		} 
		catch (BadLocationException | TemplateException ex) {
			String message = "<b>Error when resolving variables.</b><br>"
					+ "Nothing will be inserted<br><br>"
					+ escapeHTML(ex.getMessage());
			return message;
		}
		String info = escapeHTML(this.getTemplate().getDescription()) + 
				"<br><br><b>Inserts:</b><br>" + 
				escapeHTML(templateBuffer.getString());
		return info;
	}
}
