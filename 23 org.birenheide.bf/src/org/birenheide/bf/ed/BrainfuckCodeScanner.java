package org.birenheide.bf.ed;

import java.util.ArrayList;
import java.util.List;

import org.birenheide.bf.BrainfuckInterpreter;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

public class BrainfuckCodeScanner extends RuleBasedScanner {
	
	public BrainfuckCodeScanner() {
		List<IRule> rules= new ArrayList<>();
		
		rules.add(new BfRule());
		this.setRules(rules.toArray(new IRule[rules.size()]));
	}
	
	
	private static class BfRule implements IRule {

		@Override
		public IToken evaluate(ICharacterScanner scanner) {
			int val = scanner.read();
			if (val == ICharacterScanner.EOF) {
				return Token.EOF;
			}
			char value = (char) val;
			for (char c : BrainfuckInterpreter.RESERVED_CHARS) {
				if (value == c) {
					return new Token(new TextAttribute(Display.getDefault().getSystemColor(SWT.COLOR_DARK_MAGENTA), null, SWT.BOLD));
				}
			}
			return new Token(new TextAttribute(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY)));
		}
		
	}
}
