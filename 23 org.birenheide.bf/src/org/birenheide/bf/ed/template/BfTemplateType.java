package org.birenheide.bf.ed.template;

import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateVariable;

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
//		System.out.println(variable.getType() + variable.getName() + variable.getVariableType());
		
		super.resolve(variable, context);
	}
	
	
	
	private void addResolvers() {
//		this.addResolver(new BfSelectedTextParser());
		addResolver(new GlobalTemplateVariables.Cursor());
		addResolver(new GlobalTemplateVariables.WordSelection());
		addResolver(new GlobalTemplateVariables.LineSelection());
		addResolver(new GlobalTemplateVariables.Dollar());
		addResolver(new GlobalTemplateVariables.Date());
		addResolver(new GlobalTemplateVariables.Year());
		addResolver(new GlobalTemplateVariables.Time());
		addResolver(new GlobalTemplateVariables.User());
	}

}
