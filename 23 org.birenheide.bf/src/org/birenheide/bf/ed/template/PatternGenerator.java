package org.birenheide.bf.ed.template;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateVariable;

public class PatternGenerator extends ExpressionEvaluator {

	public PatternGenerator() {
		super();
	}

	public PatternGenerator(String type, String description) {
		super(type, description);
	}

	@Override
	public void resolve(TemplateVariable variable, TemplateContext context) {
		@SuppressWarnings("unchecked")
		List<String> params = (List<String>) variable.getVariableType().getParams();
		variable.setValue("");
		variable.setResolved(true);
		variable.setUnambiguous(this.isUnambiguous(context));
		if (params.size() != 2 && params.size() != 3) {
			return;
		}
		Integer repeats = this.resolve(Arrays.asList(params.get(0)), context).get(0);
		if (repeats == null) {
			return;
		}
		Integer deleteSize = null;
		if (params.size() == 3) {
			deleteSize = this.resolve(Arrays.asList(params.get(2)), context).get(0);
		}
		String pattern = params.get(1);
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < repeats; i++) {
			result.append(pattern);
		}
		if (deleteSize != null && deleteSize > 0) {
			result.delete(result.length() - deleteSize, result.length());
		}
		variable.setValue(result.toString());
		context.setVariable(variable.getName(), result.toString());
	}

	@Override
	void supportsParameters(List<String> parameters) throws TemplateException {
		if (parameters.size() != 2 && parameters.size() != 3) {
			throw new TemplateException(this.getType() + " supports only 2 or 3 parameters");
		}
		if (parameters.size() == 2) {
			this.parse(Arrays.asList(parameters.get(0)));
		}
		else if (parameters.size() == 3) {
			this.parse(Arrays.asList(parameters.get(0), parameters.get(2)));
		}
	}
}
