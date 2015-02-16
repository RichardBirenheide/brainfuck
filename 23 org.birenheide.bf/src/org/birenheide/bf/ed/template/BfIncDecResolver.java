package org.birenheide.bf.ed.template;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateVariable;

public class BfIncDecResolver extends IntegerParameterExtractor {

	public BfIncDecResolver() {
	}

	public BfIncDecResolver(String type, String description) {
		super(type, description);
	}

	@Override
	public void resolve(TemplateVariable variable, TemplateContext context) {
		@SuppressWarnings("unchecked")
		List<String> params = (List<String>) variable.getVariableType().getParams();
		int expanded = this.evaluateParameters(context, params);
		char c = ' ';
		if (expanded > 0) {
			c = '+';
		}
		else {
			c = '-';
			expanded = -expanded;
		}
		char[] incDec = new char[expanded];
		Arrays.fill(incDec, c);
		String value = new String(incDec);
		variable.setValue(value);
		variable.setUnambiguous(isUnambiguous(context));
		variable.setResolved(true);
		context.setVariable(variable.getName(), value);
	}
}
