package org.birenheide.bf.ed.template;

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
