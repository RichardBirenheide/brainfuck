package org.birenheide.bf.ed.template;

import java.util.List;

import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateVariableResolver;


public class IntegerParameterExtractor extends TemplateVariableResolver {

	public IntegerParameterExtractor() {
		super();
	}

	public IntegerParameterExtractor(String type, String description) {
		super(type, description);
	}

	@Override
	protected boolean isUnambiguous(TemplateContext context) {
		return true;
	}
	
	int evaluateParameters(TemplateContext context, List<String> params) {
		if (params.isEmpty()) {
			return 0;
		}
		if (params.size() == 1) {
			return getParameterValue(context, params.get(0));
		}
		else if (params.size() == 3) {
			int fst = getParameterValue(context, params.get(0));
			int scnd = getParameterValue(context, params.get(2));
			String command = params.get(1);
			switch (command) {
				case "-":
					return fst - scnd;
				case "+":
					return fst + scnd;
				case "plus":
					return fst + scnd;
				case "minus":
					return fst - scnd;
				default:
					return 0;
			}
		}
		return 0;
	}
	
	private int getParameterValue(TemplateContext context, String param) {
		String value = context.getVariable(param);
		if (value == null) {
			value = param;
		}
		try {
			boolean negative = value.startsWith("!") && value.length() > 1;
			if (negative) {
				return -Integer.parseInt(value.substring(1));
			}
			return Integer.parseInt(value);
		}
		catch (NumberFormatException ex) {
			return 0;
		}
	}
}
