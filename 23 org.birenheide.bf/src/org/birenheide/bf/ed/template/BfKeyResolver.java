package org.birenheide.bf.ed.template;

import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateVariableResolver;

public class BfKeyResolver extends TemplateVariableResolver {

	public BfKeyResolver() {
	}

	public BfKeyResolver(String type, String description) {
		super(type, description);
	}

	@Override
	protected String resolve(TemplateContext context) {
		if (context instanceof DocumentTemplateContext) {
			return ((DocumentTemplateContext) context).getKey();
		}
		return null;
	}

	@Override
	protected boolean isUnambiguous(TemplateContext context) {
		return true;
	}
}
