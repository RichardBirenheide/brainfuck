package org.birenheide.bf.ed.template;

import java.util.Arrays;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateException;

public class ExpressionTest {
	public static void main(String[] args) throws Exception {
		TemplateContext context = new TemplateContext(null) {
			
			@Override
			public TemplateBuffer evaluate(Template template)
					throws BadLocationException, TemplateException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public boolean canEvaluate(Template template) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public String getVariable(String name) {
				switch (name) {
					case "x0" : return "-2";
					case "x1" : return "4";
					default : return "";
				}
			}
		};
		ExpressionEvaluator ev = new ExpressionEvaluator() {};
		System.out.println(ev.resolve(Arrays.asList("-x1*-2"), context));
	}
}
