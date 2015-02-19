package org.birenheide.bf.ed;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.editors.text.EditorsUI;

public class SingleColorScanner extends BufferedRuleBasedScanner {

	public SingleColorScanner(IPreferenceStore store, String colorKey) {
		IRule singleColorRule = new AllCharactersRule(store, colorKey);
		this.setRules(new IRule[]{singleColorRule});
	}
	
	private static class AllCharactersRule implements IRule {
		private final IPreferenceStore store;
		private final String colorKey;
		
		AllCharactersRule(IPreferenceStore store, String colorKey) {
			this.store = store;
			this.colorKey = colorKey;
		}

		@Override
		public IToken evaluate(ICharacterScanner scanner) {
			int val = scanner.read();
			if (val == ICharacterScanner.EOF) {
				return Token.EOF;
			}
			Color foreGround = EditorsUI.
					getSharedTextColors().
					getColor(PreferenceConverter.getColor(
							this.store, 
							this.colorKey));
			return new Token(new TextAttribute(foreGround));
		}
		
	}
}
