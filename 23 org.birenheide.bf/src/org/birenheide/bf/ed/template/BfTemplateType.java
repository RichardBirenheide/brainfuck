package org.birenheide.bf.ed.template;

import java.util.List;

import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateVariable;
import org.eclipse.jface.text.templates.TemplateVariableResolver;

public class BfTemplateType extends TemplateContextType {
	
	public static final String TYPE_ID = "org.birenheide.bf.brainfuck";
	public static final String REGISTRY_ID = "org.birenheide.bf.BfEditor";
	
	public BfTemplateType() {
		this.addResolvers(0);
	}

	public BfTemplateType(String id) {
		super(id);
		this.addResolvers(0);
	}

	public BfTemplateType(String id, String name) {
		super(id, name);
		this.addResolvers(0);
	}
	
	BfTemplateType(String id, String name, int noOfParameters) {
		this.setId(id);
		this.setName(name);
		this.addResolvers(noOfParameters);
	}

	@Override
	public void resolve(TemplateVariable variable, TemplateContext context) {
		super.resolve(variable, context);
	}
	
	private void addResolvers(int noOfParameters) {
		addResolver(new GlobalTemplateVariables.Cursor());
		addResolver(new GlobalTemplateVariables.WordSelection());
		addResolver(new GlobalTemplateVariables.LineSelection());
		addResolver(new GlobalTemplateVariables.Dollar());
		addResolver(new GlobalTemplateVariables.Date());
		addResolver(new GlobalTemplateVariables.Year());
		addResolver(new GlobalTemplateVariables.Time());
		addResolver(new GlobalTemplateVariables.User());
		
		addResolver(new EvalResolver("eval", "Evaluates an expression. Returns empty String, used to define intermediate variables used as parameters"));
		addResolver(new BfMemShiftResolver("mem_shift", "Inserts (number) of memory pointer shifts. Negative parameter shifts left, positive shifts right"));
		addResolver(new BfIncDecResolver("inc_dec", "Inserts (number) of increments/decrements. Negative parameter decrements, positive increments"));
		addResolver(new BfKeyResolver("trigger_key", "Evaluates to the text on which the template proposal was triggered"));
		
		for (int i = noOfParameters; i>0; i--) {
			addResolver(new BfNamedParameterResolver(i));
		}
	}

	@Override
	protected void validateVariables(TemplateVariable[] variables)
			throws TemplateException {
		for (TemplateVariable variable : variables) {
			TemplateVariableResolver resolver = this.getResolver(variable.getType());
			if (resolver != null && (resolver instanceof ExpressionEvaluator)) {
				@SuppressWarnings("unchecked")
				List<String> params = (variable.getVariableType().getParams());
				((ExpressionEvaluator) resolver).supportsParameters(params);
			}
		}
	}
	
	
}
