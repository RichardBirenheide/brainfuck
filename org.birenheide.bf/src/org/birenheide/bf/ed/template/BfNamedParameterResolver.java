package org.birenheide.bf.ed.template;

import java.text.ChoiceFormat;
import java.text.MessageFormat;

import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateVariable;
import org.eclipse.jface.text.templates.TemplateVariableResolver;

public class BfNamedParameterResolver extends TemplateVariableResolver {
	
	public static final TemplateVariableResolver X0 = new BfNamedParameterResolver("x0", "First Parameter");
	public static final TemplateVariableResolver X1 = new BfNamedParameterResolver("x1", "Second Parameter");
	public static final TemplateVariableResolver X2 = new BfNamedParameterResolver("x2", "Third Parameter");
	public static final TemplateVariableResolver X3 = new BfNamedParameterResolver("x3", "Fourth Parameter");
	public static final TemplateVariableResolver X4 = new BfNamedParameterResolver("x4", "Fifth Parameter");

	public BfNamedParameterResolver() {
	}

	public BfNamedParameterResolver(String type, String description) {
		super(type, description);
	}
	
	BfNamedParameterResolver(int parameterNo) {
		MessageFormat form = new MessageFormat("{0} parameter");
		double[] limits = {1,2,3,4};
		String[] inserts = {"{0,number}st", "{0,number}nd", "{0,number}rd","{0,number}th"};
		ChoiceFormat cform = new ChoiceFormat(limits, inserts);
		form.setFormatByArgumentIndex(0, cform);
		
		String description = form.format(new Object[]{parameterNo});
		this.setDescription(description);
		this.setType("x" + (parameterNo - 1));
	}

	@Override
	protected final String resolve(TemplateContext context) {
		return context.getVariable(this.getType());
	}

	
	/** 
	 * Must always return <code>true</code>.
	 * @see org.eclipse.jface.text.templates.TemplateVariableResolver#isUnambiguous(org.eclipse.jface.text.templates.TemplateContext)
	 */
	@Override
	protected final boolean isUnambiguous(TemplateContext context) {
		return true;
	}

	@Override
	public final String getType() {
		return super.getType();
	}

	@Override
	public final String getDescription() {
		return super.getDescription();
	}

	@Override
	protected final String[] resolveAll(TemplateContext context) {
		return super.resolveAll(context);
	}

	@Override
	public final void resolve(TemplateVariable variable, TemplateContext context) {
		super.resolve(variable, context);
	}
	
	

}
