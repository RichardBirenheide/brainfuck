package org.birenheide.bf.ed.template;

import java.util.List;

import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateVariable;

public class EvalResolver extends ExpressionEvaluator {

	public EvalResolver() {
	}

	public EvalResolver(String type, String description) {
		super(type, description);
	}

	@Override
	public void resolve(TemplateVariable variable, TemplateContext context) {
		@SuppressWarnings("unchecked")
		List<String> params = (List<String>) variable.getVariableType().getParams();
		variable.setValue("");
		variable.setUnambiguous(this.isUnambiguous(context));
		variable.setResolved(true);
		if (params.size() != 1) {
			return;
		}
		List<Integer> result = this.resolve(params, context);
		if (result.get(0) != null) {
			context.setVariable(variable.getName(), "" + result.get(0));
		}
	}

	@Override
	void supportsParameters(List<String> parameters) throws TemplateException {
		if (parameters.size() != 1) {
			throw new TemplateException(this.getType() + " requires exactly 1 parameter");
		}
		this.parse(parameters);
	}
}
