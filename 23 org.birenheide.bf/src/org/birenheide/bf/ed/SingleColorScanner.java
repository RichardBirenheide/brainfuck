package org.birenheide.bf.ed;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class SingleColorScanner extends BufferedRuleBasedScanner {

	public SingleColorScanner(IPreferenceStore store, String colorKey) {
		IRule singleColorRule = new AllCharactersRule(store, colorKey);
		this.setRules(new IRule[]{singleColorRule});
	}
	
	private static class AllCharactersRule extends PreferenceColorRule {
		private final String colorKey;
		
		AllCharactersRule(IPreferenceStore store, String colorKey) {
			super(store);
			this.colorKey = colorKey;
		}

		@Override
		public IToken evaluate(ICharacterScanner scanner) {
			int val = scanner.read();
			if (val == ICharacterScanner.EOF) {
				return Token.EOF;
			}
			return this.getToken(this.colorKey);
		}
	}
}
