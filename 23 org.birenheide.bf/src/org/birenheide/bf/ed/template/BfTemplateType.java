package org.birenheide.bf.ed.template;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

import org.birenheide.bf.BfActivator;
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
		this.addResolvers();
	}

	public BfTemplateType(String id) {
		super(id);
		this.addResolvers();
	}

	public BfTemplateType(String id, String name) {
		super(id, name);
		this.addResolvers();
	}

	@Override
	public void resolve(TemplateVariable variable, TemplateContext context) {
		super.resolve(variable, context);
	}
	
	private void addResolvers() {
		addResolver(new GlobalTemplateVariables.Cursor());
		addResolver(new GlobalTemplateVariables.WordSelection());
		addResolver(new GlobalTemplateVariables.LineSelection());
		addResolver(new GlobalTemplateVariables.Dollar());
		addResolver(new GlobalTemplateVariables.Date());
		addResolver(new GlobalTemplateVariables.Year());
		addResolver(new GlobalTemplateVariables.Time());
		addResolver(new GlobalTemplateVariables.User());
		
		for (Field f : BfNamedParameterResolver.class.getFields()) {
			if (Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers()) && f.getType().equals(TemplateVariableResolver.class)) {
				try {
					addResolver((TemplateVariableResolver)f.get(null));
				} 
				catch (IllegalArgumentException | IllegalAccessException | ClassCastException ex) {
					BfActivator.getDefault().logError("Template Variables could not be resolved", ex);
				}
			}
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
