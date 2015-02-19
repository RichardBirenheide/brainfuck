package org.birenheide.bf.ed.template;

import java.util.List;

import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateVariable;

public class EvalResolver extends IntegerParameterExtractor {

	public EvalResolver() {
	}

	public EvalResolver(String type, String description) {
		super(type, description);
	}

	@Override
	public void resolve(TemplateVariable variable, TemplateContext context) {
		@SuppressWarnings("unchecked")
		List<String> params = (List<String>) variable.getVariableType().getParams(); 
		int result = this.evaluateParameters(context, params);
		variable.setValue("");
		variable.setResolved(true);
		context.setVariable(variable.getName(), "" + result);
	}
}
