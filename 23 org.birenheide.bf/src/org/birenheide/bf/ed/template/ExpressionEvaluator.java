package org.birenheide.bf.ed.template;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateVariableResolver;

/**
 * Enables evaluation of a numeric expression containing variables passed as parameter to a resolver.<br>
 * The BNF for allowed expressions is:
 * <pre>
 * expression := ( expression operator expression ) 
 *             | ( '(' expression ')' )
 *             | number
 *             | variable
 * operator   := '+' | '-' | '*' | '/' | '%'
 * number     := signed integer
 * variable	  := sign [a-z,A-Z] ([a-z,0-9,a-z])*
 * sign       := '' | '-'
 * </pre>
 * Variables in expressions must be defined in the TemplateContext used.
 * 
 * @author Richard Birenheide
 *
 */
abstract class ExpressionEvaluator extends TemplateVariableResolver {
	
	/*
	 * Expression can be:
	 * expression := ( expression operator expression ) 
	 * 			   | ( '(' expression ')' )
	 * 			   | number
	 * 			   | variable
	 * operator   := '+' | '-' | '*' | '/'
	 * number     := signed integer
	 * variable	  := sign [a-z,A-Z] ([a-z,0-9,a-z])*
	 * sign       := '' | '-'
	 */

	ExpressionEvaluator() {
	}
	
	ExpressionEvaluator(String type, String description) {
		super(type, description);
	}

	@Override
	protected boolean isUnambiguous(TemplateContext context) {
		return true;
	}
	
	final List<Integer> resolve(List<String> params, TemplateContext context) {
		List<Integer> result = new ArrayList<>(params.size());
		for (String expression : params) {
			try {
				Expression ex = new ComplexExpression(expression.trim());
				Integer value = ex.calculateValue(context);
				result.add(value);
			}
			catch (VariableEvaluationException ex) {
				throw ex;
			}
			catch (TemplateException | RuntimeException ex) {
				throw new VariableEvaluationException(ex.getMessage(), ex);
			}
		}
		return result;
	}
	
	final void parse(List<String> params) throws TemplateException {
		for (String expression : params) {
			try {
				if (expression.trim().isEmpty()) {
					throw new TemplateException("Empty expression");
				}
				new ComplexExpression(expression.trim());
			}
			catch (RuntimeException ex) {
				throw new TemplateException("Invalid expression: " + expression + "; " + ex.getMessage(), ex);
			}
		}
//		if (messages.size() > 0) {
//			StringBuilder result = new StringBuilder();
//			for (String message : messages) {
//				result.append(message).append("\n");
//			}
//			throw new TemplateException(result.toString());
//		}
	}
	
	/**
	 * Checks whether the parameters given are supported by this resolver.<br>
	 * the default implementation does nothing.
	 * @param parameters the parameters to check
	 * @throws TemplateException if one of the parameters does not conform to the specifications.
	 * The message should be user friendly.
	 */
	void supportsParameters(List<String> parameters) throws TemplateException {
	}
	
	/**
	 * @author Richard Birenheide
	 *
	 */
	private static abstract class Expression {
		abstract int calculateValue(TemplateContext context) throws VariableEvaluationException;
	}
	
	/**
	 * @author Richard Birenheide
	 *
	 */
	private static class ValueOrVariableExpression extends Expression {
		private final String value;
		private final boolean isVariable;
		private final char sign;
		
		ValueOrVariableExpression(String expression) throws TemplateException {
			String check = expression;
			if (expression.startsWith("-")) {
				check = check.substring(1).trim();
				this.sign = '-';
			}
			else {
				this.sign = '+';
			}
			if (check.isEmpty()) {
				throw new TemplateException("Invalid variable: " + expression);
			}
			this.isVariable = Character.isLetter(check.charAt(0));
			for (int i = 0; i < check.length(); i++) {
				if (!Character.isLetterOrDigit(check.charAt(i)) ||
						(!this.isVariable && !Character.isDigit(check.charAt(i)))) {
					throw new TemplateException("Invalid value or variable: " + expression);
				}
			}
			if (this.isVariable) {
				this.value = check;
			}
			else {
				this.value = expression;
			}
 		}

		@Override
		int calculateValue(TemplateContext context) {
			String parseValue = null;
			if (this.isVariable) {
				parseValue = context.getVariable(this.value);
				if (parseValue == null) {
					String contextInfo = "";
					if (context instanceof DocumentTemplateContext) {
						contextInfo = ": '" + ((DocumentTemplateContext) context).getKey() + "'";
					}
					throw new VariableEvaluationException("Variable " + this.value + " is undefined in context"+ contextInfo);
				}
				parseValue = this.sign + parseValue;
			}
			else {
				parseValue = this.value;
			}
			try {
				return Integer.parseInt(parseValue);
			}
			catch (NumberFormatException ex) {
				throw new VariableEvaluationException(ex.getMessage(), ex);
			}
		}
	}
	
