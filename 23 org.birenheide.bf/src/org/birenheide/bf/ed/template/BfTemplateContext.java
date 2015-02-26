package org.birenheide.bf.ed.template;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;

public class BfTemplateContext extends DocumentTemplateContext {

	public BfTemplateContext(TemplateContextType type, IDocument document,
			Position position) {
		super(type, document, position);
	}

	public BfTemplateContext(TemplateContextType type, IDocument document,
			int offset, int length) {
		super(type, document, offset, length);
	}

	@Override
	public TemplateBuffer evaluate(Template template)
			throws BadLocationException, TemplateException {
		try {
			return super.evaluate(template);
		}
		catch (VariableEvaluationException ex) {
			throw new TemplateException(ex.getMessage(), ex);
		}
	}
}
