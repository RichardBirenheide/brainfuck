package org.birenheide.bf.ed.template;

import org.birenheide.bf.core.BfActivator;

public enum ParametrizedTemplateTypeDescriptor {
	
	NoParameters("No parameter", 0),
	OneParameter("1 parameter", 1),
	TwoParameters("2 parameters", 2),
	ThreeParameters("3 parameters", 3),
	FourParameters("4 parameters", 4),
	FiveParameters("5 parameters", 5);
	
	
	final String id;
	final String name;
	public final int noOfParameters;
	public final BfTemplateType templateType;
	
	public static BfTemplateType findTemplateType(int noOfParameters) {
		for (ParametrizedTemplateTypeDescriptor desc : ParametrizedTemplateTypeDescriptor.values()) {
			if (desc.noOfParameters == noOfParameters) {
				return desc.templateType;
			}
		}
		return null;
	}
	
	private ParametrizedTemplateTypeDescriptor(String name, int parameters) {
		this.id = BfActivator.BUNDLE_SYMBOLIC_NAME + ".p" + parameters;
		this.name = name;
		this.noOfParameters = parameters;
		this.templateType = new BfTemplateType(id, name, parameters);
	}
}