	/**
	 * @author Richard Birenheide
	 *
	 */
	private static class ComplexExpression extends Expression {
		private Expression left;
		private Expression right;
		private Operator op;
		
		ComplexExpression(String exp) throws TemplateException {
			char first = exp.charAt(0);
			String shouldStartWithOp = null;
			if (Character.isLetterOrDigit(first) || first == '-') { //Number or Variable
				int splitAt = this.parseToken(exp);
				String number = exp.substring(0, splitAt).trim();
				left = new ValueOrVariableExpression(number);
				if (splitAt == exp.length()) {
					return; //Exhausted
				}
				shouldStartWithOp = exp.substring(splitAt).trim();
			}
			else if (first == '(') {//Opening bracket
				int openBrackets = 1;
				int i = 1;
				if (i == exp.length()) {
					throw new TemplateException("Non matching brackets: " + exp);
				}
				char c = 0;
				do {
					c = exp.charAt(i);
					if (c == ')') {
						openBrackets--;
						if (openBrackets == 0) {
							break;
						}
					}
					else if (c == '(') {
						openBrackets++;
					}
					i++;
				} while (i < exp.length());
				if (i == exp.length()) {
					throw new TemplateException("Non matching brackets: " + exp);
				}
				String bracketExp = exp.substring(1, i).trim();
				if (bracketExp.isEmpty()) {
					throw new TemplateException("Empty brackets: " + exp);
				}
				this.left = new ComplexExpression(bracketExp);
				if (i + 1 == exp.length()) {
					return;//Exhausted
				}
				shouldStartWithOp = exp.substring(i + 1).trim();
			}
			if (shouldStartWithOp == null || shouldStartWithOp.isEmpty()) {
				throw new TemplateException("Inbalanced operator: " + exp);
			}
			this.op = this.parseOperator(shouldStartWithOp);
			String remainder = shouldStartWithOp.substring(1).trim();

			if (remainder.isEmpty()) {
				throw new TemplateException("Inbalanced operator: " + exp); 
			}
			if ((this.op == Operator.MULTPLY || this.op == Operator.DIVIDE || this.op == Operator.MODULO) 
					&& !remainder.startsWith("(")) {
				int splitAt = this.parseToken(remainder);
				String l = remainder.substring(0, splitAt);
				if (splitAt < remainder.length()) {
					remainder = remainder.substring(splitAt).trim();
					Operator newOp = this.parseOperator(remainder);
					remainder = remainder.substring(1).trim();
					this.left = new ComplexExpression(this.left, this.op, new ComplexExpression(l));
					this.op = newOp;
				}
				else {
					//Caught by remainder below
				}
			}
			if (!remainder.isEmpty()) {
				this.right = new ComplexExpression(remainder);
			}
		}
		
		ComplexExpression(Expression left, Operator op, Expression right) {
			this.left = left;
			this.right = right;
			this.op = op;
		}
		
		private Operator parseOperator(String partExp) throws TemplateException {
			char operator = partExp.charAt(0);
			switch (operator) {
				case '*' :
					return Operator.MULTPLY;
				case '/' : 
					return Operator.DIVIDE;
				case '%' :
					return Operator.MODULO;
				case '+' :
					return Operator.ADD;
				case '-' :
					return Operator.SUBTRACT;
				default :
					throw new TemplateException("Illegal operator: " + partExp);
			}
		}
		
		private int parseToken(String exp) {
			int i = 0;
			char c = exp.charAt(i);
			if (c == '-') {
				i++;
			}
			do {
				c = exp.charAt(i);
				i++;
				// !Character.isWhitespace(c) && !this.isOperator(c) && i < exp.length()
			} while (Character.isLetterOrDigit(c) && i < exp.length());
			if (i < exp.length()) {
				i--;
			}
			return i;
		}
		
		
//		private boolean isOperator(char c) {
//			return c == '-' || c == '+' || c == '*' || c == '/';
//		}
		
		@Override
		int calculateValue(TemplateContext context) {
			if (op == null) {
				return left.calculateValue(context);
			}
			switch (op) {
				case MULTPLY :
					return left.calculateValue(context) * right.calculateValue(context);
				case DIVIDE :
					return left.calculateValue(context) / right.calculateValue(context);
				case MODULO :
					return left.calculateValue(context) % right.calculateValue(context);
				case ADD :
					return left.calculateValue(context) + right.calculateValue(context);
				case SUBTRACT :
					return left.calculateValue(context) - right.calculateValue(context);
				default :
					throw new VariableEvaluationException("Invalid Operator" + op);
					
			}
		}
	}
	
	/**
	 * @author Richard Birenheide
	 *
	 */
	private static enum Operator {
		MULTPLY,
		DIVIDE,
		MODULO,
		ADD,
		SUBTRACT
		
	}
}
