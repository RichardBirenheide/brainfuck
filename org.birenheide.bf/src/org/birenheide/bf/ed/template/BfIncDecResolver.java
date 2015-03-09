package org.birenheide.bf.ed.template;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateVariable;

public class BfIncDecResolver extends ExpressionEvaluator {

	public BfIncDecResolver() {
	}

	public BfIncDecResolver(String type, String description) {
		super(type, description);
	}

	@Override
	public void resolve(TemplateVariable variable, TemplateContext context) {
		@SuppressWarnings("unchecked")
		List<String> params = (List<String>) variable.getVariableType().getParams();
		variable.setValue("");
		variable.setResolved(true);
		variable.setUnambiguous(this.isUnambiguous(context));
		if (params.size() != 1) {
			return;
		}
		Integer expanded = this.resolve(params, context).get(0);
		if (expanded == null) {
			return;
		}
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
		context.setVariable(variable.getName(), value);
	}
	
	@Override
	void supportsParameters(List<String> parameters) throws TemplateException {
		if (parameters.size() != 1) {
			throw new TemplateException(this.getType() + " requires exactly 1 parameter");
		}
		this.parse(parameters);
	}
}
