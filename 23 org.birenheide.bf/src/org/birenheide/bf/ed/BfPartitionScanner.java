package org.birenheide.bf.ed;

import org.birenheide.bf.BrainfuckInterpreter;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;

public class BfPartitionScanner extends RuleBasedPartitionScanner {
	
	public static final String BRAINFUCK_CODE = "__brainfuck_code";
	public static final String TEMPLATE_PARAMETERS = "__template_parameters";
	public static final String MULTILINE_COMMENT = "__brainfuck_multiline_comment";
	
	public static final String[] BRAINFUCK_PARTITION_TYPES = 
			new String[] {
				BRAINFUCK_CODE, 
				TEMPLATE_PARAMETERS, 
				MULTILINE_COMMENT
				};
	

	public BfPartitionScanner() {
		super();
		IPredicateRule bfCodeRule = new BfCodeRule();
		/*
		 * MUST break on EOF, otherwise reparsing breaks when closing the comment section after being incomplete
		 */
		IPredicateRule multiLineCommentRule = new CommentRule();
		IPredicateRule templateParametersRule = new TemplateParametersRule();
		
		this.setPredicateRules(new IPredicateRule[]{multiLineCommentRule, bfCodeRule, templateParametersRule});
	}
	
	/**
	 * @author Richard Birenheide
	 *
	 */
	private static class BfCodeRule implements IPredicateRule {

		private static final String END_TOKEN = "[-][";
		
		private final IToken successToken = new Token(BRAINFUCK_CODE);
		
		@Override
		public IToken evaluate(ICharacterScanner scanner, boolean resume) {
			int val = scanner.read();
			if (val == ICharacterScanner.EOF) {
				return Token.EOF;
			}
			char value = (char) val;
			if (BrainfuckInterpreter.isReservedChar(value)) {
				return this.readToEnd(scanner, value);
			}
			else {
				scanner.unread();
				return Token.UNDEFINED;
			}
		}
		
		private IToken readToEnd(ICharacterScanner scanner, char consumedValue) {
			StringBuilder endTokenCompare = new StringBuilder();
			endTokenCompare.append(consumedValue);
			boolean successfullyConsumed = false;
			while (BrainfuckInterpreter.isReservedChar(consumedValue)) {
				int value = scanner.read();
				if (value == ICharacterScanner.EOF) {
					return this.successToken;
				}
				consumedValue = (char) value;
				endTokenCompare.append(consumedValue);
				if (endTokenCompare.length() > END_TOKEN.length()) {
					endTokenCompare.delete(0, 1);//Delete first character to create a 'sliding' frame
					successfullyConsumed = true; //If we delete the first character we have consumed at least one successfully
				}
				if (END_TOKEN.equals(endTokenCompare.toString())) {
					for (int i = 0; i < END_TOKEN.length(); i++) {
						scanner.unread();
					}
					if (successfullyConsumed) {
						return this.successToken;
					}
					else {
						return Token.UNDEFINED;
					}
				}
			}
			scanner.unread();
			return this.successToken;
		}

		@Override
		public IToken evaluate(ICharacterScanner scanner) {
			return this.evaluate(scanner, false);
		}

		@Override
		public IToken getSuccessToken() {
			return this.successToken;
		}
	}
	
	/**
	 * @author Richard Birenheide
	 *
	 */
	private static class CommentRule extends MultiLineRule {
		CommentRule() {
			super("[-][", "]", new Token(MULTILINE_COMMENT), (char) 0, true);
		}

		@Override
		protected boolean endSequenceDetected(ICharacterScanner scanner) {
			int openingBrackets = 1;
			int val = scanner.read();
			char value = (char) val;
			while (val != ICharacterScanner.EOF) {
				if (value == ']') {
					openingBrackets--;
					if (openingBrackets == 0) {
						return true;
					}
				}
				else if (value == '[') {
					openingBrackets++;
				}
				val = scanner.read();
				value = (char) val;
			}
			
			return true;
		}
	}
	
	/**
	 * @author Richard Birenheide
	 *
	 */
	private static class TemplateParametersRule implements IPredicateRule {
		
		private final IToken successToken = new Token(TEMPLATE_PARAMETERS);

		@Override
		public IToken evaluate(ICharacterScanner scanner) {
			return this.evaluate(scanner, false);
		}

		@Override
		public IToken getSuccessToken() {
			return this.successToken;
		}

		@Override
		public IToken evaluate(ICharacterScanner scanner, boolean resume) {
			boolean successfullyConsumed = false;
			int val = scanner.read();
			if (val == ICharacterScanner.EOF) {
				return Token.EOF;
			}
			char value = (char) val;
			while (Character.isDigit(value) || value == ';' || value == '!') {
				successfullyConsumed = true;
				value = (char) scanner.read();
			}
			scanner.unread();
			if (successfullyConsumed) {
				return this.successToken;
			}
			else {
				return Token.UNDEFINED;
			}
		}
	}
}
