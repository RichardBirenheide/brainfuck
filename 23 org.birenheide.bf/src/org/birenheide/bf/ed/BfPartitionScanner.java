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
	public static final String NON_BRAINFUCK_CHARS = "__non__brainfuck";
	public static final String MULTILINE_COMMENT = "__brainfuck_multiline_comment";
	
	public static final String[] BRAINFUCK_PARTITION_TYPES = 
			new String[] {
				BRAINFUCK_CODE, 
				NON_BRAINFUCK_CHARS, 
				MULTILINE_COMMENT
				};
	

	public BfPartitionScanner() {
		super();
//		IToken bfCode = new Token(BRAINFUCK_CODE);
//		IToken nonBfCHars = new Token(NON_BRAINFUCK_CHARS);
		IToken comment = new Token(MULTILINE_COMMENT);
		
		IPredicateRule bfCodeRule = new BfCodeRule();
		IPredicateRule nonBfCharRule = new NonBfCharRule();
		/*
		 * MUST break on EOF, otherwise reparsing breaks when closing the comment section after being incomplete
		 */
//		IPredicateRule multiLineCommentRule = new MultiLineRule("[-][", "]", comment, (char) 0, true);
		IPredicateRule multiLineCommentRule = new CommentRule();
		
		this.setPredicateRules(new IPredicateRule[]{multiLineCommentRule, bfCodeRule});
	}
	
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
//				do {
//					val = scanner.read();
//					value = (char) val;
//				}
//				while (BrainfuckInterpreter.isReservedChar(value) && val != ICharacterScanner.EOF);
//				scanner.unread();
//				return this.successToken;
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
	
	private static class NonBfCharRule implements IPredicateRule {

		private final IToken successToken = new Token(NON_BRAINFUCK_CHARS);
		
		@Override
		public IToken evaluate(ICharacterScanner scanner, boolean resume) {
			int val = scanner.read();
			if (val == ICharacterScanner.EOF) {
				return Token.EOF;
			}
			char value = (char) val;
			if (!BrainfuckInterpreter.isReservedChar(value)) {
				do {
					val = scanner.read();
					value = (char) val;
				}
				while (!BrainfuckInterpreter.isReservedChar(value) && val != ICharacterScanner.EOF);
				scanner.unread();
				return this.successToken;
			}
			else {
				scanner.unread();
				return Token.UNDEFINED;
			}
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
	
	private static class MultiLineCommentRule implements IPredicateRule {
		
		private final IToken successToken = new Token(MULTILINE_COMMENT);

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
			System.out.println(resume);
			
			return null;
		}
		
	}

}
