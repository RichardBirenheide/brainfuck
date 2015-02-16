package org.birenheide.bf.ed.template;

import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateVariable;
import org.eclipse.jface.text.templates.TemplateVariableResolver;

public class BfParameterResolver extends TemplateVariableResolver {

	public BfParameterResolver() {
	}

	public BfParameterResolver(String type, String description) {
		super(type, description);
	}

	@Override
	public void resolve(TemplateVariable variable, TemplateContext context) {
		int test = -1;
		if (variable.getVariableType().getParams().size() > 0) {
			try {
				test = Integer.parseInt((String) variable.getVariableType().getParams().get(0));
			}
			catch (NumberFormatException ex) {}
		}
		String value = this.getParameter(context, test);
		variable.setValue("");
		variable.setResolved(true);
		variable.setUnambiguous(true);
		if (test > -1) {
			context.setVariable(variable.getName(), value);
		}
	}
	
	private String getParameter(TemplateContext context, int index) {
		if (index < 0) {
			return "";
		}
		String selection = context.getVariable(GlobalTemplateVariables.SELECTION);
		if (context instanceof DocumentTemplateContext) {
//			System.out.println(((DocumentTemplateContext) context).getKey());
			selection = ((DocumentTemplateContext) context).getKey();
		}
		if (selection != null && !selection.isEmpty()) {
			String[] parts = selection.split(";");
			if (parts.length > index) {
				String result = parts[index];
				return result;
			}
		}
		return "";
	}
}
