package org.birenheide.bf.ed.template;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateVariable;

/**
 * Evaluates memory shifts based on passed parameters.
 * The parameters passed must be integers. Negative integers mean left shift,
 * positive integers mean right shift. To allow parsing input from source files
 * in which minus signs are key characters, a negative number might be prefixed
 * with an exclamation mark instead of a minus sign.
 * @author Richard Birenheide
 *
 */
public class BfMemShiftResolver extends ExpressionEvaluator {
	
	public static final String MEM_SHIFT = "mem_shift";

	public BfMemShiftResolver() {
	}

	public BfMemShiftResolver(String type, String description) {
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
			c = '>';
		}
		else {
			c = '<';
			expanded = -expanded;
		}
		
		char[] shifts = new char[expanded];
		Arrays.fill(shifts, c);
		String value = new String(shifts); 
		variable.setValue(new String(shifts));
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
